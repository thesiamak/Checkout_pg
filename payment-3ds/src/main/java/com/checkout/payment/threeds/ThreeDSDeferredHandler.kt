package com.checkout.payment.threeds

import com.checkout.payment.api.model.ThreeDSResult
import com.checkout.payment.domain.repository.ThreeDSHandler
import kotlinx.coroutines.CompletableDeferred

/**
 * Bridges Compose 3DS WebView callbacks to the suspending ThreeDSHandler interface.
 * The ViewModel calls handle(url) which suspends until the WebView calls complete().
 */
class ThreeDSDeferredHandler : ThreeDSHandler {
    private var deferred: CompletableDeferred<ThreeDSResult>? = null

    // Todo: actually can remove [url] but lets keep for multiple implementation
    override suspend fun handle(url: String): ThreeDSResult {
        deferred = CompletableDeferred()
        return deferred!!.await()
    }

    fun complete(result: ThreeDSResult) {
        deferred?.complete(result)
    }
}
