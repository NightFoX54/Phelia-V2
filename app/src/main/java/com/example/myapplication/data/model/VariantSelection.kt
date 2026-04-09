package com.example.myapplication.data.model

object VariantSelection {
    fun initialSelection(variants: List<ProductVariant>, keys: List<String>): Map<String, String> {
        if (variants.isEmpty() || keys.isEmpty()) return emptyMap()
        val v = variants.firstOrNull { it.stock > 0 } ?: variants.first()
        return keys.associateWith { k -> v.attributes[k].orEmpty() }
    }

    fun isAvailable(
        attributeKey: String,
        value: String,
        selected: Map<String, String>,
        variants: List<ProductVariant>,
    ): Boolean =
        variants.any { v ->
            v.attributes[attributeKey] == value &&
                selected.all { (k, sel) ->
                    if (k == attributeKey) true else v.attributes[k] == sel
                }
        }

    fun pickAttribute(
        attributeKey: String,
        value: String,
        selected: Map<String, String>,
        variants: List<ProductVariant>,
        keys: List<String>,
    ): Map<String, String> {
        if (isAvailable(attributeKey, value, selected, variants)) {
            return selected + (attributeKey to value)
        }
        val fallback = variants.firstOrNull { it.attributes[attributeKey] == value }
            ?: return selected
        return keys.associateWith { k -> fallback.attributes[k].orEmpty() }
    }

    fun resolveVariant(
        selected: Map<String, String>,
        variants: List<ProductVariant>,
        keys: List<String>,
    ): ProductVariant? {
        if (variants.isEmpty()) return null
        if (keys.isEmpty()) return variants.firstOrNull()
        return variants.find { v -> keys.all { k -> v.attributes[k] == selected[k] } }
    }
}

/**
 * Gallery order: variant-specific images first, then shared [Product.publicImages]
 * (e.g. color-specific shots + common box/spec photo at the end). Duplicate URLs are dropped.
 */
fun displayImagesForVariant(product: Product, variant: ProductVariant?): List<String> {
    val variantUrls = variant?.images.orEmpty().map { it.trim() }.filter { it.isNotEmpty() }
    val publicUrls = product.publicImages.map { it.trim() }.filter { it.isNotEmpty() }
    val seen = LinkedHashSet<String>()
    val out = ArrayList<String>(variantUrls.size + publicUrls.size)
    for (u in variantUrls) if (seen.add(u)) out.add(u)
    for (u in publicUrls) if (seen.add(u)) out.add(u)
    return out
}
