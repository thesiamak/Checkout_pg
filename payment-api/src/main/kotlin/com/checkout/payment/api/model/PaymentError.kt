package com.checkout.payment.api.model

/**
 * Typed payment errors for structured error handling.
 */
sealed class PaymentError : Exception() {
    data object Network : PaymentError()
    data object Declined : PaymentError()
    data object Validation : PaymentError()
    data class Unknown(override val message: String = "An unknown error occurred") : PaymentError()
}
