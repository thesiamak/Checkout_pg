package com.checkout.payment.domain.validation

import com.checkout.payment.api.model.Card
import com.checkout.payment.api.model.CardScheme

/**
 * Pure Kotlin card validation logic. No Android dependencies.
 */
object CardValidation {

    /**
     * Validates a card number using the Luhn algorithm.
     */
    fun isValidLuhn(number: String): Boolean {
        val digits = number.filter { it.isDigit() }
        if (digits.length < 13 || digits.length > 19) return false

        var sum = 0
        var alternate = false
        for (i in digits.length - 1 downTo 0) {
            var n = digits[i].digitToInt()
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    /**
     * Validates the card number format: correct length for scheme and passes Luhn.
     */
    fun isValidNumber(number: String): Boolean {
        val digits = number.filter { it.isDigit() }
        val scheme = CardScheme.detect(digits)
        if (scheme == CardScheme.UNKNOWN && digits.length != 16) return false
        if (scheme != CardScheme.UNKNOWN && digits.length != scheme.numberLength) return false
        return isValidLuhn(digits)
    }

    /**
     * Validates expiry month (1-12) and year (not in the past).
     * [year] is the full 4-digit year.
     */
    fun isValidExpiry(month: Int, year: Int, currentMonth: Int, currentYear: Int): Boolean {
        if (month < 1 || month > 12) return false
        // Commenting this during testing as some test cards cannot pass this.
        if (year < currentYear) return false
        if (year == currentYear && month < currentMonth) return false
        return true
    }

    /**
     * Validates CVV length based on card scheme.
     */
    fun isValidCvv(cvv: String, scheme: CardScheme): Boolean {
        val digits = cvv.filter { it.isDigit() }
        return digits.length == scheme.cvvLength
    }

    /**
     * Full card validation returning a list of errors (empty = valid).
     */
    fun validate(card: Card, currentMonth: Int, currentYear: Int): List<String> {
        val errors = mutableListOf<String>()
        if (!isValidNumber(card.sanitizedNumber)) {
            errors.add("Invalid card number")
        }
        if (!isValidExpiry(card.expiryMonth, card.expiryYear, currentMonth, currentYear)) {
            errors.add("Invalid expiry date")
        }
        val scheme = CardScheme.detect(card.sanitizedNumber)
        if (!isValidCvv(card.cvv, scheme)) {
            errors.add("Invalid CVV")
        }
        return errors
    }
}
