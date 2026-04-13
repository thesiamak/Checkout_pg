package com.checkout.payment.sdk

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.checkout.payment.api.model.PaymentResult
import com.checkout.payment.domain.usecase.ProcessPaymentUseCase
import com.checkout.payment.testing.FakePaymentRepository
import com.checkout.payment.testing.FakeThreeDSHandler
import com.checkout.payment.testing.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented integration test for the full payment UI flow.
 *
 * Validates that PaymentFlowInternal renders the correct screens and transitions through
 * card input → payment processing → result, using real Compose rendering on a device.
 * The network boundary is faked via :payment-testing, but everything else is real:
 * the composable tree, state machine, ViewModel, and use case.
 */
@RunWith(AndroidJUnit4::class)
class PaymentFlowIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cardInputScreen_isShownOnLaunch_andSuccessScreenAppearsAfterApprovedPayment() {
        var callbackResult: Boolean? = null

        val repo = FakePaymentRepository(
            tokenResult = TestFixtures.TEST_TOKEN,
            payResult = PaymentResult.Approved
        )

        composeTestRule.setContent {
            PaymentFlowInternal(
                useCase = ProcessPaymentUseCase(repo),
                threeDSHandler = FakeThreeDSHandler(),
                successUrl = TestFixtures.SANDBOX_CONFIG.successUrl,
                failureUrl = TestFixtures.SANDBOX_CONFIG.failureUrl,
                onPaymentComplete = { success -> callbackResult = success }
            )
        }

        // Card input screen is visible
        composeTestRule.onNodeWithText("Payment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Card Number").assertIsDisplayed()

        // Fill in valid card details
        composeTestRule.onNodeWithText("Card Number")
            .performTextInput(TestFixtures.VALID_VISA_CARD.number)
        composeTestRule.onNodeWithText("MM/YY")
            .performTextInput("0630")
        composeTestRule.onNodeWithText("CVV")
            .performTextInput(TestFixtures.VALID_VISA_CARD.cvv)

        // Tap Pay
        composeTestRule.onNodeWithText("Pay GBP 65.40").performClick()

        // Result screen appears with success message
        composeTestRule.waitUntil(timeoutMillis = 3_000) {
            composeTestRule
                .onAllNodes(androidx.compose.ui.test.hasText("Payment Successful"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithText("Payment Successful").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()

        // Dismiss and verify callback fired with success = true
        composeTestRule.onNodeWithText("Done").performClick()
        assertTrue(callbackResult == true)
    }

    // Almost same goes for a failing case but its 12AM already!
}
