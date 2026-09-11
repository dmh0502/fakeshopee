package com.fakeshopee.app.presentation.viewmodel

import app.cash.turbine.test
import com.fakeshopee.app.domain.model.Product
import com.fakeshopee.app.domain.model.ProductVariant
import com.fakeshopee.app.domain.repository.CartRepository
import com.fakeshopee.app.domain.repository.ProductRepository
import com.fakeshopee.app.presentation.mvi.StoreEffect
import com.fakeshopee.app.presentation.mvi.StoreIntent
import com.fakeshopee.app.presentation.mvi.toUiModel
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
class StoreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val productRepository: ProductRepository = mockk(relaxed = true)
    private val cartRepository: CartRepository = mockk(relaxed = true)

    private val mockProducts = listOf(
        Product(
            id = "prod-1",
            title = "Neural Glass X",
            category = "Wearables",
            price = 999.0,
            rating = 4.8,
            reviewCount = 142,
            description = "Augmented reality titanium frames",
            images = listOf("https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=600"),
            specs = mapOf("Weight" to "48g"),
            highlights = listOf("Micro-OLED Display"),
            variants = listOf(ProductVariant("Matte Black", "#1E293B", true)),
            inStock = true,
            stockQuantity = 20
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { productRepository.getProductsStream() } returns flowOf(mockProducts)
        every { productRepository.getFilteredProductsStream(any(), any(), any()) } returns flowOf(mockProducts)
        coEvery { productRepository.refreshProducts() } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads products from Room stream into StateFlow`() = runTest(testDispatcher) {
        val viewModel = StoreViewModel(productRepository, cartRepository)

        viewModel.state.test {
            testScheduler.advanceUntilIdle()

            val updatedState = expectMostRecentItem()
            assertEquals(1, updatedState.products.size)
            assertEquals("Neural Glass X", updatedState.products[0].title)
            assertFalse(updatedState.isLoading)
        }
    }

    @Test
    fun `Search intent updates searchQuery in StateFlow`() = runTest(testDispatcher) {
        val viewModel = StoreViewModel(productRepository, cartRepository)
        testScheduler.advanceUntilIdle()

        viewModel.state.test {
            val current = awaitItem()
            assertEquals("", current.searchQuery)

            viewModel.handleIntent(StoreIntent.Search("Quantum"))
            val searchState = awaitItem()
            assertEquals("Quantum", searchState.searchQuery)
        }
    }

    @Test
    fun `SelectCategory intent updates selectedCategory in StateFlow`() = runTest(testDispatcher) {
        val viewModel = StoreViewModel(productRepository, cartRepository)
        testScheduler.advanceUntilIdle()

        viewModel.state.test {
            val current = awaitItem()
            assertEquals("All", current.selectedCategory)

            viewModel.handleIntent(StoreIntent.SelectCategory("Audio"))
            val categoryState = awaitItem()
            assertEquals("Audio", categoryState.selectedCategory)
        }
    }

    @Test
    fun `QuickAddToCart intent calls repository and emits ShowToast effect via SharedFlow`() = runTest(testDispatcher) {
        val viewModel = StoreViewModel(productRepository, cartRepository)
        testScheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.handleIntent(StoreIntent.QuickAddToCart(mockProducts[0].toUiModel()))
            testScheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is StoreEffect.ShowToast)
            assertEquals("Added Neural Glass X to cart", (effect as StoreEffect.ShowToast).message)

            coVerify(exactly = 1) { cartRepository.addToCart(match { it.id == "prod-1" }, "Matte Black", 1) }
        }
    }

    @Test
    fun `RefreshCatalog failure sets isOffline true and preserves Room cache`() = runTest(testDispatcher) {
        coEvery { productRepository.refreshProducts() } returns Result.failure(Exception("No internet"))
        val viewModel = StoreViewModel(productRepository, cartRepository)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.isOffline)
        assertEquals("No internet", viewModel.state.value.errorMessage)
    }
}
