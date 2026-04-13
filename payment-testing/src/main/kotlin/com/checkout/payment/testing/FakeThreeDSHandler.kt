package com.checkout.payment.testing

import com.checkout.payment.api.model.ThreeDSResult
import com.checkout.payment.domain.repository.ThreeDSHandler

/**
 * Official fake 3DS handler for testing.
 * Returns a pre-configured result immediately, no WebView involved.
 */
class FakeThreeDSHandler(
    private val result: ThreeDSResult = ThreeDSResult.Success
) : ThreeDSHandler {

    var handleCallCount: Int = 0
        private set
    var lastUrl: String? = null
        private set

    override suspend fun handle(url: String): ThreeDSResult {
        handleCallCount++
        lastUrl = url
        return result
    }
}
