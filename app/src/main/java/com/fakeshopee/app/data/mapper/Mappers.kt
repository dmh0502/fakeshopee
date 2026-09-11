package com.fakeshopee.app.data.mapper



import com.fakeshopee.app.data.local.CartItemEntity
import com.fakeshopee.app.data.local.ProductEntity
import com.fakeshopee.app.data.local.TransactionEntity
import com.fakeshopee.app.data.local.WalletEntity
import com.fakeshopee.app.data.remote.DummyJsonProductDto
import com.fakeshopee.app.data.remote.FakeStoreProductDto
import com.fakeshopee.app.data.remote.ProductDto
import com.fakeshopee.app.data.remote.TransactionDto
import com.fakeshopee.app.domain.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.Locale

private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
private val stringListAdapter = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))
private val mapAdapter = moshi.adapter<Map<String, String>>(Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))
private val variantListAdapter = moshi.adapter<List<ProductVariant>>(Types.newParameterizedType(List::class.java, ProductVariant::class.java))
private val reviewListAdapter = moshi.adapter<List<ProductReview>>(Types.newParameterizedType(List::class.java, ProductReview::class.java))


private fun mapToStandardCategory(rawCategory: String): String {
    val lower = rawCategory.lowercase(Locale.ROOT)
    return when {
        lower.contains("phone") || lower.contains("mobile") || lower.contains("tablet") || lower.contains("smartphone") -> "Mobile"
        lower.contains("laptop") || lower.contains("computer") || lower.contains("computing") -> "Laptops"
        lower.contains("audio") || lower.contains("headphone") -> "Audio"
        lower.contains("wearable") || lower.contains("watch") -> "Wearables"
        lower.contains("accessory") || lower.contains("electronics") -> "Accessories"
        else -> rawCategory.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
}

// --- DummyJSON DTO -> Room Entity ---
fun DummyJsonProductDto.toEntity(): ProductEntity {
    val mappedVariants = listOf(
        ProductVariant(name = "Default", hexColor = "#6366F1", inStock = stock > 0)
    )
    val mappedReviews = reviews?.mapIndexed { index, r ->
        ProductReview(
            id = "rev-$id-$index",
            author = r.reviewerName,
            rating = r.rating,
            comment = r.comment,
            date = r.date,
            isVerifiedBuyer = true
        )
    } ?: emptyList()

    val mappedSpecs = mapOf(
        "Brand" to (brand ?: "Generic"),
        "Category" to mapToStandardCategory(category),
        "Stock" to "$stock units"
    )

    val calculatedRating = if (mappedReviews.isNotEmpty()) {
        val totalStars = mappedReviews.sumOf { it.rating }.toDouble()
        val avg = totalStars / mappedReviews.size
        Math.round(avg * 10.0) / 10.0
    } else {
        rating
    }

    val calculatedReviewCount = if (mappedReviews.isNotEmpty()) {
        mappedReviews.size
    } else {
        reviews?.size ?: 0
    }

    return ProductEntity(
        id = id.toString(),
        title = title,
        category = mapToStandardCategory(category),
        price = price,
        rating = calculatedRating,
        reviewCount = calculatedReviewCount,
        description = description,
        imagesJson = stringListAdapter.toJson(if (images.isNotEmpty()) images else listOfNotNull(thumbnail)),
        specsJson = mapAdapter.toJson(mappedSpecs),
        highlightsJson = stringListAdapter.toJson(listOf("Fast Shipping", "Genuine Product", "Warranty Included")),
        variantsJson = variantListAdapter.toJson(mappedVariants),
        inStock = stock > 0,
        stockQuantity = stock,
        isFavorite = false,
        reviewsJson = reviewListAdapter.toJson(mappedReviews),
        lastUpdated = System.currentTimeMillis()
    )
}

// --- Fake Store DTO -> Room Entity ---
fun FakeStoreProductDto.toEntity(): ProductEntity {
    val stdCategory = mapToStandardCategory(category)
    val mappedSpecs = mapOf(
        "Source" to "Fake Store API",
        "Category" to stdCategory
    )
    val mappedVariants = listOf(
        ProductVariant(name = "Standard", hexColor = "#4648D4", inStock = true)
    )

    return ProductEntity(
        id = "fakestore-$id",
        title = title,
        category = stdCategory,
        price = price,
        rating = rating?.rate ?: 0.0,
        reviewCount = rating?.count ?: 0,
        description = description,
        imagesJson = stringListAdapter.toJson(listOf(image)),
        specsJson = mapAdapter.toJson(mappedSpecs),
        highlightsJson = stringListAdapter.toJson(listOf("Genuine Electronics", "Warranty Included")),
        variantsJson = variantListAdapter.toJson(mappedVariants),
        inStock = true,
        stockQuantity = 25,
        isFavorite = false,
        reviewsJson = "[]",
        lastUpdated = System.currentTimeMillis()
    )
}

// --- Product DTO -> Room Entity ---
fun ProductDto.toEntity(): ProductEntity {
    val domainVariants = variants.map { ProductVariant(it.name, it.hexColor, it.inStock) }
    return ProductEntity(
        id = id,
        title = title,
        category = category,
        price = price,
        rating = rating,
        reviewCount = reviewCount,
        description = description,
        imagesJson = stringListAdapter.toJson(images),
        specsJson = mapAdapter.toJson(specs),
        highlightsJson = stringListAdapter.toJson(highlights),
        variantsJson = variantListAdapter.toJson(domainVariants),
        inStock = inStock,
        stockQuantity = stockQuantity,
        isFavorite = false,
        reviewsJson = "[]",
        lastUpdated = System.currentTimeMillis()
    )
}

// --- Room Entity -> Domain Model (Single Source of Truth) ---
fun ProductEntity.toDomain(): Product {
    val parsedImages: List<String> = try {
        stringListAdapter.fromJson(imagesJson) ?: emptyList()
    } catch (_: Exception) { emptyList() }

    val parsedSpecs: Map<String, String> = try {
        mapAdapter.fromJson(specsJson) ?: emptyMap()
    } catch (_: Exception) { emptyMap() }

    val parsedHighlights: List<String> = try {
        stringListAdapter.fromJson(highlightsJson) ?: emptyList()
    } catch (_: Exception) { emptyList() }

    val parsedVariants: List<ProductVariant> = try {
        variantListAdapter.fromJson(variantsJson) ?: emptyList()
    } catch (_: Exception) { emptyList() }

    val parsedReviews: List<ProductReview> = try {
        reviewListAdapter.fromJson(reviewsJson) ?: emptyList()
    } catch (_: Exception) { emptyList() }

    val calculatedRating: Double
    val calculatedReviewCount: Int
    if (parsedReviews.isNotEmpty()) {
        val totalStars = parsedReviews.sumOf { it.rating }.toDouble()
        val average = totalStars / parsedReviews.size
        calculatedRating = Math.round(average * 10.0) / 10.0
        calculatedReviewCount = parsedReviews.size
    } else {
        calculatedRating = rating
        calculatedReviewCount = reviewCount
    }

    return Product(
        id = id,
        title = title,
        category = category,
        price = price,
        rating = calculatedRating,
        reviewCount = calculatedReviewCount,
        description = description,
        images = parsedImages,
        specs = parsedSpecs,
        highlights = parsedHighlights,
        variants = parsedVariants,
        inStock = inStock,
        stockQuantity = stockQuantity,
        isFavorite = isFavorite,
        reviews = parsedReviews
    )
}

// --- Domain Model -> Room Entity ---
fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        title = title,
        category = category,
        price = price,
        rating = rating,
        reviewCount = reviewCount,
        description = description,
        imagesJson = stringListAdapter.toJson(images),
        specsJson = mapAdapter.toJson(specs),
        highlightsJson = stringListAdapter.toJson(highlights),
        variantsJson = variantListAdapter.toJson(variants),
        inStock = inStock,
        stockQuantity = stockQuantity,
        isFavorite = isFavorite,
        reviewsJson = reviewListAdapter.toJson(reviews),
        lastUpdated = System.currentTimeMillis()
    )
}

