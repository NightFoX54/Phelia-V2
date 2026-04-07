package com.example.myapplication.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Product
import com.example.myapplication.data.model.ProductDetailBundle
import com.example.myapplication.data.model.ProductVariant
import com.example.myapplication.data.model.VariantSelection
import com.example.myapplication.data.model.displayImagesForVariant
import com.example.myapplication.data.repository.ProductRepository
import com.example.myapplication.ui.product.colorLabelAndComposeColor
import com.example.myapplication.ui.product.isColorAttributeKey
import com.example.myapplication.viewmodel.CartViewModel
import com.example.myapplication.viewmodel.FavoritesViewModel

@Composable
fun ProductDetailScreen(
    productId: String,
    cartViewModel: CartViewModel,
    favoritesViewModel: FavoritesViewModel,
    onBack: () -> Unit,
    onAddedToCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val favoriteIds by favoritesViewModel.favoriteProductIds.collectAsState()
    val isFavorite = favoriteIds.contains(productId)
    val repository = remember { ProductRepository() }
    var loadState by remember(productId) { mutableStateOf<DetailLoadState>(DetailLoadState.Loading) }
    var selectedAttributes by remember(productId) { mutableStateOf(mapOf<String, String>()) }

    LaunchedEffect(productId) {
        loadState = DetailLoadState.Loading
        repository.fetchProductDetail(productId).fold(
            onSuccess = { loadState = DetailLoadState.Ready(it) },
            onFailure = { loadState = DetailLoadState.Error(it.message ?: "Yuklenemedi") },
        )
    }

    LaunchedEffect(productId, loadState) {
        val ready = loadState as? DetailLoadState.Ready ?: return@LaunchedEffect
        selectedAttributes = VariantSelection.initialSelection(ready.bundle.variants, ready.bundle.variantAttributeKeys)
    }

    val readyBundle = (loadState as? DetailLoadState.Ready)?.bundle
    val resolvedVariant = readyBundle?.let { b ->
        VariantSelection.resolveVariant(selectedAttributes, b.variants, b.variantAttributeKeys)
    }
    val images = readyBundle?.let { displayImagesForVariant(it.product, resolvedVariant) }.orEmpty()
    val pagerState = rememberPagerState(pageCount = { maxOf(1, images.size) })

    LaunchedEffect(images) {
        if (images.isNotEmpty()) pagerState.scrollToPage(0)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        when (val state = loadState) {
            DetailLoadState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is DetailLoadState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.message, color = Color(0xFFDC2626))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Geri") }
                }
            }
            is DetailLoadState.Ready -> {
                ProductDetailContent(
                    bundle = state.bundle,
                    selectedAttributes = selectedAttributes,
                    onSelectAttribute = { key, value ->
                        selectedAttributes = VariantSelection.pickAttribute(
                            key,
                            value,
                            selectedAttributes,
                            state.bundle.variants,
                            state.bundle.variantAttributeKeys,
                        )
                    },
                    resolvedVariant = resolvedVariant,
                    images = images,
                    pagerState = pagerState,
                    cartViewModel = cartViewModel,
                    isFavorite = isFavorite,
                    onFavoriteToggle = { favoritesViewModel.toggleFavorite(productId) },
                    onBack = onBack,
                    onAddedToCart = onAddedToCart,
                )
            }
        }
    }
}

