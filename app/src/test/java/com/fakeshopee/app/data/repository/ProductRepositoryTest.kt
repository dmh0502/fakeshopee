package com.fakeshopee.app.data.repository

import com.google.gson.Gson
import com.fakeshopee.app.data.local.AppDatabase
import com.fakeshopee.app.data.local.CartDao
import com.fakeshopee.app.data.local.ProductDao
import com.fakeshopee.app.data.local.ProductEntity
import com.fakeshopee.app.data.remote.FakeShopeeApiService
import com.fakeshopee.app.data.remote.ProductDto
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ProductRepositoryTest {

    private val database: AppDatabase = mockk(relaxed = true)
    private val productDao: ProductDao = mockk(relaxed = true)
    private val cartDao: CartDao = mockk(relaxed = true)
    private val apiService: FakeShopeeApiService = mockk()
    private val gson: Gson = Gson()

    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setUp() {
        every { database.productDao() } returns productDao
        every { database.cartDao() } returns cartDao
        repository = ProductRepositoryImpl(database, apiService, gson)
    }

    @Test
    fun `getProductsStream observes data directly from Room Database as SSOT`() = runTest {
        val cachedEntities = listOf(
            ProductEntity(
                id = "p-1",
                title = "CyberBlade Laptop",
                category = "Computing",
                price = 2499.0,
                rating = 4.9,
                reviewCount = 84,
                description = "Flagship titanium body workstation",
                imagesJson = "[]",
                specsJson = "{}",
                highlightsJson = "[]",
                variantsJson = "[]",
                inStock = true,
                stockQuantity = 15
            )
        )
        every { productDao.getAllProducts() } returns flowOf(cachedEntities)

        val products = repository.getProductsStream().first()

        assertEquals(1, products.size)
        assertEquals("p-1", products[0].id)
        assertEquals("CyberBlade Laptop", products[0].title)
        verify(exactly = 1) { productDao.getAllProducts() }
        verify { apiService wasNot Called }
    }

    @Test
    fun `refreshProducts on network failure returns failure result without breaking Room DB cache`() = runTest {
        coEvery { apiService.getProducts() } throws IOException("No internet connection")
        coEvery { apiService.getDummyJsonProducts(any(), any()) } throws IOException("No internet connection")
        coEvery { apiService.getFakeStoreProducts() } throws IOException("No internet connection")

        val result = repository.refreshProducts()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals("No internet connection", result.exceptionOrNull()?.message)

        coVerify(exactly = 0) { productDao.insertProducts(any()) }
    }

    @Test
    fun `refreshProducts on HTTP 500 error returns failure result`() = runTest {
        val errorResponse = Response.error<List<ProductDto>>(
            500,
            "Internal Server Error".toResponseBody("application/json".toMediaTypeOrNull())
        )
        val dummyErrorResponse = Response.error<com.fakeshopee.app.data.remote.DummyJsonResponse>(
            500,
            "Internal Server Error".toResponseBody("application/json".toMediaTypeOrNull())
        )
        val fakeStoreErrorResponse = Response.error<List<com.fakeshopee.app.data.remote.FakeStoreProductDto>>(
            500,
            "Internal Server Error".toResponseBody("application/json".toMediaTypeOrNull())
        )
        coEvery { apiService.getProducts() } returns errorResponse
        coEvery { apiService.getDummyJsonProducts(any(), any()) } returns dummyErrorResponse
        coEvery { apiService.getFakeStoreProducts() } returns fakeStoreErrorResponse

        val result = repository.refreshProducts()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("500") == true)
        coVerify(exactly = 0) { productDao.insertProducts(any()) }
    }

    @Test
    fun `refreshProducts on network success maps DTOs and updates Room DB cache`() = runTest {
        val remoteDtos = listOf(
            ProductDto(
                id = "p-100",
                title = "Quantum Earbuds",
                category = "Audio",
                price = 199.0,
                rating = 4.8,
                reviewCount = 120,
                description = "Active noise cancelling",
                images = listOf("https://example.com/earbuds.png"),
                specs = mapOf("Driver" to "11mm"),
                highlights = listOf("Spatial Audio"),
                variants = emptyList(),
                inStock = true,
                stockQuantity = 50
            )
        )
        coEvery { apiService.getProducts() } returns Response.success(remoteDtos)
        coEvery { productDao.insertProducts(any()) } just Runs

        val result = repository.refreshProducts()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { productDao.insertProducts(match { it.size == 1 && it[0].id == "p-100" }) }
    }

    @Test
    fun `toggleFavorite delegates mutation to Room Dao`() = runTest {
        coEvery { productDao.toggleFavorite("prod-1") } just Runs

        repository.toggleFavorite("prod-1")

        coVerify(exactly = 1) { productDao.toggleFavorite("prod-1") }
    }
}
