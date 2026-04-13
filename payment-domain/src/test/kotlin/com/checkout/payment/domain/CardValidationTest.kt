package com.checkout.payment.domain

import com.checkout.payment.api.model.Card
import com.checkout.payment.api.model.CardScheme
import com.checkout.payment.domain.validation.CardValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardValidationTest {

    // --- Luhn ---

    @Test
    fun `valid Visa card passes Luhn`() {
        assertTrue(CardValidation.isValidLuhn("4242424242424242"))
    }

    @Test
    fun `valid Mastercard passes Luhn`() {
        assertTrue(CardValidation.isValidLuhn("5436031030606378"))
    }

    @Test
    fun `invalid number fails Luhn`() {
        assertFalse(CardValidation.isValidLuhn("4242424242424241"))
    }

    @Test
    fun `too short number fails Luhn`() {
        assertFalse(CardValidation.isValidLuhn("424242"))
    }

    @Test
    fun `empty string fails Luhn`() {
        assertFalse(CardValidation.isValidLuhn(""))
    }

    // --- Card Number Validation ---

    @Test
    fun `valid 16-digit Visa number is valid`() {
        assertTrue(CardValidation.isValidNumber("4242424242424242"))
    }

    @Test
    fun `failure test card is valid`() {
        assertTrue(CardValidation.isValidNumber("4243754271700719"))
    }

    @Test
    fun `AMEX 15-digit number is valid`() {
        assertTrue(CardValidation.isValidNumber("378282246310005"))
    }

    @Test
    fun `wrong length for scheme is invalid`() {
        // 15 digits starting with 4 (Visa expects 16)
        assertFalse(CardValidation.isValidNumber("424242424242424"))
    }

    // --- Card Scheme Detection ---

    @Test
    fun `detects Visa`() {
        assertEquals(CardScheme.VISA, CardScheme.detect("4242424242424242"))
    }

    @Test
    fun `detects Mastercard range 51-55`() {
        assertEquals(CardScheme.MASTERCARD, CardScheme.detect("5436031030606378"))
    }

    @Test
    fun `detects Mastercard range 2221-2720`() {
        assertEquals(CardScheme.MASTERCARD, CardScheme.detect("2221000000000000"))
    }

    @Test
    fun `detects AMEX 34`() {
        assertEquals(CardScheme.AMEX, CardScheme.detect("340000000000000"))
    }

    @Test
    fun `detects AMEX 37`() {
        assertEquals(CardScheme.AMEX, CardScheme.detect("378282246310005"))
    }

    @Test
    fun `unknown scheme for unrecognized BIN`() {
        assertEquals(CardScheme.UNKNOWN, CardScheme.detect("9999999999999999"))
    }

    @Test
    fun `empty string returns unknown`() {
        assertEquals(CardScheme.UNKNOWN, CardScheme.detect(""))
    }

    // --- Expiry Validation ---

    @Test
    fun `future expiry is valid`() {
        assertTrue(CardValidation.isValidExpiry(6, 2030, 4, 2026))
    }

    @Test
    fun `current month and year is valid`() {
        assertTrue(CardValidation.isValidExpiry(4, 2026, 4, 2026))
    }

    @Test
    fun `past year is invalid`() {
        assertFalse(CardValidation.isValidExpiry(12, 2024, 4, 2026))
    }

    @Test
    fun `past month same year is invalid`() {
        assertFalse(CardValidation.isValidExpiry(1, 2026, 4, 2026))
    }

    @Test
    fun `month 0 is invalid`() {
        assertFalse(CardValidation.isValidExpiry(0, 2030, 4, 2026))
    }

    @Test
    fun `month 13 is invalid`() {
        assertFalse(CardValidation.isValidExpiry(13, 2030, 4, 2026))
    }

    // --- CVV Validation ---

    @Test
    fun `3-digit CVV valid for Visa`() {
        assertTrue(CardValidation.isValidCvv("100", CardScheme.VISA))
    }

    @Test
    fun `4-digit CVV valid for AMEX`() {
        assertTrue(CardValidation.isValidCvv("1234", CardScheme.AMEX))
    }

    @Test
    fun `3-digit CVV invalid for AMEX`() {
        assertFalse(CardValidation.isValidCvv("123", CardScheme.AMEX))
    }

    @Test
    fun `4-digit CVV invalid for Visa`() {
        assertFalse(CardValidation.isValidCvv("1234", CardScheme.VISA))
    }

    @Test
    fun `empty CVV is invalid`() {
        assertFalse(CardValidation.isValidCvv("", CardScheme.VISA))
    }

    // --- Full Card Validation ---

    @Test
    fun `valid card has no errors`() {
        val card = Card("4242424242424242", 6, 2030, "100")
        val errors = CardValidation.validate(card, 4, 2026)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `invalid card returns all errors`() {
        val card = Card("1234", 0, 2020, "")
        val errors = CardValidation.validate(card, 4, 2026)
        assertEquals(3, errors.size)
    }
}
