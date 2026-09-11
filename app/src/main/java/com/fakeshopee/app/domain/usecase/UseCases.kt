package com.fakeshopee.app.domain.usecase

import androidx.paging.PagingData
import com.fakeshopee.app.domain.model.Coupon
import com.fakeshopee.app.domain.model.Product
import com.fakeshopee.app.domain.model.Transaction
import com.fakeshopee.app.domain.model.TransactionType
import com.fakeshopee.app.domain.repository.CartRepository
import com.fakeshopee.app.domain.repository.ProductRepository
import com.fakeshopee.app.domain.repository.TransactionRepository
import com.fakeshopee.app.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import java.util.Locale

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = productRepository.getProductsStream()
}

class RefreshCatalogUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(): Result<Unit> = productRepository.refreshProducts()
}

class GetPagedTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(filter: TransactionType? = null): Flow<PagingData<Transaction>> =
        transactionRepository.getPagedTransactions(filter)
}

class ProcessCheckoutUseCase @Inject constructor(
    private val cartRepository: CartRepository,
    private val walletRepository: WalletRepository,
    private val cartPricingCalculator: CartPricingCalculator
) {
    suspend operator fun invoke(coupon: Coupon? = null): Result<Double> {
        val cartItems = cartRepository.getCartItemsStream().first()
        val inStockItems = cartItems.filter { it.inStock }
        if (inStockItems.isEmpty()) {
            return Result.failure(IllegalStateException("No in-stock items in cart to checkout"))
        }

        val calculation = cartPricingCalculator.calculate(cartItems, coupon)
        val grandTotal = calculation.grandTotal

        val wallet = walletRepository.getWalletStream().first()
        if (wallet.availableBalance < grandTotal) {
            return Result.failure(IllegalStateException("Insufficient balance ($${String.format(Locale.US, "%.2f", wallet.availableBalance)}) for total ($${String.format(Locale.US, "%.2f", grandTotal)})"))
        }

        // Process debit
        val paymentResult = walletRepository.processPayment(grandTotal, "FakeShopee Order")
        if (paymentResult.isSuccess) {
            cartRepository.clearCart()
            return Result.success(grandTotal)
        } else {
            return Result.failure(paymentResult.exceptionOrNull() ?: Exception("Payment failed"))
        }
    }
}
