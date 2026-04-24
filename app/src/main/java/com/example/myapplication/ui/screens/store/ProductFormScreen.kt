package com.example.myapplication.ui.screens.store

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Brand
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.repository.ProductRepository
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.ui.product.colorLabelAndComposeColor
import com.example.myapplication.ui.product.isColorAttributeKey
import kotlinx.coroutines.launch

private val FormPresetColors: List<Color> = run {
    val colors = mutableListOf<Color>()
    // Black, Grays, White
    colors.add(Color(0xFF000000))
    colors.add(Color(0xFF4B5563))
    colors.add(Color(0xFF9CA3AF))
    colors.add(Color(0xFFD1D5DB))
    colors.add(Color(0xFFF3F4F6))
    colors.add(Color(0xFFFFFFFF))
    
    // Spectral wheel generation
    val hues = 20
    val saturations = listOf(0.3f, 0.6f, 1.0f)
    val values = listOf(1.0f, 0.7f)
    
    for (s in saturations) {
        for (h in 0 until hues) {
            val hue = (h.toFloat() / hues) * 360f
            colors.add(Color.hsv(hue, s, 1.0f))
        }
    }
    for (h in 0 until hues) {
        val hue = (h.toFloat() / hues) * 360f
        colors.add(Color.hsv(hue, 1.0f, 0.6f))
    }
    colors
}

private val LanguageOptions = listOf(
    "English", "Turkish", "German", "French", "Spanish", "Italian", "Arabic", "Chinese", "Japanese", "Russian"
)

private data class VariantFormState(
    /** Compose / gallery upload key */
    val id: String,
    /** Firestore `variants` document id; null for a new row */
    val firestoreVariantId: String? = null,
    val sku: String = "",
    val price: String = "",
    val discountPercent: String = "",
    val stock: String = "",
    val attributes: Map<String, String> = emptyMap(),
    val imageInput: String = "",
    val images: List<String> = emptyList(),
)

