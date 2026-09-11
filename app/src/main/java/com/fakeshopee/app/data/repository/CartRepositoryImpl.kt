package com.fakeshopee.app.data.repository

import com.fakeshopee.app.data.local.AppDatabase
import com.fakeshopee.app.data.local.CartItemEntity
import com.fakeshopee.app.data.mapper.toDomain
import com.fakeshopee.app.domain.model.CartItem
import com.fakeshopee.app.domain.model.Product
import com.fakeshopee.app.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : CartRepository {

    private val cartDao = database.cartDao()
    private val productDao = database.productDao()

    override fun getCartItemsStream(): Flow<List<CartItem>> {
        return combine(cartDao.getAllCartItems(), productDao.getAllProducts()) { cartEntities, productEntities ->
            val productMap = productEntities.associateBy { it.id }
            cartEntities.mapNotNull { cartEntity ->
                val prodEntity = productMap[cartEntity.productId] ?: return@mapNotNull null
                val product = prodEntity.toDomain()
                CartItem(
                    id = cartEntity.id,
                    product = product,
                    quantity = cartEntity.quantity,
                    selectedColor = cartEntity.selectedColor,
                    inStock = cartEntity.inStock && product.inStock
                )
            }
        }
    }

    override suspend fun addToCart(product: Product, selectedColor: String, quantity: Int) {
        val currentCart = cartDao.getAllCartItems().first()
        val existing = currentCart.find { it.productId == product.id && it.selectedColor == selectedColor }
        if (existing != null) {
            cartDao.updateQuantity(existing.id, existing.quantity + quantity)
        } else {
            val newEntity = CartItemEntity(
                id = "cart-${System.currentTimeMillis()}-${(100..999).random()}",
                productId = product.id,
                quantity = quantity,
                selectedColor = selectedColor,
                inStock = product.inStock
            )
            cartDao.insertCartItem(newEntity)
        }
    }

    override suspend fun updateCartQuantity(cartItemId: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(cartItemId)
        } else {
            cartDao.updateQuantity(cartItemId, quantity)
        }
    }

    override suspend fun removeFromCart(cartItemId: String) {
        cartDao.deleteCartItem(cartItemId)
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }
}
