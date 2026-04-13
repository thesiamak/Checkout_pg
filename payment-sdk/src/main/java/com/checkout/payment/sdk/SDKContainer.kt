package com.checkout.payment.sdk

import com.checkout.payment.api.PaymentConfig
import com.checkout.payment.data.api.ApiFactory
import com.checkout.payment.data.repository.PaymentRepositoryImpl
import com.checkout.payment.domain.repository.PaymentRepository
import com.checkout.payment.domain.usecase.ProcessPaymentUseCase

/**
 * Internal composition root. Wires data + domain layers.
 * Not visible to consumers -- they interact through PaymentSDK/PaymentSession.
 * ALL LAZY : to have min impact on app launch.
 */
internal class SDKContainer(private val config: PaymentConfig) {

    private val api by lazy {
        ApiFactory.createPaymentApi(config.environment.baseUrl())
    }

    private val repository: PaymentRepository by lazy {
        PaymentRepositoryImpl(
            api = api,
            publicKey = config.publicKey,
            secretKey = config.secretKey,
            successUrl = config.successUrl,
            failureUrl = config.failureUrl
        )
    }

    val processPaymentUseCase by lazy {
        ProcessPaymentUseCase(repository)
    }

    val paymentConfig: PaymentConfig get() = config
}
