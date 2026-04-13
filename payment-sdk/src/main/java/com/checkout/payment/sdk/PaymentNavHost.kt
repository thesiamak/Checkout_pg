package com.checkout.payment.sdk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.checkout.payment.api.model.PaymentState
import com.checkout.payment.api.model.ThreeDSResult
import com.checkout.payment.domain.repository.ThreeDSHandler
import com.checkout.payment.domain.usecase.ProcessPaymentUseCase
import com.checkout.payment.threeds.ThreeDSDeferredHandler
import com.checkout.payment.threeds.ThreeDSScreen
import com.checkout.payment.ui.card.CardInputScreen
import com.checkout.payment.ui.card.PaymentViewModel
import com.checkout.payment.ui.card.PaymentViewModelFactory
import com.checkout.payment.ui.result.ResultScreen

/**
 * Drop-in payment flow Composable.
 * Embed this in your screen to get the full card input -> 3DS -> result experience.
 *
 * @param onPaymentComplete Called when the payment flow completes (success or failure dismissed).
 *        Pass null to handle dismissal internally (resets to Idle).
 */
@Composable
fun PaymentFlow(
    onPaymentComplete: ((Boolean) -> Unit)? = null,
    onCancel: (() -> Unit)? = null
) {
    val container = remember { PaymentSDK.requireContainer() }
    val threeDSDeferred = remember { ThreeDSDeferredHandler() }
    PaymentFlowInternal(
        useCase = container.processPaymentUseCase,
        threeDSHandler = threeDSDeferred,
        successUrl = container.paymentConfig.successUrl,
        failureUrl = container.paymentConfig.failureUrl,
        onThreeDSSuccess = { threeDSDeferred.complete(ThreeDSResult.Success) },
        onThreeDSFailure = { threeDSDeferred.complete(ThreeDSResult.Failure) },
        onPaymentComplete = onPaymentComplete,
        onCancel = onCancel
    )
}

/**
 * Testable entry point. Accepts dependencies directly instead of reading from SDKContainer.
 * Internal — consumer apps use [PaymentFlow].
 */
@Composable
internal fun PaymentFlowInternal(
    useCase: ProcessPaymentUseCase,
    threeDSHandler: ThreeDSHandler,
    successUrl: String,
    failureUrl: String,
    onThreeDSSuccess: () -> Unit = {},
    onThreeDSFailure: () -> Unit = {},
    onPaymentComplete: ((Boolean) -> Unit)? = null,
    onCancel: (() -> Unit)? = null
) {
    val viewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(useCase, threeDSHandler)
    )

    val paymentState by viewModel.paymentState.collectAsState()

    when (val state = paymentState) {
        is PaymentState.Idle,
        is PaymentState.Loading -> {
            CardInputScreen(viewModel = viewModel, onCancel = onCancel)
        }

        is PaymentState.Requires3DS -> {
            ThreeDSScreen(
                url = state.url,
                successUrl = successUrl,
                failureUrl = failureUrl,
                onSuccess = onThreeDSSuccess,
                onFailure = onThreeDSFailure
            )
        }

        is PaymentState.Success -> {
            ResultScreen(
                isSuccess = true,
                errorMessage = null,
                onDismiss = {
                    onPaymentComplete?.invoke(true) ?: viewModel.resetState()
                }
            )
        }

        is PaymentState.Failure -> {
            ResultScreen(
                isSuccess = false,
                errorMessage = state.error,
                onDismiss = {
                    onPaymentComplete?.invoke(false) ?: viewModel.resetState()
                }
            )
        }
    }
}
