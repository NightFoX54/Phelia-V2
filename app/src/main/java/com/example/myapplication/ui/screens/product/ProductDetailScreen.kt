@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.myapplication.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.data.model.EligibleReviewSlot
import com.example.myapplication.data.model.Product
import com.example.myapplication.data.model.ProductStatsSnapshot
import com.example.myapplication.data.model.Store
import com.example.myapplication.data.model.ProductQuestionDoc
import com.example.myapplication.data.model.ProductReviewDoc
import com.example.myapplication.data.model.ProductDetailBundle
import com.example.myapplication.data.model.ProductVariant
import com.example.myapplication.data.model.VariantSelection
import com.example.myapplication.data.model.displayImagesForVariant
import com.example.myapplication.data.model.finalPrice
import com.example.myapplication.data.repository.ProductRepository
import com.example.myapplication.data.repository.ProductStatsRepository
import com.example.myapplication.ui.product.colorLabelAndComposeColor
import com.example.myapplication.ui.product.isColorAttributeKey
import com.example.myapplication.viewmodel.CartViewModel
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.example.myapplication.viewmodel.ProductEngagementViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/** Height of the hero image pager (used for collapse progress). */
private val ProductDetailHeroImageHeight = 380.dp

private val PinnedProductStripHeight = 56.dp

