package com.example.appdelivery.util

import com.example.appdelivery.data.entity.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationTextsTest {

    @Test
    fun textosDeNotificacion_cubrenTodosLosEstados() {
        val (titulo, msgRecogido) = NotificationHelper.notificationText("123", OrderStatus.PICKED_UP)
        assertEquals("Pedido #123", titulo)
        assertTrue(msgRecogido.contains("repartidor ya recogió"))

        val (_, msgCamino) = NotificationHelper.notificationText("123", OrderStatus.IN_DELIVERY)
        assertTrue(msgCamino.contains("en camino"))

        val (_, msgEntregado) = NotificationHelper.notificationText("123", OrderStatus.DELIVERED)
        assertTrue(msgEntregado.contains("ha sido entregado"))
    }
}
