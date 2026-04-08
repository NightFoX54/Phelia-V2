package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Locale
import com.example.myapplication.data.model.ui.Product

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box {
            if (product.imageUrl.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .background(Color(0xFFE5E7EB))
                        .clickable(onClick = onClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No image", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodySmall)
                }
            } else {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .clickable(onClick = onClick),
                )
            }
            if (onFavoriteClick != null) {
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Favorilerden cikar" else "Favorilere ekle",
                        tint = if (isFavorite) Color(0xFFEF4444) else Color(0xFF9CA3AF),
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
        ) {
            product.brandName?.takeIf { it.isNotBlank() }?.let { b ->
                Text(
                    text = b,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.padding(top = 2.dp))
            }
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp),
            ) {
                Text(text = "★", color = Color(0xFFF59E0B))
                Text(
                    text = String.format(Locale.US, "%.1f", product.rating),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4B5563),
                )
                if (product.reviewCount > 0) {
                    Text(
                        text = "(${product.reviewCount})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF),
                    )
                }
            }
            Text(
                text = "$" + String.format("%.2f", product.price),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

