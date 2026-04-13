package com.checkout.payment.api.model

/**
 * Supported card schemes for icon display and validation.
 */
enum class CardScheme {
    VISA,
    MASTERCARD,
    AMEX,
    UNKNOWN;

    val cvvLength: Int
        get() = when (this) {
            AMEX -> 4
            else -> 3
        }

    val numberLength: Int
        get() = when (this) {
            AMEX -> 15
            else -> 16
        }

    companion object {
        fun detect(number: String): CardScheme {
            val digits = number.filter { it.isDigit() }
            if (digits.isEmpty()) return UNKNOWN

            return when {
                digits.startsWith("4") -> VISA
                digits.length >= 2 && digits.substring(0, 2).toIntOrNull()?.let { it in 51..55 } == true -> MASTERCARD
                digits.length >= 4 && digits.substring(0, 4).toIntOrNull()?.let { it in 2221..2720 } == true -> MASTERCARD
                digits.startsWith("34") || digits.startsWith("37") -> AMEX
                else -> UNKNOWN
            }
        }
    }
}
