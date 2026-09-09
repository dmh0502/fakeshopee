package com.fakeshopee.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.fakeshopee.app.domain.model.Transaction
import com.fakeshopee.app.domain.model.TransactionType
import com.fakeshopee.app.presentation.theme.*
import java.util.Locale

import androidx.paging.compose.itemKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    lazyPagingItems: LazyPagingItems<Transaction>,
    selectedFilter: TransactionType?,
    onFilterChange: (TransactionType?) -> Unit,
    onSelectTransaction: (Transaction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Transaction History", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("FAKESHOPEE LEDGER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FakeShopeeOrange, letterSpacing = 1.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { lazyPagingItems.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FakeShopeeBackground)
            )
        },
        containerColor = FakeShopeeBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search merchant, reference ID...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FakeShopeeSubtext) },
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )

            // Filter Chips (All, Recharge, Payment, Refund)
            val filters = listOf(
                null to "All",
                TransactionType.RECHARGE to "Recharges (+)",
                TransactionType.PAYMENT to "Payments (-)",
                TransactionType.REFUND to "Refunds (+)"
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                items(filters) { (type, label) ->
                    val isSelected = selectedFilter == type
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) FakeShopeeOrange else FakeShopeeSurface,
                        modifier = Modifier.clickable { onFilterChange(type) }
                    ) {
                        Text(
                            label,
                            color = if (isSelected) Color.White else FakeShopeeNavy,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // LazyColumn with Paging 3 items
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Refresh Loading State
                if (lazyPagingItems.loadState.refresh is LoadState.Loading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = FakeShopeeOrange)
                        }
                    }
                }

                // Refresh Error State (Only show if list is empty)
                if (lazyPagingItems.loadState.refresh is LoadState.Error && lazyPagingItems.itemCount == 0) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFDAD6),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("You are offline", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF93000A))
                                TextButton(onClick = { lazyPagingItems.retry() }) {
                                    Text("Retry", fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A))
                                }
                            }
                        }
                    }
                }

                // Paginated Items
                items(count = lazyPagingItems.itemCount, key = lazyPagingItems.itemKey { it.id }) { index ->
                    val tx = lazyPagingItems[index]
                    if (tx != null) {
                        val matchesQuery = searchQuery.isBlank() ||
                                tx.title.contains(searchQuery, ignoreCase = true) ||
                                tx.referenceId.contains(searchQuery, ignoreCase = true) ||
                                (tx.merchant?.contains(searchQuery, ignoreCase = true) == true)

                        if (matchesQuery) {
                            val isCredit = tx.amount > 0
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectTransaction(tx) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isCredit) FakeShopeeMint.copy(alpha = 0.3f) else FakeShopeeSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isCredit) Icons.Default.AddCard else Icons.Default.ShoppingBag,
                                            contentDescription = null,
                                            tint = if (isCredit) FakeShopeeMintDark else FakeShopeeNavy,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${tx.formattedDate} • ${tx.referenceId}", fontSize = 10.sp, color = FakeShopeeSubtext)
                                    }

                                    Text(
                                        (if (isCredit) "+ $" else "- $") + String.format(Locale.US, "%,.2f", Math.abs(tx.amount)),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isCredit) FakeShopeeMintDark else FakeShopeeOnSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Append Loading State (Skeleton or Spinner)
                if (lazyPagingItems.loadState.append is LoadState.Loading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = FakeShopeeOrange)
                        }
                    }
                }
            }
        }
    }
}
