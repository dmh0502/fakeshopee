package com.fakeshopee.app.presentation.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fakeshopee.app.R
import com.fakeshopee.app.presentation.mvi.ProductUiModel
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
    val categories = listOf(
        "All" to Icons.Default.Apps,
        "Laptops" to Icons.Default.Laptop,
        "Mobile" to Icons.Default.Smartphone,
        "Audio" to Icons.Default.Headphones,
        "Wearables" to Icons.Default.Watch,
        "Accessories" to Icons.Default.Devices
    )

    Scaffold(
        topBar = {
            Surface(
                color = FakeShopeeBackground.copy(alpha = 0.95f),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fakeshopee_logo),
                            contentDescription = "FakeShopee Brand Logo",
                            modifier = Modifier.size(38.dp)
                        )
                        Column {
                            Text(
                                "FakeShopee",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = FakeShopeeNavy,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                "FLAGSHIP TECH STORE",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = FakeShopeeOrange,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(onClick = { }) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = FakeShopeeSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FakeShopeeOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
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
                    color = FakeShopeeErrorContainer,
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
                                tint = FakeShopeeError,
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
                            Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FakeShopeeError)
                        }
                    }
                }
            }

            // Sub-Header Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FakeShopeeTertiaryFixedDim)
                        )
                        Text(
                            "FLAGSHIP TECH STORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FakeShopeeOrange,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Explore Next-Gen Hardware",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FakeShopeeOnSurface
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = FakeShopeeSurfaceContainer,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onIntent(StoreIntent.RefreshCatalog) }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Refresh",
                            tint = FakeShopeeSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Search Pill (No voice search icon as requested)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = FakeShopeeSurfaceContainerLowest,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = FakeShopeeSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { onIntent(StoreIntent.Search(it)) },
                        placeholder = { Text("Search products, silicon, chipsets...", fontSize = 13.sp, color = FakeShopeeSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = FakeShopeeSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Category Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                items(categories) { (cat, icon) ->
                    val isSelected = state.selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) FakeShopeeOrange else FakeShopeeSurfaceContainerLowest,
                        shadowElevation = if (isSelected) 4.dp else 1.dp,
                        modifier = Modifier.clickable { onIntent(StoreIntent.SelectCategory(cat)) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else FakeShopeeSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                cat,
                                color = if (isSelected) Color.White else FakeShopeeOnSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
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
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) FakeShopeeSecondaryFixed else FakeShopeeSurfaceContainerLow,
                        modifier = Modifier.clickable { onIntent(StoreIntent.ChangeSort(key)) }
                    ) {
                        Text(
                            label,
                            color = if (isSelected) FakeShopeeOnSecondaryFixed else FakeShopeeSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Products Grid directly from Room Flow State
            val gridState = rememberLazyGridState()

            LaunchedEffect(state.sortBy, state.selectedCategory, state.searchQuery) {
                if (state.products.isNotEmpty()) {
                    gridState.scrollToItem(0)
                }
            }

            if (state.products.isEmpty()) {
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
                            tint = FakeShopeeSecondary,
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
                            color = FakeShopeeSecondary
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
                    items(state.products, key = { it.id }) { product ->
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
    product: ProductUiModel,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FakeShopeeSurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Product Image Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FakeShopeeSurfaceContainerLow)
            ) {
                AsyncImage(
                    model = product.primaryImageUrl,
                    contentDescription = product.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )

                // Favorite Heart Button
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    shadowElevation = 2.dp,
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
                            tint = if (product.isFavorite) FakeShopeeOrange else FakeShopeeSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Specs / Category Tag Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = FakeShopeeNavy.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        product.category.uppercase(Locale.US),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                product.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = FakeShopeeOnSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Ratings
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB800),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    "${product.rating}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FakeShopeeOnSurface
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    "(${product.reviewCount})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = FakeShopeeSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price & Quick Add Mint Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    product.formattedPrice,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = FakeShopeeOrange
                )

                Surface(
                    shape = CircleShape,
                    color = FakeShopeeTertiaryFixed,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { onAddToCart() }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add to Cart",
                            tint = FakeShopeeOnTertiaryFixed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
