package com.fakeshopee.app.domain.model

data class Product(
    val id: String,
    val title: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val reviewCount: Int,
    val description: String,
    val images: List<String>,
    val specs: Map<String, String>,
    val highlights: List<String>,
    val variants: List<ProductVariant>,
    val inStock: Boolean,
    val stockQuantity: Int,
    val isFavorite: Boolean = false,
    val reviews: List<ProductReview> = emptyList()
)

data class ProductVariant(
    val name: String,
    val hexColor: String,
    val inStock: Boolean
)

data class ProductReview(
    val id: String,
    val author: String,
    val rating: Int,
    val comment: String,
    val date: String,
    val isVerifiedBuyer: Boolean
)

data class CartItem(
    val id: String,
    val product: Product,
    val quantity: Int,
    val selectedColor: String?,
    val inStock: Boolean
)

data class Coupon(
    val code: String,
    val discountPercentage: Int = 0,
    val discountFlat: Double = 0.0,
    val description: String
)
