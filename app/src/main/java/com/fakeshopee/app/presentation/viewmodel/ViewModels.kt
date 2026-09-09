package com.fakeshopee.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fakeshopee.app.domain.model.*
import com.fakeshopee.app.domain.repository.ProductRepository
import com.fakeshopee.app.domain.repository.TransactionRepository
import com.fakeshopee.app.domain.repository.WalletRepository
import com.fakeshopee.app.domain.usecase.GetPagedTransactionsUseCase
import com.fakeshopee.app.domain.usecase.ProcessCheckoutUseCase
import com.fakeshopee.app.presentation.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StoreState())
    val state: StateFlow<StoreState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<StoreEffect>(extraBufferCapacity = 64)
    val effect: SharedFlow<StoreEffect> = _effect.asSharedFlow()

    init {
        observeCatalog()
        refreshCatalog()
    }

    private fun observeCatalog() {
        viewModelScope.launch {
            productRepository.getProductsStream().collect { products ->
                _state.update { it.copy(products = products, isLoading = false) }
            }
        }
    }

    fun handleIntent(intent: StoreIntent) {
        when (intent) {
            is StoreIntent.Search -> _state.update { it.copy(searchQuery = intent.query) }
            is StoreIntent.SelectCategory -> _state.update { it.copy(selectedCategory = intent.category) }
            is StoreIntent.ChangeSort -> _state.update { it.copy(sortBy = intent.sort) }
            is StoreIntent.ToggleFavorite -> viewModelScope.launch {
                productRepository.toggleFavorite(intent.productId)
            }
            is StoreIntent.QuickAddToCart -> viewModelScope.launch {
                val color = intent.product.variants.firstOrNull()?.name ?: "Standard"
                productRepository.addToCart(intent.product, color, 1)
                _effect.emit(StoreEffect.ShowToast("Added ${intent.product.title} to cart"))
            }
            StoreIntent.ToggleOfflineSimulator -> refreshCatalog()
            StoreIntent.RefreshCatalog -> refreshCatalog()
        }
    }

    fun addReview(productId: String, author: String, rating: Int, comment: String) {
        viewModelScope.launch {
            productRepository.addReview(productId, author, rating, comment)
            _effect.emit(StoreEffect.ShowToast("Review submitted successfully!"))
        }
    }

    private fun refreshCatalog() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }
            val res = productRepository.refreshProducts()
            _state.update { it.copy(isSyncing = false) }
            if (res.isSuccess) {
                _state.update { it.copy(isOffline = false, errorMessage = null) }
                _effect.emit(StoreEffect.ShowToast("Catalog synchronized successfully"))
            } else {
                _state.update { it.copy(isOffline = true, errorMessage = res.exceptionOrNull()?.message) }
            }
        }
    }
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val processCheckoutUseCase: ProcessCheckoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CartEffect>(extraBufferCapacity = 64)
    val effect: SharedFlow<CartEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            productRepository.getCartItemsStream().collect { items ->
                val inStockItems = items.filter { it.inStock }
                val subtotal = inStockItems.sumOf { it.product.price * it.quantity }
                var discount = 0.0
                val coupon = _state.value.appliedCoupon
                if (coupon != null) {
                    discount = if (coupon.discountPercentage > 0) (subtotal * coupon.discountPercentage) / 100.0 else minOf(subtotal, coupon.discountFlat)
                }
                val taxable = maxOf(0.0, subtotal - discount)
                val tax = taxable * 0.08
                _state.update {
                    it.copy(
                        items = items,
                        subtotal = subtotal,
                        discount = discount,
                        tax = tax,
                        grandTotal = taxable + tax
                    )
                }
            }
        }
    }

    fun handleIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.AddToCart -> viewModelScope.launch {
                productRepository.addToCart(intent.product, intent.selectedColor, intent.quantity)
                _effect.emit(CartEffect.ShowToast("Added ${intent.product.title} to cart"))
            }
            is CartIntent.UpdateQuantity -> viewModelScope.launch {
                productRepository.updateCartQuantity(intent.cartItemId, intent.quantity)
            }
            is CartIntent.RemoveItem -> viewModelScope.launch {
                productRepository.removeFromCart(intent.cartItemId)
                _effect.emit(CartEffect.ShowToast("Item removed from cart"))
            }
            is CartIntent.ApplyCoupon -> {
                if (intent.code.equals("SHOPEE20", ignoreCase = true) || intent.code.equals("KINETIC20", ignoreCase = true)) {
                    val coupon = Coupon("SHOPEE20", discountPercentage = 20, description = "20% off FakeShopee store items")
                    _state.update { currentState ->
                        val subtotal = currentState.subtotal
                        val discount = (subtotal * coupon.discountPercentage) / 100.0
                        val taxable = maxOf(0.0, subtotal - discount)
                        val tax = taxable * 0.08
                        currentState.copy(
                            appliedCoupon = coupon,
                            couponError = null,
                            discount = discount,
                            tax = tax,
                            grandTotal = taxable + tax
                        )
                    }
                    viewModelScope.launch { _effect.emit(CartEffect.ShowToast("Coupon SHOPEE20 applied!")) }
                } else {
                    _state.update { it.copy(couponError = "Invalid coupon code. Try SHOPEE20") }
                }
            }
            CartIntent.RemoveCoupon -> {
                _state.update { currentState ->
                    val subtotal = currentState.subtotal
                    val tax = subtotal * 0.08
                    currentState.copy(
                        appliedCoupon = null,
                        couponError = null,
                        discount = 0.0,
                        tax = tax,
                        grandTotal = subtotal + tax
                    )
                }
                viewModelScope.launch { _effect.emit(CartEffect.ShowToast("Coupon removed")) }
            }
            CartIntent.StartCheckout -> viewModelScope.launch {
                _effect.emit(CartEffect.OpenCheckoutDialog)
            }
        }
    }

    fun confirmPayment(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isCheckingOut = true) }
            val result = processCheckoutUseCase(_state.value.appliedCoupon)
            _state.update { it.copy(isCheckingOut = false) }
            if (result.isSuccess) {
                _state.update { it.copy(appliedCoupon = null, couponError = null, discount = 0.0) }
                _effect.emit(CartEffect.ShowToast("Payment successful! Balance deducted."))
                onSuccess()
            } else {
                _effect.emit(CartEffect.ShowToast("Payment failed: ${result.exceptionOrNull()?.message}"))
            }
        }
    }
}

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WalletState())
    val state: StateFlow<WalletState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<WalletEffect>(extraBufferCapacity = 64)
    val effect: SharedFlow<WalletEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            walletRepository.getWalletStream().collect { wallet ->
                _state.update { it.copy(wallet = wallet) }
            }
        }
        viewModelScope.launch {
            transactionRepository.getRecentTransactionsStream().collect { txList ->
                _state.update { it.copy(recentTransactions = txList) }
            }
        }
    }

    fun handleIntent(intent: WalletIntent) {
        when (intent) {
            is WalletIntent.FilterTransactions -> _state.update { it.copy(filterType = intent.type) }
            is WalletIntent.QuickAdd -> viewModelScope.launch {
                val res = walletRepository.topUp(intent.amount, intent.method)
                if (res.isSuccess) {
                    _state.update { it.copy(isQuickAddDialogVisible = false) }
                    _effect.emit(WalletEffect.ShowToast("Added $${intent.amount} to wallet"))
                }
            }
            is WalletIntent.SelectTransaction -> viewModelScope.launch {
                _effect.emit(WalletEffect.ShowReceiptDialog(intent.transaction))
            }
            WalletIntent.ShowQuickAddDialog -> _state.update { it.copy(isQuickAddDialogVisible = true) }
            WalletIntent.DismissQuickAddDialog -> _state.update { it.copy(isQuickAddDialogVisible = false) }
        }
    }
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getPagedTransactionsUseCase: GetPagedTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HistoryEffect>(extraBufferCapacity = 64)
    val effect: SharedFlow<HistoryEffect> = _effect.asSharedFlow()

    private val _filterFlow = MutableStateFlow<TransactionType?>(null)

    val pagedTransactions: Flow<PagingData<Transaction>> = _filterFlow
        .flatMapLatest { filter -> getPagedTransactionsUseCase(filter) }
        .cachedIn(viewModelScope)

    fun handleIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.ChangeFilter -> {
                _state.update { it.copy(selectedFilter = intent.filter) }
                _filterFlow.value = intent.filter
            }
            HistoryIntent.Refresh -> {
                _state.update { it.copy(isRefreshing = true) }
                viewModelScope.launch {
                    _effect.emit(HistoryEffect.ShowToast("Refreshed transaction ledger"))
                    _state.update { it.copy(isRefreshing = false) }
                }
            }
            HistoryIntent.Retry -> {
                viewModelScope.launch {
                    _effect.emit(HistoryEffect.ShowToast("Retrying remote synchronization..."))
                }
            }
        }
    }

    fun setFilter(filter: TransactionType?) {
        handleIntent(HistoryIntent.ChangeFilter(filter))
    }
}
