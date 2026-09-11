package com.fakeshopee.app.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fakeshopee.app.R
import com.fakeshopee.app.domain.model.CartItem
import com.fakeshopee.app.presentation.mvi.CartItemUiModel
import com.fakeshopee.app.presentation.mvi.CartIntent
import com.fakeshopee.app.presentation.mvi.CartState
import com.fakeshopee.app.presentation.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    state: CartState,
    onIntent: (CartIntent) -> Unit,
    onProceedCheckout: () -> Unit,
    onStartShopping: () -> Unit = {}
) {
    var promoInput by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

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
                            modifier = Modifier.size(36.dp)
                        )
                        Column {
                            Text(
                                "FakeShopee",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = FakeShopeeOrange,
                                letterSpacing = 1.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Cart", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = FakeShopeeOnSurface)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = FakeShopeeOrange.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        "${state.items.sumOf { it.quantity }} items",
                                        color = FakeShopeeOrange,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = FakeShopeeSurfaceContainerLow
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = FakeShopeeTertiary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fintech Safe", color = FakeShopeeSecondary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (state.items.isNotEmpty()) {
                Surface(
                    shadowElevation = 16.dp,
                    color = FakeShopeeSurfaceContainerLowest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                onIntent(CartIntent.StartCheckout)
                                showConfirmDialog = true
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FakeShopeeOrange),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text("Proceed to Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        },
        containerColor = FakeShopeeBackground
    ) { paddingValues ->
        if (state.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = FakeShopeeSurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(FakeShopeeOrange.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = FakeShopeeOrange,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Your cart is empty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = FakeShopeeOnSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Explore the latest tech products and add them to your FakeShopee cart.",
                            color = FakeShopeeSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onStartShopping,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FakeShopeeOrange),
                            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "Start Shopping",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cart Items
                items(state.items, key = { it.id }) { item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { onIntent(CartIntent.UpdateQuantity(item.id, item.quantity + 1)) },
                        onDecrease = { onIntent(CartIntent.UpdateQuantity(item.id, item.quantity - 1)) },
                        onRemove = { onIntent(CartIntent.RemoveItem(item.id)) }
                    )
                }

                // Promo Code Section
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = FakeShopeeSurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Sell, contentDescription = null, tint = FakeShopeeOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PROMOTIONS & DISCOUNTS", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = FakeShopeeSecondary, letterSpacing = 1.sp)
                                }
                                Text(
                                    "Apply SHOPEE20",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FakeShopeeOrange,
                                    modifier = Modifier.clickable { promoInput = "SHOPEE20" }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = promoInput,
                                    onValueChange = { promoInput = it.uppercase() },
                                    placeholder = { Text("Promo Code (SHOPEE20)", fontSize = 12.sp, color = FakeShopeeSecondary) },
                                    leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = FakeShopeeSecondary, modifier = Modifier.size(18.dp)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = FakeShopeeSurfaceContainerLow,
                                        focusedContainerColor = FakeShopeeSurfaceContainerLowest,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = FakeShopeeOrange
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onIntent(CartIntent.ApplyCoupon(promoInput)) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FakeShopeeSecondaryContainer, contentColor = FakeShopeeOnSecondaryFixed)
                                ) {
                                    Text("Apply", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (state.couponError != null) {
                                Text(state.couponError, color = FakeShopeeError, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                            }
                            if (state.appliedCoupon != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FakeShopeeMintDark, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Voucher ${state.appliedCoupon.code} applied (-20%)", color = FakeShopeeMintDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    TextButton(onClick = { onIntent(CartIntent.RemoveCoupon) }) {
                                        Text("Remove", color = FakeShopeeError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Order Summary Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = FakeShopeeSurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = FakeShopeeOnSurface)
                                Text("USD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FakeShopeeSecondary)
                            }
                            SummaryRow("Subtotal", "$${String.format(Locale.US, "%,.2f", state.subtotal)}")
                            if (state.discount > 0) {
                                SummaryRow("Discount (20%)", "-$${String.format(Locale.US, "%,.2f", state.discount)}", color = FakeShopeeMintDark)
                            }
                            SummaryRow("Estimated Tax (8%)", "$${String.format(Locale.US, "%,.2f", state.tax)}")
                            SummaryRow("Shipping", "Free", color = FakeShopeeMintDark)
                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = FakeShopeeBorder)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FakeShopeeOnSurface)
                                    Text("Includes VAT and local levies", fontSize = 10.sp, color = FakeShopeeSecondary)
                                }
                                Text(
                                    "$${String.format(Locale.US, "%,.2f", state.grandTotal)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = FakeShopeeOrange
                                )
                            }
                        }
                    }
                }

                // ShopeePay Encrypted Protection Banner
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FakeShopeeSurfaceContainerLow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(FakeShopeeSurfaceContainerLowest),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = FakeShopeeOrange, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Encrypted ShopeePay Guarantee", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FakeShopeeOnSurface)
                                Text("Instant refund if parcel does not arrive as promised", fontSize = 11.sp, color = FakeShopeeSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Checkout", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Total items: ${state.items.sumOf { it.quantity }}")
                    Text("Subtotal: $${String.format(Locale.US, "%,.2f", state.subtotal)}")
                    if (state.discount > 0) {
                        Text("Discount: -$${String.format(Locale.US, "%,.2f", state.discount)}", color = FakeShopeeMintDark)
                    }
                    Text("Estimated Tax: $${String.format(Locale.US, "%,.2f", state.tax)}")
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Grand Total: $${String.format(Locale.US, "%,.2f", state.grandTotal)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = FakeShopeeOrange
                    )
                    Text("Payment will be deducted directly from your FakeShopee Pay balance.", fontSize = 11.sp, color = FakeShopeeSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onProceedCheckout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FakeShopeeOrange)
                ) {
                    Text("Confirm & Pay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CartItemRow(
    item: CartItemUiModel,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FakeShopeeSurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FakeShopeeSurfaceContainerLow)
            ) {
                AsyncImage(
                    model = item.product.primaryImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(item.product.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(1f))
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = FakeShopeeError, modifier = Modifier.size(18.dp))
                    }
                }

                if (item.selectedColor != null) {
                    Text(item.selectedColor, fontSize = 11.sp, color = FakeShopeeSecondary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.formattedItemTotal,
                        fontWeight = FontWeight.ExtraBold,
                        color = FakeShopeeOrange,
                        fontSize = 15.sp
                    )

                    // Stepper Pill Container
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FakeShopeeSurfaceContainerLow
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp), tint = FakeShopeeSecondary)
                            }
                            Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp))
                            IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp), tint = FakeShopeeSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(title: String, value: String, color: Color = FakeShopeeOnSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 12.sp, color = FakeShopeeSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
