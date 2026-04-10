package com.example.myapplication.data.repository

import com.example.myapplication.data.model.readMillis
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AdminStoreListItem(
    val storeId: String,
    val name: String,
    val ownerName: String,
    val ownerEmail: String,
    val rating: Double,
    val totalSales: Double,
    val totalProducts: Int,
    val totalOrders: Int,
    val joinDate: String,
)

data class AdminStoreDetail(
    val storeId: String,
    val name: String,
    val ownerName: String,
    val ownerEmail: String,
    val rating: Double,
    val totalSales: Double,
    val totalProducts: Int,
    val totalOrders: Int,
    val joinDate: String,
    val description: String,
    val monthlyRevenue: List<Float>,
    val categoryDistribution: List<Float>,
    val categoryLabels: List<String>,
    val topProducts: List<Triple<String, String, String>>,
    val recentOrders: List<Triple<String, String, String>>,
)

class AdminStoreManagementRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun fetchStores(): Result<List<AdminStoreListItem>> = runCatching {
        val storesSnap = db.collection(COLLECTION_STORES).get().await()
        val usersSnap = db.collection(COLLECTION_USERS).get().await()
        val productsSnap = db.collection(COLLECTION_PRODUCTS).get().await()
        val subordersSnap = db.collectionGroup(SUBCOLLECTION_SUBORDERS).get().await()

        val userById = usersSnap.documents.associateBy { it.id }
        val productCountByStore = productsSnap.documents
            .groupBy { it.getString(FIELD_STORE_ID).orEmpty() }
            .mapValues { (_, docs) -> docs.size }

        data class StoreAgg(var orderCount: Int, var sales: Double)
        val orderAggByStore = mutableMapOf<String, StoreAgg>()
        subordersSnap.documents.forEach { doc ->
            val sid = doc.getString(FIELD_STORE_ID).orEmpty()
            if (sid.isBlank()) return@forEach
            val merch = doc.getDouble(FIELD_TOTAL_PRICE) ?: 0.0
            val tax = doc.getDouble(FIELD_TOTAL_TAX) ?: 0.0
            val agg = orderAggByStore[sid] ?: StoreAgg(0, 0.0)
            agg.orderCount += 1
            agg.sales += (merch + tax)
            orderAggByStore[sid] = agg
        }

