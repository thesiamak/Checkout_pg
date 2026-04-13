package com.checkout.payment.api

import com.checkout.payment.api.model.Card
import com.checkout.payment.api.model.Token

/**
 * Public interface for the Payment SDK. Consumers program against this contract.
 * Implemented by :payment-sdk module.
 */
interface PaymentSDKContract {
    /**
     * Tokenize a card without making a payment.
     * Use when the merchant handles payment server-side.
     */
    suspend fun tokenize(card: Card): TokenResult

    /**
     * Get the config this SDK was initialized with.
     */
    val config: PaymentConfig
}

sealed class TokenResult {
    data class Success(val token: Token) : TokenResult()
    data class Failed(val error: String) : TokenResult()
}
