package com.checkout.payment.testing

import com.checkout.payment.api.model.Card
import com.checkout.payment.api.model.PaymentResult
import com.checkout.payment.api.model.Token
import com.checkout.payment.domain.repository.PaymentRepository

/**
 * Official fake for testing. Configurable to return specific results or throw errors.
 */
class FakePaymentRepository(
    private val tokenResult: Token? = null,
    private val payResult: PaymentResult? = null,
    private val tokenError: Throwable? = null,
    private val payError: Throwable? = null
) : PaymentRepository {

    var tokenizeCallCount: Int = 0
        private set
    var payCallCount: Int = 0
        private set
    var lastTokenizedCard: Card? = null
        private set

    override suspend fun tokenize(card: Card): Token {
        tokenizeCallCount++
        lastTokenizedCard = card
        tokenError?.let { throw it }
        return tokenResult ?: throw IllegalStateException("No token configured in FakePaymentRepository")
    }

    override suspend fun pay(token: Token): PaymentResult {
        payCallCount++
        payError?.let { throw it }
        return payResult ?: throw IllegalStateException("No pay result configured in FakePaymentRepository")
    }
}
