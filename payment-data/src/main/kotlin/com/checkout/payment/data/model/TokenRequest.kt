package com.checkout.payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(
    @SerialName("type") val type: String = "card",
    @SerialName("number") val number: String,
    @SerialName("expiry_month") val expiryMonth: String,
    @SerialName("expiry_year") val expiryYear: String,
    @SerialName("cvv") val cvv: String
)
