package com.checkout.payment.ui.card

import com.checkout.payment.api.model.PaymentResult
import com.checkout.payment.api.model.PaymentState
import com.checkout.payment.api.model.ThreeDSResult
import com.checkout.payment.api.model.Token
import com.checkout.payment.testing.FakePaymentRepository
import com.checkout.payment.testing.FakeThreeDSHandler
import com.checkout.payment.domain.usecase.ProcessPaymentUseCase
import com.checkout.payment.api.model.CardScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        tokenResult: Token = Token("tok_test"),
        payResult: PaymentResult = PaymentResult.Approved,
        threeDSHandler: com.checkout.payment.domain.repository.ThreeDSHandler = FakeThreeDSHandler()
    ): PaymentViewModel {
        val repo = FakePaymentRepository(
            tokenResult = tokenResult,
            payResult = payResult
        )
        return PaymentViewModel(ProcessPaymentUseCase(repo), threeDSHandler)
    }

    @Test
    fun `initial state is Idle`() {
        val vm = createViewModel()
        assertEquals(PaymentState.Idle, vm.paymentState.value)
    }

    @Test
    fun `card number updates state and detects scheme`() {
        val vm = createViewModel()
        vm.onCardNumberChanged("4242424242424242")

        assertEquals("4242424242424242", vm.cardInput.value.cardNumber)
        assertEquals(
            CardScheme.VISA,
            vm.cardInput.value.cardScheme
        )
    }

    @Test
    fun `expiry updates state`() {
        val vm = createViewModel()
        vm.onExpiryChanged("0630")

        assertEquals("0630", vm.cardInput.value.expiryDate)
    }

    @Test
    fun `cvv updates state`() {
        val vm = createViewModel()
        vm.onCvvChanged("100")

        assertEquals("100", vm.cardInput.value.cvv)
    }

    @Test
    fun `form is valid with correct inputs`() {
        val vm = createViewModel()
        vm.onCardNumberChanged("4242424242424242")
        vm.onExpiryChanged("0630")
        vm.onCvvChanged("100")

        assertTrue(vm.cardInput.value.isFormValid)
    }

    @Test
    fun `form is invalid with empty inputs`() {
        val vm = createViewModel()
        assertFalse(vm.cardInput.value.isFormValid)
    }

    @Test
    fun `pay with approved result transitions to Success`() = runTest {
        val vm = createViewModel(payResult = PaymentResult.Approved)
        vm.onCardNumberChanged("4242424242424242")
        vm.onExpiryChanged("0630")
        vm.onCvvChanged("100")

        vm.onPayClicked()
        advanceUntilIdle()

        assertEquals(PaymentState.Success, vm.paymentState.value)
    }

    @Test
    fun `pay with 3DS triggers handler and resolves to Success`() = runTest {
        val handler = FakeThreeDSHandler(ThreeDSResult.Success)
        val vm = createViewModel(
            payResult = PaymentResult.Requires3DS("https://3ds.test.com"),
            threeDSHandler = handler
        )
        vm.onCardNumberChanged("4242424242424242")
        vm.onExpiryChanged("0630")
        vm.onCvvChanged("100")

        vm.onPayClicked()
        advanceUntilIdle()

        assertEquals(PaymentState.Success, vm.paymentState.value)
    }

    @Test
    fun `pay with 3DS failure transitions to Failure`() = runTest {
        val handler = FakeThreeDSHandler(ThreeDSResult.Failure)
        val vm = createViewModel(
            payResult = PaymentResult.Requires3DS("https://3ds.test.com"),
            threeDSHandler = handler
        )
        vm.onCardNumberChanged("4242424242424242")
        vm.onExpiryChanged("0630")
        vm.onCvvChanged("100")

        vm.onPayClicked()
        advanceUntilIdle()

        assertTrue(vm.paymentState.value is PaymentState.Failure)
    }

    @Test
    fun `resetState returns to Idle with empty form`() {
        val vm = createViewModel()
        vm.onCardNumberChanged("4242424242424242")
        vm.resetState()

        assertEquals(PaymentState.Idle, vm.paymentState.value)
        assertEquals("", vm.cardInput.value.cardNumber)
    }

    @Test
    fun `pay with invalid form does not trigger payment`() = runTest {
        val vm = createViewModel()
        // Don't fill in card details

        vm.onPayClicked()
        advanceUntilIdle()

        // Should remain Idle since validation fails before triggering payment
        assertEquals(PaymentState.Idle, vm.paymentState.value)
    }
}
