package com.fakeshopee.app.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fakeshopee.app.domain.model.CartItem
import com.fakeshopee.app.presentation.mvi.CartIntent
import com.fakeshopee.app.presentation.mvi.CartState
import com.fakeshopee.app.presentation.theme.*

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
            TopAppBar(
                title = { Text("Your Cart (${state.items.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FakeShopeeBackground)
            )
        },
        bottomBar = {
            if (state.items.isNotEmpty()) {
                Surface(
                    shadowElevation = 12.dp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                onIntent(CartIntent.StartCheckout)
                                showConfirmDialog = true
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FakeShopeeOrange),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text("Proceed to Checkout", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(FakeShopeeSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = FakeShopeeOrange,
                                modifier = Modifier.size(26.dp)
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
                            "Explore the latest products and add them to your FakeShopee cart.",
                            color = FakeShopeeSubtext,
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = promoInput,
                                    onValueChange = { promoInput = it.uppercase() },
                                    placeholder = { Text("Promo Code (SHOPEE20)", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onIntent(CartIntent.ApplyCoupon(promoInput)) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FakeShopeeSurface, contentColor = FakeShopeeNavy)
                                ) {
                                    Text("Apply", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (state.couponError != null) {
                                Text(state.couponError, color = FakeShopeeError, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                            if (state.appliedCoupon != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Coupon ${state.appliedCoupon.code} Applied (-20%)", color = FakeShopeeMintDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    TextButton(onClick = { onIntent(CartIntent.RemoveCoupon) }) {
                                        Text("Remove", color = FakeShopeeError, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Order Summary Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F3FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            SummaryRow("Subtotal", "$${String.format("%,.2f", state.subtotal)}")
                            if (state.discount > 0) {
                                SummaryRow("Discount", "-$${String.format("%,.2f", state.discount)}", color = FakeShopeeMintDark)
                            }
                            SummaryRow("Estimated Tax (8%)", "$${String.format("%,.2f", state.tax)}")
                            SummaryRow("Shipping", "Free", color = FakeShopeeMintDark)
                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = FakeShopeeBorder)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("$${String.format("%,.2f", state.grandTotal)}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = FakeShopeeOrange)
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
                    Text("Subtotal: $${String.format("%,.2f", state.subtotal)}")
                    if (state.discount > 0) {
                        Text("Discount: -$${String.format("%,.2f", state.discount)}", color = FakeShopeeMintDark)
                    }
                    Text("Estimated Tax: $${String.format("%,.2f", state.tax)}")
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Grand Total: $${String.format("%,.2f", state.grandTotal)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = FakeShopeeOrange
                    )
                    Text("Payment will be deducted directly from your FakeShopee Pay balance.", fontSize = 11.sp, color = FakeShopeeSubtext)
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
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAEDFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = item.product.images.firstOrNull(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                if (item.selectedColor != null) {
                    Text(item.selectedColor, fontSize = 11.sp, color = FakeShopeeSubtext)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "$${String.format("%,.2f", item.product.price * item.quantity)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = FakeShopeeOrange,
                    fontSize = 13.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                }
                Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = FakeShopeeError, modifier = Modifier.size(18.dp))
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
        Text(title, fontSize = 12.sp, color = FakeShopeeSubtext)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}
