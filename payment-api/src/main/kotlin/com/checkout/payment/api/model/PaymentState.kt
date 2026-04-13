package com.checkout.payment.api.model

/**
 * Sealed class representing the full lifecycle of a payment flow.
 * Drives the UI state machine -- each state maps to a distinct screen or UI state.
 */
sealed class PaymentState {
    data object Idle : PaymentState()
    data object Loading : PaymentState()
    data class Requires3DS(val url: String) : PaymentState()
    data object Success : PaymentState()
    data class Failure(val error: String) : PaymentState()
}
