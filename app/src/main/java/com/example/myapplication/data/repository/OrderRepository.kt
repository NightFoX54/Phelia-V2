package com.example.myapplication.data.repository

import com.example.myapplication.data.model.EligibleReviewSlot
import com.example.myapplication.data.model.OrderDetailBundle
import com.example.myapplication.data.model.OrderDoc
import com.example.myapplication.data.model.OrderItemDoc
import com.example.myapplication.data.model.OrderItemReviewEmb
import com.example.myapplication.data.model.OrderStatus
import com.example.myapplication.data.model.SuborderDetailUi
import com.example.myapplication.data.model.SuborderDoc
import com.example.myapplication.data.model.ShippingAddressDoc
import com.example.myapplication.data.model.readMillis
import com.example.myapplication.data.model.cartDocId
import com.example.myapplication.data.model.ui.CartLineUi
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
) {

    /**
     * Sadece [FIELD_USER_ID] eşitliği kullanılır; [orderBy] yok — böylece
     * `userId + createdAt` composite index zorunluluğu oluşmaz. Sıralama istemcide yapılır.
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

    /**
     * Tamamlanmış siparişlerde bu ürün için henüz yorum yazılmamış ilk kalemi döner.
     */
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

    /**
     * Tek transaction: varyant stok kontrolü, orders + suborders + items yazar, sepeti temizler.
     */
    suspend fun placeOrder(
        userId: String,
        lines: List<CartLineUi>,
        shippingAddress: ShippingAddressDoc,
        paymentMethodId: String,
        shippingFee: Double,
        taxRate: Double = DEFAULT_TAX_RATE,
    ): Result<String> = runCatching {
        require(lines.isNotEmpty()) { "Sepet boş" }
        lines.forEach { line ->
            require(line.storeId.isNotBlank()) { "Ürün mağaza bilgisi eksik: ${line.productName}" }
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
                    ?: error("Ürün okunamadı")
                if (!pSnap.exists()) {
                    error("Ürün artık yok: ${line.productName}")
                }
                val productActive = pSnap.getBoolean(FIELD_IS_ACTIVE) ?: true
                if (!productActive) {
                    error("Ürün satışta değil: ${line.productName}")
                }
                val snap = snapByVariant[line.productId to line.variantId]
                    ?: error("Varyant okunamadı")
                if (!snap.exists()) {
                    error("Ürün artık yok: ${line.productName}")
                }
                val variantActive = snap.getBoolean(FIELD_IS_ACTIVE) ?: true
                if (!variantActive) {
                    error("Seçilen varyant satışta değil: ${line.productName}")
                }
                val stock = (snap.getLong(FIELD_STOCK) ?: 0L).toInt()
                if (stock < line.quantity) {
                    error("Yetersiz stok: ${line.productName}")
                }
            }

            data class Calc(val line: CartLineUi, val lineMerch: Double, val lineTax: Double)
            val calcs = lines.map { line ->
                val merch = line.unitPrice * line.quantity
                val tax = merch * taxRate
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

            null
        }.await()

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

        private const val DEFAULT_TAX_RATE = 0.08
    }
}
