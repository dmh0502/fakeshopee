package com.fakeshopee.app.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.gson.Gson
import com.fakeshopee.app.data.local.AppDatabase
import com.fakeshopee.app.data.local.CartItemEntity
import com.fakeshopee.app.data.local.ProductEntity
import com.fakeshopee.app.data.local.TransactionEntity
import com.fakeshopee.app.data.local.WalletEntity
import com.fakeshopee.app.data.mapper.*
import com.fakeshopee.app.data.mediator.TransactionRemoteMediator
import com.fakeshopee.app.data.remote.FakeShopeeApiService
import com.fakeshopee.app.domain.model.*
import com.fakeshopee.app.domain.repository.ProductRepository
import com.fakeshopee.app.domain.repository.TransactionRepository
import com.fakeshopee.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val apiService: FakeShopeeApiService,
    private val gson: Gson
) : ProductRepository {

    private val productDao = database.productDao()
    private val cartDao = database.cartDao()

    override fun getProductsStream(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain(gson) }
        }
    }

    override fun getProductByIdStream(productId: String): Flow<Product?> {
        return productDao.getProductById(productId).map { it?.toDomain(gson) }
    }

    override suspend fun refreshProducts(): Result<Unit> {
        return try {
            val techEntities = mutableListOf<ProductEntity>()
            var lastError: Throwable? = null

            // 1. Fetch from DummyJSON Products API
            try {
                val dummyResponse = apiService.getDummyJsonProducts(limit = 100, skip = 0)
                if (dummyResponse.isSuccessful && dummyResponse.body() != null) {
                    val dummyTech = dummyResponse.body()!!.products.filter { dto ->
                        isTechProduct(dto.category, dto.title, dto.description)
                    }
                    techEntities.addAll(dummyTech.map { it.toEntity(gson) })
                } else {
                    lastError = Exception("API returned error code ${dummyResponse.code()}")
                }
            } catch (e: Exception) {
                lastError = e
            }

            // 2. Fetch from Fake Store API
            try {
                val fakeStoreResponse = apiService.getFakeStoreProducts()
                if (fakeStoreResponse.isSuccessful && fakeStoreResponse.body() != null) {
                    val fakeStoreTech = fakeStoreResponse.body()!!.filter { dto ->
                        isTechProduct(dto.category, dto.title, dto.description)
                    }
                    techEntities.addAll(fakeStoreTech.map { it.toEntity(gson) })
                } else if (lastError == null) {
                    lastError = Exception("API returned error code ${fakeStoreResponse.code()}")
                }
            } catch (e: Exception) {
                if (lastError == null) lastError = e
            }

            // 3. Fallback to Primary API
            if (techEntities.isEmpty()) {
                try {
                    val primaryResponse = apiService.getProducts()
                    if (primaryResponse.isSuccessful && primaryResponse.body() != null) {
                        val primaryTech = primaryResponse.body()!!.filter { dto ->
                            isTechProduct(dto.category, dto.title, dto.description)
                        }
                        techEntities.addAll(primaryTech.map { it.toEntity(gson) })
                    } else if (lastError == null) {
                        lastError = Exception("API returned error code ${primaryResponse.code()}")
                    }
                } catch (e: Exception) {
                    if (lastError == null) lastError = e
                }
            }

            if (techEntities.isNotEmpty()) {
                productDao.insertProducts(techEntities)
                Result.success(Unit)
            } else {
                Result.failure(lastError ?: Exception("Failed to fetch products from APIs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isTechProduct(category: String, title: String, description: String): Boolean {
        val techCategories = setOf(
            "smartphones", "laptops", "tablets", "mobile-accessories", "electronics",
            "mobile", "wearables", "audio", "accessories", "computing"
        )
        val catLower = category.lowercase(Locale.ROOT)
        if (techCategories.contains(catLower)) return true

        val techKeywords = listOf(
            "phone", "laptop", "tablet", "watch", "headphone", "audio", "electronics",
            "camera", "tech", "computer", "gaming", "drive", "ssd", "ram", "monitor", "tv", "silicon"
        )
        val textCombined = "$title $category $description".lowercase(Locale.ROOT)
        return techKeywords.any { textCombined.contains(it) }
    }

    override suspend fun toggleFavorite(productId: String) {
        productDao.toggleFavorite(productId)
    }

    override suspend fun addReview(productId: String, author: String, rating: Int, comment: String) {
        val currentEntity = productDao.getProductById(productId).firstOrNull() ?: return
        val currentDomain = currentEntity.toDomain(gson)
        val newReview = ProductReview(
            id = "rev-${System.currentTimeMillis()}",
            author = author,
            rating = rating,
            comment = comment,
            date = "Just now",
            isVerifiedBuyer = true
        )
        val updatedReviews = currentDomain.reviews + newReview
        val avgRating = (updatedReviews.sumOf { it.rating }.toDouble() / updatedReviews.size)
        val rounded = Math.round(avgRating * 10.0) / 10.0

        val updatedEntity = currentEntity.copy(
            rating = rounded,
            reviewCount = updatedReviews.size,
            reviewsJson = gson.toJson(updatedReviews)
        )
        productDao.updateProduct(updatedEntity)
    }

    override fun getCartItemsStream(): Flow<List<CartItem>> {
        return combine(cartDao.getAllCartItems(), productDao.getAllProducts()) { cartEntities, productEntities ->
            val productMap = productEntities.associateBy { it.id }
            cartEntities.mapNotNull { cartEntity ->
                val prodEntity = productMap[cartEntity.productId] ?: return@mapNotNull null
                val product = prodEntity.toDomain(gson)
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

@OptIn(ExperimentalPagingApi::class)
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val apiService: FakeShopeeApiService
) : TransactionRepository {

    private val transactionDao = database.transactionDao()

    override fun getPagedTransactions(filterType: TransactionType?): Flow<PagingData<Transaction>> {
        val pagingSourceFactory = {
            if (filterType == null) {
                transactionDao.getPagedTransactions()
            } else {
                transactionDao.getPagedTransactionsByType(filterType.name)
            }
        }

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 10,
                prefetchDistance = 3
            ),
            remoteMediator = TransactionRemoteMediator(
                database = database,
                apiService = apiService,
                filterType = filterType?.name
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun getRecentTransactionsStream(limit: Int): Flow<List<Transaction>> {
        return transactionDao.getRecentTransactions(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshTransactions(): Result<Unit> {
        return try {
            val response = apiService.getTransactions(page = 1, pageSize = 10)
            if (response.isSuccessful && response.body() != null) {
                val entities = response.body()!!.items.map { it.toEntity() }
                transactionDao.insertTransactions(entities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Refresh error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : WalletRepository {

    private val walletDao = database.walletDao()
    private val transactionDao = database.transactionDao()

    override fun getWalletStream(): Flow<Wallet> {
        return walletDao.getWallet().map { entity ->
            if (entity != null) {
                Wallet(availableBalance = entity.availableBalance, monthlySpend = entity.monthlySpend)
            } else {
                Wallet(availableBalance = 4250.00, monthlySpend = 2778.00)
            }
        }
    }

    override suspend fun topUp(amount: Double, method: String): Result<Unit> {
        return try {
            val current = walletDao.getWallet().first() ?: WalletEntity(id = 1, availableBalance = 4250.00, monthlySpend = 2778.00)
            val updated = current.copy(availableBalance = current.availableBalance + amount)
            walletDao.setWallet(updated)

            val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
            val newTx = TransactionEntity(
                id = "tx-topup-${System.currentTimeMillis()}",
                title = "Wallet Top Up ($method)",
                amount = amount,
                type = "RECHARGE",
                timestamp = System.currentTimeMillis(),
                formattedDate = formatter.format(Date()),
                category = "Recharge",
                merchant = "FakeShopee Fast Deposit",
                referenceId = "FS-DEP-${(10000..99999).random()}",
                status = "COMPLETED",
                iconName = "add_card"
            )
            transactionDao.insertTransactions(listOf(newTx))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun processPayment(amount: Double, orderTitle: String): Result<String> {
        return try {
            val current = walletDao.getWallet().first() ?: WalletEntity(id = 1, availableBalance = 4250.00, monthlySpend = 2778.00)
            if (current.availableBalance < amount) {
                return Result.failure(IllegalStateException("Insufficient funds"))
            }

            val updated = current.copy(
                availableBalance = current.availableBalance - amount,
                monthlySpend = current.monthlySpend + amount
            )
            walletDao.setWallet(updated)

            val refId = "FS-ORD-${(10000..99999).random()}"
            val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
            val paymentTx = TransactionEntity(
                id = "tx-pay-${System.currentTimeMillis()}",
                title = orderTitle,
                amount = -amount,
                type = "PAYMENT",
                timestamp = System.currentTimeMillis(),
                formattedDate = formatter.format(Date()),
                category = "Electronics",
                merchant = "FakeShopee Store",
                referenceId = refId,
                status = "COMPLETED",
                iconName = "shopping_bag"
            )
            transactionDao.insertTransactions(listOf(paymentTx))
            Result.success(refId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