@Composable
fun ProductDetailScreen(
    productId: String,
    highlightQuestionId: String = "",
    audience: ProductDetailAudience = ProductDetailAudience.Customer,
    cartViewModel: CartViewModel,
    favoritesViewModel: FavoritesViewModel,
    engagementViewModel: ProductEngagementViewModel,
    ownerStoreId: String = "",
    onBack: () -> Unit,
    onAddedToCart: () -> Unit = {},
    onOpenStore: (String) -> Unit = {},
    onEditProduct: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val favoriteIds by favoritesViewModel.favoriteProductIds.collectAsState()
    val isFavorite = favoriteIds.contains(productId)
    val repository = remember { ProductRepository() }
    val productStatsRepository = remember { ProductStatsRepository() }
    var loadState by remember(productId) { mutableStateOf<DetailLoadState>(DetailLoadState.Loading) }
    var selectedAttributes by remember(productId) { mutableStateOf(mapOf<String, String>()) }
    var productViewRecorded by remember(productId) { mutableStateOf(false) }

    LaunchedEffect(productId, audience) {
        loadState = DetailLoadState.Loading
        repository.fetchProductDetail(
            productId,
            forStoreManagement = audience == ProductDetailAudience.StoreOwner,
        ).fold(
            onSuccess = { loadState = DetailLoadState.Ready(it) },
            onFailure = { loadState = DetailLoadState.Error(it.message ?: "Couldn't load product") },
        )
    }

    LaunchedEffect(productId, audience, loadState) {
        if (audience != ProductDetailAudience.Customer) return@LaunchedEffect
        if (loadState !is DetailLoadState.Ready || productViewRecorded) return@LaunchedEffect
        productViewRecorded = true
        productStatsRepository.recordProductView(productId)
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
            .background(MaterialTheme.colorScheme.surface),
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
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Back") }
                }
            }
            is DetailLoadState.Ready -> {
                ProductDetailContent(
                    bundle = state.bundle,
                    audience = audience,
                    ownerStoreId = ownerStoreId,
                    highlightQuestionId = highlightQuestionId,
                    onOpenStore = onOpenStore,
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
                    engagementViewModel = engagementViewModel,
                    isFavorite = isFavorite,
                    onFavoriteToggle = { favoritesViewModel.toggleFavorite(productId) },
                    onBack = onBack,
                    onAddedToCart = onAddedToCart,
                    onEditProduct = onEditProduct,
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
    audience: ProductDetailAudience,
    ownerStoreId: String,
    highlightQuestionId: String,
    onOpenStore: (String) -> Unit,
    selectedAttributes: Map<String, String>,
    onSelectAttribute: (String, String) -> Unit,
    resolvedVariant: ProductVariant?,
    images: List<String>,
    pagerState: PagerState,
    cartViewModel: CartViewModel,
    engagementViewModel: ProductEngagementViewModel,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onBack: () -> Unit,
    onAddedToCart: () -> Unit,
    onEditProduct: (() -> Unit)?,
) {
    val product = bundle.product
    val isStoreManagement =
        audience == ProductDetailAudience.StoreOwner &&
            ownerStoreId.isNotBlank() &&
            ownerStoreId == product.storeId
    val keys = bundle.variantAttributeKeys
    val variants = bundle.variants
    var newQuestion by remember { mutableStateOf("") }
    var reviewRating by remember { mutableIntStateOf(5) }
    var newReviewComment by remember { mutableStateOf("") }
    val auth = remember { FirebaseAuth.getInstance() }
    val signedIn = auth.currentUser != null
    val reviews by engagementViewModel.reviews.collectAsState()
    val summaryRating = remember(reviews, product.rating) {
        if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else product.rating
    }
    val summaryReviewCount = if (reviews.isNotEmpty()) reviews.size else product.reviewCount
    val questions by engagementViewModel.questions.collectAsState()
    val questionsBringIntoView = remember { BringIntoViewRequester() }
    LaunchedEffect(highlightQuestionId, questions) {
        if (highlightQuestionId.isBlank()) return@LaunchedEffect
        delay(450)
        questionsBringIntoView.bringIntoView()
    }
    val eligibleReview by engagementViewModel.eligibleReviewSlot.collectAsState()
    val engagementBusy by engagementViewModel.busy.collectAsState()
    val engagementError by engagementViewModel.lastError.collectAsState()

    val statsRepo = remember { ProductStatsRepository() }
    var ownerStats by remember(product.productId) { mutableStateOf<ProductStatsSnapshot?>(null) }
    var ownerStatsLoading by remember(product.productId) { mutableStateOf(false) }
    LaunchedEffect(product.productId, isStoreManagement) {
        if (!isStoreManagement) {
            ownerStats = null
            ownerStatsLoading = false
            return@LaunchedEffect
        }
        ownerStatsLoading = true
        ownerStats = statsRepo.fetchProductStatsSnapshot(product.productId).getOrNull() ?: ProductStatsSnapshot()
        ownerStatsLoading = false
    }

    val displayPages = if (images.isEmpty()) listOf<String?>(null) else images.map { it }

    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val heroHeightPx = remember(density) { with(density) { ProductDetailHeroImageHeight.roundToPx() } }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        LazyColumn(state = listState) {
            item {
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
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
                                    .height(ProductDetailHeroImageHeight),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(ProductDetailHeroImageHeight)
                                    .background(MaterialTheme.colorScheme.outlineVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("No image", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), shadowElevation = 2.dp) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (isStoreManagement && onEditProduct != null) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), shadowElevation = 2.dp) {
                                IconButton(onClick = onEditProduct) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit product", tint = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            Spacer(modifier = Modifier.size(10.dp))
                        } else {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), shadowElevation = 2.dp) {
                                IconButton(onClick = {}) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.onSurface) }
                            }
                            Spacer(modifier = Modifier.size(10.dp))
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), shadowElevation = 2.dp) {
                                IconButton(onClick = onFavoriteToggle) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
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
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                    product.brand["name"]?.takeIf { it.isNotBlank() }?.let { b ->
                        Text(b, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        product.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val price = resolvedVariant?.finalPrice() ?: variants.minOfOrNull { it.finalPrice() } ?: 0.0
                    Text(
                        "$" + String.format("%.2f", price),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    resolvedVariant?.let { v ->
                        val pct = v.discountPercent.coerceIn(0, 100)
                        if (pct > 0 && v.price > v.finalPrice()) {
                            val save = (v.price - v.finalPrice()).coerceAtLeast(0.0)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(top = 6.dp),
                            ) {
                                Text(
                                    text = "$" + String.format("%.2f", v.price),
                                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(999.dp),
                                ) {
                                    Text(
                                        text = "Save $" + String.format(java.util.Locale.US, "%.2f", save) + " ($pct% OFF)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                            .fillMaxWidth(),
                    ) {
                        Text("★★★★★", color = Color(0xFFF59E0B))
                        Text(
                            "  " + String.format("%.1f", summaryRating),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            " ($summaryReviewCount reviews)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    resolvedVariant?.let { v ->
                        Text(
                            text = if (v.stock > 0) "In stock: ${v.stock}" else "Out of stock",
                            color = if (v.stock > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
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
                                                        !available -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                                        else -> MaterialTheme.colorScheme.outlineVariant
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
                                                        .background(MaterialTheme.colorScheme.surface)
                                                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
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
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Clip,
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
                                                maxLines = 2,
                                                overflow = TextOverflow.Clip,
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
                        text = product.description.ifBlank { "No description provided." },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (isStoreManagement) {
                        DividerLike()
                        Text(
                            "Performance",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
                        )
                        Text(
                            "Product analytics (all time)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 10.dp),
                        )
                        if (ownerStatsLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp,
                                )
                            }
                        } else {
                            ownerStats?.let { s ->
                                val scheme = MaterialTheme.colorScheme
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        OwnerStatTile(
                                            icon = Icons.Default.Visibility,
                                            label = "Views",
                                            value = formatStatCount(s.views),
                                            tint = scheme.onPrimaryContainer,
                                            bg = scheme.primaryContainer,
                                            modifier = Modifier.weight(1f),
                                        )
                                        OwnerStatTile(
                                            icon = Icons.Default.ShoppingCart,
                                            label = "Add to cart",
                                            value = formatStatCount(s.addedToCart),
                                            tint = scheme.onSecondaryContainer,
                                            bg = scheme.secondaryContainer,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        OwnerStatTile(
                                            icon = Icons.Default.Favorite,
                                            label = "In favorites",
                                            value = formatStatCount(s.addedToFavorite),
                                            tint = scheme.onTertiaryContainer,
                                            bg = scheme.tertiaryContainer,
                                            modifier = Modifier.weight(1f),
                                        )
                                        OwnerStatTile(
                                            icon = Icons.Default.Inventory,
                                            label = "Units sold",
                                            value = formatStatCount(s.purchased),
                                            tint = scheme.onSurfaceVariant,
                                            bg = scheme.surfaceVariant,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (audience == ProductDetailAudience.Customer) {
                        bundle.store?.takeIf { it.storeId.isNotBlank() }?.let { st ->
                            Spacer(modifier = Modifier.height(16.dp))
                            ProductStorePreviewRow(
                                store = st,
                                onClick = { onOpenStore(st.storeId) },
                            )
                        }
                    }

                    DividerLike()
                    ProductQuestionsSection(
                        modifier = Modifier.bringIntoViewRequester(questionsBringIntoView),
                        highlightQuestionId = highlightQuestionId,
                        questions = questions,
                        questionDraft = newQuestion,
                        onQuestionChange = { newQuestion = it },
                        signedIn = signedIn,
                        busy = engagementBusy,
                        errorText = engagementError,
                        customerQuestionEnabled = !isStoreManagement,
                        isStoreManagement = isStoreManagement,
                        onSubmitQuestion = {
                            engagementViewModel.submitQuestion(it) { result ->
                                result.onSuccess { newQuestion = "" }
                            }
                        },
                        onAnswerQuestion = { qId, text ->
                            engagementViewModel.answerQuestionAsStore(qId, text) { }
                        },
                    )

                    DividerLike()
                    ProductReviewsSection(
                        reviews = reviews,
                        reviewRating = reviewRating,
                        onReviewRatingChange = { reviewRating = it },
                        reviewComment = newReviewComment,
                        onReviewCommentChange = { newReviewComment = it },
                        eligibleReview = eligibleReview,
                        signedIn = signedIn,
                        busy = engagementBusy,
                        errorText = engagementError,
                        customerReviewEnabled = !isStoreManagement,
                        isStoreManagement = isStoreManagement,
                        onSubmitReview = { r, c ->
                            engagementViewModel.submitReview(r, c) { result ->
                                result.onSuccess { newReviewComment = "" }
                            }
                        },
                        onStoreReplyReview = { reviewId, text ->
                            engagementViewModel.respondToReviewAsStore(reviewId, text) { }
                        },
                    )

                    Spacer(modifier = Modifier.height(if (isStoreManagement) 32.dp else 120.dp))
                }
            }
        }

        if (!isStoreManagement) {
            val priceVal = resolvedVariant?.finalPrice() ?: variants.minOfOrNull { it.finalPrice() } ?: 0.0
            val stockLine = resolvedVariant?.let {
                if (it.stock > 0) "In stock: ${it.stock}" else "Out of stock"
            } ?: "Select options"
            val thumbUrl = displayPages.getOrNull(pagerState.currentPage)?.takeIf { !it.isNullOrBlank() }
            CustomerPinnedProductStrip(
                modifier = Modifier.align(Alignment.TopCenter),
                listState = listState,
                heroHeightPx = heroHeightPx,
                productName = product.name,
                price = priceVal,
                stockLine = stockLine,
                imageUrl = thumbUrl,
                onBack = onBack,
            )
        }

        val canAdd = resolvedVariant != null && resolvedVariant.stock > 0

        if (!isStoreManagement) {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
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
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        val label = when {
                            resolvedVariant == null -> "Select options"
                            resolvedVariant.stock <= 0 -> "Out of stock"
                            else -> "Add to cart — $" + String.format("%.2f", resolvedVariant.finalPrice())
                        }
                        Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

/**
 * Customer-only: as the hero image scrolls away, a compact strip appears under the status bar
 * with thumbnail, title, price, and stock so key info stays visible.
 */
@Composable
private fun CustomerPinnedProductStrip(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    heroHeightPx: Int,
    productName: String,
    price: Double,
    stockLine: String,
    imageUrl: String?,
    onBack: () -> Unit,
) {
    val scrollProgress by remember(heroHeightPx) {
        derivedStateOf {
            when (listState.firstVisibleItemIndex) {
                0 -> (listState.firstVisibleItemScrollOffset.toFloat() / heroHeightPx.coerceAtLeast(1)).coerceIn(0f, 1f)
                else -> 1f
            }
        }
    }

    if (scrollProgress >= 0.04f) {
        val enter = ((scrollProgress - 0.04f) / 0.96f).coerceIn(0f, 1f)
        val smooth = enter * enter * (3f - 2f * enter)

        val scheme = MaterialTheme.colorScheme
        val stockColor = when {
            stockLine.startsWith("In stock") -> scheme.tertiary
            stockLine == "Out of stock" -> scheme.error
            else -> scheme.onSurfaceVariant
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .zIndex(24f)
                .graphicsLayer {
                    alpha = smooth
                    translationY = -8f * (1f - smooth)
                },
            color = scheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = (6.dp * smooth),
        ) {
            Column(Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PinnedProductStripHeight)
                        .padding(start = 2.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp)),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            productName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "$" + String.format("%.2f", price),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                            )
                            Text(
                                stockLine,
                                style = MaterialTheme.typography.bodySmall,
                                color = stockColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun uniqueValuesForKey(variants: List<ProductVariant>, key: String): List<String> =
    variants.mapNotNull { it.attributes[key]?.takeIf { v -> v.isNotBlank() } }.distinct().sorted()

private fun formatStatCount(n: Long): String = when {
    n < 1000 -> n.toString()
    n < 1_000_000 -> String.format(java.util.Locale.US, "%.1fK", n / 1000.0)
    else -> String.format(java.util.Locale.US, "%.1fM", n / 1_000_000.0)
}

@Composable
private fun OwnerStatTile(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    value,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun ProductStorePreviewRow(
    store: Store,
    onClick: () -> Unit,
) {
    Text("Sold by", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (store.logo.isNotBlank()) {
                AsyncImage(
                    model = store.logo,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(modifier = Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(
                    store.name.ifBlank { "Store" },
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("★★★★★", color = Color(0xFFF59E0B), style = MaterialTheme.typography.bodySmall)
                    Text(
                        "  " + String.format("%.1f", store.rating),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        " (${store.reviewCount} reviews)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text("›", color = MaterialTheme.colorScheme.outline, fontSize = 22.sp)
        }
    }
}

@Composable
private fun DividerLike() {
    Spacer(modifier = Modifier.height(14.dp))
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun SpecsCard(product: Product) {
    val specs = buildList {
        product.brand["name"]?.takeIf { it.isNotBlank() }?.let { add("Brand" to it) }
        product.category["name"]?.takeIf { it.isNotBlank() }?.let { add("Category" to it) }
    }
    if (specs.isEmpty()) {
        Text("No additional details.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    } else {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                specs.forEach { (k, v) ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(k, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(v, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductQuestionsSection(
    modifier: Modifier = Modifier,
    highlightQuestionId: String,
    questions: List<ProductQuestionDoc>,
    questionDraft: String,
    onQuestionChange: (String) -> Unit,
    signedIn: Boolean,
    busy: Boolean,
    errorText: String?,
    customerQuestionEnabled: Boolean,
    isStoreManagement: Boolean,
    onSubmitQuestion: (String) -> Unit,
    onAnswerQuestion: (String, String) -> Unit,
) {
    Column(modifier = modifier) {
    Text(
        if (isStoreManagement) "Customer questions" else "Product questions",
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp),
    )
    if (customerQuestionEnabled) {
        if (!signedIn) {
            Text("Sign in to ask a question.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        } else {
            OutlinedTextField(
                value = questionDraft,
                onValueChange = onQuestionChange,
                placeholder = { Text("Ask the store...") },
                trailingIcon = {
                    IconButton(
                        onClick = { onSubmitQuestion(questionDraft) },
                        enabled = !busy && questionDraft.isNotBlank(),
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send question")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !busy,
            )
        }
    } else if (isStoreManagement) {
        Text("Reply to shoppers below.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
    errorText?.takeIf { it.isNotBlank() }?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
    }
    Text("Questions", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
    if (questions.isEmpty()) {
        Text("No questions yet.", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodySmall)
    } else {
        FlowRow(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            questions.forEach { q ->
                var answerDraft by remember(q.questionId) { mutableStateOf("") }
                val highlighted = highlightQuestionId.isNotBlank() && q.questionId == highlightQuestionId
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        if (highlighted) 2.dp else 1.dp,
                        if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Q: ${q.question}", fontWeight = FontWeight.SemiBold)
                        val ans = q.answer
                        if (!ans.isNullOrBlank()) {
                            Text("A: $ans", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                        } else {
                            Text(
                                "Awaiting store reply",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        if (isStoreManagement && ans.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = answerDraft,
                                onValueChange = { answerDraft = it },
                                placeholder = { Text("Your answer…") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !busy,
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp),
                            )
                            Button(
                                onClick = {
                                    onAnswerQuestion(q.questionId, answerDraft)
                                    answerDraft = ""
                                },
                                enabled = !busy && answerDraft.isNotBlank(),
                                modifier = Modifier.padding(top = 6.dp),
                            ) {
                                Text("Post answer")
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun ProductReviewsSection(
    reviews: List<ProductReviewDoc>,
    reviewRating: Int,
    onReviewRatingChange: (Int) -> Unit,
    reviewComment: String,
    onReviewCommentChange: (String) -> Unit,
    eligibleReview: EligibleReviewSlot?,
    signedIn: Boolean,
    busy: Boolean,
    errorText: String?,
    customerReviewEnabled: Boolean,
    isStoreManagement: Boolean,
    onSubmitReview: (Double, String) -> Unit,
    onStoreReplyReview: (String, String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Reviews",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp),
        )
    if (customerReviewEnabled && eligibleReview != null && signedIn) {
        Text(
            "You can review this product from a completed order.",
            color = scheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            repeat(5) { index ->
                val filled = index < reviewRating
                Text(
                    text = if (filled) "★" else "☆",
                    fontSize = 26.sp,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.clickable(enabled = !busy) { onReviewRatingChange(index + 1) },
                )
            }
        }
        OutlinedTextField(
            value = reviewComment,
            onValueChange = onReviewCommentChange,
            placeholder = { Text("Write your review...", color = scheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 2,
            enabled = !busy,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = scheme.surfaceVariant,
                unfocusedContainerColor = scheme.surfaceVariant,
                unfocusedBorderColor = scheme.outlineVariant,
                focusedBorderColor = scheme.primary,
            ),
        )
        Button(
            onClick = { onSubmitReview(reviewRating.toDouble(), reviewComment) },
            enabled = !busy && reviewComment.isNotBlank(),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text("Submit review")
        }
    } else if (customerReviewEnabled && signedIn && eligibleReview == null) {
        Text(
            "Reviews can only be submitted for products you bought in a completed order (once per purchase).",
            color = scheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp),
        )
    } else if (customerReviewEnabled && !signedIn) {
        Text("Sign in to leave a review.", color = scheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    } else if (isStoreManagement) {
        Text("Respond to buyer reviews below.", color = scheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
    errorText?.takeIf { it.isNotBlank() }?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (reviews.isEmpty()) {
            Text(
                "No reviews yet.",
                color = scheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            reviews.forEach { rev ->
                var replyDraft by remember(rev.reviewId) { mutableStateOf("") }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = scheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val label = "Buyer ···${rev.userId.takeLast(4)}"
                        Text(
                            label,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleSmall,
                            color = scheme.onSurface,
                        )
                        val n = rev.rating.roundToInt().coerceIn(0, 5)
                        Text(
                            "★".repeat(n) + "☆".repeat(5 - n),
                            color = Color(0xFFF59E0B),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            rev.comment,
                            color = scheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                        )
                        val sr = rev.storeResponse
                        if (!sr.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = scheme.surface,
                                tonalElevation = 1.dp,
                                shadowElevation = 0.dp,
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        "Store reply",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = scheme.primary,
                                    )
                                    Text(
                                        sr,
                                        color = scheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 20.sp,
                                    )
                                }
                            }
                        }
                        if (isStoreManagement && sr.isNullOrBlank()) {
                            OutlinedTextField(
                                value = replyDraft,
                                onValueChange = { replyDraft = it },
                                placeholder = { Text("Public reply…", color = scheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !busy,
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = scheme.surface,
                                    unfocusedContainerColor = scheme.surface,
                                    unfocusedBorderColor = scheme.outlineVariant,
                                    focusedBorderColor = scheme.primary,
                                ),
                            )
                            Button(
                                onClick = {
                                    onStoreReplyReview(rev.reviewId, replyDraft)
                                    replyDraft = ""
                                },
                                enabled = !busy && replyDraft.isNotBlank(),
                                modifier = Modifier.padding(top = 4.dp),
                            ) {
                                Text("Post reply")
                            }
                        }
                    }
                }
            }
        }
    }
    }
}
