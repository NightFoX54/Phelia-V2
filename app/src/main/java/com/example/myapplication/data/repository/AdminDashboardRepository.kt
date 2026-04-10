package com.example.myapplication.data.repository

import com.example.myapplication.data.model.readMillis
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AdminDashboardOverview(
    val totalUsers: Int,
    val totalStores: Int,
    val totalProducts: Int,
    val totalOrders: Int,
    val ordersOverTime: List<Pair<String, Float>>,
    val topSellingProducts: List<Pair<String, Float>>,
    val topProducts: List<AdminProductPerformance>,
    val worstProducts: List<AdminProductPerformance>,
    val topStores: List<AdminStorePerformance>,
)

data class AdminProductPerformance(
    val name: String,
    val unitsSold: Int,
    val revenue: Double,
)

data class AdminStorePerformance(
    val storeName: String,
    val orderCount: Int,
    val revenue: Double,
)

class AdminDashboardRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    suspend fun fetchOverview(): Result<AdminDashboardOverview> = runCatching {
        val usersTask = db.collection(COLLECTION_USERS).get()
        val storesTask = db.collection(COLLECTION_STORES).get()
        val productsTask = db.collection(COLLECTION_PRODUCTS).get()
        val ordersTask = db.collection(COLLECTION_ORDERS).get()
        val itemsTask = db.collectionGroup(SUBCOLLECTION_ITEMS).get()
        val subordersTask = db.collectionGroup(SUBCOLLECTION_SUBORDERS).get()

        val usersSnap = usersTask.await()
        val storesSnap = storesTask.await()
        val productsSnap = productsTask.await()
        val ordersSnap = ordersTask.await()
        val itemsSnap = itemsTask.await()
        val subordersSnap = subordersTask.await()

        val zone = ZoneId.systemDefault()
        val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
        val currentMonth = LocalDate.now(zone).withDayOfMonth(1)
        val months = (5 downTo 0).map { currentMonth.minusMonths(it.toLong()) }
        val counts = LinkedHashMap<LocalDate, Int>()
        months.forEach { counts[it] = 0 }

        ordersSnap.documents.forEach { doc ->
            val createdAtMs = doc.readMillis("createdAt")
            if (createdAtMs <= 0L) return@forEach
            val month = Instant.ofEpochMilli(createdAtMs).atZone(zone).toLocalDate().withDayOfMonth(1)
            if (month in counts.keys) {
                counts[month] = (counts[month] ?: 0) + 1
            }
        }

        data class ProductAgg(var name: String, var unitsSold: Int, var revenue: Double)
        val productAgg = linkedMapOf<String, ProductAgg>()
        itemsSnap.documents.forEach { doc ->
            val productId = doc.getString(FIELD_PRODUCT_ID)?.takeIf { it.isNotBlank() } ?: doc.id
            val name = doc.getString(FIELD_NAME)?.takeIf { it.isNotBlank() } ?: "Product"
            val qty = (doc.getLong(FIELD_QUANTITY) ?: 0L).toInt().coerceAtLeast(0)
            val unitPrice = doc.getDouble(FIELD_UNIT_PRICE) ?: 0.0
            val revenue = unitPrice * qty
            val agg = productAgg[productId]
            if (agg == null) {
                productAgg[productId] = ProductAgg(name = name, unitsSold = qty, revenue = revenue)
            } else {
                agg.unitsSold += qty
                agg.revenue += revenue
                if (agg.name.isBlank() && name.isNotBlank()) agg.name = name
            }
        }
        val rankedProducts = productAgg.values.filter { it.unitsSold > 0 }
        val topProducts = rankedProducts
            .sortedWith(compareByDescending<ProductAgg> { it.unitsSold }.thenByDescending { it.revenue })
            .take(3)
            .map { AdminProductPerformance(name = it.name, unitsSold = it.unitsSold, revenue = it.revenue) }
        val worstProducts = rankedProducts
            .sortedWith(compareBy<ProductAgg> { it.unitsSold }.thenBy { it.revenue })
            .take(3)
            .map { AdminProductPerformance(name = it.name, unitsSold = it.unitsSold, revenue = it.revenue) }
        val topSellingChart = rankedProducts
            .sortedByDescending { it.unitsSold }
            .take(5)
            .map { it.name to it.unitsSold.toFloat() }

        val storeNamesById = storesSnap.documents.associate { it.id to (it.getString(FIELD_NAME)?.trim().orEmpty()) }
        data class StoreAgg(var orderCount: Int, var revenue: Double)
        val storeAgg = linkedMapOf<String, StoreAgg>()
        subordersSnap.documents.forEach { doc ->
            val sid = doc.getString(FIELD_STORE_ID)?.takeIf { it.isNotBlank() } ?: return@forEach
            val merch = doc.getDouble(FIELD_TOTAL_PRICE) ?: 0.0
            val tax = doc.getDouble(FIELD_TOTAL_TAX) ?: 0.0
            val agg = storeAgg[sid]
            if (agg == null) {
                storeAgg[sid] = StoreAgg(orderCount = 1, revenue = merch + tax)
            } else {
                agg.orderCount += 1
                agg.revenue += (merch + tax)
            }
        }
        val topStores = storeAgg.entries
            .sortedByDescending { it.value.revenue }
            .take(3)
            .map { (sid, agg) ->
                AdminStorePerformance(
                    storeName = storeNamesById[sid]?.takeIf { it.isNotBlank() } ?: sid,
                    orderCount = agg.orderCount,
                    revenue = agg.revenue,
                )
            }

        AdminDashboardOverview(
            totalUsers = usersSnap.size(),
            totalStores = storesSnap.size(),
            totalProducts = productsSnap.size(),
            totalOrders = ordersSnap.size(),
            ordersOverTime = months.map { m ->
                m.format(monthFormatter) to ((counts[m] ?: 0).toFloat())
            },
            topSellingProducts = topSellingChart,
            topProducts = topProducts,
            worstProducts = worstProducts,
            topStores = topStores,
        )
    }

    private companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_STORES = "stores"
        private const val COLLECTION_PRODUCTS = "products"
        private const val COLLECTION_ORDERS = "orders"
        private const val SUBCOLLECTION_ITEMS = "items"
        private const val SUBCOLLECTION_SUBORDERS = "suborders"
        private const val FIELD_PRODUCT_ID = "productId"
        private const val FIELD_NAME = "name"
        private const val FIELD_QUANTITY = "quantity"
        private const val FIELD_UNIT_PRICE = "unitPrice"
        private const val FIELD_STORE_ID = "storeId"
        private const val FIELD_TOTAL_PRICE = "totalPrice"
        private const val FIELD_TOTAL_TAX = "totalTax"
    }
}
