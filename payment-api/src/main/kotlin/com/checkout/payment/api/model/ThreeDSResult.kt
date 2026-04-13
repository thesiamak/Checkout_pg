package com.checkout.payment.api.model

/**
 * Result of the 3DS challenge handled in the WebView.
 */
sealed class ThreeDSResult {
    data object Success : ThreeDSResult()
    data object Failure : ThreeDSResult()
    data class Error(val message: String) : ThreeDSResult()
}
