package com.checkout.payment.testing

import com.checkout.payment.api.model.Card
import com.checkout.payment.api.model.Token
import com.checkout.payment.api.PaymentConfig
import com.checkout.payment.api.Environment

/**
 * Pre-built test objects for consumer app tests.
 * Uses Checkout.com sandbox test card numbers.
 */
object TestFixtures {

    val VALID_VISA_CARD = Card(
        number = "4242424242424242",
        expiryMonth = 6,
        expiryYear = 2030,
        cvv = "100"
    )

    val FAILING_VISA_CARD = Card(
        number = "4243754271700719",
        expiryMonth = 6,
        expiryYear = 2030,
        cvv = "100"
    )

    val TEST_TOKEN = Token("tok_test_fixture")

    val SANDBOX_CONFIG = PaymentConfig(
        publicKey = "pk_test_xxx",
        secretKey = "sk_test_xxx",
        successUrl = "https://example.com/payments/success",
        failureUrl = "https://example.com/payments/fail",
        environment = Environment.SANDBOX
    )
}
