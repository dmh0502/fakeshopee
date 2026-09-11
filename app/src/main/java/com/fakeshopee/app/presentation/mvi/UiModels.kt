package com.fakeshopee.app.presentation.mvi

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.fakeshopee.app.domain.model.*

@Stable
@Immutable
data class ProductUiModel(
    val id: String,
    val title: String,
    val category: String,
    val price: Double,
    val formattedPrice: String,
    val rating: Double,
    val reviewCount: Int,
    val description: String,
    val primaryImageUrl: String,
    val images: List<String>,
    val specs: Map<String, String>,
    val highlights: List<String>,
    val variants: List<ProductVariant>,
    val inStock: Boolean,
    val stockQuantity: Int,
    val isFavorite: Boolean = false,
    val reviews: List<ProductReview> = emptyList()
)

@Stable
@Immutable
data class CartItemUiModel(
    val id: String,
    val product: ProductUiModel,
    val quantity: Int,
    val selectedColor: String?,
    val inStock: Boolean,
    val itemTotal: Double,
    val formattedItemTotal: String
)

@Stable
@Immutable
data class TransactionUiModel(
    val id: String,
    val title: String,
    val amount: Double,
    val formattedAmount: String,
    val isPositive: Boolean,
    val type: TransactionType,
    val timestamp: Long,
    val formattedDate: String,
    val category: String,
    val merchant: String?,
    val referenceId: String,
    val status: String,
    val iconName: String
)

@Stable
@Immutable
data class WalletUiModel(
    val availableBalance: Double,
    val formattedBalance: String,
    val monthlySpend: Double,
    val formattedMonthlySpend: String
)

// --- Domain -> UI State Mappers ---

fun Product.toUiModel(): ProductUiModel {
    val primary = images.firstOrNull() ?: ""
    return ProductUiModel(
        id = id,
        title = title,
        category = category,
        price = price,
        formattedPrice = "$%.2f".format(price),
        rating = rating,
        reviewCount = reviewCount,
        description = description,
        primaryImageUrl = primary,
        images = images,
        specs = specs,
        highlights = highlights,
        variants = variants,
        inStock = inStock,
        stockQuantity = stockQuantity,
        isFavorite = isFavorite,
        reviews = reviews
    )
}

fun ProductUiModel.toDomain(): Product {
    return Product(
        id = id,
        title = title,
        category = category,
        price = price,
        rating = rating,
        reviewCount = reviewCount,
        description = description,
        images = images,
        specs = specs,
        highlights = highlights,
        variants = variants,
        inStock = inStock,
        stockQuantity = stockQuantity,
        isFavorite = isFavorite,
        reviews = reviews
    )
}

fun CartItem.toUiModel(): CartItemUiModel {
    val total = product.price * quantity
    return CartItemUiModel(
        id = id,
        product = product.toUiModel(),
        quantity = quantity,
        selectedColor = selectedColor,
        inStock = inStock,
        itemTotal = total,
        formattedItemTotal = "$%.2f".format(total)
    )
}

fun Transaction.toUiModel(): TransactionUiModel {
    val isPos = type == TransactionType.RECHARGE || type == TransactionType.REFUND
    val prefix = if (isPos) "+" else "-"
    return TransactionUiModel(
        id = id,
        title = title,
        amount = amount,
        formattedAmount = "$prefix$%.2f".format(amount),
        isPositive = isPos,
        type = type,
        timestamp = timestamp,
        formattedDate = formattedDate,
        category = category,
        merchant = merchant,
        referenceId = referenceId,
        status = status.name,
        iconName = iconName
    )
}

fun Wallet.toUiModel(): WalletUiModel {
    return WalletUiModel(
        availableBalance = availableBalance,
        formattedBalance = "$%.2f".format(availableBalance),
        monthlySpend = monthlySpend,
        formattedMonthlySpend = "$%.2f".format(monthlySpend)
    )
}
