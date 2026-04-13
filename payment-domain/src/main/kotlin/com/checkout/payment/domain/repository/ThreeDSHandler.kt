package com.checkout.payment.domain.repository

import com.checkout.payment.api.model.ThreeDSResult

/**
 * Domain-layer abstraction for 3DS challenge handling.
 * Implemented in the platform layer (Android WebView).
 */
interface ThreeDSHandler {
    suspend fun handle(url: String): ThreeDSResult
}
