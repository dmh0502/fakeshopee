package com.fakeshopee.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.fakeshopee.app.domain.model.*
import com.fakeshopee.app.domain.repository.CartRepository
import com.fakeshopee.app.domain.repository.ProductRepository
import com.fakeshopee.app.domain.repository.TransactionRepository
import com.fakeshopee.app.domain.repository.WalletRepository
import com.fakeshopee.app.domain.usecase.CartPricingCalculator
import com.fakeshopee.app.domain.usecase.GetPagedTransactionsUseCase
import com.fakeshopee.app.domain.usecase.ProcessCheckoutUseCase
import com.fakeshopee.app.presentation.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StoreViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
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
            _state
                .map { Triple(it.selectedCategory, it.searchQuery, it.sortBy) }
                .distinctUntilChanged()
                .flatMapLatest { (category, query, sortBy) ->
                    productRepository.getFilteredProductsStream(category, query, sortBy)
                }
                .collect { products ->
                    val uiModels = products.map { it.toUiModel() }
                    _state.update { it.copy(products = uiModels, isLoading = false) }
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
                val domainProduct = intent.productUiModel.toDomain()
                val color = domainProduct.variants.firstOrNull()?.name ?: "Standard"
                cartRepository.addToCart(domainProduct, color, 1)
                _effect.emit(StoreEffect.ShowToast("Added ${domainProduct.title} to cart"))
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
    private val cartRepository: CartRepository,
    private val processCheckoutUseCase: ProcessCheckoutUseCase,
    private val cartPricingCalculator: CartPricingCalculator
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CartEffect>(extraBufferCapacity = 64)
    val effect: SharedFlow<CartEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            cartRepository.getCartItemsStream().collect { items ->
                val calculation = cartPricingCalculator.calculate(items, _state.value.appliedCoupon)
                val uiItems = items.map { it.toUiModel() }
                _state.update {
                    it.copy(
                        items = uiItems,
                        subtotal = calculation.subtotal,
                        discount = calculation.discount,
                        tax = calculation.tax,
                        grandTotal = calculation.grandTotal
                    )
                }
            }
        }
    }

    fun handleIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.AddToCart -> viewModelScope.launch {
                cartRepository.addToCart(intent.product.toDomain(), intent.selectedColor, intent.quantity)
                _effect.emit(CartEffect.ShowToast("Added ${intent.product.title} to cart"))
            }
            is CartIntent.UpdateQuantity -> viewModelScope.launch {
                cartRepository.updateCartQuantity(intent.cartItemId, intent.quantity)
            }
            is CartIntent.RemoveItem -> viewModelScope.launch {
                cartRepository.removeFromCart(intent.cartItemId)
                _effect.emit(CartEffect.ShowToast("Item removed from cart"))
            }
            is CartIntent.ApplyCoupon -> {
                val coupon = AppConfig.getValidCoupon(intent.code)
                if (coupon != null) {
                    viewModelScope.launch {
                        val currentItems = cartRepository.getCartItemsStream().first()
                        val calculation = cartPricingCalculator.calculate(currentItems, coupon)
                        _state.update {
                            it.copy(
                                appliedCoupon = coupon,
                                couponError = null,
                                subtotal = calculation.subtotal,
                                discount = calculation.discount,
                                tax = calculation.tax,
                                grandTotal = calculation.grandTotal
                            )
                        }
                        _effect.emit(CartEffect.ShowToast("Coupon ${coupon.code} applied!"))
                    }
                } else {
                    _state.update { it.copy(couponError = "Invalid coupon code. Try SHOPEE20") }
                }
            }
            CartIntent.RemoveCoupon -> viewModelScope.launch {
                val currentItems = cartRepository.getCartItemsStream().first()
                val calculation = cartPricingCalculator.calculate(currentItems, null)
                _state.update {
                    it.copy(
                        appliedCoupon = null,
                        couponError = null,
                        subtotal = calculation.subtotal,
                        discount = calculation.discount,
                        tax = calculation.tax,
                        grandTotal = calculation.grandTotal
                    )
                }
                _effect.emit(CartEffect.ShowToast("Coupon removed"))
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
                _state.update { it.copy(wallet = wallet.toUiModel()) }
            }
        }
        viewModelScope.launch {
            transactionRepository.getRecentTransactionsStream().collect { txList ->
                val uiList = txList.map { it.toUiModel() }
                _state.update { it.copy(recentTransactions = uiList) }
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

    val pagedTransactions: Flow<PagingData<TransactionUiModel>> = _filterFlow
        .flatMapLatest { filter ->
            getPagedTransactionsUseCase(filter).map { pagingData ->
                pagingData.map { it.toUiModel() }
            }
        }
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
