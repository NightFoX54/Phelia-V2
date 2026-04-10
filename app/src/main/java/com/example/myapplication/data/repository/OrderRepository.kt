package com.example.myapplication.data.repository

import com.example.myapplication.data.model.EligibleReviewSlot
import com.example.myapplication.data.model.OrderDetailBundle
import com.example.myapplication.data.model.OrderDoc
import com.example.myapplication.data.model.OrderItemDoc
import com.example.myapplication.data.model.OrderItemReviewEmb
import com.example.myapplication.data.model.OrderStatus
import com.example.myapplication.data.model.StoreOrderDetailBundle
import com.example.myapplication.data.model.StoreOrderItemLine
import com.example.myapplication.data.model.StoreSuborderListRow
import com.example.myapplication.data.model.SuborderDetailUi
import com.example.myapplication.data.model.SuborderDoc
import com.example.myapplication.data.model.ShippingAddressDoc
import com.example.myapplication.data.model.aggregateParentOrderStatus
import com.example.myapplication.data.model.allowedNextSuborderStatuses
import com.example.myapplication.data.model.normalizeOrderStatus
import com.example.myapplication.data.model.orderStatusLabelEnglish
import com.example.myapplication.data.model.readMillis
import com.example.myapplication.data.model.cartDocId
import com.example.myapplication.data.model.StoreSalesDayBucket
import com.example.myapplication.data.model.StoreWeeklySalesSummary
import com.example.myapplication.data.model.ui.CartLineUi
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class OrderRepository(
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
    private val productRepository: ProductRepository = ProductRepository(),
    private val productStatsRepository: ProductStatsRepository = ProductStatsRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository(),
) {

    /**
     * Uses only [FIELD_USER_ID] equality (no [orderBy]) so a `userId + createdAt` composite index
     * is not required. Sorting is done on the client.
     */
    fun listenUserOrders(
        userId: String,
        onUpdate: (List<OrderDoc>) -> Unit,
    ): ListenerRegistration =
        db.collection(COLLECTION_ORDERS)
            .whereEqualTo(FIELD_USER_ID, userId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toOrderDoc() }
                    ?.sortedByDescending { it.createdAtMs }
                    ?: emptyList()
                onUpdate(list)
            }

    suspend fun fetchOrderDetail(orderId: String, expectedUserId: String): Result<OrderDetailBundle> = runCatching {
        val orderRef = db.collection(COLLECTION_ORDERS).document(orderId)
        val orderSnap = orderRef.get().await()
        if (!orderSnap.exists()) error("Order not found")
        val order = orderSnap.toOrderDoc() ?: error("Invalid order")
        if (order.userId != expectedUserId) error("Not your order")

        val addressLines = orderSnap.shippingAddressLines()
        val subSnaps = orderRef.collection(SUBCOLLECTION_SUBORDERS).get().await().documents
        val suborders = coroutineScope {
            subSnaps.map { doc ->
                async {
                    val so = doc.toSuborderDoc() ?: return@async null
                    val storeName = fetchStoreDisplayName(so.storeId)
                    val itemDocs = doc.reference.collection(SUBCOLLECTION_ITEMS).get().await().documents
                        .mapNotNull { it.toOrderItemDoc() }
                    SuborderDetailUi(so, storeName, itemDocs)
                }
            }.awaitAll().filterNotNull()
        }
        OrderDetailBundle(
            order = order,
            shippingAddressLines = addressLines,
            suborders = suborders,
        )
    }

    /** First order line for this product in a completed order that does not yet have a review. */
    suspend fun findEligibleReviewSlot(userId: String, productId: String): EligibleReviewSlot? {
        if (productId.isBlank()) return null
        val ordersSnap = db.collection(COLLECTION_ORDERS)
            .whereEqualTo(FIELD_USER_ID, userId)
            .get()
            .await()
        val completedOrders = ordersSnap.documents
            .mapNotNull { it.toOrderDoc() }
            .filter { isOrderCompletedForReview(it.status) }
            .sortedByDescending { it.createdAtMs }
        for (order in completedOrders) {
            val orderRef = db.collection(COLLECTION_ORDERS).document(order.orderId)
            val subSnaps = orderRef.collection(SUBCOLLECTION_SUBORDERS).get().await().documents
            for (sub in subSnaps) {
                val subId = sub.getString(FIELD_SUBORDER_ID)?.takeIf { it.isNotBlank() } ?: sub.id
                val itemSnaps = sub.reference.collection(SUBCOLLECTION_ITEMS).get().await().documents
                for (itemSnap in itemSnaps) {
                    val item = itemSnap.toOrderItemDoc() ?: continue
                    if (item.productId == productId && item.review == null) {
                        return EligibleReviewSlot(
                            orderId = order.orderId,
                            suborderId = subId,
                            itemId = item.itemId,
                            productId = productId,
                        )
                    }
                }
            }
        }
        return null
    }

    private suspend fun fetchStoreDisplayName(storeId: String): String {
        if (storeId.isBlank()) return "Store"
        return runCatching {
            val snap = db.collection(COLLECTION_STORES).document(storeId).get().await()
            snap.getString("name")?.trim()?.takeIf { it.isNotEmpty() } ?: storeId
        }.getOrElse { storeId }
    }

    /** Single transaction: variant stock check, writes orders + suborders + items, clears cart. */
    suspend fun placeOrder(
        userId: String,
        lines: List<CartLineUi>,
        shippingAddress: ShippingAddressDoc,
        paymentMethodId: String,
        shippingFee: Double,
    ): Result<String> = runCatching {
        require(lines.isNotEmpty()) { "Cart is empty" }
        lines.forEach { line ->
            require(line.storeId.isNotBlank()) { "Missing store for product: ${line.productName}" }
        }

        val orderRef = db.collection(COLLECTION_ORDERS).document()
        val orderId = orderRef.id

        db.runTransaction { tx ->
            val snapByProduct = mutableMapOf<String, com.google.firebase.firestore.DocumentSnapshot>()
            val snapByVariant = mutableMapOf<Pair<String, String>, com.google.firebase.firestore.DocumentSnapshot>()
            for (line in lines) {
                if (line.productId !in snapByProduct) {
                    val pRef = db.collection(COLLECTION_PRODUCTS).document(line.productId)
                    snapByProduct[line.productId] = tx.get(pRef)
                }
                val key = line.productId to line.variantId
                if (key !in snapByVariant) {
                    val vRef = db.collection(COLLECTION_PRODUCTS).document(line.productId)
                        .collection(SUBCOLLECTION_VARIANTS).document(line.variantId)
                    snapByVariant[key] = tx.get(vRef)
                }
            }
            for (line in lines) {
                val pSnap = snapByProduct[line.productId]
                    ?: error("Could not read product")
                if (!pSnap.exists()) {
                    error("Product no longer available: ${line.productName}")
                }
                val productActive = pSnap.getBoolean(FIELD_IS_ACTIVE) ?: true
                if (!productActive) {
                    error("Product is not for sale: ${line.productName}")
                }
                val snap = snapByVariant[line.productId to line.variantId]
                    ?: error("Could not read variant")
                if (!snap.exists()) {
                    error("Product no longer available: ${line.productName}")
                }
                val variantActive = snap.getBoolean(FIELD_IS_ACTIVE) ?: true
                if (!variantActive) {
                    error("Selected variant is not for sale: ${line.productName}")
                }
                val stock = (snap.getLong(FIELD_STOCK) ?: 0L).toInt()
                if (stock < line.quantity) {
                    error("Insufficient stock: ${line.productName}")
                }
            }

            data class Calc(val line: CartLineUi, val lineMerch: Double, val lineTax: Double)
            val calcs = lines.map { line ->
                val merch = line.unitPrice * line.quantity
                val tax = merch * (line.taxRatePercent.coerceAtLeast(0) / 100.0)
                Calc(line, merch, tax)
            }
            val productsSubtotal = calcs.sumOf { it.lineMerch }
            val totalTax = calcs.sumOf { it.lineTax }
            val shipping = if (productsSubtotal > 0) shippingFee else 0.0
            val grandTotal = productsSubtotal + totalTax + shipping
            val now = FieldValue.serverTimestamp()

            val orderFields = mutableMapOf<String, Any>(
                FIELD_ORDER_ID to orderId,
                FIELD_USER_ID to userId,
                FIELD_TOTAL_PRICE to grandTotal,
                FIELD_TOTAL_TAX to totalTax,
                FIELD_SHIPPING_FEE to shipping,
                FIELD_STATUS to OrderStatus.ORDER_RECEIVED,
                FIELD_PAYMENT_METHOD_ID to paymentMethodId,
                FIELD_SHIPPING_ADDRESS to shippingAddress.toFirestoreMap(),
                FIELD_CREATED_AT to now,
                FIELD_UPDATED_AT to now,
            )
            tx.set(orderRef, orderFields)

            val byStore = calcs.groupBy { it.line.storeId }
            for ((storeId, storeCalcs) in byStore) {
                val subRef = orderRef.collection(SUBCOLLECTION_SUBORDERS).document()
                val subId = subRef.id
                val subMerch = storeCalcs.sumOf { it.lineMerch }
                val subTax = storeCalcs.sumOf { it.lineTax }
                tx.set(
                    subRef,
                    mapOf(
                        FIELD_SUBORDER_ID to subId,
                        FIELD_STORE_ID to storeId,
                        FIELD_STATUS to OrderStatus.ORDER_RECEIVED,
                        FIELD_TOTAL_PRICE to subMerch,
                        FIELD_TOTAL_TAX to subTax,
                        FIELD_CREATED_AT to now,
                        FIELD_UPDATED_AT to now,
                    ),
                )
                for (calc in storeCalcs) {
                    val itemRef = subRef.collection(SUBCOLLECTION_ITEMS).document()
                    val itemId = itemRef.id
                    tx.set(
                        itemRef,
                        mapOf(
                            FIELD_ITEM_ID to itemId,
                            FIELD_PRODUCT_ID to calc.line.productId,
                            FIELD_VARIANT_ID to calc.line.variantId,
                            FIELD_NAME to calc.line.productName,
                            FIELD_VARIANT_ATTRS to calc.line.attributes,
                            FIELD_UNIT_PRICE to calc.line.unitPrice,
                            FIELD_QUANTITY to calc.line.quantity,
                            FIELD_TAX to calc.lineTax,
                            FIELD_CREATED_AT to now,
                        ),
                    )
                    val vRef = db.collection(COLLECTION_PRODUCTS).document(calc.line.productId)
                        .collection(SUBCOLLECTION_VARIANTS).document(calc.line.variantId)
                    val snap = snapByVariant[calc.line.productId to calc.line.variantId]!!
                    val cur = (snap.getLong(FIELD_STOCK) ?: 0L).toInt()
                    tx.update(vRef, FIELD_STOCK, cur - calc.line.quantity)
                }
            }

            for (line in lines) {
                val cartRef = db.collection(COLLECTION_USERS).document(userId)
                    .collection(SUBCOLLECTION_CART).document(cartDocId(line.productId, line.variantId))
                tx.delete(cartRef)
            }

            val purchasedByProduct = lines.groupBy { it.productId }.mapValues { (_, ls) ->
                ls.sumOf { it.quantity }.coerceAtLeast(0)
            }
            for ((pid, qty) in purchasedByProduct) {
                if (qty <= 0) continue
                val pSnap = snapByProduct[pid] ?: continue
                productStatsRepository.applyPurchasedIncrement(tx, pid, qty, pSnap)
            }

            null
        }.await()

        // Notify each store owner that a new order package arrived.
        val storeIds = lines.map { it.storeId }.filter { it.isNotBlank() }.distinct()
        val ownerIds = mutableListOf<String>()
        storeIds.forEach { sid ->
            val s = db.collection(COLLECTION_STORES).document(sid).get().await()
            val ownerId = s.getString(FIELD_OWNER_ID).orEmpty()
            if (ownerId.isNotBlank()) ownerIds.add(ownerId)
        }
        notificationRepository.sendToUsers(
            userIds = ownerIds.distinct(),
            type = NotificationTypes.NEW_ORDER_FOR_STORE,
            title = "New order received",
            body = "A customer placed an order that includes your store items.",
            orderId = orderId,
        )

        orderId
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.shippingAddressLines(): List<String> {
        @Suppress("UNCHECKED_CAST")
        val m = get(FIELD_SHIPPING_ADDRESS) as? Map<String, Any?> ?: return emptyList()
        fun s(key: String) = m[key]?.toString()?.trim().orEmpty()
        val line1 = s("line1")
        val line2 = s("line2")
        val cityLine = listOf(s("district"), s("city"), s("postalCode")).filter { it.isNotBlank() }.joinToString(", ")
        val country = s("country")
        val name = s("fullName")
        val phone = s("phone")
        val label = s("label")
        return buildList {
            if (label.isNotBlank()) add(label)
            if (name.isNotBlank()) add(name)
            if (line1.isNotBlank()) add(line1)
            if (line2.isNotBlank()) add(line2)
            if (cityLine.isNotBlank()) add(cityLine)
            if (country.isNotBlank()) add(country)
            if (phone.isNotBlank()) add(phone)
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toSuborderDoc(): SuborderDoc? {
        val sid = getString("suborderId")?.takeIf { it.isNotBlank() } ?: id
        return SuborderDoc(
            suborderId = sid,
            storeId = getString("storeId").orEmpty(),
            status = getString("status").orEmpty(),
            totalPrice = getDouble("totalPrice") ?: 0.0,
            totalTax = getDouble("totalTax") ?: 0.0,
            createdAtMs = this.readMillis("createdAt"),
            updatedAtMs = this.readMillis("updatedAt"),
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toOrderItemDoc(): OrderItemDoc? {
        val iid = getString("itemId")?.takeIf { it.isNotBlank() } ?: id
        val variantRaw = get("variant")
        val variant = when (variantRaw) {
            is Map<*, *> -> variantRaw.entries.mapNotNull { (k, v) ->
                (k as? String)?.let { it to (v?.toString() ?: "") }
            }.toMap()
            else -> emptyMap()
        }
        return OrderItemDoc(
            itemId = iid,
            productId = getString("productId").orEmpty(),
            variantId = getString("variantId").orEmpty(),
            name = getString("name").orEmpty(),
            variant = variant,
            unitPrice = getDouble("unitPrice") ?: 0.0,
            quantity = (getLong("quantity") ?: 0L).toInt().coerceAtLeast(0),
            tax = getDouble("tax") ?: 0.0,
            createdAtMs = this.readMillis("createdAt"),
            review = parseEmbeddedItemReview(),
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.parseEmbeddedItemReview(): OrderItemReviewEmb? {
        @Suppress("UNCHECKED_CAST")
        val m = get(FIELD_ITEM_REVIEW) as? Map<*, *> ?: return null
        val rating = (m[FIELD_ITEM_REVIEW_RATING] as? Number)?.toDouble() ?: return null
        val comment = m[FIELD_ITEM_REVIEW_COMMENT]?.toString().orEmpty()
        val createdRaw = m[FIELD_ITEM_REVIEW_CREATED_AT]
        val createdMs = when (createdRaw) {
            is Number -> createdRaw.toLong()
            is Timestamp -> createdRaw.toDate().time
            else -> 0L
        }
        return OrderItemReviewEmb(rating = rating, comment = comment, createdAtMs = createdMs)
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toOrderDoc(): OrderDoc? {
        val oid = getString("orderId")?.takeIf { it.isNotBlank() } ?: id
        return OrderDoc(
            orderId = oid,
            userId = getString("userId").orEmpty(),
            totalPrice = getDouble("totalPrice") ?: 0.0,
            totalTax = getDouble("totalTax") ?: 0.0,
            shippingFee = getDouble("shippingFee") ?: 0.0,
            status = getString("status").orEmpty(),
            paymentMethodId = getString("paymentMethodId").orEmpty(),
            createdAtMs = this.readMillis("createdAt"),
            updatedAtMs = this.readMillis("updatedAt"),
        )
    }

    private fun isOrderCompletedForReview(status: String): Boolean =
        status == OrderStatus.COMPLETED || status == "delivered"

    /**
     * Store panel: `suborders` collection group query with `storeId` equality.
     * Firestore may log a composite index URL on first run.
     */
    fun listenStoreSuborders(
        storeId: String,
        onDocuments: (List<com.google.firebase.firestore.DocumentSnapshot>) -> Unit,
    ): ListenerRegistration =
        db.collectionGroup(SUBCOLLECTION_SUBORDERS)
            .whereEqualTo(FIELD_STORE_ID, storeId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onDocuments(emptyList())
                    return@addSnapshotListener
                }
                onDocuments(snap?.documents ?: emptyList())
            }

    /** One-shot fetch (e.g. refresh when returning to the orders list). */
    suspend fun fetchStoreSubordersSnapshot(storeId: String): List<DocumentSnapshot> =
        db.collectionGroup(SUBCOLLECTION_SUBORDERS)
            .whereEqualTo(FIELD_STORE_ID, storeId)
            .get()
            .await()
            .documents

    /**
     * Suborders for [storeId] with `createdAt` at or after [sinceMillis].
     * Requires a composite index on collection group `suborders`: `storeId` + `createdAt`.
     */
    suspend fun fetchStoreSubordersCreatedSince(storeId: String, sinceMillis: Long): Result<List<DocumentSnapshot>> = runCatching {
        if (storeId.isBlank()) return@runCatching emptyList()
        val ts = Timestamp(Date(sinceMillis.coerceAtLeast(0L)))
        db.collectionGroup(SUBCOLLECTION_SUBORDERS)
            .whereEqualTo(FIELD_STORE_ID, storeId)
            .whereGreaterThanOrEqualTo(FIELD_CREATED_AT, ts)
            .get()
            .await()
            .documents
    }

    /**
     * Rolling last 7 calendar days in the device timezone (today and the 6 days before).
     * Revenue per day = suborder `totalPrice` + `totalTax` (store’s share of the order).
     */
    suspend fun fetchLastSevenDaysStoreSales(storeId: String): Result<StoreWeeklySalesSummary> = runCatching {
        if (storeId.isBlank()) error("Missing store")
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val firstDay = today.minusDays(6)
        val sinceMillis = firstDay.atStartOfDay(zone).toInstant().toEpochMilli()
        val docs = fetchStoreSubordersCreatedSince(storeId, sinceMillis).getOrElse { throw it }
        val revenue = DoubleArray(7)
        val counts = IntArray(7)
        val firstEpochDay = firstDay.toEpochDay()
        for (doc in docs) {
            val ms = doc.readMillis(FIELD_CREATED_AT)
            if (ms <= 0L) continue
            val d = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
            val idx = (d.toEpochDay() - firstEpochDay).toInt()
            if (idx !in 0..6) continue
            val price = doc.getDouble(FIELD_TOTAL_PRICE) ?: 0.0
            val tax = doc.getDouble(FIELD_TOTAL_TAX) ?: 0.0
            revenue[idx] += price + tax
            counts[idx] += 1
        }
        val dayFmt = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        val days = (0..6).map { i ->
            val d = firstDay.plusDays(i.toLong())
            StoreSalesDayBucket(
                label = d.format(dayFmt),
                revenue = revenue[i],
                suborderCount = counts[i],
            )
        }
        val rangeFmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
        val rangeLabel = "${firstDay.format(rangeFmt)} – ${today.format(rangeFmt)}"
        StoreWeeklySalesSummary(
            days = days,
            weekTotalRevenue = revenue.sum(),
            weekSuborderCount = counts.sum(),
            rangeLabel = rangeLabel,
        )
    }

    suspend fun enrichStoreSuborderDocuments(
        docs: List<com.google.firebase.firestore.DocumentSnapshot>,
    ): List<StoreSuborderListRow> {
        if (docs.isEmpty()) return emptyList()
        return coroutineScope {
            docs.map { doc ->
                async {
                    val orderDocRef = doc.reference.parent?.parent ?: return@async null
                    val orderId = orderDocRef.id
                    val so = doc.toSuborderDoc() ?: return@async null
                    val orderSnap = db.collection(COLLECTION_ORDERS).document(orderId).get().await()
                    if (!orderSnap.exists()) return@async null
                    val order = orderSnap.toOrderDoc() ?: return@async null
                    val uid = order.userId
                    val userSnap = db.collection(COLLECTION_USERS).document(uid).get().await()
                    val buyerName = userSnap.getString("name").orEmpty().ifBlank { "Customer" }
                    val itemSnaps = doc.reference.collection(SUBCOLLECTION_ITEMS).get().await().documents
                    val itemCount = itemSnaps.size
                    val thumbPid = itemSnaps.firstOrNull()?.getString(FIELD_PRODUCT_ID)?.takeIf { it.isNotBlank() }
                    val thumb = thumbPid?.let { fetchPrimaryProductImageUrl(it) }
                    StoreSuborderListRow(
                        orderId = orderId,
                        suborderFirestoreId = doc.id,
                        suborder = so,
                        parentOrderStatus = order.status,
                        buyerDisplayName = buyerName,
                        orderCreatedAtMs = order.createdAtMs,
                        itemCount = itemCount,
                        thumbnailUrl = thumb,
                    )
                }
            }.awaitAll().filterNotNull()
        }
    }

    suspend fun fetchStoreOrderDetailForOwner(
        orderId: String,
        storeId: String,
    ): Result<StoreOrderDetailBundle> = runCatching {
        val orderRef = db.collection(COLLECTION_ORDERS).document(orderId)
        val orderSnap = orderRef.get().await()
        if (!orderSnap.exists()) error("Order not found")
        val order = orderSnap.toOrderDoc() ?: error("Invalid order")
        val addressLines = orderSnap.shippingAddressLines()
        val subSnaps = orderRef.collection(SUBCOLLECTION_SUBORDERS).get().await().documents
        val mine = subSnaps.filter { it.getString(FIELD_STORE_ID) == storeId }
        if (mine.isEmpty()) error("No package for your store in this order")
        val subDocSnap = mine.first()
        val so = subDocSnap.toSuborderDoc() ?: error("Invalid suborder")
        val items = subDocSnap.reference.collection(SUBCOLLECTION_ITEMS).get().await().documents
            .mapNotNull { it.toOrderItemDoc() }
        val itemLines = coroutineScope {
            items.map { item ->
                async {
                    val url = productRepository.fetchLineItemDisplayImageUrl(item.productId, item.variantId)
                    StoreOrderItemLine(item = item, imageUrl = url)
                }
            }.awaitAll()
        }
        val uid = order.userId
        val userSnap = db.collection(COLLECTION_USERS).document(uid).get().await()
        val buyerName = userSnap.getString("name").orEmpty().ifBlank { "Customer" }
        val buyerEmail = userSnap.getString("email").orEmpty()
        val thumb = itemLines.firstOrNull()?.imageUrl
            ?: items.firstOrNull()?.productId?.takeIf { it.isNotBlank() }?.let { fetchPrimaryProductImageUrl(it) }
        StoreOrderDetailBundle(
            order = order,
            shippingAddressLines = addressLines,
            buyerName = buyerName,
            buyerEmail = buyerEmail,
            ourSuborder = so,
            ourSuborderFirestoreId = subDocSnap.id,
            items = itemLines,
            thumbnailUrl = thumb,
        )
    }

    suspend fun updateSuborderStatusForStore(
        orderId: String,
        suborderFirestoreId: String,
        newStatus: String,
        storeId: String,
    ): Result<Unit> = runCatching {
        var customerUserId = ""
        val orderRef = db.collection(COLLECTION_ORDERS).document(orderId)
        val allIds = orderRef.collection(SUBCOLLECTION_SUBORDERS).get().await().documents.map { it.id }
        val targetNorm = normalizeOrderStatus(newStatus)
        db.runTransaction { tx ->
            val subRef = orderRef.collection(SUBCOLLECTION_SUBORDERS).document(suborderFirestoreId)
            val orderSnap = tx.get(orderRef)
            customerUserId = orderSnap.getString(FIELD_USER_ID).orEmpty()
            val subSnap = tx.get(subRef)
            if (!subSnap.exists()) error("Suborder not found")
            if (subSnap.getString(FIELD_STORE_ID) != storeId) error("Not your store order")
            val current = subSnap.getString(FIELD_STATUS).orEmpty()
            val allowed = allowedNextSuborderStatuses(current).map { normalizeOrderStatus(it) }.toSet()
            if (targetNorm !in allowed) error("Invalid status change")
            val now = FieldValue.serverTimestamp()
            tx.update(subRef, mapOf(FIELD_STATUS to newStatus, FIELD_UPDATED_AT to now))
            val statuses = allIds.map { sid ->
                if (sid == suborderFirestoreId) {
                    newStatus
                } else {
                    val s = tx.get(orderRef.collection(SUBCOLLECTION_SUBORDERS).document(sid))
                    s.getString(FIELD_STATUS).orEmpty()
                }
            }
            val parentStatus = aggregateParentOrderStatus(statuses)
            tx.update(orderRef, mapOf(FIELD_STATUS to parentStatus, FIELD_UPDATED_AT to now))
            null
        }.await()
        if (customerUserId.isNotBlank()) {
            notificationRepository.sendToUser(
                userId = customerUserId,
                type = NotificationTypes.ORDER_STATUS_UPDATED,
                title = "Order status updated",
                body = "Your order has a new status: ${orderStatusLabelEnglish(newStatus)}.",
                orderId = orderId,
            )
        }
    }

    private suspend fun fetchPrimaryProductImageUrl(productId: String): String? = runCatching {
        val p = db.collection(COLLECTION_PRODUCTS).document(productId).get().await()
        if (!p.exists()) return@runCatching null
        @Suppress("UNCHECKED_CAST")
        val imgs = (p.get("publicImages") as? List<*>)?.mapNotNull { it?.toString() }?.filter { it.isNotBlank() }
        imgs?.firstOrNull()
    }.getOrNull()

    private fun ShippingAddressDoc.toFirestoreMap(): Map<String, Any?> = mapOf(
        "addressId" to addressId,
        "label" to label,
        "fullName" to fullName,
        "phone" to phone,
        "line1" to line1,
        "line2" to line2,
        "district" to district,
        "city" to city,
        "postalCode" to postalCode,
        "country" to country,
    )

    companion object {
        private const val COLLECTION_ORDERS = "orders"
        private const val COLLECTION_PRODUCTS = "products"
        private const val COLLECTION_STORES = "stores"
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_VARIANTS = "variants"
        private const val SUBCOLLECTION_SUBORDERS = "suborders"
        private const val SUBCOLLECTION_ITEMS = "items"
        private const val SUBCOLLECTION_CART = "cartItems"

        private const val FIELD_ORDER_ID = "orderId"
        private const val FIELD_USER_ID = "userId"
        private const val FIELD_TOTAL_PRICE = "totalPrice"
        private const val FIELD_TOTAL_TAX = "totalTax"
        private const val FIELD_SHIPPING_FEE = "shippingFee"
        private const val FIELD_STATUS = "status"
        private const val FIELD_PAYMENT_METHOD_ID = "paymentMethodId"
        private const val FIELD_SHIPPING_ADDRESS = "shippingAddress"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_UPDATED_AT = "updatedAt"

        private const val FIELD_SUBORDER_ID = "suborderId"
        private const val FIELD_STORE_ID = "storeId"
        private const val FIELD_OWNER_ID = "ownerId"
        private const val FIELD_STOCK = "stock"
        private const val FIELD_IS_ACTIVE = "isActive"

        private const val FIELD_ITEM_ID = "itemId"
        private const val FIELD_PRODUCT_ID = "productId"
        private const val FIELD_VARIANT_ID = "variantId"
        private const val FIELD_NAME = "name"
        private const val FIELD_VARIANT_ATTRS = "variant"
        private const val FIELD_UNIT_PRICE = "unitPrice"
        private const val FIELD_QUANTITY = "quantity"
        private const val FIELD_TAX = "tax"

        private const val FIELD_ITEM_REVIEW = "review"
        private const val FIELD_ITEM_REVIEW_RATING = "rating"
        private const val FIELD_ITEM_REVIEW_COMMENT = "comment"
        private const val FIELD_ITEM_REVIEW_CREATED_AT = "createdAt"

    }
}
