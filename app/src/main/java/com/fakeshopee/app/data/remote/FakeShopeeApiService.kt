package com.fakeshopee.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class ProductDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("price") val price: Double,
    @SerializedName("rating") val rating: Double,
    @SerializedName("reviewCount") val reviewCount: Int,
    @SerializedName("description") val description: String,
    @SerializedName("images") val images: List<String>,
    @SerializedName("specs") val specs: Map<String, String>,
    @SerializedName("highlights") val highlights: List<String>,
    @SerializedName("variants") val variants: List<VariantDto>,
    @SerializedName("inStock") val inStock: Boolean,
    @SerializedName("stockQuantity") val stockQuantity: Int
)

data class VariantDto(
    @SerializedName("name") val name: String,
    @SerializedName("hexColor") val hexColor: String,
    @SerializedName("inStock") val inStock: Boolean
)

// --- DummyJSON Products API Models (https://dummyjson.com/products) ---
data class DummyJsonResponse(
    @SerializedName("products") val products: List<DummyJsonProductDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("skip") val skip: Int,
    @SerializedName("limit") val limit: Int
)

data class DummyJsonProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("price") val price: Double,
    @SerializedName("discountPercentage") val discountPercentage: Double? = null,
    @SerializedName("rating") val rating: Double,
    @SerializedName("stock") val stock: Int,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("thumbnail") val thumbnail: String? = null,
    @SerializedName("reviews") val reviews: List<DummyJsonReviewDto>? = null
)

data class DummyJsonReviewDto(
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String,
    @SerializedName("date") val date: String,
    @SerializedName("reviewerName") val reviewerName: String
)

// --- Fake Store API Models (https://fakestoreapi.com/products) ---
data class FakeStoreProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("price") val price: Double,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("image") val image: String,
    @SerializedName("rating") val rating: FakeStoreRatingDto? = null
)

data class FakeStoreRatingDto(
    @SerializedName("rate") val rate: Double,
    @SerializedName("count") val count: Int
)

data class TransactionDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("type") val type: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("formattedDate") val formattedDate: String,
    @SerializedName("category") val category: String,
    @SerializedName("merchant") val merchant: String?,
    @SerializedName("referenceId") val referenceId: String,
    @SerializedName("status") val status: String,
    @SerializedName("iconName") val iconName: String
)

data class PagedResponse<T>(
    @SerializedName("items") val items: List<T>,
    @SerializedName("page") val page: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("hasMore") val hasMore: Boolean
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
