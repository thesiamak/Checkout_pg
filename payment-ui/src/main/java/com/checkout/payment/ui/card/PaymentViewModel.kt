package com.checkout.payment.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkout.payment.api.model.Card
import com.checkout.payment.api.model.CardScheme
import com.checkout.payment.api.model.PaymentState
import com.checkout.payment.api.model.ThreeDSResult
import com.checkout.payment.domain.validation.CardValidation
import com.checkout.payment.domain.repository.ThreeDSHandler
import com.checkout.payment.domain.usecase.ProcessPaymentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class PaymentViewModel(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val threeDSHandler: ThreeDSHandler
) : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    private val _cardInput = MutableStateFlow(CardInputState())
    val cardInput: StateFlow<CardInputState> = _cardInput.asStateFlow()

    fun onCardNumberChanged(raw: String) {
        val digits = raw.filter { it.isDigit() }
        val scheme = CardScheme.detect(digits)
        val maxLen = scheme.numberLength
        val truncated = if (digits.length > maxLen) digits.take(maxLen) else digits

        _cardInput.value = _cardInput.value.copy(
            cardNumber = truncated,
            cardScheme = scheme,
            cardNumberError = null
        )
        validateForm()
    }

    fun onExpiryChanged(raw: String) {
        val digits = raw.filter { it.isDigit() }
        val truncated = if (digits.length > 4) digits.take(4) else digits

        _cardInput.value = _cardInput.value.copy(
            expiryDate = truncated,
            expiryError = null
        )
        validateForm()
    }

    fun onCvvChanged(raw: String) {
        val digits = raw.filter { it.isDigit() }
        val scheme = _cardInput.value.cardScheme
        val maxLen = scheme.cvvLength
        val truncated = if (digits.length > maxLen) digits.take(maxLen) else digits

        _cardInput.value = _cardInput.value.copy(
            cvv = truncated,
            cvvError = null
        )
        validateForm()
    }

    private fun validateForm() {
        val state = _cardInput.value
        val (month, year) = parseExpiry(state.expiryDate)
        val scheme = state.cardScheme

        val numberValid = state.cardNumber.length >= 13 &&
            CardValidation.isValidNumber(state.cardNumber)
        val expiryValid = state.expiryDate.length == 4 &&
            month > 0 && year > 0 &&
            CardValidation.isValidExpiry(month, year, currentMonth(), currentYear())
        val cvvValid = CardValidation.isValidCvv(state.cvv, scheme)

        _cardInput.value = state.copy(
            isFormValid = numberValid && expiryValid && cvvValid
        )
    }

    fun onPayClicked() {
        val state = _cardInput.value
        val (month, year) = parseExpiry(state.expiryDate)

        // Run validation and set errors
        val errors = mutableListOf<String>()
        if (!CardValidation.isValidNumber(state.cardNumber)) {
            _cardInput.value = _cardInput.value.copy(cardNumberError = "Invalid card number")
            errors.add("number")
        }
        if (!CardValidation.isValidExpiry(month, year, currentMonth(), currentYear())) {
            _cardInput.value = _cardInput.value.copy(expiryError = "Invalid expiry date")
            errors.add("expiry")
        }
        if (!CardValidation.isValidCvv(state.cvv, state.cardScheme)) {
            _cardInput.value = _cardInput.value.copy(cvvError = "Invalid CVV")
            errors.add("cvv")
        }

        if (errors.isNotEmpty()) return

        val card = Card(
            number = state.cardNumber,
            expiryMonth = month,
            expiryYear = year,
            cvv = state.cvv
        )

        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading

            when (val result = processPaymentUseCase.execute(card)) {
                is PaymentState.Requires3DS -> {
                    _paymentState.value = result
                    handle3DS(result.url)
                }
                else -> {
                    _paymentState.value = result
                }
            }
        }
    }

    private suspend fun handle3DS(url: String) {
        val threeDSResult = threeDSHandler.handle(url)
        _paymentState.value = when (threeDSResult) {
            is ThreeDSResult.Success -> PaymentState.Success
            is ThreeDSResult.Failure -> PaymentState.Failure("3DS verification failed")
            is ThreeDSResult.Error -> PaymentState.Failure(threeDSResult.message)
        }
    }

    fun resetState() {
        _paymentState.value = PaymentState.Idle
        _cardInput.value = CardInputState()
    }

    private fun parseExpiry(expiry: String): Pair<Int, Int> {
        // all done by AI :) but will verify by UT later.
        if (expiry.length < 4) return Pair(0, 0)
        val month = expiry.substring(0, 2).toIntOrNull() ?: 0
        val yearSuffix = expiry.substring(2, 4).toIntOrNull() ?: 0
        val year = 2000 + yearSuffix
        return Pair(month, year)
    }

    private fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    private fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
}