// --- CartItem Entity -> Domain Model ---
fun CartItemEntity.toDomain(product: Product): CartItem {
    return CartItem(
        id = id,
        product = product,
        quantity = quantity,
        selectedColor = selectedColor,
        inStock = inStock
    )
}

// --- Domain CartItem -> Room Entity ---
fun CartItem.toEntity(): CartItemEntity {
    return CartItemEntity(
        id = id,
        productId = product.id,
        quantity = quantity,
        selectedColor = selectedColor ?: "Default",
        inStock = inStock
    )
}

// --- Transaction DTO -> Room Entity ---
fun TransactionDto.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        type = type,
        timestamp = timestamp,
        formattedDate = formattedDate,
        category = category,
        merchant = merchant,
        referenceId = referenceId,
        status = status,
        iconName = iconName
    )
}

// --- Transaction Entity -> Domain Model ---
fun TransactionEntity.toDomain(): Transaction {
    val parsedType = try {
        TransactionType.valueOf(type.uppercase(Locale.ROOT))
    } catch (_: Exception) {
        TransactionType.PAYMENT
    }

    val parsedStatus = try {
        TransactionStatus.valueOf(status.uppercase(Locale.ROOT))
    } catch (_: Exception) {
        TransactionStatus.COMPLETED
    }

    return Transaction(
        id = id,
        title = title,
        amount = amount,
        type = parsedType,
        timestamp = timestamp,
        formattedDate = formattedDate,
        category = category,
        merchant = merchant,
        referenceId = referenceId,
        status = parsedStatus,
        iconName = iconName
    )
}

// --- Domain Transaction -> Room Entity ---
fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        type = type.name,
        timestamp = timestamp,
        formattedDate = formattedDate,
        category = category,
        merchant = merchant,
        referenceId = referenceId,
        status = status.name,
        iconName = iconName
    )
}

// --- Wallet Entity -> Domain Model ---
fun WalletEntity.toDomain(): Wallet {
    return Wallet(
        availableBalance = availableBalance,
        monthlySpend = monthlySpend
    )
}

// --- Domain Wallet -> Room Entity ---
fun Wallet.toEntity(): WalletEntity {
    return WalletEntity(
        id = 1,
        availableBalance = availableBalance,
        monthlySpend = monthlySpend
    )
}
