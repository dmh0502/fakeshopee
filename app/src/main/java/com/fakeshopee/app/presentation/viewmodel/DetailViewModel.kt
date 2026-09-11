package com.fakeshopee.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakeshopee.app.domain.repository.CartRepository
import com.fakeshopee.app.domain.repository.ProductRepository
import com.fakeshopee.app.presentation.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DetailEffect>(extraBufferCapacity = 64)
    val effect: SharedFlow<DetailEffect> = _effect.asSharedFlow()

    init {
        val productId: String? = savedStateHandle["productId"]
        if (productId != null) {
            loadProduct(productId)
        }
    }

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            productRepository.getProductByIdStream(productId).collect { product ->
                if (product != null) {
                    val uiModel = product.toUiModel()
                    val defaultColor = uiModel.variants.firstOrNull()?.name ?: "Standard"
                    _state.update { currentState ->
                        currentState.copy(
                            product = uiModel,
                            selectedColor = if (currentState.selectedColor.isEmpty()) defaultColor else currentState.selectedColor,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Product not found") }
                }
            }
        }
    }

    fun handleIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.LoadProduct -> loadProduct(intent.productId)
            is DetailIntent.SelectColor -> _state.update { it.copy(selectedColor = intent.color) }
            is DetailIntent.UpdateQuantity -> _state.update { it.copy(quantity = maxOf(1, intent.quantity)) }
            DetailIntent.AddToCart -> viewModelScope.launch {
                val currentProduct = _state.value.product ?: return@launch
                cartRepository.addToCart(currentProduct.toDomain(), _state.value.selectedColor, _state.value.quantity)
                _effect.emit(DetailEffect.ShowToast("Added ${currentProduct.title} to cart"))
            }
            DetailIntent.ToggleFavorite -> viewModelScope.launch {
                val currentProduct = _state.value.product ?: return@launch
                productRepository.toggleFavorite(currentProduct.id)
            }
            is DetailIntent.SubmitReview -> viewModelScope.launch {
                val currentProduct = _state.value.product ?: return@launch
                productRepository.addReview(currentProduct.id, intent.author, intent.rating, intent.comment)
                _effect.emit(DetailEffect.ShowToast("Review submitted successfully!"))
            }
        }
    }
}
