package com.example.myapplication.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.viewmodels.CatalogViewModel

@Composable
fun ProductDetailScreen(
    productId: String,
    onBack: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
    catalogViewModel: CatalogViewModel = viewModel(),
) {
    val product = catalogViewModel.findProduct(productId) ?: catalogViewModel.findProduct("1")!!

    val (selectedColor, setSelectedColor) = remember { mutableIntStateOf(0) }
    val (selectedStorage, setSelectedStorage) = remember { mutableIntStateOf(0) }
    val (isFavorite, setFavorite) = remember { mutableStateOf(false) }
    var newReview by remember { mutableStateOf("") }
    var newQuestion by remember { mutableStateOf("") }

    val images = listOf(product.imageUrl, product.imageUrl, product.imageUrl)
    val pagerState = rememberPagerState { images.size }
    val storageOptions = listOf("Standard" to 0.0, "Premium" to 50.0)

    Box(
        modifier = modifier
            .background(Color.White),
    ) {
        LazyColumn {
            item {
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6))) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        AsyncImage(
                            model = images[page],
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.92f), shadowElevation = 2.dp) {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF374151)) }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.92f), shadowElevation = 2.dp) {
                            IconButton(onClick = {}) { Icon(Icons.Default.Share, null, tint = Color(0xFF374151)) }
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.92f), shadowElevation = 2.dp) {
                            IconButton(onClick = { setFavorite(!isFavorite) }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isFavorite) Color(0xFFEF4444) else Color(0xFF374151),
                                )
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                    ) {
                        repeat(images.size) { index ->
                            val selected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .size(if (selected) 22.dp else 8.dp, 8.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f)),
                            )
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Text(
                    "$" + String.format("%.2f", product.price),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Text("★★★★★", color = Color(0xFFF59E0B))
                    Text(
                        "  " + String.format("%.1f", product.rating),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                    )
                    Text(" (324 reviews)", color = Color(0xFF6B7280))
                }

                DividerLike()

                val colors = listOf("Midnight Black" to Color(0xFF1A1A1A), "Silver" to Color(0xFFC0C0C0), "Rose Gold" to Color(0xFFB76E79), "Sky Blue" to Color(0xFF87CEEB))
                Text(
                    text = "Color: ${colors[selectedColor].first}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 14.dp, bottom = 10.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    colors.forEachIndexed { index, (_, color) ->
                        val selected = selectedColor == index
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { setSelectedColor(index) }
                                .border(2.dp, if (selected) MaterialTheme.colorScheme.primary else Color(0xFFE5E7EB), CircleShape),
                        ) {
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(2.dp, Color(0xFF111827), CircleShape),
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Variant",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 10.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    storageOptions.forEachIndexed { index, option ->
                        val selected = selectedStorage == index
                        Card(
                            onClick = { setSelectedStorage(index) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) Color(0xFFEEF2FF) else Color.White,
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                if (selected) MaterialTheme.colorScheme.primary else Color(0xFFE5E7EB),
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp)),
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(option.first, fontWeight = FontWeight.SemiBold)
                                if (option.second > 0) {
                                    Text("+$${option.second.toInt()}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                                }
                            }
                        }
                    }
                }

                DividerLike()

                Text("Specifications", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                SpecsCard()

                Text("Description", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                Text(
                    text = "Premium product with a clean Figma-like layout. Designed for comfort, performance and long-term usage.",
                    color = Color(0xFF4B5563),
                )

                Text("Key Features", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                listOf(
                    "Premium build quality with attention to detail",
                    "Advanced technology for superior performance",
                    "Industry-leading warranty and support",
                    "Free shipping on all orders",
                ).forEach { feature ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                        Text(
                            text = feature,
                            color = Color(0xFF4B5563),
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                }

                DividerLike()
                QaSection(
                    value = newQuestion,
                    onValueChange = { newQuestion = it },
                )

                DividerLike()
                ReviewsSection(
                    value = newReview,
                    onValueChange = { newReview = it },
                )

                Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }

        Surface(
            shadowElevation = 8.dp,
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.size(10.dp))
                    val total = product.price + storageOptions[selectedStorage].second
                    Text("Add to Cart — $" + String.format("%.2f", total), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DividerLike() {
    Spacer(modifier = Modifier.height(14.dp))
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun SpecsCard() {
    val specs = listOf(
        "Brand" to "MyApplication Tech",
        "Model" to "Pro X",
        "Connectivity" to "Bluetooth 5.3",
        "Battery" to "Up to 30 hours",
        "Warranty" to "2 years",
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            specs.forEach { (k, v) ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(k, color = Color(0xFF6B7280), modifier = Modifier.weight(1f))
                    Text(v, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QaSection(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Text("Product Questions", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Store owner'a soru sor...") },
        trailingIcon = { Icon(Icons.Default.Send, null) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    )
    Text("Popular Q&A", color = Color(0xFF6B7280), modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
    val qas = listOf(
        "Bu ürün garantili mi?" to "Evet, 2 yıl resmi distribütör garantisi var.",
        "Aynı gün kargo var mı?" to "Hafta içi 15:00 öncesi siparişler aynı gün kargolanır.",
        "Kutudan neler çıkıyor?" to "Ürün, kablo, kullanım kılavuzu ve garanti belgesi.",
    )
    FlowRow(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        qas.forEach { (q, a) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Q: $q", fontWeight = FontWeight.SemiBold)
                    Text("A: $a", color = Color(0xFF4B5563), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun ReviewsSection(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Text("Reviews (324)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Yorum yaz...") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    )
    Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            Triple("Alex M.", 5, "Mükemmel ses kalitesi ve pil ömrü."),
            Triple("Sarah K.", 4, "Konforlu ama taşıma kutusu daha iyi olabilirdi."),
            Triple("David C.", 5, "Parasını hak ediyor, tavsiye ederim."),
        ).forEach { (name, rating, comment) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(name, fontWeight = FontWeight.SemiBold)
                    Text("★".repeat(rating) + "☆".repeat(5 - rating), color = Color(0xFFF59E0B))
                    Text(comment, color = Color(0xFF4B5563), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

