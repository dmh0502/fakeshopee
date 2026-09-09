package com.fakeshopee.app.presentation.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakeshopee.app.domain.model.Transaction
import com.fakeshopee.app.domain.model.TransactionType
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
            TopAppBar(
                title = { Text("FakeShopee Pay", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FakeShopeeBackground)
            )
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
            // Balance Hero Gradient Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(FakeShopeeOrange, Color(0xFFD3361B), FakeShopeeNavy)
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
                                Text(
                                    "AVAILABLE BALANCE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFECE8),
                                    letterSpacing = 1.sp
                                )
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFFFFECE8)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

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

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Monthly Spend", fontSize = 11.sp, color = Color(0xFFFFECE8))
                                    Text(
                                        "$${String.format(Locale.US, "%,.2f", state.wallet.monthlySpend)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Button(
                                    onClick = { onIntent(WalletIntent.ShowQuickAddDialog) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FakeShopeeMint,
                                        contentColor = Color(0xFF002113)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Quick Add", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Recent Activity Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextButton(onClick = onViewAllHistory) {
                        Text("View All", color = FakeShopeeOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Activity Items
            items(state.recentTransactions, key = { it.id }) { tx ->
                val isCredit = tx.amount > 0
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isCredit) FakeShopeeMint.copy(alpha = 0.3f) else FakeShopeeSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCredit) Icons.Default.AddCard else Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = if (isCredit) FakeShopeeMintDark else FakeShopeeNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                            Text(tx.formattedDate, fontSize = 11.sp, color = FakeShopeeSubtext)
                        }

                        Text(
                            text = (if (isCredit) "+ $" else "- $") + String.format(Locale.US, "%,.2f", Math.abs(tx.amount)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isCredit) FakeShopeeMintDark else FakeShopeeOnSurface
                        )
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
                    Text("Select Preset Amount", fontSize = 11.sp, color = FakeShopeeSubtext, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.forEach { preset ->
                            val isSelected = amountText == preset.toInt().toString()
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) FakeShopeeOrange else FakeShopeeSurface,
                                modifier = Modifier.clickable { amountText = preset.toInt().toString() }
                            ) {
                                Text(
                                    "$${preset.toInt()}",
                                    color = if (isSelected) Color.White else FakeShopeeNavy,
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

                    Text("Payment Method", fontSize = 11.sp, color = FakeShopeeSubtext, fontWeight = FontWeight.SemiBold)
                    val methods = listOf("Bank Account", "Debit Card", "Instant Transfer")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        methods.forEach { method ->
                            val isSelected = selectedMethod == method
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) FakeShopeeNavy else FakeShopeeSurface,
                                modifier = Modifier.clickable { selectedMethod = method }
                            ) {
                                Text(
                                    method,
                                    color = if (isSelected) Color.White else FakeShopeeSubtext,
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
