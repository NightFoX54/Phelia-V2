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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

private val FormPresetColors = listOf(
    Color(0xFF000000), Color(0xFFFFFFFF), Color(0xFFEF4444), Color(0xFF22C55E),
    Color(0xFF3B82F6), Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFFEC4899),
    Color(0xFF14B8A6), Color(0xFF64748B), Color(0xFF92400E), Color(0xFFFBBF24),
    Color(0xFF1E293B), Color(0xFFF97316), Color(0xFF84CC16), Color(0xFF06B6D4),
)

private data class VariantFormState(
    val id: String,
    val sku: String = "",
    val price: String = "",
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

    var variants by remember {
        mutableStateOf(
            listOf(VariantFormState(id = System.currentTimeMillis().toString())),
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
                if (selectedCategoryId.isBlank() && fetched.isNotEmpty()) {
                    selectedCategoryId = fetched.first().categoryId
                }
            },
            onFailure = { e ->
                categoryError = e.message ?: "Kategoriler yuklenemedi"
            },
        )
        repository.fetchBrands().fold(
            onSuccess = { fetched ->
                brands = fetched
                if (selectedBrandId.isBlank() && fetched.isNotEmpty()) {
                    selectedBrandId = fetched.first().brandId
                }
            },
            onFailure = { e ->
                brandError = e.message ?: "Markalar yuklenemedi"
            },
        )
        loading = false
    }

    LaunchedEffect(selectedCategoryId) {
        val keys = variantAttributeKeys
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
                    error = e.message ?: "Gorsel yuklenemedi"
                },
            )
            imageUploadBusy = false
        }
    }

    fun createProduct() {
        error = null
        successMessage = null

        if (isEdit) {
            error = "Edit akisi hen?z bagli degil. Simdilik yeni ?r?n ekleyin."
            return
        }
        if (name.isBlank()) {
            error = "?r?n adi zorunlu"
            return
        }
        if (selectedBrand == null) {
            error = "Marka secin"
            return
        }
        if (selectedCategory == null) {
            error = "Kategori secin"
            return
        }
        if (variants.isEmpty()) {
            error = "En az bir varyant olmali"
            return
        }

        val variantDrafts = variants.mapIndexed { i, v ->
            val p = v.price.toDoubleOrNull()
            val s = v.stock.toIntOrNull()
            if (v.sku.isBlank()) {
                error = "Variant ${i + 1}: SKU zorunlu"
                return
            }
            if (p == null) {
                error = "Variant ${i + 1}: price ge?erli sayi olmali"
                return
            }
            if (s == null) {
                error = "Variant ${i + 1}: stock ge?erli sayi olmali"
                return
            }
            ProductRepository.VariantDraft(
                sku = v.sku,
                attributes = v.attributes.filterValues { it.isNotBlank() },
                price = p,
                stock = s,
                images = v.images,
            )
        }

        if (error != null) return

        loading = true
        scope.launch {
            repository.createProductForCurrentOwner(
                ProductRepository.CreateProductInput(
                    name = name,
                    description = description,
                    brandId = selectedBrand.brandId,
                    brandName = selectedBrand.name,
                    categoryId = selectedCategory.categoryId,
                    categoryName = selectedCategory.name,
                    publicImages = publicImages,
                    variants = variantDrafts,
                ),
            ).fold(
                onSuccess = { productIdCreated ->
                    successMessage = "?r?n kaydedildi: $productIdCreated"
                    name = ""
                    description = ""
                    selectedBrandId = if (brands.isNotEmpty()) brands.first().brandId else ""
                    publicImageInput = ""
                    publicImages = emptyList()
                    variants = listOf(VariantFormState(id = System.currentTimeMillis().toString()))
                },
                onFailure = { e ->
                    error = e.message ?: "?r?n kaydedilemedi"
                },
            )
            loading = false
        }
    }

    colorPickerContext?.let { (variantId, attrKey) ->
        AlertDialog(
            onDismissRequest = { colorPickerContext = null },
            title = { Text("Renk sec: $attrKey") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(FormPresetColors) { c ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(c)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                                .clickable {
                                    val hex = String.format("#%06X", 0xFFFFFF and c.toArgb())
                                    updateVariant(variantId) {
                                        it.copy(attributes = it.attributes + (attrKey to hex))
                                    }
                                    colorPickerContext = null
                                },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { colorPickerContext = null }) { Text("Kapat") }
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
                                "Marka listesi bos. Firestore'da Brands veya brands koleksiyonuna dokuman ekleyin (alanlar: brandId, name).",
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
                        Text("Urun gorselleri (public)", style = MaterialTheme.typography.titleSmall)
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
                                Text("Galeriden yukle")
                            }
                            if (imageUploadBusy) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = publicImageInput,
                                onValueChange = { publicImageInput = it },
                                label = { Text("Veya URL yap??t?r") },
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
                            ) { Text("Ekle") }
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
                                        id = System.currentTimeMillis().toString(),
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
                                                    },
                                                    label = { Text("$key (#hex veya Etiket|#hex)") },
                                                    modifier = Modifier.weight(1f),
                                                )
                                                Button(
                                                    onClick = { colorPickerContext = variant.id to key },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFEEF2FF),
                                                        contentColor = Color(0xFF4338CA),
                                                    ),
                                                ) { Text("Paletten") }
                                            }
                                        } else {
                                            OutlinedTextField(
                                                value = variant.attributes[key].orEmpty(),
                                                onValueChange = { text ->
                                                    updateVariant(variant.id) {
                                                        it.copy(attributes = it.attributes + (key to text))
                                                    }
                                                },
                                                label = { Text(key) },
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                        }
                                    }

                                    Text("Varyant gorselleri", color = Color(0xFF374151), style = MaterialTheme.typography.bodySmall)
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
                                            Text("Galeriden yukle")
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = variant.imageInput,
                                            onValueChange = { text -> updateVariant(variant.id) { it.copy(imageInput = text) } },
                                            label = { Text("Veya URL") },
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
                                            Text("Ekle")
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
                    onClick = { createProduct() },
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
