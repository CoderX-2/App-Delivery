package com.example.appdelivery.data.payment

import java.util.UUID
import kotlinx.coroutines.delay

enum class PaymentMethod(val displayName: String) {
    CASH("Efectivo al entregar"),
    CARD("Tarjeta de crédito / débito"),
    TRANSFER("Transferencia electrónica (SPEI)")
}

data class PaymentRequest(
    val amount: Double,
    val method: PaymentMethod,
    val cardNumber: String? = null,
    val cardHolder: String? = null,
    val expiry: String? = null,
    val cvv: String? = null
)

sealed class PaymentResult {
    data class Success(val transactionId: String, val method: PaymentMethod, val last4: String?) : PaymentResult()
    data class Failed(val reason: String) : PaymentResult()
}

interface PaymentGateway {
    suspend fun processPayment(request: PaymentRequest): PaymentResult
}

class SimulatedPaymentGateway : PaymentGateway {
    override suspend fun processPayment(request: PaymentRequest): PaymentResult {
        delay(400) // Simulación de latencia de red bancaria
        return when (request.method) {
            PaymentMethod.CASH -> PaymentResult.Success(
                transactionId = "CASH-${UUID.randomUUID().toString().take(8).uppercase()}",
                method = PaymentMethod.CASH,
                last4 = null
            )
            PaymentMethod.TRANSFER -> PaymentResult.Success(
                transactionId = "SPEI-${UUID.randomUUID().toString().take(8).uppercase()}",
                method = PaymentMethod.TRANSFER,
                last4 = null
            )
            PaymentMethod.CARD -> {
                val clean = request.cardNumber?.filter { it.isDigit() }.orEmpty()
                if (clean.endsWith("0000")) {
                    PaymentResult.Failed("Pago declinado por el banco: Fondos insuficientes o tarjeta bloqueada (terminación 0000)")
                } else {
                    PaymentResult.Success(
                        transactionId = "TX-${UUID.randomUUID().toString().take(8).uppercase()}",
                        method = PaymentMethod.CARD,
                        last4 = clean.takeLast(4)
                    )
                }
            }
        }
    }
}
