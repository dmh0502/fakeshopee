package com.fakeshopee.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fakeshopee.app.domain.model.Product
import com.fakeshopee.app.presentation.mvi.StoreIntent
import com.fakeshopee.app.presentation.mvi.StoreState
import com.fakeshopee.app.presentation.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    state: StoreState,
    onIntent: (StoreIntent) -> Unit,
    onProductClick: (String) -> Unit
) {
    val categories = listOf("All", "Laptops", "Mobile", "Audio", "Wearables", "Accessories")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "FakeShopee",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = FakeShopeeNavy
                        )
                        Text(
                            "FLAGSHIP TECH STORE",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = FakeShopeeOrange,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onIntent(StoreIntent.RefreshCatalog) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh catalog",
                            tint = FakeShopeeNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FakeShopeeBackground
                )
            )
        },
        containerColor = FakeShopeeBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sticky Offline Warning Banner
            if (state.isOffline) {
                Surface(
                    color = Color(0xFFFFDAD6),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = Color(0xFFBA1A1A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "You are offline",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF93000A)
                            )
                        }
                        TextButton(onClick = { onIntent(StoreIntent.RefreshCatalog) }) {
                            Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A))
                        }
                    }
                }
            }

            // Search Box
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onIntent(StoreIntent.Search(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search products, silicon...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FakeShopeeSubtext) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF2F3FF),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = FakeShopeeOrange
                ),
                singleLine = true
            )

            // Category Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = state.selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) FakeShopeeOrange else FakeShopeeSurface,
                        modifier = Modifier.clickable { onIntent(StoreIntent.SelectCategory(cat)) }
                    ) {
                        Text(
                            cat,
                            color = if (isSelected) Color.White else FakeShopeeSubtext,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Sort Filter Chips Row
            val sortOptions = listOf(
                "featured" to "Featured",
                "price_asc" to "Price: Low to High",
                "price_desc" to "Price: High to Low",
                "rating" to "Top Rated"
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(sortOptions) { (key, label) ->
                    val isSelected = state.sortBy == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { onIntent(StoreIntent.ChangeSort(key)) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FakeShopeeOrange.copy(alpha = 0.15f),
                            selectedLabelColor = FakeShopeeOrange
                        )
                    )
                }
            }

            // Products Grid
            val filteredProducts = state.products.filter {
                val matchesCat = state.selectedCategory == "All" || it.category.equals(state.selectedCategory, ignoreCase = true)
                val matchesQuery = it.title.contains(state.searchQuery, ignoreCase = true) ||
                        it.description.contains(state.searchQuery, ignoreCase = true)
                matchesCat && matchesQuery
            }.let { list ->
                when (state.sortBy) {
                    "price_asc" -> list.sortedBy { it.price }
                    "price_desc" -> list.sortedByDescending { it.price }
                    "rating" -> list.sortedByDescending { it.rating }
                    else -> list
                }
            }

            val gridState = rememberLazyGridState()

            LaunchedEffect(state.sortBy, state.selectedCategory, state.searchQuery) {
                if (filteredProducts.isNotEmpty()) {
                    gridState.scrollToItem(0)
                }
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = FakeShopeeSubtext,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No products found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = FakeShopeeOnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Try adjusting your category or search query",
                            fontSize = 12.sp,
                            color = FakeShopeeSubtext
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) },
                            onFavoriteClick = { onIntent(StoreIntent.ToggleFavorite(product.id)) },
                            onAddToCart = { onIntent(StoreIntent.QuickAddToCart(product)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Product Image with Favorite Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF2F3FF))
            ) {
                AsyncImage(
                    model = product.images.firstOrNull(),
                    contentDescription = product.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clickable { onFavoriteClick() }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (product.isFavorite) Color(0xFFBA1A1A) else FakeShopeeSubtext,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                product.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = FakeShopeeOnSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Rating & Reviews
            if (product.reviewCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        "${product.rating} (${product.reviewCount})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = FakeShopeeSubtext
                    )
                }
            } else {
                Text(
                    "No reviews yet",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = FakeShopeeSubtext
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price & Quick Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$${String.format(Locale.US, "%,.2f", product.price)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = FakeShopeeOrange
                )

                Surface(
                    shape = CircleShape,
                    color = FakeShopeeMint,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onAddToCart() }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add to Cart",
                            tint = Color(0xFF002113),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
