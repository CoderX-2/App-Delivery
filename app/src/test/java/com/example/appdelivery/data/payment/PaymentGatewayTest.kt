package com.example.appdelivery.data.payment

import com.example.appdelivery.util.LuhnValidator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentGatewayTest {

    private val gateway = SimulatedPaymentGateway()

    @Test
    fun luhn_tarjetasValidas_retornanTrue() {
        assertTrue(LuhnValidator.isValid("4532015112830366"))
        assertTrue(LuhnValidator.isValid("4532 0151 1283 0366"))
    }

    @Test
    fun luhn_tarjetaInvalida_retornaFalse() {
        assertFalse(LuhnValidator.isValid("4532015112830367"))
        assertFalse(LuhnValidator.isValid("1234"))
    }

    @Test
    fun pagoEfectivo_siempreEsExitoso() = runTest {
        val req = PaymentRequest(amount = 150.0, method = PaymentMethod.CASH)
        val res = gateway.processPayment(req)
        assertTrue(res is PaymentResult.Success)
        assertEquals(PaymentMethod.CASH, (res as PaymentResult.Success).method)
    }

    @Test
    fun pagoTransferencia_siempreEsExitoso() = runTest {
        val req = PaymentRequest(amount = 220.0, method = PaymentMethod.TRANSFER)
        val res = gateway.processPayment(req)
        assertTrue(res is PaymentResult.Success)
        assertEquals(PaymentMethod.TRANSFER, (res as PaymentResult.Success).method)
    }

    @Test
    fun pagoTarjetaAprobada_devuelveTransaccionYUltimos4() = runTest {
        val req = PaymentRequest(
            amount = 300.0,
            method = PaymentMethod.CARD,
            cardNumber = "4532015112830366"
        )
        val res = gateway.processPayment(req)
        assertTrue(res is PaymentResult.Success)
        val success = res as PaymentResult.Success
        assertEquals("0366", success.last4)
    }

    @Test
    fun pagoTarjetaTerminadaEn0000_esRechazada() = runTest {
        val req = PaymentRequest(
            amount = 300.0,
            method = PaymentMethod.CARD,
            cardNumber = "4000000000000000"
        )
        val res = gateway.processPayment(req)
        assertTrue(res is PaymentResult.Failed)
    }
}
