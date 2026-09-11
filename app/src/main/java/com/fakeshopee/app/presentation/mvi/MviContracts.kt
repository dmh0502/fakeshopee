package com.fakeshopee.app.presentation.mvi

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.fakeshopee.app.domain.model.*

// --- STORE MVI ---
@Stable
@Immutable
data class StoreState(
    val products: List<ProductUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val isSyncing: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val sortBy: String = "featured",
    val errorMessage: String? = null
)

@Stable
sealed interface StoreIntent {
    data class Search(val query: String) : StoreIntent
    data class SelectCategory(val category: String) : StoreIntent
    data class ChangeSort(val sort: String) : StoreIntent
    data class ToggleFavorite(val productId: String) : StoreIntent
    data class QuickAddToCart(val productUiModel: ProductUiModel) : StoreIntent
    object ToggleOfflineSimulator : StoreIntent
    object RefreshCatalog : StoreIntent
}

@Stable
sealed interface StoreEffect {
    data class ShowToast(val message: String) : StoreEffect
    data class NavigateToDetail(val productId: String) : StoreEffect
}

// --- PRODUCT DETAIL MVI ---
@Stable
@Immutable
data class DetailState(
    val product: ProductUiModel? = null,
    val selectedColor: String = "",
    val quantity: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null
)

@Stable
sealed interface DetailIntent {
    data class LoadProduct(val productId: String) : DetailIntent
    data class SelectColor(val color: String) : DetailIntent
    data class UpdateQuantity(val quantity: Int) : DetailIntent
    object AddToCart : DetailIntent
    object ToggleFavorite : DetailIntent
    data class SubmitReview(val author: String, val rating: Int, val comment: String) : DetailIntent
}

@Stable
sealed interface DetailEffect {
    data class ShowToast(val message: String) : DetailEffect
    object NavigateBack : DetailEffect
}

// --- CART MVI ---
@Stable
@Immutable
data class CartState(
    val items: List<CartItemUiModel> = emptyList(),
    val appliedCoupon: Coupon? = null,
    val couponError: String? = null,
    val isCheckingOut: Boolean = false,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val grandTotal: Double = 0.0
)

@Stable
sealed interface CartIntent {
    data class AddToCart(val product: ProductUiModel, val selectedColor: String, val quantity: Int) : CartIntent
    data class UpdateQuantity(val cartItemId: String, val quantity: Int) : CartIntent
    data class RemoveItem(val cartItemId: String) : CartIntent
    data class ApplyCoupon(val code: String) : CartIntent
    object RemoveCoupon : CartIntent
    object StartCheckout : CartIntent
}

@Stable
sealed interface CartEffect {
    data class ShowToast(val message: String) : CartEffect
    object OpenCheckoutDialog : CartEffect
}

// --- WALLET MVI ---
@Stable
@Immutable
data class WalletState(
    val wallet: WalletUiModel = Wallet(availableBalance = 4250.00, monthlySpend = 2778.00).toUiModel(),
    val recentTransactions: List<TransactionUiModel> = emptyList(),
    val filterType: TransactionType? = null,
    val isQuickAddDialogVisible: Boolean = false
)

@Stable
sealed interface WalletIntent {
    data class FilterTransactions(val type: TransactionType?) : WalletIntent
    data class QuickAdd(val amount: Double, val method: String) : WalletIntent
    data class SelectTransaction(val transaction: TransactionUiModel) : WalletIntent
    object ShowQuickAddDialog : WalletIntent
    object DismissQuickAddDialog : WalletIntent
}

@Stable
sealed interface WalletEffect {
    data class ShowToast(val message: String) : WalletEffect
    data class ShowReceiptDialog(val transaction: TransactionUiModel) : WalletEffect
}

// --- HISTORY MVI (Paging 3) ---
@Stable
@Immutable
data class HistoryState(
    val selectedFilter: TransactionType? = null,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@Stable
sealed interface HistoryIntent {
    data class ChangeFilter(val filter: TransactionType?) : HistoryIntent
    object Retry : HistoryIntent
    object Refresh : HistoryIntent
}

@Stable
sealed interface HistoryEffect {
    data class ShowToast(val message: String) : HistoryEffect
    data class ShowReceipt(val transaction: TransactionUiModel) : HistoryEffect
}
