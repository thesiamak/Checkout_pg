package com.checkout.payment.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.checkout.payment.sdk.PaymentFlow
import com.checkout.payment.ui.theme.CheckoutTheme

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<BasketViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CheckoutTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val screen by viewModel.screen.collectAsState()
                    val basket by viewModel.basket.collectAsState()

                    AnimatedContent(
                        targetState = screen,
                        transitionSpec = {
                            val forward = targetState.order > initialState.order
                            val slideIn = slideInHorizontally { width ->
                                if (forward) width else -width
                            } + fadeIn()
                            val slideOut = slideOutHorizontally { width ->
                                if (forward) -width else width
                            } + fadeOut()
                            slideIn togetherWith slideOut
                        },
                        label = "DemoScreenTransition"
                    ) { current ->
                        when (current) {
                            is DemoScreen.Basket -> {
                                BasketScreen(
                                    basket = basket,
                                    onCheckout = viewModel::onCheckout
                                )
                            }

                            is DemoScreen.Payment -> {
                                PaymentFlow(
                                    onPaymentComplete = { success ->
                                        viewModel.onPaymentComplete(success)
                                    },
                                    onCancel = viewModel::onBackToBasket
                                )
                            }

                            is DemoScreen.Result -> {
                                OrderResultScreen(
                                    success = current.success,
                                    message = current.message,
                                    onBackToBasket = viewModel::onBackToBasket,
                                    onRetryPayment = viewModel::onCheckout
                                )
                            }
                        }
                    }
                }
            }
        }
        setListeners()
    }

    private fun setListeners(){
        onBackPressedDispatcher.addCallback(this,) {
            // If state is Basket screen(root) then just let it be handled by system, otherwise
            // use viewmodel back state
            if(viewModel.screen.value is DemoScreen.Basket){
                this.handleOnBackPressed()
            } else {
                viewModel.onBackToBasket()
            }
        }
    }
}
