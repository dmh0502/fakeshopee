package com.fakeshopee.app.data.local

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Pre-population / Seed Data for Room Database Single Source of Truth.
 * Ensures the Android application opens with fully populated products, cart item,
 * and wallet funds immediately on first launch, even if completely offline or without a server.
 */
object SeedData {

    private val gson = Gson()

    val INITIAL_PRODUCTS = listOf(
        ProductEntity(
            id = "prod-nexus-pro-5g",
            title = "Nexus Pro 5G Ultra",
            category = "Mobile",
            price = 1199.00,
            rating = 5.0,
            reviewCount = 1,
            description = "The pinnacle of mobile engineering. Powered by Quantum Core G9 neural engine with 16GB LPDDR5X RAM and a 200MP computational camera sensor capable of 10x lossless optical zoom.",
            imagesJson = gson.toJson(listOf(
                "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80"
            )),
            specsJson = gson.toJson(mapOf(
                "Processor" to "Quantum Core G9 (3.4GHz)",
                "Memory" to "16GB LPDDR5X",
                "Storage" to "1TB NVMe Gen4",
                "Display" to "6.8\" 144Hz AMOLED HDR10+",
                "Battery" to "5,500 mAh (120W HyperCharge)",
                "Camera" to "200MP Main + 50MP Ultra-wide"
            )),
            highlightsJson = gson.toJson(listOf("200MP Computational Sensor", "120W HyperCharge", "Quantum Core G9")),
            variantsJson = gson.toJson(listOf(
                mapOf("name" to "Abyss Indigo", "hexColor" to "#4648d4", "inStock" to true),
                mapOf("name" to "Obsidian Black", "hexColor" to "#283044", "inStock" to true),
                mapOf("name" to "Glacier White", "hexColor" to "#dae2fd", "inStock" to true)
            )),
            inStock = true,
            stockQuantity = 42,
            isFavorite = true,
            reviewsJson = gson.toJson(listOf(
                mapOf(
                    "id" to "rev-1",
                    "author" to "Jason D.",
                    "rating" to 5,
                    "comment" to "The battery life is incredible and the camera is a huge step up from last year.",
                    "date" to "2 days ago",
                    "isVerifiedBuyer" to true
                )
            ))
        ),
        ProductEntity(
            id = "prod-cyberblade-16",
            title = "CyberBlade Pro 16",
            category = "Laptops",
            price = 2499.00,
            rating = 5.0,
            reviewCount = 1,
            description = "Aerospace-grade magnesium-alloy chassis weighing just 1.42kg. Equipped with the Neural M4 Max silicon and a 120Hz Liquid Retina XDR display with 1600 nits peak brightness.",
            imagesJson = gson.toJson(listOf(
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&auto=format&fit=crop&q=80"
            )),
            specsJson = gson.toJson(mapOf(
                "Processor" to "Neural M4 Max (16-Core)",
                "GPU" to "40-Core Neural Graphic Engine",
                "RAM" to "64GB Unified Memory",
                "Display" to "16.2\" Liquid Retina XDR",
                "Battery" to "Up to 22 hours wireless web"
            )),
            highlightsJson = gson.toJson(listOf("Neural M4 Max Silicon", "64GB Unified Memory", "120Hz Liquid Retina")),
            variantsJson = gson.toJson(listOf(
                mapOf("name" to "Space Titanium", "hexColor" to "#242526", "inStock" to true),
                mapOf("name" to "Silver Frost", "hexColor" to "#E4E6EB", "inStock" to true)
            )),
            inStock = true,
            stockQuantity = 18,
            isFavorite = false,
            reviewsJson = gson.toJson(listOf(
                mapOf(
                    "id" to "rev-2",
                    "author" to "Marcus Vance",
                    "rating" to 5,
                    "comment" to "Compilation speeds are blazingly fast. Compiles our entire Android codebase in 30 seconds.",
                    "date" to "Yesterday",
                    "isVerifiedBuyer" to true
                )
            ))
        ),
        ProductEntity(
            id = "prod-neural-x",
            title = "Neural Glass X AR",
            category = "Wearables",
            price = 899.00,
            rating = 0.0,
            reviewCount = 0,
            description = "Lightweight spatial computing glasses with 4K micro-OLED optical wave guides per eye, eye-tracking precision down to 0.5 degrees, and 6-DoF spatial anchoring.",
            imagesJson = gson.toJson(listOf(
                "https://images.unsplash.com/photo-1593508512255-86ab42a8e620?w=800&auto=format&fit=crop&q=80"
            )),
            specsJson = gson.toJson(mapOf(
                "Optics" to "Dual 4K Micro-OLED Waveguides",
                "FOV" to "54-degree spatial field of view",
                "Weight" to "78 grams ultra-light",
                "Tracking" to "6DoF Inside-Out Optical + LiDAR"
            )),
            highlightsJson = gson.toJson(listOf("Dual 4K Micro-OLED", "78g Ultra-lightweight", "6DoF Spatial Tracking")),
            variantsJson = gson.toJson(listOf(
                mapOf("name" to "Carbon Matte", "hexColor" to "#18181b", "inStock" to true)
            )),
            inStock = true,
            stockQuantity = 25,
            isFavorite = true,
            reviewsJson = "[]"
        ),
        ProductEntity(
            id = "prod-quantum-pod",
            title = "Quantum Buds Spatial Pro",
            category = "Audio",
            price = 279.00,
            rating = 0.0,
            reviewCount = 0,
            description = "Active Noise Cancellation up to 48dB with real-time room acoustics scanning, lossless 24-bit 192kHz audiophile transmission, and 38-hour total playback battery.",
            imagesJson = gson.toJson(listOf(
                "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&auto=format&fit=crop&q=80"
            )),
            specsJson = gson.toJson(mapOf(
                "ANC" to "48dB Hybrid Active Noise Cancellation",
                "Drivers" to "11mm Beryllium Planar Drivers",
                "Battery" to "9h per earbud (38h with case)",
                "Connectivity" to "Bluetooth 5.4 LE Audio / LC3"
            )),
            highlightsJson = gson.toJson(listOf("48dB Active Noise Cancellation", "Lossless 24-bit 192kHz", "38 Hours Total Battery")),
            variantsJson = gson.toJson(listOf(
                mapOf("name" to "Midnight Black", "hexColor" to "#09090b", "inStock" to true),
                mapOf("name" to "Pearl White", "hexColor" to "#f8fafc", "inStock" to true)
            )),
            inStock = true,
            stockQuantity = 75,
            isFavorite = false,
            reviewsJson = "[]"
        ),
        ProductEntity(
            id = "prod-haptic-controller",
            title = "Vortex Haptic Controller",
            category = "Accessories",
            price = 149.00,
            rating = 0.0,
            reviewCount = 0,
            description = "Precision low-latency wireless gaming and productivity peripheral with magnetic hall-effect sensors, adaptive micro-triggers, and ergonomic customizable grips.",
            imagesJson = gson.toJson(listOf(
                "https://images.unsplash.com/photo-1592840496694-26d035b52b48?w=800&auto=format&fit=crop&q=80"
            )),
            specsJson = gson.toJson(mapOf(
                "Sensors" to "Hall Effect Magnetic Sticks & Triggers",
                "Polling Rate" to "1000Hz Ultra-Low Latency 1ms",
                "Battery" to "40 hours rechargeable Li-Ion",
                "Compatibility" to "Android, PC, Steam, macOS"
            )),
            highlightsJson = gson.toJson(listOf("Hall Effect Zero-Drift", "1000Hz Polling Rate", "Adaptive Micro-Triggers")),
            variantsJson = gson.toJson(listOf(
                mapOf("name" to "Stealth Gray", "hexColor" to "#3f3f46", "inStock" to true)
            )),
            inStock = true,
            stockQuantity = 60,
            isFavorite = false,
            reviewsJson = "[]"
        )
    )

