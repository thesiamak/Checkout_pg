package com.checkout.payment.ui.card

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.checkout.payment.api.model.CardScheme
import com.checkout.payment.ui.R
import com.checkout.payment.api.model.PaymentState
import com.checkout.payment.ui.theme.CheckoutTheme

@Composable
fun CardInputScreen(
    viewModel: PaymentViewModel,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardInput by viewModel.cardInput.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()
    val isLoading = paymentState is PaymentState.Loading

    CardInputContent(
        cardInput = cardInput,
        isLoading = isLoading,
        onCardNumberChanged = viewModel::onCardNumberChanged,
        onExpiryChanged = viewModel::onExpiryChanged,
        onCvvChanged = viewModel::onCvvChanged,
        onPayClicked = viewModel::onPayClicked,
        onCancel = onCancel,
        modifier = modifier
    )
}

@Composable
fun CardInputContent(
    cardInput: CardInputState,
    isLoading: Boolean,
    onCardNumberChanged: (String) -> Unit,
    onExpiryChanged: (String) -> Unit,
    onCvvChanged: (String) -> Unit,
    onPayClicked: () -> Unit,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val expiryFocusRequester = FocusRequester()
    val cvvFocusRequester = FocusRequester()

    Scaffold { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Payment",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "GBP 65.40",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card Number
            OutlinedTextField(
                value = cardInput.cardNumber,
                onValueChange = { value ->
                    onCardNumberChanged(value)
                    val digits = value.filter { it.isDigit() }
                    val maxLen = cardInput.cardScheme.numberLength
                    if (digits.length >= maxLen) {
                        expiryFocusRequester.requestFocus()
                    }
                },
                label = { Text("Card Number") },
                placeholder = { Text("4242 4242 4242 4242") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = CardNumberVisualTransformation(),
                isError = cardInput.cardNumberError != null,
                supportingText = cardInput.cardNumberError?.let { error ->
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                },
                trailingIcon = {
                    CardSchemeIcon(scheme = cardInput.cardScheme)
                },
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Expiry Date
                OutlinedTextField(
                    value = cardInput.expiryDate,
                    onValueChange = { value ->
                        onExpiryChanged(value)
                        val digits = value.filter { it.isDigit() }
                        if (digits.length >= 4) {
                            cvvFocusRequester.requestFocus()
                        }
                    },
                    label = { Text("MM/YY") },
                    placeholder = { Text("06/30") },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(expiryFocusRequester),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ExpiryVisualTransformation(),
                    isError = cardInput.expiryError != null,
                    supportingText = cardInput.expiryError?.let { error ->
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    },
                    singleLine = true,
                    enabled = !isLoading
                )

                // CVV
                OutlinedTextField(
                    value = cardInput.cvv,
                    onValueChange = onCvvChanged,
                    label = { Text("CVV") },
                    placeholder = { Text("100") },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(cvvFocusRequester),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = cardInput.cvvError != null,
                    supportingText = cardInput.cvvError?.let { error ->
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    },
                    singleLine = true,
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onPayClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = cardInput.isFormValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing...")
                } else {
                    Text("Pay GBP 65.40", style = MaterialTheme.typography.titleMedium)
                }
            }

            if (onCancel != null) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading
                ) {
                    Text("Cancel", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CardInputEmptyPreview() {
    CheckoutTheme {
        CardInputContent(
            cardInput = CardInputState(),
            isLoading = false,
            onCardNumberChanged = {},
            onExpiryChanged = {},
            onCvvChanged = {},
            onPayClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CardInputFilledPreview() {
    CheckoutTheme {
        CardInputContent(
            cardInput = CardInputState(
                cardNumber = "4242424242424242",
                expiryDate = "0630",
                cvv = "100",
                cardScheme = CardScheme.VISA,
                isFormValid = true
            ),
            isLoading = false,
            onCardNumberChanged = {},
            onExpiryChanged = {},
            onCvvChanged = {},
            onPayClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CardInputLoadingPreview() {
    CheckoutTheme {
        CardInputContent(
            cardInput = CardInputState(
                cardNumber = "4242424242424242",
                expiryDate = "0630",
                cvv = "100",
                cardScheme = CardScheme.VISA,
                isFormValid = true
            ),
            isLoading = true,
            onCardNumberChanged = {},
            onExpiryChanged = {},
            onCvvChanged = {},
            onPayClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CardInputErrorPreview() {
    CheckoutTheme {
        CardInputContent(
            cardInput = CardInputState(
                cardNumber = "1234",
                expiryDate = "13",
                cvv = "1",
                cardScheme = CardScheme.UNKNOWN,
                cardNumberError = "Invalid card number",
                expiryError = "Invalid expiry",
                cvvError = "Too short"
            ),
            isLoading = false,
            onCardNumberChanged = {},
            onExpiryChanged = {},
            onCvvChanged = {},
            onPayClicked = {}
        )
    }
}

@Composable
fun CardSchemeIcon(scheme: CardScheme) {
    val iconRes = when (scheme) {
        CardScheme.VISA -> R.drawable.ic_visa
        CardScheme.MASTERCARD -> R.drawable.ic_mastercard
        CardScheme.AMEX -> R.drawable.ic_amex
        CardScheme.UNKNOWN -> null
    }

    AnimatedContent(
        targetState = iconRes,
        transitionSpec = {
            (fadeIn() + scaleIn(initialScale = 0.8f))
                .togetherWith(fadeOut() + scaleOut(targetScale = 0.8f))
        },
        label = "CardSchemeIcon"
    ) { res ->
        if (res != null) {
            Image(
                painter = painterResource(id = res),
                contentDescription = scheme.name,
                modifier = Modifier.size(40.dp, 28.dp)
            )
        }
    }
}

/**
 * Formats card number as "4242 4242 4242 4242" (groups of 4).
 * AMEX uses "3782 822463 10005" (4-6-5) but we simplify to groups of 4 for all.
 */
class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            for (i in digits.indices) {
                if (i > 0 && i % 4 == 0) append(' ')
                append(digits[i])
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val spacesBeforeOffset = (offset - 1) / 4
                return offset + spacesBeforeOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val spacesBeforeOffset = (offset - 1) / 5
                return offset - spacesBeforeOffset
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

/**
 * Formats expiry as "MM/YY".
 */
class ExpiryVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            for (i in digits.indices) {
                if (i == 2) append('/')
                append(digits[i])
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset <= 2) offset else offset + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                return if (offset <= 2) offset else offset - 1
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
