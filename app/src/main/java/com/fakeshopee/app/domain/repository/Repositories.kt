package com.fakeshopee.app.domain.repository

import androidx.paging.PagingData
import com.fakeshopee.app.domain.model.CartItem
import com.fakeshopee.app.domain.model.Product
import com.fakeshopee.app.domain.model.Transaction
import com.fakeshopee.app.domain.model.TransactionType
import com.fakeshopee.app.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductsStream(): Flow<List<Product>>
    fun getProductByIdStream(productId: String): Flow<Product?>
    suspend fun refreshProducts(): Result<Unit>
    suspend fun toggleFavorite(productId: String)
    suspend fun addReview(productId: String, author: String, rating: Int, comment: String)

    // Cart operations
    fun getCartItemsStream(): Flow<List<CartItem>>
    suspend fun addToCart(product: Product, selectedColor: String, quantity: Int)
    suspend fun updateCartQuantity(cartItemId: String, quantity: Int)
    suspend fun removeFromCart(cartItemId: String)
    suspend fun clearCart()
}

interface TransactionRepository {
    fun getPagedTransactions(filterType: TransactionType?): Flow<PagingData<Transaction>>
    fun getRecentTransactionsStream(limit: Int = 10): Flow<List<Transaction>>
    suspend fun refreshTransactions(): Result<Unit>
}

interface WalletRepository {
    fun getWalletStream(): Flow<Wallet>
    suspend fun topUp(amount: Double, method: String): Result<Unit>
    suspend fun processPayment(amount: Double, orderTitle: String): Result<String>
}
