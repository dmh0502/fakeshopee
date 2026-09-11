package com.fakeshopee.app.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.fakeshopee.app.R
import com.fakeshopee.app.domain.model.TransactionType
import com.fakeshopee.app.presentation.mvi.TransactionUiModel
import com.fakeshopee.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    lazyPagingItems: LazyPagingItems<TransactionUiModel>,
    selectedFilter: TransactionType?,
    onFilterChange: (TransactionType?) -> Unit,
    onSelectTransaction: (TransactionUiModel) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

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
                            Text("History", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = FakeShopeeOnSurface)
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = FakeShopeeSurfaceContainer,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { lazyPagingItems.refresh() }
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
            }
        },
        containerColor = FakeShopeeBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = FakeShopeeSurfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
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
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search merchant, reference ID...", fontSize = 13.sp, color = FakeShopeeSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // Filter Chips Rail
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
                        color = if (isSelected) FakeShopeeOrange else FakeShopeeSurfaceContainerHigh,
                        modifier = Modifier.clickable { onFilterChange(type) }
                    ) {
                        Text(
                            label,
                            color = if (isSelected) Color.White else FakeShopeeSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // LazyColumn with Paging 3
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

                // Refresh Error State
                if (lazyPagingItems.loadState.refresh is LoadState.Error && lazyPagingItems.itemCount == 0) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = FakeShopeeErrorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Network offline - Cached ledger loaded", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF93000A))
                                TextButton(onClick = { lazyPagingItems.retry() }) {
                                    Text("Retry", fontWeight = FontWeight.Bold, color = FakeShopeeError)
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
                            val isCredit = tx.isPositive
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = FakeShopeeSurfaceContainerLowest),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                        Text("${tx.formattedDate} • ${tx.referenceId}", fontSize = 10.sp, color = FakeShopeeSecondary)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            tx.formattedAmount,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = if (isCredit) FakeShopeeTertiary else FakeShopeeOnSurface
                                        )
                                        Text(
                                            text = tx.status,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCredit) FakeShopeeTertiary else FakeShopeeSecondary,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Append Loading State
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
