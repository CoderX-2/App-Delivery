package com.example.appdelivery.ui.payment

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdelivery.data.payment.PaymentGateway
import com.example.appdelivery.data.payment.PaymentMethod
import com.example.appdelivery.data.payment.PaymentRequest
import com.example.appdelivery.data.payment.PaymentResult
import com.example.appdelivery.data.payment.SimulatedPaymentGateway
import com.example.appdelivery.util.LuhnValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(app: Application) : AndroidViewModel(app) {

    private val gateway: PaymentGateway = SimulatedPaymentGateway()

    val selectedMethod = MutableStateFlow(PaymentMethod.CARD)
    val isProcessing = MutableStateFlow(false)
    val lastResult = MutableStateFlow<PaymentResult?>(null)

    var cardNumber by mutableStateOf("")
    var cardHolder by mutableStateOf("")
    var expiry by mutableStateOf("")
    var cvv by mutableStateOf("")
    var errors by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    fun setMethod(method: PaymentMethod) {
        selectedMethod.value = method
        errors = emptyMap()
    }

    fun dismissResult() {
        lastResult.value = null
    }

    fun validateCard(): Boolean {
        if (selectedMethod.value != PaymentMethod.CARD) return true
        val err = mutableMapOf<String, String>()
        val cleanCard = cardNumber.filter { it.isDigit() }
        if (!LuhnValidator.isValid(cleanCard)) {
            err["cardNumber"] = "Número de tarjeta inválido"
        }
        if (cardHolder.trim().length < 3) {
            err["cardHolder"] = "Nombre del titular requerido"
        }
        if (!expiry.matches(Regex("""^(0[1-9]|1[0-2])\/?([2-9][0-9])$"""))) {
            err["expiry"] = "Formato MM/AA inválido"
        }
        if (cvv.length !in 3..4 || !cvv.all { it.isDigit() }) {
            err["cvv"] = "CVV de 3 o 4 dígitos"
        }
        errors = err
        return err.isEmpty()
    }

    fun submitPayment(amount: Double, onPaymentSuccess: (PaymentResult.Success) -> Unit) {
        if (!validateCard()) return
        viewModelScope.launch {
            isProcessing.value = true
            val request = PaymentRequest(
                amount = amount,
                method = selectedMethod.value,
                cardNumber = cardNumber,
                cardHolder = cardHolder,
                expiry = expiry,
                cvv = cvv
            )
            val result = gateway.processPayment(request)
            isProcessing.value = false
            lastResult.value = result
            if (result is PaymentResult.Success) {
                onPaymentSuccess(result)
            }
        }
    }
}
