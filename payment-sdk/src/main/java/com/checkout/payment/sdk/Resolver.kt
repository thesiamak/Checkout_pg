package com.checkout.payment.sdk

import com.checkout.payment.api.Environment


// These endpoints are provided as part of the public api ref so I'll leave them here to avoid some over engineering to hide publuc info.
// In a real-world architecture, mobile clients should not directly
// communicate with payment provider APIs. A backend should handle
// all sensitive operations and environment configuration. or at least coveyed thru safe channels.

internal fun Environment.baseUrl(): String = when (this) {
    Environment.SANDBOX -> "https://api.sandbox.checkout.com/"
    Environment.PRODUCTION -> "https://api.checkout.com/"
}