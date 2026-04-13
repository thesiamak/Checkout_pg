package com.checkout.payment.domain.usecase

import com.checkout.payment.api.model.Card
import com.checkout.payment.api.model.PaymentError
import com.checkout.payment.api.model.PaymentResult
import com.checkout.payment.api.model.PaymentState
import com.checkout.payment.domain.repository.PaymentRepository

/**
 * Orchestrates the payment flow: tokenize -> pay -> map result to state.
 * Pure Kotlin, no Android dependencies.
 */
class ProcessPaymentUseCase(
    private val repository: PaymentRepository
) {
    suspend fun execute(card: Card): PaymentState {
        return try {
            val token = repository.tokenize(card)
            val result = repository.pay(token)

            when (result) {
                is PaymentResult.Requires3DS -> PaymentState.Requires3DS(result.redirectUrl)
                is PaymentResult.Approved -> PaymentState.Success
                is PaymentResult.Declined -> PaymentState.Failure(result.reason)
            }
        } catch (e: PaymentError.Network) {
            PaymentState.Failure("Network error. Please check your connection.")
        } catch (e: PaymentError.Declined) {
            PaymentState.Failure("Payment was declined.")
        } catch (e: PaymentError.Validation) {
            PaymentState.Failure("Invalid card details.")
        } catch (e: PaymentError) {
            PaymentState.Failure(e.message ?: "An unknown error occurred.")
        } catch (e: Exception) {
            PaymentState.Failure("An unexpected error occurred.")
        }
    }
}