        storesSnap.documents.map { doc ->
            val ownerId = doc.getString(FIELD_OWNER_ID).orEmpty()
            val owner = userById[ownerId]
            val agg = orderAggByStore[doc.id]
            AdminStoreListItem(
                storeId = doc.id,
                name = doc.getString(FIELD_NAME).orEmpty().ifBlank { "Store" },
                ownerName = owner?.getString(FIELD_NAME).orEmpty().ifBlank { "Unknown owner" },
                ownerEmail = owner?.getString(FIELD_EMAIL).orEmpty(),
                rating = doc.getDouble(FIELD_RATING) ?: 0.0,
                totalSales = agg?.sales ?: 0.0,
                totalProducts = productCountByStore[doc.id] ?: 0,
                totalOrders = agg?.orderCount ?: 0,
                joinDate = formatDate(doc.readMillis(FIELD_CREATED_AT)),
            )
        }.sortedBy { it.name.lowercase() }
    }

    suspend fun fetchStoreDetail(storeId: String): Result<AdminStoreDetail> = runCatching {
        if (storeId.isBlank()) error("Missing store id")
        val storeSnap = db.collection(COLLECTION_STORES).document(storeId).get().await()
        if (!storeSnap.exists()) error("Store not found")

        val ownerId = storeSnap.getString(FIELD_OWNER_ID).orEmpty()
        val ownerSnap = if (ownerId.isNotBlank()) db.collection(COLLECTION_USERS).document(ownerId).get().await() else null
        val productDocs = db.collection(COLLECTION_PRODUCTS).whereEqualTo(FIELD_STORE_ID, storeId).get().await().documents
        val categoryNameByProductId = productDocs.associate { p ->
            val categoryMap = p.get(FIELD_CATEGORY) as? Map<*, *>
            val categoryName = categoryMap?.get(FIELD_NAME)?.toString()?.trim().orEmpty().ifBlank { "Uncategorized" }
            p.id to categoryName
        }
        val suborderDocs = db.collectionGroup(SUBCOLLECTION_SUBORDERS).whereEqualTo(FIELD_STORE_ID, storeId).get().await().documents

        val totalOrders = suborderDocs.size
        val totalSales = suborderDocs.sumOf { (it.getDouble(FIELD_TOTAL_PRICE) ?: 0.0) + (it.getDouble(FIELD_TOTAL_TAX) ?: 0.0) }

        val zone = ZoneId.systemDefault()
        val monthFmt = DateTimeFormatter.ofPattern("MMM", Locale.US)
        val now = java.time.LocalDate.now(zone).withDayOfMonth(1)
        val months = (5 downTo 0).map { now.minusMonths(it.toLong()) }
        val revenueByMonth = mutableMapOf<java.time.LocalDate, Double>()
        months.forEach { revenueByMonth[it] = 0.0 }
        suborderDocs.forEach { doc ->
            val created = doc.readMillis(FIELD_CREATED_AT)
            if (created <= 0L) return@forEach
            val month = Instant.ofEpochMilli(created).atZone(zone).toLocalDate().withDayOfMonth(1)
            if (month in revenueByMonth.keys) {
                revenueByMonth[month] = (revenueByMonth[month] ?: 0.0) + (doc.getDouble(FIELD_TOTAL_PRICE) ?: 0.0) + (doc.getDouble(FIELD_TOTAL_TAX) ?: 0.0)
            }
        }
        val monthlyRevenue = months.map { (revenueByMonth[it] ?: 0.0).toFloat() }

        val orderMetaByOrderId = suborderDocs.mapNotNull { d ->
            val orderId = d.reference.parent.parent?.id ?: return@mapNotNull null
            val createdMs = d.readMillis(FIELD_CREATED_AT)
            orderId to createdMs
        }.toMap()

        data class ProductAgg(var qty: Int, var sales: Double)
        val productAgg = mutableMapOf<String, ProductAgg>()
        for (suborder in suborderDocs) {
            val itemDocs = suborder.reference.collection(SUBCOLLECTION_ITEMS).get().await().documents
            for (item in itemDocs) {
                val name = item.getString(FIELD_ITEM_NAME).orEmpty().ifBlank { "Product" }
                val qty = (item.getLong(FIELD_ITEM_QUANTITY) ?: 0L).toInt().coerceAtLeast(0)
                val unitPrice = item.getDouble(FIELD_ITEM_UNIT_PRICE) ?: 0.0
                val agg = productAgg[name] ?: ProductAgg(0, 0.0)
                agg.qty += qty
                agg.sales += (qty * unitPrice)
                productAgg[name] = agg
            }
        }
        val topProducts = productAgg.entries
            .sortedByDescending { it.value.qty }
            .take(5)
            .map { (name, agg) ->
                Triple(name, "${agg.qty} sales", formatCompactCurrency(agg.sales))
            }

        val categoryAgg = mutableMapOf<String, Int>()
        for (suborder in suborderDocs) {
            val itemDocs = suborder.reference.collection(SUBCOLLECTION_ITEMS).get().await().documents
            for (item in itemDocs) {
                val productId = item.getString(FIELD_ITEM_PRODUCT_ID).orEmpty()
                val categoryName = categoryNameByProductId[productId] ?: "Uncategorized"
                val qty = (item.getLong(FIELD_ITEM_QUANTITY) ?: 0L).toInt().coerceAtLeast(0)
                categoryAgg[categoryName] = (categoryAgg[categoryName] ?: 0) + qty
            }
        }
        if (categoryAgg.isEmpty()) {
            productDocs.forEach { p ->
                val category = categoryNameByProductId[p.id] ?: "Uncategorized"
                categoryAgg[category] = (categoryAgg[category] ?: 0) + 1
            }
        }
        val sortedCategories = categoryAgg.entries.sortedByDescending { it.value }.take(6)
        val categoryLabels = sortedCategories.map { it.key }
        val categoryDistribution = sortedCategories.map { it.value.toFloat() }

        val recentOrders = orderMetaByOrderId.entries
            .sortedByDescending { it.value }
            .take(8)
            .map { (orderId, createdMs) ->
                val amount = suborderDocs
                    .filter { it.reference.parent.parent?.id == orderId }
                    .sumOf { (it.getDouble(FIELD_TOTAL_PRICE) ?: 0.0) + (it.getDouble(FIELD_TOTAL_TAX) ?: 0.0) }
                Triple(
                    "ORD-${orderId.takeLast(6).uppercase(Locale.US)}",
                    formatDate(createdMs),
                    formatCompactCurrency(amount),
                )
            }

        AdminStoreDetail(
            storeId = storeId,
            name = storeSnap.getString(FIELD_NAME).orEmpty().ifBlank { "Store" },
            ownerName = ownerSnap?.getString(FIELD_NAME).orEmpty().ifBlank { "Unknown owner" },
            ownerEmail = ownerSnap?.getString(FIELD_EMAIL).orEmpty(),
            rating = storeSnap.getDouble(FIELD_RATING) ?: 0.0,
            totalSales = totalSales,
            totalProducts = productDocs.size,
            totalOrders = totalOrders,
            joinDate = formatDate(storeSnap.readMillis(FIELD_CREATED_AT)),
            description = storeSnap.getString(FIELD_DESCRIPTION).orEmpty(),
            monthlyRevenue = monthlyRevenue,
            categoryDistribution = categoryDistribution,
            categoryLabels = categoryLabels,
            topProducts = topProducts,
            recentOrders = recentOrders,
        )
    }

    private fun formatDate(ms: Long): String {
        if (ms <= 0L) return "—"
        return runCatching {
            val dt = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
            dt.toString()
        }.getOrDefault("—")
    }

    private fun formatCompactCurrency(value: Double): String = when {
        value >= 1_000_000.0 -> "$" + String.format(Locale.US, "%.2fM", value / 1_000_000.0)
        value >= 1_000.0 -> "$" + String.format(Locale.US, "%.1fK", value / 1_000.0)
        else -> "$" + String.format(Locale.US, "%.0f", value)
    }

    private companion object {
        private const val COLLECTION_STORES = "stores"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_PRODUCTS = "products"
        private const val SUBCOLLECTION_SUBORDERS = "suborders"
        private const val SUBCOLLECTION_ITEMS = "items"

        private const val FIELD_OWNER_ID = "ownerId"
        private const val FIELD_STORE_ID = "storeId"
        private const val FIELD_NAME = "name"
        private const val FIELD_EMAIL = "email"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_CATEGORY = "category"
        private const val FIELD_RATING = "rating"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_TOTAL_PRICE = "totalPrice"
        private const val FIELD_TOTAL_TAX = "totalTax"

        private const val FIELD_ITEM_NAME = "name"
        private const val FIELD_ITEM_PRODUCT_ID = "productId"
        private const val FIELD_ITEM_QUANTITY = "quantity"
        private const val FIELD_ITEM_UNIT_PRICE = "unitPrice"
    }
}
