package com.checkout.payment.domain.repository

import com.checkout.payment.api.model.Card
import com.checkout.payment.api.model.PaymentResult
import com.checkout.payment.api.model.Token

/**
 * Domain-layer abstraction for payment operations.
 * Implemented in the data layer -- domain has no knowledge of Retrofit, OkHttp, etc.
 */
interface PaymentRepository {
    suspend fun tokenize(card: Card): Token
    suspend fun pay(token: Token): PaymentResult
}
