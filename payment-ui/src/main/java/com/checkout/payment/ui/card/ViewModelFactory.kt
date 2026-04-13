package com.checkout.payment.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.checkout.payment.domain.repository.ThreeDSHandler
import com.checkout.payment.domain.usecase.ProcessPaymentUseCase

class PaymentViewModelFactory(
    private val useCase: ProcessPaymentUseCase,
    private val threeDSHandler: ThreeDSHandler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PaymentViewModel(useCase, threeDSHandler) as T
    }
}