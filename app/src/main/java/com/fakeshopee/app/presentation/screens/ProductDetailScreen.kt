package com.fakeshopee.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fakeshopee.app.domain.model.Product
import com.fakeshopee.app.presentation.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    onBack: () -> Unit,
    onAddToCart: (color: String, quantity: Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onAddReview: (author: String, rating: Int, comment: String) -> Unit = { _, _, _ -> }
) {
    var selectedImageIndex by remember(product.id) { mutableIntStateOf(0) }
    var selectedColor by remember(product.id) {
        mutableStateOf(product.variants.firstOrNull()?.name ?: "Standard")
    }
    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewAuthor by remember { mutableStateOf("") }
    var reviewRating by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(
                color = FakeShopeeBackground.copy(alpha = 0.9f),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = FakeShopeeOnSurface)
                        }
                        Text("Product Detail", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = FakeShopeeOnSurface)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = FakeShopeeSecondary)
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
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                color = FakeShopeeSurfaceContainerLowest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "TOTAL PRICE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FakeShopeeSecondary,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "$${String.format(Locale.US, "%,.2f", product.price)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FakeShopeeOrange
                            )
                            val listPrice = product.price * 1.1f
                            Text(
                                "$${String.format(Locale.US, "%,.2f", listPrice)}",
                                fontSize = 12.sp,
                                color = FakeShopeeSecondary,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }

                    Button(
                        onClick = { onAddToCart(selectedColor, 1) },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FakeShopeeOrange
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Cart", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        },
        containerColor = FakeShopeeBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Subheader Action Strip
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FakeShopeeOrange.copy(alpha = 0.12f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = FakeShopeeOrange, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("OFFICIAL MALL", color = FakeShopeeOrange, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 0.5.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FakeShopeeSurfaceContainerHigh
                        ) {
                            Text("In Stock", color = FakeShopeeSecondary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = FakeShopeeSurfaceContainerLow,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { onToggleFavorite() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (product.isFavorite) FakeShopeeOrange else FakeShopeeOnSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Showcase Gallery Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(FakeShopeeSurfaceContainerLow, FakeShopeeSurfaceContainerLowest)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = product.images.getOrNull(selectedImageIndex) ?: product.images.firstOrNull(),
                        contentDescription = product.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FakeShopeeNavy.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            product.category.uppercase(Locale.US),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Image Thumbnails (if multiple)
            if (product.images.size > 1) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(product.images.indices.toList()) { index ->
                            val isSelected = selectedImageIndex == index
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FakeShopeeSurfaceContainerLowest)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) FakeShopeeOrange else FakeShopeeBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedImageIndex = index }
                                    .padding(4.dp)
                            ) {
                                AsyncImage(
                                    model = product.images[index],
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // Title & Rating Header
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = FakeShopeeOrange,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "FAKESHOPEE MALL",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = FakeShopeeSecondaryFixed,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = FakeShopeeOnSecondaryFixed, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Free Express Delivery", color = FakeShopeeOnSecondaryFixed, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        product.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = FakeShopeeOnSurface,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FakeShopeeSurfaceContainerHigh
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${product.rating}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(" / 5.0", color = FakeShopeeSecondary, fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "${product.reviewCount} customer reviews",
                            color = FakeShopeeSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Finish / Color Selector
            if (product.variants.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(
                            "FINISH / COLOR",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = FakeShopeeSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            product.variants.forEach { variant ->
                                val isSelected = selectedColor == variant.name
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) FakeShopeeOrange else FakeShopeeSurfaceContainerLowest,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) FakeShopeeOrange else FakeShopeeBorder),
                                    modifier = Modifier.clickable { selectedColor = variant.name }
                                ) {
                                    Text(
                                        variant.name,
                                        color = if (isSelected) Color.White else FakeShopeeOnSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hardware Specs Bento Grid
            item {
                val displaySpecs = product.specs.filterKeys { !it.equals("Rating", ignoreCase = true) }
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "HARDWARE SPECIFICATIONS",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = FakeShopeeSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = FakeShopeeSurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Source", fontSize = 11.sp, color = FakeShopeeSecondary, fontWeight = FontWeight.Medium)
                                    Text("Fake Store API", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FakeShopeeOnSurface)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Category", fontSize = 11.sp, color = FakeShopeeSecondary, fontWeight = FontWeight.Medium)
                                    Text(product.category, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FakeShopeeOnSurface)
                                }
                            }

                            if (displaySpecs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = FakeShopeeBorder.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.height(12.dp))

                                displaySpecs.entries.chunked(2).forEach { rowEntries ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        rowEntries.forEach { entry ->
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(entry.key, fontSize = 10.sp, color = FakeShopeeSecondary, fontWeight = FontWeight.Medium)
                                                Text(entry.value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FakeShopeeOnSurface)
                                            }
                                        }
                                        if (rowEntries.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Overview Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(
                        "OVERVIEW",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = FakeShopeeSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = FakeShopeeSurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            product.description,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = FakeShopeeOnSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Verified Customer Reviews Section
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "VERIFIED CUSTOMER REVIEWS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = FakeShopeeSecondary,
                            letterSpacing = 1.sp
                        )
                        TextButton(onClick = { showReviewDialog = true }) {
                            Text("+ Write Review", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FakeShopeeOrange)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    if (product.reviews.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = FakeShopeeSurfaceContainerLowest),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(FakeShopeeOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = FakeShopeeOrange)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Be the first trendsetter!", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Share your experience with other tech enthusiasts.", fontSize = 12.sp, color = FakeShopeeSecondary)
                            }
                        }
                    } else {
                        product.reviews.forEach { rev ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = FakeShopeeSurfaceContainerLowest),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(rev.author, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(rev.date, fontSize = 10.sp, color = FakeShopeeSecondary)
                                    }
                                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                        repeat(rev.rating) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(13.dp))
                                        }
                                    }
                                    Text(rev.comment, fontSize = 12.sp, color = FakeShopeeOnSurface)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Write a Review", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = reviewAuthor,
                        onValueChange = { reviewAuthor = it },
                        label = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Rating", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { reviewRating = star }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= reviewRating) Color(0xFFFFB800) else FakeShopeeBorder
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Your Review Comment") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reviewAuthor.isNotBlank() && reviewComment.isNotBlank()) {
                            onAddReview(reviewAuthor.trim(), reviewRating, reviewComment.trim())
                            showReviewDialog = false
                            reviewAuthor = ""
                            reviewComment = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FakeShopeeOrange)
                ) {
                    Text("Submit Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
