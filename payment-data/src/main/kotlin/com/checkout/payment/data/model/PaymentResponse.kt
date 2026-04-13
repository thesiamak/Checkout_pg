package com.checkout.payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("_links") val links: PaymentLinks? = null
)

@Serializable
data class PaymentLinks(
    @SerialName("redirect") val redirect: LinkHref? = null
)

@Serializable
data class LinkHref(
    @SerialName("href") val href: String? = null
)
