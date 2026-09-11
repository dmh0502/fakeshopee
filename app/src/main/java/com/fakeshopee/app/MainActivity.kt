package com.fakeshopee.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.paging.compose.collectAsLazyPagingItems
import com.fakeshopee.app.presentation.mvi.*
import com.fakeshopee.app.presentation.screens.*
import com.fakeshopee.app.presentation.theme.FakeShopeeBackground
import com.fakeshopee.app.presentation.theme.FakeShopeeOrange
import com.fakeshopee.app.presentation.theme.FakeShopeeTheme
import com.fakeshopee.app.presentation.theme.FakeShopeeOnSurface
import com.fakeshopee.app.presentation.theme.FakeShopeeSecondary
import com.fakeshopee.app.presentation.theme.FakeShopeeSurfaceContainerLowest
import com.fakeshopee.app.presentation.theme.FakeShopeeTertiaryFixed
import com.fakeshopee.app.presentation.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Store : Screen("store", "Store", Icons.Default.Storefront)
    object Cart : Screen("cart", "Cart", Icons.Default.ShoppingBag)
    object Wallet : Screen("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
    object History : Screen("history", "History", Icons.Default.ReceiptLong)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val storeViewModel: StoreViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

    private val topToastMessage = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScopeLaunch()

        setContent {
            FakeShopeeTheme {
                var showSplash by remember { mutableStateOf(true) }

                Crossfade(targetState = showSplash, label = "splashTransition") { isSplashing ->
                    if (isSplashing) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    } else {
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        val topToastMsg by topToastMessage.collectAsStateWithLifecycle()
                        val cartState by cartViewModel.state.collectAsStateWithLifecycle()
                        val totalCartCount = cartState.items.sumOf { it.quantity }

                        val items = listOf(
                            Screen.Store,
                            Screen.Cart,
                            Screen.Wallet,
                            Screen.History
                        )

                        Box(modifier = Modifier.fillMaxSize()) {
                            Scaffold(
                                bottomBar = {
                                    val currentRoute = currentDestination?.route
                                    if (currentRoute != "detail/{productId}") {
                                        Surface(
                                            shadowElevation = 12.dp,
                                            color = FakeShopeeSurfaceContainerLowest
                                        ) {
                                            NavigationBar(
                                                containerColor = FakeShopeeSurfaceContainerLowest,
                                                tonalElevation = 0.dp
                                            ) {
                                                items.forEach { screen ->
                                                    val selected = currentRoute == screen.route
                                                    NavigationBarItem(
                                                        icon = {
                                                            if (screen == Screen.Cart && totalCartCount > 0) {
                                                                BadgedBox(
                                                                    badge = {
                                                                        Badge(
                                                                            containerColor = FakeShopeeOrange,
                                                                            contentColor = Color.White
                                                                        ) {
                                                                            val badgeText = if (totalCartCount > 99) "99+" else totalCartCount.toString()
                                                                            Text(
                                                                                text = badgeText,
                                                                                fontSize = 10.sp,
                                                                                fontWeight = FontWeight.Bold
                                                                            )
                                                                        }
                                                                    }
                                                                ) {
                                                                    Icon(screen.icon, contentDescription = screen.title)
                                                                }
                                                            } else {
                                                                Icon(screen.icon, contentDescription = screen.title)
                                                            }
                                                        },
                                                        label = {
                                                            Text(
                                                                screen.title,
                                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                            )
                                                        },
                                                        selected = selected,
                                                        onClick = {
                                                            navController.navigate(screen.route) {
                                                                popUpTo(navController.graph.findStartDestination().id) {
                                                                    saveState = true
                                                                }
                                                                launchSingleTop = true
                                                                restoreState = true
                                                            }
                                                        },
                                                        colors = NavigationBarItemDefaults.colors(
                                                            indicatorColor = FakeShopeeOrange.copy(alpha = 0.12f),
                                                            selectedIconColor = FakeShopeeOrange,
                                                            selectedTextColor = FakeShopeeOrange,
                                                            unselectedIconColor = FakeShopeeSecondary,
                                                            unselectedTextColor = FakeShopeeSecondary
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            ) { innerPadding ->
                                NavHost(
                                    navController = navController,
                                    startDestination = Screen.Store.route,
                                    modifier = Modifier.padding(innerPadding)
                                ) {
                                    composable(Screen.Store.route) {
                                        val storeState by storeViewModel.state.collectAsStateWithLifecycle()
                                        StoreScreen(
                                            state = storeState,
                                            onIntent = storeViewModel::handleIntent,
                                            onProductClick = { id -> navController.navigate("detail/$id") }
                                        )
                                    }

                                    composable("detail/{productId}") {
                                        val detailViewModel: DetailViewModel = hiltViewModel()
                                        val detailState by detailViewModel.state.collectAsStateWithLifecycle()
                                        val product = detailState.product

                                        if (product != null) {
                                            ProductDetailScreen(
                                                product = product,
                                                onBack = { navController.popBackStack() },
                                                onAddToCart = { color, qty ->
                                                    detailViewModel.handleIntent(DetailIntent.SelectColor(color))
                                                    detailViewModel.handleIntent(DetailIntent.UpdateQuantity(qty))
                                                    detailViewModel.handleIntent(DetailIntent.AddToCart)
                                                },
                                                onToggleFavorite = {
                                                    detailViewModel.handleIntent(DetailIntent.ToggleFavorite)
                                                },
                                                onAddReview = { author, rating, comment ->
                                                    detailViewModel.handleIntent(
                                                        DetailIntent.SubmitReview(author, rating, comment)
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    composable(Screen.Cart.route) {
                                        val cartState by cartViewModel.state.collectAsStateWithLifecycle()
                                        CartScreen(
                                            state = cartState,
                                            onIntent = cartViewModel::handleIntent,
                                            onProceedCheckout = {
                                                cartViewModel.confirmPayment {
                                                    navController.navigate(Screen.Wallet.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                            onStartShopping = {
                                                navController.navigate(Screen.Store.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        )
                                    }

                                    composable(Screen.Wallet.route) {
                                        val walletState by walletViewModel.state.collectAsStateWithLifecycle()
                                        WalletScreen(
                                            state = walletState,
                                            onIntent = walletViewModel::handleIntent,
                                            onViewAllHistory = {
                                                navController.navigate(Screen.History.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        )
                                    }

                                    composable(Screen.History.route) {
                                        val pagedTransactions = historyViewModel.pagedTransactions.collectAsLazyPagingItems()
                                        val historyState by historyViewModel.state.collectAsStateWithLifecycle()
                                        HistoryScreen(
                                            lazyPagingItems = pagedTransactions,
                                            selectedFilter = historyState.selectedFilter,
                                            onFilterChange = { filter -> historyViewModel.setFilter(filter) },
                                            onSelectTransaction = { tx ->
                                                showTopToast("Receipt: ${tx.referenceId}")
                                            }
                                        )
                                    }
                                }
                            }

                            TopToastNotification(
                                message = topToastMsg,
                                onDismiss = { topToastMessage.value = null }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showTopToast(msg: String) {
        topToastMessage.value = msg
    }

    private fun lifecycleScopeLaunch() {
        lifecycleScope.launch {
            storeViewModel.effect.collectLatest { effect ->
                when (effect) {
                    is StoreEffect.ShowToast -> showTopToast(effect.message)
                    else -> {}
                }
            }
        }
        lifecycleScope.launch {
            cartViewModel.effect.collectLatest { effect ->
                when (effect) {
                    is CartEffect.ShowToast -> showTopToast(effect.message)
                    else -> {}
                }
            }
        }
        lifecycleScope.launch {
            walletViewModel.effect.collectLatest { effect ->
                when (effect) {
                    is WalletEffect.ShowToast -> showTopToast(effect.message)
                    is WalletEffect.ShowReceiptDialog -> showTopToast("Receipt: ${effect.transaction.referenceId}")
                }
            }
        }
    }
}

@Composable
fun TopToastNotification(
    message: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
            .zIndex(999f)
    ) {
        if (message != null) {
            LaunchedEffect(message) {
                delay(2500)
                onDismiss()
            }

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0xEC191C1E),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(FakeShopeeTertiaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF002113),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
