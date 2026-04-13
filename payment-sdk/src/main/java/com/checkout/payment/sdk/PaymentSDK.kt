package com.checkout.payment.sdk

import com.checkout.payment.api.PaymentConfig

/**
 * Public entry point for the Payment SDK.
 * Consumer apps call initialize() once, typically in Application.onCreate().
 */
object PaymentSDK {

    private var container: SDKContainer? = null

    /**
     * Initialize the SDK with the given configuration.
     * Must be called before any payment operations.
     */
    fun initialize(config: PaymentConfig) {
        container = SDKContainer(config)
    }

    internal fun requireContainer(): SDKContainer {
        return container ?: throw IllegalStateException(
            "PaymentSDK.initialize() must be called before using the SDK."
        )
    }
}
