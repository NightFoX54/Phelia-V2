package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Brand
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class AdminCatalogRepository(
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
) {
    suspend fun fetchCategories(): Result<List<Category>> = runCatching {
        db.collection(COLLECTION_CATEGORIES).get().await().documents
            .map { it.toCategory() }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun fetchBrands(): Result<List<Brand>> = runCatching {
        db.collection(COLLECTION_BRANDS).get().await().documents
            .map { it.toBrand() }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun loadAll(): Result<Pair<List<Category>, List<Brand>>> = coroutineScope {
        val c = async { fetchCategories() }
        val b = async { fetchBrands() }
        val cr = c.await()
        val br = b.await()
        if (cr.isFailure) Result.failure(cr.exceptionOrNull() ?: Exception("Categories failed."))
        else if (br.isFailure) Result.failure(br.exceptionOrNull() ?: Exception("Brands failed."))
        else Result.success(cr.getOrThrow() to br.getOrThrow())
    }

    suspend fun createCategory(name: String, variantAttributes: List<String>, taxRate: Int): Result<String> =
        runCatching {
            val trimmedName = name.trim().ifBlank { error("Name is required.") }
            val ref = db.collection(COLLECTION_CATEGORIES).document()
            val attrs = variantAttributes.map { it.trim() }.filter { it.isNotEmpty() }
            ref.set(
                mapOf(
                    "name" to trimmedName,
                    "taxRate" to taxRate.coerceAtLeast(0).toLong(),
                    "variantAttributes" to attrs,
                ),
            ).await()
            ref.id
        }

    suspend fun updateCategory(
        categoryId: String,
        name: String,
        variantAttributes: List<String>,
        taxRate: Int,
    ): Result<Unit> = runCatching {
        if (categoryId.isBlank()) error("Category id is missing.")
        val trimmedName = name.trim().ifBlank { error("Name is required.") }
        val attrs = variantAttributes.map { it.trim() }.filter { it.isNotEmpty() }
        db.collection(COLLECTION_CATEGORIES).document(categoryId).set(
            mapOf(
                "name" to trimmedName,
                "taxRate" to taxRate.coerceAtLeast(0).toLong(),
                "variantAttributes" to attrs,
            ),
        ).await()
    }

    suspend fun createBrand(name: String): Result<String> = runCatching {
        val trimmed = name.trim().ifBlank { error("Name is required.") }
        val ref = db.collection(COLLECTION_BRANDS).document()
        ref.set(
            mapOf(
                "brandId" to ref.id,
                "name" to trimmed,
            ),
        ).await()
        ref.id
    }

    suspend fun updateBrand(brandId: String, name: String): Result<Unit> = runCatching {
        if (brandId.isBlank()) error("Brand id is missing.")
        val trimmed = name.trim().ifBlank { error("Name is required.") }
        db.collection(COLLECTION_BRANDS).document(brandId).set(
            mapOf(
                "brandId" to brandId,
                "name" to trimmed,
            ),
        ).await()
    }

    private fun DocumentSnapshot.toBrand(): Brand =
        Brand(
            brandId = getString("brandId")?.takeIf { it.isNotBlank() } ?: id,
            name = getString("name").orEmpty(),
        )

    private fun DocumentSnapshot.toCategory(): Category {
        val rawAttributes = get("variantAttributes") as? List<*>
        val attrs = rawAttributes?.mapNotNull { it?.toString()?.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        return Category(
            categoryId = id,
            name = getString("name").orEmpty(),
            variantAttributes = attrs,
            taxRate = (getLong("taxRate") ?: 0L).toInt().coerceAtLeast(0),
        )
    }

    companion object {
        private const val COLLECTION_CATEGORIES = "categories"
        private const val COLLECTION_BRANDS = "brands"
    }
}
