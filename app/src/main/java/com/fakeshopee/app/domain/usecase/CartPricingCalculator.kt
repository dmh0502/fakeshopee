package com.fakeshopee.app.domain.usecase

import com.fakeshopee.app.domain.model.CartItem
import com.fakeshopee.app.domain.model.Coupon
import javax.inject.Inject
import javax.inject.Singleton

data class CartCalculation(
    val subtotal: Double,
    val discount: Double,
    val taxable: Double,
    val tax: Double,
    val grandTotal: Double
)

@Singleton
class CartPricingCalculator @Inject constructor() {
    fun calculate(items: List<CartItem>, coupon: Coupon? = null): CartCalculation {
        val inStockItems = items.filter { it.inStock }
        val subtotal = inStockItems.sumOf { it.product.price * it.quantity }
        var discount = 0.0
        if (coupon != null) {
            discount = if (coupon.discountPercentage > 0) {
                (subtotal * coupon.discountPercentage) / 100.0
            } else {
                minOf(subtotal, coupon.discountFlat)
            }
        }

        val taxable = maxOf(0.0, subtotal - discount)
        val tax = taxable * 0.08
        val rawTotal = taxable + tax
        val grandTotal = Math.round(rawTotal * 100.0) / 100.0

        return CartCalculation(
            subtotal = Math.round(subtotal * 100.0) / 100.0,
            discount = Math.round(discount * 100.0) / 100.0,
            taxable = Math.round(taxable * 100.0) / 100.0,
            tax = Math.round(tax * 100.0) / 100.0,
            grandTotal = grandTotal
        )
    }
}
