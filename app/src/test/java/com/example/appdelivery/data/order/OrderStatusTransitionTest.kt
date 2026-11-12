package com.example.appdelivery.data.order

import com.example.appdelivery.data.entity.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OrderStatusTransitionTest {

    @Test
    fun flujoDeEstados_avanzaEnOrdenPermitido() {
        val inicial = OrderStatus.PREPARING
        val recogido = inicial.nextDeliveryStatus()
        assertEquals(OrderStatus.PICKED_UP, recogido)

        val enCamino = recogido?.nextDeliveryStatus()
        assertEquals(OrderStatus.IN_DELIVERY, enCamino)

        val entregado = enCamino?.nextDeliveryStatus()
        assertEquals(OrderStatus.DELIVERED, entregado)

        // Estado terminal no avanza más
        val despuesDeEntregado = entregado?.nextDeliveryStatus()
        assertNull(despuesDeEntregado)
    }

    @Test
    fun etiquetasDeAccion_coincidenConCadaEstado() {
        assertEquals("Marcar pedido como recogido", OrderStatus.PREPARING.actionLabel())
        assertEquals("Iniciar viaje al domicilio", OrderStatus.PICKED_UP.actionLabel())
        assertEquals("Confirmar entrega al cliente", OrderStatus.IN_DELIVERY.actionLabel())
        assertNull(OrderStatus.DELIVERED.actionLabel())
    }
}
