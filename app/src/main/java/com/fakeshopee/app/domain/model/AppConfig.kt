package com.fakeshopee.app.domain.model

object AppConfig {
    const val DEFAULT_TAX_RATE = 0.08

    val VALID_COUPONS = mapOf(
        "SHOPEE20" to Coupon("SHOPEE20", discountPercentage = 20, description = "20% off FakeShopee store items"),
        "KINETIC20" to Coupon("KINETIC20", discountPercentage = 20, description = "20% off Kinetic tech items")
    )

    val TECH_CATEGORIES = setOf(
        "smartphones", "laptops", "tablets", "mobile-accessories", "electronics",
        "mobile", "wearables", "audio", "accessories", "computing"
    )

    val TECH_KEYWORDS = listOf(
        "phone", "laptop", "tablet", "watch", "headphone", "audio", "electronics",
        "camera", "tech", "computer", "gaming", "drive", "ssd", "ram", "monitor", "tv", "silicon"
    )

    fun getValidCoupon(code: String): Coupon? {
        return VALID_COUPONS[code.trim().uppercase()]
    }
}
