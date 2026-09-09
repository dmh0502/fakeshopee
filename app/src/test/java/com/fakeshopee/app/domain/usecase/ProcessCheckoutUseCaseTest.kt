package com.fakeshopee.app.domain.usecase

import com.fakeshopee.app.domain.model.*
import com.fakeshopee.app.domain.repository.ProductRepository
import com.fakeshopee.app.domain.repository.WalletRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProcessCheckoutUseCaseTest {

    private val productRepository: ProductRepository = mockk(relaxed = true)
    private val walletRepository: WalletRepository = mockk(relaxed = true)
    private lateinit var useCase: ProcessCheckoutUseCase

    private val sampleProduct = Product(
        id = "p-1",
        title = "OLED Monitor",
        category = "Display",
        price = 500.0,
        rating = 4.9,
        reviewCount = 30,
        description = "4K 240Hz",
        images = emptyList(),
        specs = emptyMap(),
        highlights = emptyList(),
        variants = emptyList(),
        inStock = true,
        stockQuantity = 5
    )

    private val sampleCartItem = CartItem(
        id = "cart-1",
        product = sampleProduct,
        quantity = 1,
        selectedColor = "Default",
        inStock = true
    )

    @Before
    fun setUp() {
        useCase = ProcessCheckoutUseCase(productRepository, walletRepository)
    }

    @Test
    fun `checkout with empty cart returns failure`() = runTest {
        every { productRepository.getCartItemsStream() } returns flowOf(emptyList())

        val result = useCase(null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("No in-stock items") == true)
        coVerify(exactly = 0) { walletRepository.processPayment(any(), any()) }
    }

    @Test
    fun `checkout with sufficient balance succeeds, deducts wallet, and clears cart`() = runTest {
        every { productRepository.getCartItemsStream() } returns flowOf(listOf(sampleCartItem))
        every { walletRepository.getWalletStream() } returns flowOf(Wallet(availableBalance = 1000.0, monthlySpend = 100.0))
        coEvery { walletRepository.processPayment(540.0, any()) } returns Result.success("FS-ORD-12345")
        coEvery { productRepository.clearCart() } just Runs

        val result = useCase(null)

        assertTrue(result.isSuccess)
        assertEquals(540.0, result.getOrNull() ?: 0.0, 0.01)
        coVerify(exactly = 1) { walletRepository.processPayment(540.0, any()) }
        coVerify(exactly = 1) { productRepository.clearCart() }
    }

    @Test
    fun `checkout with SHOPEE20 coupon applies 20 percent discount before tax`() = runTest {
        val coupon = Coupon("SHOPEE20", discountPercentage = 20, description = "20% off")
        every { productRepository.getCartItemsStream() } returns flowOf(listOf(sampleCartItem))
        every { walletRepository.getWalletStream() } returns flowOf(Wallet(availableBalance = 1000.0, monthlySpend = 100.0))
        coEvery { walletRepository.processPayment(432.0, any()) } returns Result.success("FS-ORD-12345")
        coEvery { productRepository.clearCart() } just Runs

        val result = useCase(coupon)

        assertTrue(result.isSuccess)
        assertEquals(432.0, result.getOrNull() ?: 0.0, 0.01)
        coVerify(exactly = 1) { walletRepository.processPayment(432.0, any()) }
        coVerify(exactly = 1) { productRepository.clearCart() }
    }

    @Test
    fun `checkout with insufficient balance returns failure and does NOT clear cart`() = runTest {
        every { productRepository.getCartItemsStream() } returns flowOf(listOf(sampleCartItem))
        every { walletRepository.getWalletStream() } returns flowOf(Wallet(availableBalance = 200.0, monthlySpend = 0.0))

        val result = useCase(null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Insufficient balance") == true)
        coVerify(exactly = 0) { productRepository.clearCart() }
    }
}
