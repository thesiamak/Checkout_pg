package com.checkout.payment.sdk

import com.checkout.payment.api.Environment

internal fun Environment.baseUrl(): String = when (this) {
    Environment.SANDBOX -> "https://api.sandbox.checkout.com/"
    Environment.PRODUCTION -> "https://api.checkout.com/"
}