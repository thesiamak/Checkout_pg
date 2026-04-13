package com.checkout.payment.data.api

import com.checkout.payment.data.model.PaymentRequest
import com.checkout.payment.data.model.PaymentResponse
import com.checkout.payment.data.model.TokenRequest
import com.checkout.payment.data.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface for the Checkout.com sandbox API.
 */
interface PaymentApi {

    @POST("tokens")
    suspend fun tokenize(
        @Header("Authorization") publicKey: String,
        @Body request: TokenRequest
    ): Response<TokenResponse>

    @POST("payments")
    suspend fun requestPayment(
        @Header("Authorization") authorization: String,
        @Body request: PaymentRequest
    ): Response<PaymentResponse>
}
