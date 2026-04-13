package com.checkout.payment.data.repository

import com.checkout.payment.data.api.PaymentApi
import com.checkout.payment.data.model.PaymentRequest
import com.checkout.payment.data.model.PaymentSource
import com.checkout.payment.data.model.ThreeDSConfig
import com.checkout.payment.data.model.TokenRequest
import com.checkout.payment.api.model.Card
import com.checkout.payment.api.model.PaymentError
import com.checkout.payment.api.model.PaymentResult
import com.checkout.payment.api.model.Token
import com.checkout.payment.domain.repository.PaymentRepository
import java.io.IOException

/**
 * Concrete implementation of [PaymentRepository].
 * Maps API responses to domain models and translates HTTP errors to [PaymentError].
 */
class PaymentRepositoryImpl(
    private val api: PaymentApi,
    private val publicKey: String,
    private val secretKey: String,
    private val successUrl: String,
    private val failureUrl: String
) : PaymentRepository {


    // TODO: We're interpreting network response in both API. [Duplication]. Think about defining a handler with scalability.
    override suspend fun tokenize(card: Card): Token {
        try {
            val request = TokenRequest(
                number = card.sanitizedNumber,
                expiryMonth = card.expiryMonth.toString(),
                expiryYear = card.expiryYear.toString(),
                cvv = card.cvv
            )
            val response = api.tokenize(publicKey, request)

            if (response.isSuccessful) {
                val body = response.body()
                    ?: throw PaymentError.Unknown("Empty response from tokenize")
                return Token(body.token)
            }

            when (response.code()) {
                401 -> throw PaymentError.Validation
                422 -> throw PaymentError.Validation
                else -> throw PaymentError.Unknown("Tokenize failed: HTTP ${response.code()}")
            }
        } catch (e: PaymentError) {
            throw e
        } catch (e: IOException) {
            throw PaymentError.Network
        } catch (e: Exception) {
            throw PaymentError.Unknown(e.message ?: "Unknown tokenize error")
        }
    }

    override suspend fun pay(token: Token): PaymentResult {
        try {
            val request = PaymentRequest(
                source = PaymentSource(token = token.value),
                amount = 6540,
                currency = "GBP",
                threeds = ThreeDSConfig(enabled = true),
                successUrl = successUrl,
                failureUrl = failureUrl
            )
            val response = api.requestPayment("Bearer $secretKey", request)

            if (response.isSuccessful) {
                val body = response.body()
                    ?: throw PaymentError.Unknown("Empty response from payment")

                return when {
                    body.status.equals("Pending", ignoreCase = true) -> {
                        val redirectUrl = body.links?.redirect?.href
                            ?: throw PaymentError.Unknown("3DS redirect URL missing")
                        PaymentResult.Requires3DS(redirectUrl)
                    }
                    body.status.equals("Authorized", ignoreCase = true) ||
                    body.status.equals("Captured", ignoreCase = true) -> {
                        PaymentResult.Approved
                    }
                    body.status.equals("Declined", ignoreCase = true) -> {
                        PaymentResult.Declined("Payment was declined by the issuer.")
                    }
                    else -> {
                        PaymentResult.Declined("Payment status: ${body.status}")
                    }
                }
            }

            when (response.code()) {
                // TODO: Magic numbers: Need to make these network errors constants.
                401 -> throw PaymentError.Unknown("Authentication failed")
                422 -> throw PaymentError.Validation
                else -> throw PaymentError.Unknown("Payment failed: HTTP ${response.code()}")
            }
        } catch (e: PaymentError) {
            throw e
        } catch (e: IOException) {
            throw PaymentError.Network
        } catch (e: Exception) {
            throw PaymentError.Unknown(e.message ?: "Unknown payment error")
        }
    }
}
