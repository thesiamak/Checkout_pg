package com.checkout.payment.api.model

/**
 * Result of a payment request.
 * - [Requires3DS]: Payment requires 3DS verification, contains redirect URL.
 * - [Approved]: Payment approved without 3DS.
 * - [Declined]: Payment was declined.
 */
sealed class PaymentResult {
    data class Requires3DS(val redirectUrl: String) : PaymentResult()
    data object Approved : PaymentResult()
    data class Declined(val reason: String) : PaymentResult()
}
