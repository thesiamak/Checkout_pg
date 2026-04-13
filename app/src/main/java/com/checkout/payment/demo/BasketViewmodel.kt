package com.checkout.payment.demo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BasketItem(
    val name: String,
    val description: String,
    val priceMinor: Int,
    val quantity: Int = 1
)

data class BasketState(
    val items: List<BasketItem> = DEFAULT_ITEMS,
    val currency: String = "GBP",
    val userId: String = "user_demo_928371"
) {
    val totalMinor: Int get() = items.sumOf { it.priceMinor * it.quantity }

    val formattedTotal: String
        get() {
            val symbol = if (currency == "GBP") "\u00A3" else currency
            return "$symbol${String.format("%.2f", totalMinor / 100.0)}"
        }
}

sealed class DemoScreen(val order: Int) {
    data object Basket : DemoScreen(0)
    data object Payment : DemoScreen(1)
    data class Result(val success: Boolean, val message: String) : DemoScreen(2)
}

class BasketViewModel : ViewModel() {

    private val _basket = MutableStateFlow(BasketState())
    val basket: StateFlow<BasketState> = _basket.asStateFlow()

    private val _screen = MutableStateFlow<DemoScreen>(DemoScreen.Basket)
    val screen: StateFlow<DemoScreen> = _screen.asStateFlow()

    fun onCheckout() {
        _screen.value = DemoScreen.Payment
    }

    fun onPaymentComplete(success: Boolean) {
        _screen.value = DemoScreen.Result(
            success = success,
            message = if (success) {
                "Payment of ${_basket.value.formattedTotal} completed successfully."
            } else {
                "Payment failed. You have not been charged."
            }
        )
    }

    fun onBackToBasket() {
        _screen.value = DemoScreen.Basket
    }
}

private val DEFAULT_ITEMS = listOf(
    BasketItem(
        name = "Wireless Headphones",
        description = "Noise-cancelling, Bluetooth 5.3",
        priceMinor = 3500
    ),
    BasketItem(
        name = "USB-C Cable (2m)",
        description = "Braided, 100W PD",
        priceMinor = 1290
    ),
    BasketItem(
        name = "Phone Case",
        description = "Clear, MagSafe compatible",
        priceMinor = 1750
    )
)
