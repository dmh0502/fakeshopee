package com.fakeshopee.app.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.gson.Gson
import com.fakeshopee.app.data.local.AppDatabase
import com.fakeshopee.app.data.local.ProductEntity
import com.fakeshopee.app.data.local.WalletEntity
import com.fakeshopee.app.data.mapper.*
import com.fakeshopee.app.data.mediator.TransactionRemoteMediator
import com.fakeshopee.app.data.remote.FakeShopeeApiService
import com.fakeshopee.app.domain.model.*
import com.fakeshopee.app.domain.repository.ProductRepository
import com.fakeshopee.app.domain.repository.TransactionRepository
import com.fakeshopee.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.*
import retrofit2.Response
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

    override fun getProductsStream(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain(gson) }
        }
    }

    override fun getFilteredProductsStream(category: String, query: String, sortBy: String): Flow<List<Product>> {
        return productDao.getFilteredProducts(category, query, sortBy).map { entities ->
            entities.map { it.toDomain(gson) }
        }
    }

    override fun getProductByIdStream(productId: String): Flow<Product?> {
        return productDao.getProductById(productId).map { it?.toDomain(gson) }
    }

    private suspend fun <T> fetchAndFilterTechProducts(
        apiCall: suspend () -> Response<T>,
        extractEntities: (T) -> List<ProductEntity>
    ): Pair<List<ProductEntity>?, Throwable?> {
        return try {
            val response = apiCall()
            if (response.isSuccessful && response.body() != null) {
                Pair(extractEntities(response.body()!!), null)
            } else {
                Pair(null, Exception("API returned error code ${response.code()}"))
            }
        } catch (e: Exception) {
            Pair(null, e)
        }
    }

    override suspend fun refreshProducts(): Result<Unit> {
        return try {
            val techEntities = mutableListOf<ProductEntity>()
            var lastError: Throwable? = null

            // 1. Fetch from DummyJSON Products API
            val (dummyTech, dummyError) = fetchAndFilterTechProducts({ apiService.getDummyJsonProducts(limit = 100, skip = 0) }) { body ->
                body.products
                    .filter { isTechProduct(it.category, it.title, it.description) }
                    .map { it.toEntity(gson) }
            }
            if (dummyTech != null) techEntities.addAll(dummyTech)
            if (dummyError != null) lastError = dummyError

            // 2. Fetch from Fake Store API
            val (fakeStoreTech, fakeStoreError) = fetchAndFilterTechProducts({ apiService.getFakeStoreProducts() }) { body ->
                body
                    .filter { isTechProduct(it.category, it.title, it.description) }
                    .map { it.toEntity(gson) }
            }
            if (fakeStoreTech != null) techEntities.addAll(fakeStoreTech)
            if (fakeStoreError != null && lastError == null) lastError = fakeStoreError

            // 3. Fallback to Primary API
            if (techEntities.isEmpty()) {
                val (primaryTech, primaryError) = fetchAndFilterTechProducts({ apiService.getProducts() }) { body ->
                    body
                        .filter { isTechProduct(it.category, it.title, it.description) }
                        .map { it.toEntity(gson) }
                }
                if (primaryTech != null) techEntities.addAll(primaryTech)
                if (primaryError != null && lastError == null) lastError = primaryError
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
        val catLower = category.lowercase(Locale.ROOT)
        if (AppConfig.TECH_CATEGORIES.contains(catLower)) return true

        val textCombined = "$title $category $description".lowercase(Locale.ROOT)
        return AppConfig.TECH_KEYWORDS.any { textCombined.contains(it) }
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

    override suspend fun recordTransaction(transaction: Transaction): Result<Unit> {
        return try {
            transactionDao.insertTransactions(listOf(transaction.toEntity()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val transactionRepository: TransactionRepository
) : WalletRepository {

    private val walletDao = database.walletDao()

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
            val newTx = Transaction(
                id = "tx-topup-${System.currentTimeMillis()}",
                title = "Wallet Top Up ($method)",
                amount = amount,
                type = TransactionType.RECHARGE,
                timestamp = System.currentTimeMillis(),
                formattedDate = formatter.format(Date()),
                category = "Recharge",
                merchant = "FakeShopee Fast Deposit",
                referenceId = "FS-DEP-${(10000..99999).random()}",
                status = TransactionStatus.COMPLETED,
                iconName = "add_card"
            )
            transactionRepository.recordTransaction(newTx)
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
            val paymentTx = Transaction(
                id = "tx-pay-${System.currentTimeMillis()}",
                title = orderTitle,
                amount = -amount,
                type = TransactionType.PAYMENT,
                timestamp = System.currentTimeMillis(),
                formattedDate = formatter.format(Date()),
                category = "Electronics",
                merchant = "FakeShopee Store",
                referenceId = refId,
                status = TransactionStatus.COMPLETED,
                iconName = "shopping_bag"
            )
            transactionRepository.recordTransaction(paymentTx)
            Result.success(refId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
