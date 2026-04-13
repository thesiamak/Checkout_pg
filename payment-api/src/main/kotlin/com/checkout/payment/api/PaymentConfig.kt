package com.checkout.payment.api

/**
 * Configuration for initializing the Payment SDK.
 * Provided by the consumer app at initialization time.
 */
data class PaymentConfig(
    val publicKey: String,
    val secretKey: String,
    val successUrl: String,
    val failureUrl: String,
    val environment: Environment = Environment.SANDBOX
)

enum class Environment {
    SANDBOX,
    PRODUCTION
}
