package com.fakeshopee.app.domain.usecase

import com.fakeshopee.app.domain.model.CartItem
import com.fakeshopee.app.domain.model.Coupon
import javax.inject.Inject
import javax.inject.Singleton

data class CartCalculation(
    val subtotal: Int,
    val discount: Int,
    val taxable: Int,
    val tax: Int,
    val grandTotal: Int
)

@Singleton
class CartPricingCalculator @Inject constructor() {
    fun calculate(items: List<CartItem>, coupon: Coupon? = null): CartCalculation {
        val inStockItems = items.filter { it.inStock }
        val subtotal = inStockItems.sumOf { it.product.price * it.quantity }
        var discount = 0
        if (coupon != null) {
            discount = if (coupon.discountPercentage > 0) {
                (subtotal * coupon.discountPercentage) / 100
            } else {
                minOf(subtotal, coupon.discountFlat)
            }
        }

        val taxable = maxOf(0, subtotal - discount)
        val tax = (taxable * 8) / 100
        val grandTotal = taxable + tax

        return CartCalculation(
            subtotal = subtotal,
            discount = discount,
            taxable = taxable,
            tax = tax,
            grandTotal = grandTotal
        )
    }
}
