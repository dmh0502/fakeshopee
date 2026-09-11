package com.fakeshopee.app.data.remote

import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class ProductDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "category") val category: String,
    @Json(name = "price") val price: Double,
    @Json(name = "rating") val rating: Double,
    @Json(name = "reviewCount") val reviewCount: Int,
    @Json(name = "description") val description: String,
    @Json(name = "images") val images: List<String>,
    @Json(name = "specs") val specs: Map<String, String>,
    @Json(name = "highlights") val highlights: List<String>,
    @Json(name = "variants") val variants: List<VariantDto>,
    @Json(name = "inStock") val inStock: Boolean,
    @Json(name = "stockQuantity") val stockQuantity: Int
)

data class VariantDto(
    @Json(name = "name") val name: String,
    @Json(name = "hexColor") val hexColor: String,
    @Json(name = "inStock") val inStock: Boolean
)

// --- DummyJSON Products API Models (https://dummyjson.com/products) ---
data class DummyJsonResponse(
    @Json(name = "products") val products: List<DummyJsonProductDto>,
    @Json(name = "total") val total: Int,
    @Json(name = "skip") val skip: Int,
    @Json(name = "limit") val limit: Int
)

data class DummyJsonProductDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "category") val category: String,
    @Json(name = "price") val price: Double,
    @Json(name = "discountPercentage") val discountPercentage: Double? = null,
    @Json(name = "rating") val rating: Double,
    @Json(name = "stock") val stock: Int,
    @Json(name = "brand") val brand: String? = null,
    @Json(name = "images") val images: List<String> = emptyList(),
    @Json(name = "thumbnail") val thumbnail: String? = null,
    @Json(name = "reviews") val reviews: List<DummyJsonReviewDto>? = null
)

data class DummyJsonReviewDto(
    @Json(name = "rating") val rating: Int,
    @Json(name = "comment") val comment: String,
    @Json(name = "date") val date: String,
    @Json(name = "reviewerName") val reviewerName: String
)

// --- Fake Store API Models (https://fakestoreapi.com/products) ---
data class FakeStoreProductDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "price") val price: Double,
    @Json(name = "description") val description: String,
    @Json(name = "category") val category: String,
    @Json(name = "image") val image: String,
    @Json(name = "rating") val rating: FakeStoreRatingDto? = null
)

data class FakeStoreRatingDto(
    @Json(name = "rate") val rate: Double,
    @Json(name = "count") val count: Int
)

data class TransactionDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "type") val type: String,
    @Json(name = "timestamp") val timestamp: Long,
    @Json(name = "formattedDate") val formattedDate: String,
    @Json(name = "category") val category: String,
    @Json(name = "merchant") val merchant: String?,
    @Json(name = "referenceId") val referenceId: String,
    @Json(name = "status") val status: String,
    @Json(name = "iconName") val iconName: String
)

data class PagedResponse<T>(
    @Json(name = "items") val items: List<T>,
    @Json(name = "page") val page: Int,
    @Json(name = "pageSize") val pageSize: Int,
    @Json(name = "hasMore") val hasMore: Boolean
)

interface FakeShopeeApiService {
    // DummyJSON API integration (https://dummyjson.com/products)
    @GET("https://dummyjson.com/products")
    suspend fun getDummyJsonProducts(
        @Query("limit") limit: Int = 100,
        @Query("skip") skip: Int = 0
    ): Response<DummyJsonResponse>

    @GET("https://dummyjson.com/products/search")
    suspend fun searchDummyJsonProducts(
        @Query("q") query: String
    ): Response<DummyJsonResponse>

    @GET("https://dummyjson.com/products/categories")
    suspend fun getDummyJsonCategories(): Response<List<Any>>

    // Fake Store API integration (https://fakestoreapi.com/products)
    @GET("https://fakestoreapi.com/products")
    suspend fun getFakeStoreProducts(): Response<List<FakeStoreProductDto>>

    // Primary Product & Transaction Endpoints
    @GET("api/products")
    suspend fun getProducts(): Response<List<ProductDto>>

    @GET("api/transactions")
    suspend fun getTransactions(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("type") type: String? = null
    ): Response<PagedResponse<TransactionDto>>
}
