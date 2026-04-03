package com.example.myapplication.screens.store

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.components.AppTopBar

private data class VariantForm(
    val id: String,
    var color: String,
    var storage: String,
    var price: String,
    var stock: String,
)

@Composable
fun ProductFormScreen(
    productId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEdit = productId != null
    var name by remember { mutableStateOf(if (isEdit) "Wireless Headphones Pro" else "") }
    var description by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf(if (isEdit) "MyBrand" else "") }
    var category by remember { mutableStateOf(if (isEdit) "Electronics" else "") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var variants by remember {
        mutableStateOf(
            mutableListOf(
                VariantForm("1", if (isEdit) "Black" else "", if (isEdit) "256GB" else "", if (isEdit) "299.99" else "", if (isEdit) "45" else ""),
            ),
        )
    }

    Column(
        modifier = modifier
            .background(Color(0xFFF9FAFB)),
    ) {
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Product Images", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            onClick = {},
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD1D5DB)),
                            color = Color.White,
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(modifier = Modifier.size(56.dp).background(Color(0xFFEEF2FF), RoundedCornerShape(999.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Upload, null, tint = Color(0xFF4338CA))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Upload product images", style = MaterialTheme.typography.titleSmall)
                                Text("PNG, JPG up to 10MB", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA))) {
                                    Text("Choose Files")
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Basic Information", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name *") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    label = { Text("Category *") },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                )
                                DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                                    listOf("Electronics", "Fashion", "Home & Garden", "Sports", "Books").forEach {
                                        DropdownMenuItem(text = { Text(it) }, onClick = { category = it; categoryExpanded = false })
                                    }
                                }
                            }
                            OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.weight(1f))
                        }
                        TextButton(onClick = { categoryExpanded = true }) { Text("Select Category") }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Product Variants", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            Button(onClick = {
                                variants = variants.toMutableList().apply {
                                    add(VariantForm(System.currentTimeMillis().toString(), "", "", "", ""))
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF), contentColor = Color(0xFF4338CA)) ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Add Variant")
                            }
                        }
                        variants.forEachIndexed { index, v ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Variant ${index + 1}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                        if (variants.size > 1) {
                                            TextButton(onClick = {
                                                variants = variants.filterNot { it.id == v.id }.toMutableList()
                                            }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFDC2626)) }
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(v.color, { v.color = it }, label = { Text("Color") }, modifier = Modifier.weight(1f))
                                        OutlinedTextField(v.storage, { v.storage = it }, label = { Text("Storage/Size") }, modifier = Modifier.weight(1f))
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(v.price, { v.price = it }, label = { Text("Price ($)") }, modifier = Modifier.weight(1f))
                                        OutlinedTextField(v.stock, { v.stock = it }, label = { Text("Stock") }, modifier = Modifier.weight(1f))
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
            Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(if (isEdit) "Update Product" else "Save Product")
                }
            }
        }
    }
}

