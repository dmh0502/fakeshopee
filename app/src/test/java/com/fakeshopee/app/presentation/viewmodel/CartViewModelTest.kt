package com.fakeshopee.app.presentation.viewmodel

import app.cash.turbine.test
import com.fakeshopee.app.domain.model.CartItem
import com.fakeshopee.app.domain.model.Product
import com.fakeshopee.app.domain.model.ProductVariant
import com.fakeshopee.app.domain.repository.ProductRepository
import com.fakeshopee.app.domain.usecase.ProcessCheckoutUseCase
import com.fakeshopee.app.presentation.mvi.CartEffect
import com.fakeshopee.app.presentation.mvi.CartIntent
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val productRepository: ProductRepository = mockk(relaxed = true)
    private val processCheckoutUseCase: ProcessCheckoutUseCase = mockk()

    private val testProduct = Product(
        id = "p-1",
        title = "Haptic Controller",
        category = "Gaming",
        price = 100.0,
        rating = 4.7,
        reviewCount = 50,
        description = "Precision feedback",
        images = emptyList(),
        specs = emptyMap(),
        highlights = emptyList(),
        variants = listOf(ProductVariant("Silver", "#E2E8F0", true)),
        inStock = true,
        stockQuantity = 10
    )

    private val testCartItems = listOf(
        CartItem(
            id = "c-1",
            product = testProduct,
            quantity = 2,
            selectedColor = "Silver",
            inStock = true
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { productRepository.getCartItemsStream() } returns flowOf(testCartItems)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cart items emission automatically calculates subtotal, tax, and grandTotal in StateFlow`() = runTest(testDispatcher) {
        val viewModel = CartViewModel(productRepository, processCheckoutUseCase)

        viewModel.state.test {
            awaitItem()
            testScheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(1, state.items.size)
            assertEquals(200.0, state.subtotal, 0.01)
            assertEquals(0.0, state.discount, 0.01)
            assertEquals(16.0, state.tax, 0.01)
            assertEquals(216.0, state.grandTotal, 0.01)
        }
    }

    @Test
    fun `ApplyCoupon intent with SHOPEE20 applies 20 percent discount correctly`() = runTest(testDispatcher) {
        val viewModel = CartViewModel(productRepository, processCheckoutUseCase)
        testScheduler.advanceUntilIdle()

        viewModel.state.test {
            val baseState = awaitItem()
            assertEquals(200.0, baseState.subtotal, 0.01)

            viewModel.handleIntent(CartIntent.ApplyCoupon("SHOPEE20"))
            testScheduler.advanceUntilIdle()

            val discountedState = awaitItem()
            assertNotNull(discountedState.appliedCoupon)
            assertEquals("SHOPEE20", discountedState.appliedCoupon?.code)
            assertEquals(40.0, discountedState.discount, 0.01)
            assertEquals(160.0, discountedState.subtotal - discountedState.discount, 0.01)
            assertEquals(12.8, discountedState.tax, 0.01)
            assertEquals(172.8, discountedState.grandTotal, 0.01)
        }
    }

    @Test
    fun `ApplyCoupon intent with invalid code sets couponError`() = runTest(testDispatcher) {
        val viewModel = CartViewModel(productRepository, processCheckoutUseCase)
        testScheduler.advanceUntilIdle()

        viewModel.state.test {
            awaitItem()
            viewModel.handleIntent(CartIntent.ApplyCoupon("FAKECODE99"))
            testScheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertNull(errorState.appliedCoupon)
            assertEquals("Invalid coupon code. Try SHOPEE20", errorState.couponError)
        }
    }

    @Test
    fun `RemoveItem intent calls repository and emits ShowToast effect`() = runTest(testDispatcher) {
        val viewModel = CartViewModel(productRepository, processCheckoutUseCase)
        testScheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.handleIntent(CartIntent.RemoveItem("c-1"))
            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is CartEffect.ShowToast)
            assertEquals("Item removed from cart", (effect as CartEffect.ShowToast).message)
            coVerify(exactly = 1) { productRepository.removeFromCart("c-1") }
        }
    }

    @Test
    fun `StartCheckout intent emits OpenCheckoutDialog effect`() = runTest(testDispatcher) {
        val viewModel = CartViewModel(productRepository, processCheckoutUseCase)
        testScheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.handleIntent(CartIntent.StartCheckout)
            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is CartEffect.OpenCheckoutDialog)
        }
    }
}