private sealed interface DetailLoadState {
    data object Loading : DetailLoadState
    data class Error(val message: String) : DetailLoadState
    data class Ready(val bundle: ProductDetailBundle) : DetailLoadState
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductDetailContent(
    bundle: ProductDetailBundle,
    selectedAttributes: Map<String, String>,
    onSelectAttribute: (String, String) -> Unit,
    resolvedVariant: ProductVariant?,
    images: List<String>,
    pagerState: PagerState,
    cartViewModel: CartViewModel,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onBack: () -> Unit,
    onAddedToCart: () -> Unit,
) {
    val product = bundle.product
    val keys = bundle.variantAttributeKeys
    val variants = bundle.variants
    var newReview by remember { mutableStateOf("") }
    var newQuestion by remember { mutableStateOf("") }

    val displayPages = if (images.isEmpty()) listOf<String?>(null) else images.map { it }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        LazyColumn {
            item {
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6))) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        val url = displayPages.getOrNull(page)?.takeIf { !it.isNullOrBlank() }
                        if (url != null) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp)
                                    .background(Color(0xFFE5E7EB)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("Gorsel yok", color = Color(0xFF6B7280))
                            }
                        }
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
                            IconButton(onClick = onFavoriteToggle) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isFavorite) "Favorilerden cikar" else "Favorilere ekle",
                                    tint = if (isFavorite) Color(0xFFEF4444) else Color(0xFF374151),
                                )
                            }
                        }
                    }
                    if (displayPages.size > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                        ) {
                            repeat(displayPages.size) { index ->
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
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                    product.brand["name"]?.takeIf { it.isNotBlank() }?.let { b ->
                        Text(b, style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
                    }
                    Text(
                        product.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                    )
                    val price = resolvedVariant?.price ?: variants.minOfOrNull { it.price } ?: 0.0
                    Text(
                        "$" + String.format("%.2f", price),
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
                        Text(" (${product.reviewCount} reviews)", color = Color(0xFF6B7280))
                    }

                    resolvedVariant?.let { v ->
                        Text(
                            text = if (v.stock > 0) "Stokta: ${v.stock}" else "Stokta yok",
                            color = if (v.stock > 0) Color(0xFF16A34A) else Color(0xFFDC2626),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }

                    DividerLike()

                    keys.forEach { attrKey ->
                        val values = uniqueValuesForKey(variants, attrKey)
                        if (values.isEmpty()) return@forEach
                        Text(
                            text = attrKey.replaceFirstChar { it.uppercaseChar() },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 14.dp, bottom = 10.dp),
                        )
                        if (isColorAttributeKey(attrKey)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                values.forEach { value ->
                                    val available = VariantSelection.isAvailable(attrKey, value, selectedAttributes, variants)
                                    val selected = selectedAttributes[attrKey] == value
                                    val (_, composeColor) = colorLabelAndComposeColor(value)
                                    if (composeColor != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    2.dp,
                                                    when {
                                                        selected -> MaterialTheme.colorScheme.primary
                                                        !available -> Color(0xFFE5E7EB).copy(alpha = 0.5f)
                                                        else -> Color(0xFFE5E7EB)
                                                    },
                                                    CircleShape,
                                                )
                                                .clickable { onSelectAttribute(attrKey, value) },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .background(composeColor),
                                            )
                                            if (!available) {
                                                Box(
                                                    Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(alpha = 0.35f)),
                                                )
                                            }
                                            if (selected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White)
                                                        .border(2.dp, Color(0xFF111827), CircleShape),
                                                )
                                            }
                                        }
                                    } else {
                                        FilterChip(
                                            selected = selected,
                                            onClick = { onSelectAttribute(attrKey, value) },
                                            label = {
                                                Text(
                                                    value,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            },
                                            modifier = Modifier.alpha(if (available) 1f else 0.45f),
                                        )
                                    }
                                }
                            }
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                values.forEach { value ->
                                    val available = VariantSelection.isAvailable(attrKey, value, selectedAttributes, variants)
                                    val selected = selectedAttributes[attrKey] == value
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onSelectAttribute(attrKey, value) },
                                        label = {
                                            Text(
                                                value,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                        modifier = Modifier.alpha(if (available) 1f else 0.45f),
                                    )
                                }
                            }
                        }
                    }

                    DividerLike()

                    Text("Specifications", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                    SpecsCard(product = product)

                    Text("Description", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                    Text(
                        text = product.description.ifBlank { "Aciklama eklenmemis." },
                        color = Color(0xFF4B5563),
                    )

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

        val canAdd = resolvedVariant != null && resolvedVariant.stock > 0

        Surface(
            shadowElevation = 8.dp,
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                Button(
                    onClick = {
                        val v = resolvedVariant ?: return@Button
                        cartViewModel.addToCart(
                            productId = product.productId,
                            variantId = v.variantId,
                            quantity = 1,
                        )
                        onAddedToCart()
                    },
                    enabled = canAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.size(10.dp))
                    val label = when {
                        resolvedVariant == null -> "Varyant secin"
                        resolvedVariant.stock <= 0 -> "Stokta yok"
                        else -> "Sepete ekle — $" + String.format("%.2f", resolvedVariant.price)
                    }
                    Text(label, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun uniqueValuesForKey(variants: List<ProductVariant>, key: String): List<String> =
    variants.mapNotNull { it.attributes[key]?.takeIf { v -> v.isNotBlank() } }.distinct().sorted()

@Composable
private fun DividerLike() {
    Spacer(modifier = Modifier.height(14.dp))
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun SpecsCard(product: Product) {
    val specs = buildList {
        product.brand["name"]?.takeIf { it.isNotBlank() }?.let { add("Brand" to it) }
        product.category["name"]?.takeIf { it.isNotBlank() }?.let { add("Category" to it) }
    }
    if (specs.isEmpty()) {
        Text("Ek bilgi yok.", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodySmall)
    } else {
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
    Text("Reviews", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
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
