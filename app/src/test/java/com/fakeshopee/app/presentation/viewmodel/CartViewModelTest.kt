package com.fakeshopee.app.presentation.viewmodel

import app.cash.turbine.test
import com.fakeshopee.app.domain.model.CartItem
import com.fakeshopee.app.domain.model.Product
import com.fakeshopee.app.domain.model.ProductVariant
import com.fakeshopee.app.domain.repository.CartRepository
import com.fakeshopee.app.domain.usecase.CartPricingCalculator
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
    private val cartRepository: CartRepository = mockk(relaxed = true)
    private val processCheckoutUseCase: ProcessCheckoutUseCase = mockk()
    private val cartPricingCalculator = CartPricingCalculator()

    private val testProduct = Product(
        id = "p-1",
        title = "Haptic Controller",
        category = "Gaming",
        price = 10000L,
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
        every { cartRepository.getCartItemsStream() } returns flowOf(testCartItems)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cart items emission automatically calculates subtotal, tax, and grandTotal in StateFlow`() = runTest(testDispatcher) {
        val viewModel = CartViewModel(cartRepository, processCheckoutUseCase, cartPricingCalculator)

        viewModel.state.test {
            awaitItem()
            testScheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(1, state.items.size)
            assertEquals(20000L, state.subtotal)
            assertEquals(0L, state.discount)
            assertEquals(1600L, state.tax)
            assertEquals(21600L, state.grandTotal)
        }
    }

    @Test
    fun `ApplyCoupon intent with SHOPEE20 applies 20 percent discount correctly`() = runTest(testDispatcher) {
        val viewModel = CartViewModel(cartRepository, processCheckoutUseCase, cartPricingCalculator)
        testScheduler.advanceUntilIdle()

        viewModel.state.test {
            val baseState = awaitItem()
            assertEquals(20000L, baseState.subtotal)

            viewModel.handleIntent(CartIntent.ApplyCoupon("SHOPEE20"))
            testScheduler.advanceUntilIdle()

            val discountedState = awaitItem()
            assertNotNull(discountedState.appliedCoupon)
            assertEquals("SHOPEE20", discountedState.appliedCoupon?.code)
            assertEquals(4000L, discountedState.discount)
            assertEquals(16000L, discountedState.subtotal - discountedState.discount)
            assertEquals(1280L, discountedState.tax)
            assertEquals(17280L, discountedState.grandTotal)
        }
    }

    @Test
    fun `ApplyCoupon intent with invalid code sets couponError`() = runTest(testDispatcher) {
        val viewModel = CartViewModel(cartRepository, processCheckoutUseCase, cartPricingCalculator)
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
        val viewModel = CartViewModel(cartRepository, processCheckoutUseCase, cartPricingCalculator)
        testScheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.handleIntent(CartIntent.RemoveItem("c-1"))
            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is CartEffect.ShowToast)
            assertEquals("Item removed from cart", (effect as CartEffect.ShowToast).message)
            coVerify(exactly = 1) { cartRepository.removeFromCart("c-1") }
        }
    }

    @Test
    fun `StartCheckout intent emits OpenCheckoutDialog effect`() = runTest(testDispatcher) {
        val viewModel = CartViewModel(cartRepository, processCheckoutUseCase, cartPricingCalculator)
        testScheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.handleIntent(CartIntent.StartCheckout)
            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is CartEffect.OpenCheckoutDialog)
        }
    }
}
