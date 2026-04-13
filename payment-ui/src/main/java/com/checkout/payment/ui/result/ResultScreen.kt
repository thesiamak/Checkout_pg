package com.checkout.payment.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.checkout.payment.ui.theme.CheckoutTheme

@Composable
fun ResultScreen(
    isSuccess: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSuccess) "\u2713" else "\u2717",
            fontSize = 72.sp,
            color = if (isSuccess) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isSuccess) "Payment Successful" else "Payment Failed",
            style = MaterialTheme.typography.headlineMedium,
            color = if (isSuccess) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )

        if (!isSuccess && errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = onDismiss) {
            Text(if (isSuccess) "Done" else "Try Again")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultScreenSuccessPreview() {
    CheckoutTheme {
        ResultScreen(
            isSuccess = true,
            errorMessage = null,
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultScreenFailurePreview() {
    CheckoutTheme {
        ResultScreen(
            isSuccess = false,
            errorMessage = "Payment was declined by the issuer.",
            onDismiss = {}
        )
    }
}
