package com.example.myapplication.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Brand
import com.example.myapplication.data.model.Category
import com.example.myapplication.ui.components.AppTopBar
import com.example.myapplication.viewmodel.AdminCatalogUiState
import com.example.myapplication.viewmodel.AdminCatalogViewModel

private sealed interface CategoryDialogState {
    data object None : CategoryDialogState
    data object Add : CategoryDialogState
    data class Edit(val category: Category) : CategoryDialogState
}

private sealed interface BrandDialogState {
    data object None : BrandDialogState
    data object Add : BrandDialogState
    data class Edit(val brand: Brand) : BrandDialogState
}

@Composable
fun AdminCatalogScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminCatalogViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val banner by viewModel.banner.collectAsState()
    val saving by viewModel.saving.collectAsState()
    var tabIndex by remember { mutableIntStateOf(0) }
    var categoryDialog by remember { mutableStateOf<CategoryDialogState>(CategoryDialogState.None) }
    var brandDialog by remember { mutableStateOf<BrandDialogState>(BrandDialogState.None) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)),
    ) {
        Surface(color = Color.White, shadowElevation = 1.dp) {
            Column {
                AppTopBar(
                    title = "Categories & brands",
                    onBack = onBack,
                    containerColor = Color.White,
                )
                if (banner != null) {
                    Text(
                        text = banner!!,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        color = Color(0xFF2563EB),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FilterChip(
                        selected = tabIndex == 0,
                        onClick = {
                            tabIndex = 0
                            viewModel.dismissBanner()
                        },
                        label = { Text("Categories") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    )
                    FilterChip(
                        selected = tabIndex == 1,
                        onClick = {
                            tabIndex = 1
                            viewModel.dismissBanner()
                        },
                        label = { Text("Brands") },
                        leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null) },
                    )
                }
            }
        }

        when (val state = uiState) {
            is AdminCatalogUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is AdminCatalogUiState.Error -> {
                Column(Modifier.padding(20.dp)) {
                    Text(state.message, color = Color(0xFFDC2626))
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                }
            }
            is AdminCatalogUiState.Ready -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = {
                            viewModel.dismissBanner()
                            if (tabIndex == 0) categoryDialog = CategoryDialogState.Add
                            else brandDialog = BrandDialogState.Add
                        },
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(if (tabIndex == 0) "Add category" else "Add brand")
                    }
                }
                if (tabIndex == 0) {
                    CategoryList(
                        categories = state.categories,
                        onEdit = {
                            viewModel.dismissBanner()
                            categoryDialog = CategoryDialogState.Edit(it)
                        },
                    )
                } else {
                    BrandList(
                        brands = state.brands,
                        onEdit = {
                            viewModel.dismissBanner()
                            brandDialog = BrandDialogState.Edit(it)
                        },
                    )
                }
            }
        }
    }

    when (val d = categoryDialog) {
        CategoryDialogState.None -> Unit
        CategoryDialogState.Add -> CategoryEditorDialog(
            title = "New category",
            initial = null,
            saving = saving,
            onDismiss = { categoryDialog = CategoryDialogState.None },
            onSave = { name, attrs, tax ->
                viewModel.saveCategory(
                    isEdit = false,
                    categoryId = null,
                    name = name,
                    attributesCsv = attrs,
                    taxRateInput = tax,
                ) { ok ->
                    if (ok) categoryDialog = CategoryDialogState.None
                }
            },
        )
        is CategoryDialogState.Edit -> CategoryEditorDialog(
            title = "Edit category",
            initial = d.category,
            saving = saving,
            onDismiss = { categoryDialog = CategoryDialogState.None },
            onSave = { name, attrs, tax ->
                viewModel.saveCategory(
                    isEdit = true,
                    categoryId = d.category.categoryId,
                    name = name,
                    attributesCsv = attrs,
                    taxRateInput = tax,
                ) { ok ->
                    if (ok) categoryDialog = CategoryDialogState.None
                }
            },
        )
    }

    when (val d = brandDialog) {
        BrandDialogState.None -> Unit
        BrandDialogState.Add -> BrandEditorDialog(
            title = "New brand",
            initial = null,
            saving = saving,
            onDismiss = { brandDialog = BrandDialogState.None },
            onSave = { name ->
                viewModel.saveBrand(isEdit = false, brandId = null, name = name) { ok ->
                    if (ok) brandDialog = BrandDialogState.None
                }
            },
        )
        is BrandDialogState.Edit -> BrandEditorDialog(
            title = "Edit brand",
            initial = d.brand,
            saving = saving,
            onDismiss = { brandDialog = BrandDialogState.None },
            onSave = { name ->
                viewModel.saveBrand(isEdit = true, brandId = d.brand.brandId, name = name) { ok ->
                    if (ok) brandDialog = BrandDialogState.None
                }
            },
        )
    }
}

@Composable
private fun CategoryList(
    categories: List<Category>,
    onEdit: (Category) -> Unit,
) {
    if (categories.isEmpty()) {
        Text(
            "No categories yet. Add one to use in products.",
            modifier = Modifier.padding(horizontal = 20.dp),
            color = Color(0xFF6B7280),
        )
        return
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        items(categories, key = { it.categoryId }) { c ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(c.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Tax: ${c.taxRate}% · ID: ${c.categoryId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280),
                        )
                        if (c.variantAttributes.isNotEmpty()) {
                            Text(
                                "Attributes: ${c.variantAttributes.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280),
                            )
                        }
                    }
                    TextButton(onClick = { onEdit(c) }) { Text("Edit") }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun BrandList(
    brands: List<Brand>,
    onEdit: (Brand) -> Unit,
) {
    if (brands.isEmpty()) {
        Text(
            "No brands yet. Add one to use in products.",
            modifier = Modifier.padding(horizontal = 20.dp),
            color = Color(0xFF6B7280),
        )
        return
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        items(brands, key = { it.brandId }) { b ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(b.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            "ID: ${b.brandId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280),
                        )
                    }
                    TextButton(onClick = { onEdit(b) }) { Text("Edit") }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun CategoryEditorDialog(
    title: String,
    initial: Category?,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, attributesCsv: String, taxRate: String) -> Unit,
) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var tax by remember(initial) { mutableStateOf(initial?.taxRate?.toString() ?: "0") }
    var attrs by remember(initial) {
        mutableStateOf(initial?.variantAttributes?.joinToString(", ").orEmpty())
    }
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving,
                )
                OutlinedTextField(
                    value = tax,
                    onValueChange = { tax = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Tax rate (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving,
                )
                OutlinedTextField(
                    value = attrs,
                    onValueChange = { attrs = it },
                    label = { Text("Variant attributes") },
                    placeholder = { Text("e.g. Size, Color") },
                    supportingText = { Text("Comma-separated attribute names for variants.") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, attrs, tax) },
                enabled = !saving && name.isNotBlank(),
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") }
        },
    )
}

@Composable
private fun BrandEditorDialog(
    title: String,
    initial: Brand?,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String) -> Unit,
) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Brand name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name) },
                enabled = !saving && name.isNotBlank(),
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") }
        },
    )
}
