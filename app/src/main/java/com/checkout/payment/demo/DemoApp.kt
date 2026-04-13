package com.checkout.payment.demo

import android.app.Application
import com.checkout.payment.api.PaymentConfig
import com.checkout.payment.api.Environment
import com.checkout.payment.sdk.PaymentSDK

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        PaymentSDK.initialize(
            PaymentConfig(
                publicKey = BuildConfig.CKO_PUBLIC_KEY,
                secretKey = BuildConfig.CKO_SECRET_KEY,
                successUrl = BuildConfig.SUCCESS_URL,
                failureUrl = BuildConfig.FAILURE_URL,
                environment = Environment.SANDBOX,
            )
        )
    }
}