@Composable
fun ProductFormScreen(
    productId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val repository = remember { ProductRepository() }
    val scope = rememberCoroutineScope()

    val isEdit = productId != null
    var loading by remember { mutableStateOf(false) }
    var imageUploadBusy by remember { mutableStateOf(false) }
    var pendingUpload by remember { mutableStateOf<ProductRepository.ImageUploadTarget?>(null) }

    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var brands by remember { mutableStateOf<List<Brand>>(emptyList()) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var brandError by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedBrandId by remember { mutableStateOf("") }
    var brandExpanded by remember { mutableStateOf(false) }

    var publicImageInput by remember { mutableStateOf("") }
    var publicImages by remember { mutableStateOf<List<String>>(emptyList()) }

    var selectedCategoryId by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    fun newLocalVariantId() = "local-${System.nanoTime()}"

    var variants by remember {
        mutableStateOf(
            listOf(VariantFormState(id = newLocalVariantId(), firestoreVariantId = null, discountPercent = "0")),
        )
    }

    var error by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var colorPickerContext by remember { mutableStateOf<Pair<String, String>?>(null) }

    val selectedCategory = categories.firstOrNull { it.categoryId == selectedCategoryId }
    val selectedBrand = brands.firstOrNull { it.brandId == selectedBrandId }
    val variantAttributeKeys = selectedCategory?.variantAttributes ?: emptyList()

    LaunchedEffect(Unit) {
        loading = true
        categoryError = null
        brandError = null
        repository.fetchCategories().fold(
            onSuccess = { fetched ->
                categories = fetched
                if (!isEdit && selectedCategoryId.isBlank() && fetched.isNotEmpty()) {
                    selectedCategoryId = fetched.first().categoryId
                }
            },
            onFailure = { e ->
                categoryError = e.message ?: "Could not load categories"
            },
        )
        repository.fetchBrands().fold(
            onSuccess = { fetched ->
                brands = fetched
                if (!isEdit && selectedBrandId.isBlank() && fetched.isNotEmpty()) {
                    selectedBrandId = fetched.first().brandId
                }
            },
            onFailure = { e ->
                brandError = e.message ?: "Could not load brands"
            },
        )
        loading = false
    }

    LaunchedEffect(productId, categories.size, brands.size) {
        val pid = productId ?: return@LaunchedEffect
        if (categories.isEmpty() || brands.isEmpty()) return@LaunchedEffect
        loading = true
        error = null
        repository.fetchProductDetail(pid, forStoreManagement = true).fold(
            onSuccess = { bundle ->
                val p = bundle.product
                name = p.name
                description = p.description
                selectedBrandId = p.brand["brandId"].orEmpty().ifBlank {
                    brands.firstOrNull()?.brandId.orEmpty()
                }
                selectedCategoryId = p.category["categoryId"].orEmpty().ifBlank {
                    categories.firstOrNull()?.categoryId.orEmpty()
                }
                publicImages = p.publicImages
                val keys = bundle.variantAttributeKeys
                variants = bundle.variants.map { v ->
                    VariantFormState(
                        id = v.variantId,
                        firestoreVariantId = v.variantId,
                        sku = v.sku,
                        price = v.price.toString(),
                        discountPercent = v.discountPercent.toString(),
                        stock = v.stock.toString(),
                        attributes = keys.associateWith { k -> v.attributes[k].orEmpty() },
                        images = v.images,
                    )
                }.ifEmpty {
                    listOf(
                        VariantFormState(
                            id = newLocalVariantId(),
                            firestoreVariantId = null,
                            discountPercent = "0",
                            attributes = keys.associateWith { "" },
                        ),
                    )
                }
            },
            onFailure = { e ->
                error = e.message ?: "Could not load product"
            },
        )
        loading = false
    }

    LaunchedEffect(selectedCategoryId) {
        val keys = variantAttributeKeys
        if (keys.isEmpty()) return@LaunchedEffect
        variants = variants.map { v ->
            v.copy(attributes = keys.associateWith { key -> v.attributes[key].orEmpty() })
        }
    }

    fun updateVariant(id: String, transform: (VariantFormState) -> VariantFormState) {
        variants = variants.map { if (it.id == id) transform(it) else it }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        val target = pendingUpload
        pendingUpload = null
        if (uri == null || target == null) return@rememberLauncherForActivityResult
        scope.launch {
            imageUploadBusy = true
            error = null
            repository.uploadProductImage(uri, target).fold(
                onSuccess = { url ->
                    when (target) {
                        ProductRepository.ImageUploadTarget.Public -> publicImages = publicImages + url
                        is ProductRepository.ImageUploadTarget.Variant -> updateVariant(target.localVariantId) {
                            it.copy(images = it.images + url)
                        }
                    }
                },
                onFailure = { e ->
                    error = e.message ?: "Could not upload image"
                },
            )
            imageUploadBusy = false
        }
    }

    fun saveProduct() {
        error = null
        successMessage = null

        if (name.isBlank()) {
            error = "Product name is required"
            return
        }
        if (selectedBrand == null) {
            error = "Select a brand"
            return
        }
        if (selectedCategory == null) {
            error = "Select a category"
            return
        }
        if (variants.isEmpty()) {
            error = "At least one variant is required"
            return
        }

        val variantDraftsCreate = mutableListOf<ProductRepository.VariantDraft>()
        val variantDraftsUpdate = mutableListOf<ProductRepository.VariantDraftPersisted>()
        variants.forEachIndexed { i, v ->
            val p = v.price.toDoubleOrNull()
            val disc = v.discountPercent.toIntOrNull()
            val s = v.stock.toIntOrNull()
            if (v.sku.isBlank()) {
                error = "Variant ${i + 1}: SKU is required"
                return
            }
            if (p == null) {
                error = "Variant ${i + 1}: price must be a valid number"
                return
            }
            if (disc == null || disc !in 0..100) {
                error = "Variant ${i + 1}: discount must be a whole number 0 to 100"
                return
            }
            if (s == null) {
                error = "Variant ${i + 1}: stock must be a valid number"
                return
            }
            val attrs = v.attributes.filterValues { it.isNotBlank() }
            if (isEdit) {
                variantDraftsUpdate.add(
                    ProductRepository.VariantDraftPersisted(
                        firestoreVariantId = v.firestoreVariantId,
                        sku = v.sku,
                        attributes = attrs,
                        price = p,
                        discountPercent = disc,
                        stock = s,
                        images = v.images,
                    ),
                )
            } else {
                variantDraftsCreate.add(
                    ProductRepository.VariantDraft(
                        sku = v.sku,
                        attributes = attrs,
                        price = p,
                        discountPercent = disc,
                        stock = s,
                        images = v.images,
                    ),
                )
            }
        }

        if (error != null) return

        loading = true
        scope.launch {
            if (isEdit && productId != null) {
                repository.updateProductForCurrentOwner(
                    productId = productId,
                    input = ProductRepository.UpdateProductInput(
                        name = name,
                        description = description,
                        brandId = selectedBrand.brandId,
                        brandName = selectedBrand.name,
                        categoryId = selectedCategory.categoryId,
                        categoryName = selectedCategory.name,
                        publicImages = publicImages,
                        variants = variantDraftsUpdate,
                    ),
                ).fold(
                    onSuccess = {
                        successMessage = "Product updated"
                    },
                    onFailure = { e ->
                        error = e.message ?: "Update failed"
                    },
                )
            } else {
                repository.createProductForCurrentOwner(
                    ProductRepository.CreateProductInput(
                        name = name,
                        description = description,
                        brandId = selectedBrand.brandId,
                        brandName = selectedBrand.name,
                        categoryId = selectedCategory.categoryId,
                        categoryName = selectedCategory.name,
                        publicImages = publicImages,
                        variants = variantDraftsCreate,
                    ),
                ).fold(
                    onSuccess = { productIdCreated ->
                        successMessage = "Product saved: $productIdCreated"
                        name = ""
                        description = ""
                        selectedBrandId = if (brands.isNotEmpty()) brands.first().brandId else ""
                        selectedCategoryId = if (categories.isNotEmpty()) categories.first().categoryId else ""
                        publicImageInput = ""
                        publicImages = emptyList()
                        variants = listOf(VariantFormState(id = newLocalVariantId(), firestoreVariantId = null, discountPercent = "0"))
                    },
                    onFailure = { e ->
                        error = e.message ?: "Could not save product"
                    },
                )
            }
            loading = false
        }
    }

    colorPickerContext?.let { (variantId, attrKey) ->
        AlertDialog(
            onDismissRequest = { colorPickerContext = null },
            title = { Text("Pick color: $attrKey") },
            text = {
                Box(modifier = Modifier.height(400.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(FormPresetColors) { c ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(c)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                    .clickable {
                                        val hex = String.format("#%06X", 0xFFFFFF and c.toArgb())
                                        updateVariant(variantId) {
                                            it.copy(attributes = it.attributes + (attrKey to hex))
                                        }
                                        colorPickerContext = null
                                        error = null
                                    },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { colorPickerContext = null }) { Text("Close") }
            },
        )
    }

    Column(modifier = modifier.background(Color(0xFFF9FAFB))) {
        Surface(color = Color.White, shadowElevation = 1.dp) {
            AppTopBar(
                title = if (isEdit) "Edit Product" else "Add Product",
                onBack = onBack,
                containerColor = Color.White,
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            item {
                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Basic Information", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(name, { name = it }, label = { Text("Product Name *") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

                        Box {
                            OutlinedTextField(
                                value = selectedBrand?.name.orEmpty(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Brand *") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            DropdownMenu(expanded = brandExpanded, onDismissRequest = { brandExpanded = false }) {
                                brands.forEach { brand ->
                                    DropdownMenuItem(
                                        text = { Text(brand.name) },
                                        onClick = {
                                            selectedBrandId = brand.brandId
                                            brandExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                        Button(
                            onClick = { brandExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)),
                        ) { Text("Select Brand") }
                        if (!loading && brands.isEmpty() && brandError == null) {
                            Text(
                                "Brand list is empty. Add documents to Brands or brands in Firestore (fields: brandId, name).",
                                color = Color(0xFF92400E),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }

                        Box {
                            OutlinedTextField(
                                value = selectedCategory?.name.orEmpty(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category *") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            selectedCategoryId = category.categoryId
                                            categoryExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                        Button(
                            onClick = { categoryExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)),
                        ) { Text("Select Category") }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Product images (public)", style = MaterialTheme.typography.titleSmall)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                onClick = {
                                    pendingUpload = ProductRepository.ImageUploadTarget.Public
                                    pickImageLauncher.launch("image/*")
                                },
                                enabled = !imageUploadBusy,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEEF2FF),
                                    contentColor = Color(0xFF4338CA),
                                ),
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Upload from gallery")
                            }
                            if (imageUploadBusy) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = publicImageInput,
                                onValueChange = { publicImageInput = it },
                                label = { Text("Or paste image URL") },
                                modifier = Modifier.weight(1f),
                            )
                            Button(
                                onClick = {
                                    val url = publicImageInput.trim()
                                    if (url.isNotEmpty()) {
                                        publicImages = publicImages + url
                                        publicImageInput = ""
                                    }
                                },
                                enabled = !imageUploadBusy,
                            ) { Text("Add") }
                        }
                        publicImages.forEach { url ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(url, modifier = Modifier.weight(1f), color = Color(0xFF374151))
                                Button(
                                    onClick = { publicImages = publicImages - url },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFDC2626)),
                                ) { Text("Remove") }
                            }
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Product Variants", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            Button(
                                onClick = {
                                    variants = variants + VariantFormState(
                                        id = newLocalVariantId(),
                                        firestoreVariantId = null,
                                        discountPercent = "0",
                                        attributes = variantAttributeKeys.associateWith { "" },
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)),
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Add Variant")
                            }
                        }

                        variants.forEachIndexed { index, variant ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Variant ${index + 1}", modifier = Modifier.weight(1f))
                                        if (variants.size > 1) {
                                            Button(
                                                onClick = { variants = variants.filterNot { it.id == variant.id } },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFDC2626)),
                                            ) { Icon(Icons.Default.Delete, null) }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = variant.sku,
                                        onValueChange = { text -> updateVariant(variant.id) { it.copy(sku = text) } },
                                        label = { Text("SKU *") },
                                        modifier = Modifier.fillMaxWidth(),
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = variant.price,
                                            onValueChange = { text -> updateVariant(variant.id) { it.copy(price = text) } },
                                            label = { Text("Price") },
                                            modifier = Modifier.weight(1f),
                                        )
                                        OutlinedTextField(
                                            value = variant.discountPercent,
                                            onValueChange = { text -> updateVariant(variant.id) { it.copy(discountPercent = text) } },
                                            label = { Text("Discount %") },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }

                                    val finalPriceText by remember(variant.price, variant.discountPercent) {
                                        derivedStateOf {
                                            val base = variant.price.toDoubleOrNull()
                                            val pct = variant.discountPercent.toIntOrNull()
                                            if (base == null || pct == null) return@derivedStateOf "Final price: —"
                                            val clamped = pct.coerceIn(0, 100)
                                            val final = base * (1.0 - (clamped / 100.0))
                                            "Final price: $" + String.format(java.util.Locale.US, "%.2f", final)
                                        }
                                    }
                                    Text(
                                        finalPriceText,
                                        color = Color(0xFF374151),
                                        style = MaterialTheme.typography.bodySmall,
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = variant.stock,
                                            onValueChange = { text -> updateVariant(variant.id) { it.copy(stock = text) } },
                                            label = { Text("Stock") },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }

                                    variantAttributeKeys.forEach { key ->
                                        if (isColorAttributeKey(key)) {
                                            val raw = variant.attributes[key].orEmpty()
                                            val (_, composeColor) = colorLabelAndComposeColor(raw)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(composeColor ?: Color(0xFFE5E7EB))
                                                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp)),
                                                )
                                                OutlinedTextField(
                                                    value = raw,
                                                    onValueChange = { text ->
                                                        updateVariant(variant.id) {
                                                            it.copy(attributes = it.attributes + (key to text))
                                                        }
                                                        error = null
                                                    },
                                                    label = { Text("$key (#hex or Label|#hex)") },
                                                    modifier = Modifier.weight(1f),
                                                )
                                                Button(
                                                    onClick = { colorPickerContext = variant.id to key },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFEEF2FF),
                                                        contentColor = Color(0xFF4338CA),
                                                    ),
                                                ) { Text("From palette") }
                                            }
                                        } else if (key.contains("Language", ignoreCase = true)) {
                                            var langExpanded by remember { mutableStateOf(false) }
                                            Box {
                                                OutlinedTextField(
                                                    value = variant.attributes[key].orEmpty(),
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text(key) },
                                                    modifier = Modifier.fillMaxWidth().clickable { langExpanded = true },
                                                    enabled = false,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                )
                                                DropdownMenu(expanded = langExpanded, onDismissRequest = { langExpanded = false }) {
                                                    LanguageOptions.forEach { lang ->
                                                        DropdownMenuItem(
                                                            text = { Text(lang) },
                                                            onClick = {
                                                                updateVariant(variant.id) {
                                                                    it.copy(attributes = it.attributes + (key to lang))
                                                                }
                                                                langExpanded = false
                                                                error = null
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                            Button(
                                                onClick = { langExpanded = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)),
                                            ) { Text("Select $key") }
                                        } else {
                                            OutlinedTextField(
                                                value = variant.attributes[key].orEmpty(),
                                                onValueChange = { text ->
                                                    updateVariant(variant.id) {
                                                        it.copy(attributes = it.attributes + (key to text))
                                                    }
                                                    error = null
                                                },
                                                label = { Text(key) },
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                        }
                                    }

                                    Text("Variant images", color = Color(0xFF374151), style = MaterialTheme.typography.bodySmall)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Button(
                                            onClick = {
                                                pendingUpload = ProductRepository.ImageUploadTarget.Variant(variant.id)
                                                pickImageLauncher.launch("image/*")
                                            },
                                            enabled = !imageUploadBusy,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFEEF2FF),
                                                contentColor = Color(0xFF4338CA),
                                            ),
                                        ) {
                                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.size(6.dp))
                                            Text("Upload from gallery")
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = variant.imageInput,
                                            onValueChange = { text -> updateVariant(variant.id) { it.copy(imageInput = text) } },
                                            label = { Text("Or image URL") },
                                            modifier = Modifier.weight(1f),
                                        )
                                        Button(
                                            onClick = {
                                                val trimmed = variant.imageInput.trim()
                                                if (trimmed.isNotEmpty()) {
                                                    updateVariant(variant.id) {
                                                        it.copy(
                                                            images = it.images + trimmed,
                                                            imageInput = "",
                                                        )
                                                    }
                                                }
                                            },
                                            enabled = !imageUploadBusy,
                                        ) {
                                            Text("Add")
                                        }
                                    }

                                    variant.images.forEach { url ->
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(url, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                            Button(
                                                onClick = {
                                                    updateVariant(variant.id) { it.copy(images = it.images - url) }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFDC2626)),
                                            ) {
                                                Text("Remove")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }

        Surface(color = Color.White, shadowElevation = 8.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (loading) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Loading...")
                    }
                }
                if (categoryError != null) Text(categoryError!!, color = Color(0xFFDC2626))
                if (brandError != null) Text(brandError!!, color = Color(0xFFDC2626))
                if (error != null) Text(error!!, color = Color(0xFFDC2626))
                if (successMessage != null) Text(successMessage!!, color = Color(0xFF16A34A))

                Button(
                    onClick = { saveProduct() },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !loading && !imageUploadBusy,
                ) {
                    Text(if (isEdit) "Update Product" else "Save Product")
                }
            }
        }
    }
}
