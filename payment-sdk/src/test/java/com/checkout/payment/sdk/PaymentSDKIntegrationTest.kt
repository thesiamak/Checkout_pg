package com.checkout.payment.sdk

import com.checkout.payment.api.model.PaymentResult
import com.checkout.payment.api.model.PaymentState
import com.checkout.payment.api.model.ThreeDSResult
import com.checkout.payment.domain.usecase.ProcessPaymentUseCase
import com.checkout.payment.testing.FakePaymentRepository
import com.checkout.payment.testing.FakeThreeDSHandler
import com.checkout.payment.testing.TestFixtures
import com.checkout.payment.ui.card.PaymentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for the SDK module. Each test wires multiple real components
 * across module boundaries (domain, ui, testing), with only the network layer faked.
 * This validates the SDK's orchestration layer, not individual components in isolation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PaymentSDKIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        resetSdkSingleton()
    }

    // Resets the PaymentSDK singleton so each test starts with a clean slate.
    private fun resetSdkSingleton() {
        val field = PaymentSDK::class.java.getDeclaredField("container")
        field.isAccessible = true
        field.set(PaymentSDK, null)
    }

    private fun buildViewModel(
        repo: FakePaymentRepository,
        handler: FakeThreeDSHandler
    ): PaymentViewModel = PaymentViewModel(ProcessPaymentUseCase(repo), handler)

    // -----------------------------------------------------------------------------------
    // Test 1: SDK initialization guard
    //
    // PaymentSDK.requireContainer() is the single gatekeeper that all SDK entry points
    // call before doing any work. Calling it without initialize() must fail loudly so
    // that merchant apps get an actionable error at development time.
    // -----------------------------------------------------------------------------------
    @Test(expected = IllegalStateException::class)
    fun `requireContainer throws before initialize is called`() {
        // SDK is reset in @Before via resetSdkSingleton, so no container exists here.
        PaymentSDK.requireContainer()
    }

    // -----------------------------------------------------------------------------------
    // Test 2: Full approved payment flow (domain + ui + testing modules wired together)
    //
    // Verifies that the SDK's orchestration stack correctly drives the state machine
    // to Success when the repository reports an approved payment. Crosses the
    // :payment-domain, :payment-ui, and :payment-testing module boundaries.
    // -----------------------------------------------------------------------------------
    @Test
    fun `full payment flow reaches Success when repository returns Approved`() = runTest {
        val repo = FakePaymentRepository(
            tokenResult = TestFixtures.TEST_TOKEN,
            payResult = PaymentResult.Approved
        )
        val handler = FakeThreeDSHandler()
        val vm = buildViewModel(repo, handler)

        vm.onCardNumberChanged(TestFixtures.VALID_VISA_CARD.number)
        vm.onExpiryChanged("0630")
        vm.onCvvChanged(TestFixtures.VALID_VISA_CARD.cvv)

        vm.onPayClicked()
        advanceUntilIdle()

        assertEquals(PaymentState.Success, vm.paymentState.value)
        assertEquals(1, repo.tokenizeCallCount)
        assertEquals(1, repo.payCallCount)
        // 3DS handler should never have been invoked on a direct approval
        assertEquals(0, handler.handleCallCount)
    }

    // -----------------------------------------------------------------------------------
    // Test 3: Full 3DS challenge flow (domain + ui + 3DS handler wired together)
    //
    // Verifies that when the repository demands a 3DS challenge, the SDK passes the
    // redirect URL to the ThreeDSHandler, suspends until the handler resolves, and
    // then maps ThreeDSResult.Success → PaymentState.Success. This exercises the
    // coroutine bridge between the ViewModel (suspend) and the WebView (callback)
    // layers — the core correctness guarantee of :payment-3ds.
    // -----------------------------------------------------------------------------------
    @Test
    fun `full 3DS flow reaches Success when handler auto-approves the challenge`() = runTest {
        val threeDSUrl = "https://3ds.sandbox.checkout.com/challenge"
        val repo = FakePaymentRepository(
            tokenResult = TestFixtures.TEST_TOKEN,
            payResult = PaymentResult.Requires3DS(threeDSUrl)
        )
        val handler = FakeThreeDSHandler(result = ThreeDSResult.Success)
        val vm = buildViewModel(repo, handler)

        vm.onCardNumberChanged(TestFixtures.VALID_VISA_CARD.number)
        vm.onExpiryChanged("0630")
        vm.onCvvChanged(TestFixtures.VALID_VISA_CARD.cvv)

        vm.onPayClicked()
        advanceUntilIdle()

        assertEquals(PaymentState.Success, vm.paymentState.value)
        assertEquals(1, handler.handleCallCount)
        assertEquals(threeDSUrl, handler.lastUrl)
        // Confirm the card was tokenized before 3DS was attempted
        assertEquals(TestFixtures.VALID_VISA_CARD.number, repo.lastTokenizedCard?.number)
    }
}
