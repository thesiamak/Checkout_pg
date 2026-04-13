package com.checkout.payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    @SerialName("source") val source: PaymentSource,
    @SerialName("amount") val amount: Int,
    @SerialName("currency") val currency: String,
    @SerialName("3ds") val threeds: ThreeDSConfig,
    @SerialName("success_url") val successUrl: String,
    @SerialName("failure_url") val failureUrl: String
)

@Serializable
data class PaymentSource(
    @SerialName("type") val type: String = "token",
    @SerialName("token") val token: String
)

@Serializable
data class ThreeDSConfig(
    @SerialName("enabled") val enabled: Boolean = true
)
