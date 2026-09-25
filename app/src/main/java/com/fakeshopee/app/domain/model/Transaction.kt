package com.fakeshopee.app.domain.model

enum class TransactionType {
    RECHARGE,
    PAYMENT,
    REFUND
}

enum class TransactionStatus {
    COMPLETED,
    PENDING,
    FAILED
}

data class Transaction(
    val id: String,
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val timestamp: Long,
    val formattedDate: String,
    val category: String,
    val merchant: String?,
    val referenceId: String,
    val status: TransactionStatus,
    val iconName: String
)

data class Wallet(
    val availableBalance: Long,
    val monthlySpend: Long,
    val currency: String = "USD"
)