    suspend fun seedDatabaseIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val productDao = database.productDao()
        val cartDao = database.cartDao()
        val walletDao = database.walletDao()
        val transactionDao = database.transactionDao()

        // 1. Seed Products if table is empty
        if (productDao.getProductCount() == 0) {
            productDao.insertProducts(INITIAL_PRODUCTS)
        }

        // 2. Seed Initial Cart Item so user can immediately view Cart screen
        if (cartDao.getCartItemCount() == 0) {
            val sampleCartItem = CartItemEntity(
                id = "cart-seed-1",
                productId = "prod-cyberblade-16",
                quantity = 1,
                selectedColor = "Space Titanium",
                inStock = true
            )
            cartDao.insertCartItem(sampleCartItem)
        }

        // 3. Seed Wallet Info if missing
        if (walletDao.getWalletOnce() == null) {
            walletDao.setWallet(
                WalletEntity(
                    id = 1,
                    availableBalance = 4250.00,
                    monthlySpend = 2778.00
                )
            )
        }

        // 4. Seed Initial Transactions for Ledger History
        if (transactionDao.getTransactionCount() == 0) {
            val sampleTransactions = listOf(
                TransactionEntity(
                    id = "tx-seed-1",
                    title = "Nexus Pro 5G Ultra Order",
                    amount = -1199.00,
                    type = "PAYMENT",
                    timestamp = System.currentTimeMillis() - 86400000 * 2,
                    formattedDate = "Sep 2, 02:45 PM",
                    category = "Mobile",
                    merchant = "FakeShopee Flagship Store",
                    referenceId = "FS-ORD-90214",
                    status = "COMPLETED",
                    iconName = "smartphone"
                ),
                TransactionEntity(
                    id = "tx-seed-2",
                    title = "Direct Deposit Payroll",
                    amount = 3250.00,
                    type = "RECHARGE",
                    timestamp = System.currentTimeMillis() - 86400000 * 5,
                    formattedDate = "Aug 30, 09:00 AM",
                    category = "Deposit",
                    merchant = "FakeShopee Treasury",
                    referenceId = "FS-DEP-84210",
                    status = "COMPLETED",
                    iconName = "account_balance"
                ),
                TransactionEntity(
                    id = "tx-seed-3",
                    title = "Quantum Buds Spatial Pro",
                    amount = -279.00,
                    type = "PAYMENT",
                    timestamp = System.currentTimeMillis() - 86400000 * 7,
                    formattedDate = "Aug 28, 11:20 AM",
                    category = "Audio",
                    merchant = "FakeShopee Online Express",
                    referenceId = "FS-ORD-66291",
                    status = "COMPLETED",
                    iconName = "headphones"
                )
            )
            transactionDao.insertTransactions(sampleTransactions)
        }
    }
}
