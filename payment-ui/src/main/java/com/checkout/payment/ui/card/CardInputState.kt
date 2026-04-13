package com.checkout.payment.ui.card

import com.checkout.payment.api.model.CardScheme

/**
 * UI state for the card input form.
 */
data class CardInputState(
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cvv: String = "",
    val cardScheme: CardScheme = CardScheme.UNKNOWN,
    val cardNumberError: String? = null,
    val expiryError: String? = null,
    val cvvError: String? = null,
    val isFormValid: Boolean = false
)
