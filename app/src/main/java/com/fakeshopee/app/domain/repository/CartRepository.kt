package com.fakeshopee.app.domain.repository

import com.fakeshopee.app.domain.model.CartItem
import com.fakeshopee.app.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItemsStream(): Flow<List<CartItem>>
    suspend fun addToCart(product: Product, selectedColor: String, quantity: Int)
    suspend fun updateCartQuantity(cartItemId: String, quantity: Int)
    suspend fun removeFromCart(cartItemId: String)
    suspend fun clearCart()
}
