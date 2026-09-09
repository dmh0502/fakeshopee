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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
            TopAppBar(
                title = { Text(product.title, maxLines = 1, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (product.isFavorite) Color(0xFFBA1A1A) else FakeShopeeOnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FakeShopeeBackground)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White,
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
                        Text("TOTAL PRICE", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = FakeShopeeSubtext)
                        Text(
                            "$${String.format(Locale.US, "%,.2f", product.price)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FakeShopeeOrange
                        )
                    }

                    Button(
                        onClick = { onAddToCart(selectedColor, 1) },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FakeShopeeOrange),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Cart", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(FakeShopeeBackground)
        ) {
            // Image Carousel Preview
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(Color(0xFFEAEDFF)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = product.images.getOrNull(selectedImageIndex) ?: product.images.firstOrNull(),
                        contentDescription = product.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    )
                }
            }

            // Image Thumbnails
            if (product.images.size > 1) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(product.images.indices.toList()) { index ->
                            val isSelected = selectedImageIndex == index
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
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

            // Title & Flagship Tag
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        color = Color(0xFFFFECE8),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            "FAKESHOPEE MALL",
                            color = FakeShopeeOrange,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(product.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = FakeShopeeOnSurface)

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (product.reviewCount > 0) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${product.rating} Rating", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(" • ${product.reviewCount} customer reviews", color = FakeShopeeSubtext, fontSize = 12.sp)
                        } else {
                            Text("No reviews yet", color = FakeShopeeSubtext, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Color Variant Selector
            if (product.variants.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text("FINISH / COLOR", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FakeShopeeSubtext)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            product.variants.forEach { variant ->
                                val isSelected = selectedColor == variant.name
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) FakeShopeeOrange else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) FakeShopeeOrange else FakeShopeeBorder),
                                    modifier = Modifier.clickable { selectedColor = variant.name }
                                ) {
                                    Text(
                                        variant.name,
                                        color = if (isSelected) Color.White else FakeShopeeOnSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
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
                if (displaySpecs.isNotEmpty()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("HARDWARE SPECIFICATIONS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FakeShopeeSubtext)
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                displaySpecs.entries.chunked(2).forEach { rowEntries ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        rowEntries.forEach { entry ->
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(entry.key, fontSize = 10.sp, color = FakeShopeeSubtext, fontWeight = FontWeight.Medium)
                                                Text(entry.value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FakeShopeeOnSurface)
                                            }
                                        }
                                        if (rowEntries.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                    Divider(color = FakeShopeeBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Description
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("OVERVIEW", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FakeShopeeSubtext)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(product.description, fontSize = 13.sp, lineHeight = 20.sp, color = FakeShopeeSubtext)
                }
            }

            // Reviews List
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("VERIFIED CUSTOMER REVIEWS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FakeShopeeSubtext)
                        TextButton(onClick = { showReviewDialog = true }) {
                            Text("+ Write Review", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FakeShopeeOrange)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    if (product.reviews.isEmpty()) {
                        Text(
                            "No reviews yet. Be the first to review this product!",
                            fontSize = 12.sp,
                            color = FakeShopeeSubtext,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        product.reviews.forEach { rev ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(rev.author, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(rev.date, fontSize = 10.sp, color = FakeShopeeSubtext)
                                    }
                                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                        repeat(rev.rating) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Text(rev.comment, fontSize = 12.sp, color = FakeShopeeSubtext)
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
