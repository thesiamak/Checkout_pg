package com.checkout.payment.api.model

/**
 * Domain model representing card details entered by the user.
 * This is a pure Kotlin data class with no Android dependencies.
 */
data class Card(
    val number: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String
) {
    /**
     * Returns the card number with all non-digit characters stripped.
     */
    val sanitizedNumber: String get() = number.filter { it.isDigit() }
}
