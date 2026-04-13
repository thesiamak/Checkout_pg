package com.checkout.payment.domain

import com.checkout.payment.api.model.Card
import com.checkout.payment.api.model.PaymentError
import com.checkout.payment.api.model.PaymentResult
import com.checkout.payment.api.model.PaymentState
import com.checkout.payment.api.model.Token
import com.checkout.payment.testing.FakePaymentRepository
import com.checkout.payment.domain.usecase.ProcessPaymentUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessPaymentUseCaseTest {

    private val testCard = Card("4242424242424242", 6, 2030, "100")

    @Test
    fun `returns Requires3DS when payment is pending`() = runTest {
        val repo = FakePaymentRepository(
            tokenResult = Token("tok_test"),
            payResult = PaymentResult.Requires3DS("https://3ds.example.com")
        )
        val useCase = ProcessPaymentUseCase(repo)

        val result = useCase.execute(testCard)

        assertTrue(result is PaymentState.Requires3DS)
        assertEquals("https://3ds.example.com", (result as PaymentState.Requires3DS).url)
    }

    @Test
    fun `returns Success when payment is approved`() = runTest {
        val repo = FakePaymentRepository(
            tokenResult = Token("tok_test"),
            payResult = PaymentResult.Approved
        )
        val useCase = ProcessPaymentUseCase(repo)

        val result = useCase.execute(testCard)

        assertEquals(PaymentState.Success, result)
    }

    @Test
    fun `returns Failure when payment is declined`() = runTest {
        val repo = FakePaymentRepository(
            tokenResult = Token("tok_test"),
            payResult = PaymentResult.Declined("Insufficient funds")
        )
        val useCase = ProcessPaymentUseCase(repo)

        val result = useCase.execute(testCard)

        assertTrue(result is PaymentState.Failure)
        assertEquals("Insufficient funds", (result as PaymentState.Failure).error)
    }

    @Test
    fun `returns Failure on network error during tokenize`() = runTest {
        val repo = FakePaymentRepository(
            tokenError = PaymentError.Network
        )
        val useCase = ProcessPaymentUseCase(repo)

        val result = useCase.execute(testCard)

        assertTrue(result is PaymentState.Failure)
        assertTrue((result as PaymentState.Failure).error.contains("Network"))
    }

    @Test
    fun `returns Failure on validation error`() = runTest {
        val repo = FakePaymentRepository(
            tokenError = PaymentError.Validation
        )
        val useCase = ProcessPaymentUseCase(repo)

        val result = useCase.execute(testCard)

        assertTrue(result is PaymentState.Failure)
        assertTrue((result as PaymentState.Failure).error.contains("Invalid"))
    }

    @Test
    fun `returns Failure on unexpected exception`() = runTest {
        val repo = FakePaymentRepository(
            tokenError = RuntimeException("boom")
        )
        val useCase = ProcessPaymentUseCase(repo)

        val result = useCase.execute(testCard)

        assertTrue(result is PaymentState.Failure)
    }
}
