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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakeshopee.app.R
import com.fakeshopee.app.domain.model.Transaction
import com.fakeshopee.app.presentation.mvi.WalletIntent
import com.fakeshopee.app.presentation.mvi.WalletState
import com.fakeshopee.app.presentation.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    state: WalletState,
    onIntent: (WalletIntent) -> Unit,
    onViewAllHistory: () -> Unit
) {
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
                            Text(
                                "Wallet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = FakeShopeeOnSurface
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = FakeShopeeSecondary)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Hero Fintech Card
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFEE4D2D),
                                        Color(0xFF441349),
                                        Color(0xFF0B132B)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(FakeShopeeTertiaryFixed)
                                    )
                                    Text(
                                        "AVAILABLE BALANCE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 1.2.sp
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val formattedBalance = "$${String.format(Locale.US, "%,.2f", state.wallet.availableBalance)}"
                            val dynamicFontSize = when {
                                formattedBalance.length > 15 -> 24.sp
                                formattedBalance.length > 12 -> 28.sp
                                formattedBalance.length > 9 -> 32.sp
                                else -> 36.sp
                            }
                            val dynamicLineHeight = dynamicFontSize * 1.25f

                            Text(
                                text = formattedBalance,
                                fontSize = dynamicFontSize,
                                lineHeight = dynamicLineHeight,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Divider(color = Color.White.copy(alpha = 0.2f))

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Monthly Spend", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "$${String.format(Locale.US, "%,.2f", state.wallet.monthlySpend)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Button(
                                    onClick = { onIntent(WalletIntent.ShowQuickAddDialog) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FakeShopeeTertiaryFixed,
                                        contentColor = FakeShopeeOnTertiaryFixed
                                    ),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Quick Add", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }


            // Recent Activity Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Recent Activity", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = FakeShopeeOnSurface)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FakeShopeeSurfaceContainerHigh
                        ) {
                            Text("Today", color = FakeShopeeSecondary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                    TextButton(onClick = onViewAllHistory) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("View All", color = FakeShopeeOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = FakeShopeeOrange, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Activity Items
            items(state.recentTransactions, key = { it.id }) { tx ->
                val isCredit = tx.amount > 0
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FakeShopeeSurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIntent(WalletIntent.SelectTransaction(tx)) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isCredit) FakeShopeeTertiaryFixed.copy(alpha = 0.6f) else FakeShopeeSecondaryFixed.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCredit) Icons.Default.AccountBalance else Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = if (isCredit) FakeShopeeTertiary else FakeShopeeOnSecondaryFixed,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, color = FakeShopeeOnSurface)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(tx.formattedDate, fontSize = 11.sp, color = FakeShopeeSecondary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = (if (isCredit) "+ $" else "- $") + String.format(Locale.US, "%,.2f", Math.abs(tx.amount)),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = if (isCredit) FakeShopeeTertiary else FakeShopeeOnSurface
                            )
                            Text(
                                text = if (isCredit) "Success" else "Completed",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCredit) FakeShopeeTertiary else FakeShopeeSecondary
                            )
                        }
                    }
                }
            }

        }
    }

    if (state.isQuickAddDialogVisible) {
        var amountText by remember { mutableStateOf("100") }
        var selectedMethod by remember { mutableStateOf("Bank Account") }
        val presets = listOf(50.0, 100.0, 250.0, 500.0)

        AlertDialog(
            onDismissRequest = { onIntent(WalletIntent.DismissQuickAddDialog) },
            title = { Text("Quick Top Up Wallet", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Preset Amount", fontSize = 11.sp, color = FakeShopeeSecondary, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.forEach { preset ->
                            val isSelected = amountText == preset.toInt().toString()
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) FakeShopeeOrange else FakeShopeeSurfaceContainerLow,
                                modifier = Modifier.clickable { amountText = preset.toInt().toString() }
                            ) {
                                Text(
                                    "$${preset.toInt()}",
                                    color = if (isSelected) Color.White else FakeShopeeOnSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Amount ($)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Payment Method", fontSize = 11.sp, color = FakeShopeeSecondary, fontWeight = FontWeight.SemiBold)
                    val methods = listOf("Bank Account", "Debit Card", "Instant Transfer")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        methods.forEach { method ->
                            val isSelected = selectedMethod == method
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) FakeShopeeNavy else FakeShopeeSurfaceContainerLow,
                                modifier = Modifier.clickable { selectedMethod = method }
                            ) {
                                Text(
                                    method,
                                    color = if (isSelected) Color.White else FakeShopeeSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = amountText.toDoubleOrNull() ?: 0.0
                        if (parsed > 0) {
                            onIntent(WalletIntent.QuickAdd(parsed, selectedMethod))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FakeShopeeOrange)
                ) {
                    Text("Top Up Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(WalletIntent.DismissQuickAddDialog) }) {
                    Text("Cancel")
                }
            }
        )
    }
}


