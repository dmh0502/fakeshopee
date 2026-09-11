package com.fakeshopee.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fakeshopee.app.domain.model.Product
import com.fakeshopee.app.domain.model.ProductVariant
import com.fakeshopee.app.domain.repository.CartRepository
import com.fakeshopee.app.domain.repository.ProductRepository
import com.fakeshopee.app.presentation.mvi.DetailEffect
import com.fakeshopee.app.presentation.mvi.DetailIntent
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
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val productRepository: ProductRepository = mockk(relaxed = true)
    private val cartRepository: CartRepository = mockk(relaxed = true)

    private val testProduct = Product(
        id = "p-100",
        title = "VR Pro Headset",
        category = "Wearables",
        price = 899.0,
        rating = 4.8,
        reviewCount = 42,
        description = "4K Micro-OLED",
        images = listOf("https://example.com/vr.png"),
        specs = mapOf("Resolution" to "4K"),
        highlights = listOf("OLED"),
        variants = listOf(ProductVariant("Black", "#000000", true)),
        inStock = true,
        stockQuantity = 5
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { productRepository.getProductByIdStream("p-100") } returns flowOf(testProduct)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `savedStateHandle loads product and emits ProductUiModel in StateFlow`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("productId" to "p-100"))
        val viewModel = DetailViewModel(productRepository, cartRepository, savedStateHandle)

        viewModel.state.test {
            testScheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertNotNull(state.product)
            assertEquals("p-100", state.product?.id)
            assertEquals("VR Pro Headset", state.product?.title)
            assertEquals("Black", state.selectedColor)
        }
    }

    @Test
    fun `AddToCart intent delegates to cartRepository and emits ShowToast effect`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("productId" to "p-100"))
        val viewModel = DetailViewModel(productRepository, cartRepository, savedStateHandle)
        testScheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.handleIntent(DetailIntent.AddToCart)
            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is DetailEffect.ShowToast)
            coVerify(exactly = 1) { cartRepository.addToCart(any(), "Black", 1) }
        }
    }
}
