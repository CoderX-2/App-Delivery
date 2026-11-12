package com.example.appdelivery.ui.order

import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.util.EstimatedTimeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EstimatedTimeTest {

    @Test
    fun tiempoEstimado_correspondeAEtapasLogicas() {
        assertEquals("25 - 35 min", EstimatedTimeCalculator.calculate(OrderStatus.PREPARING))
        assertEquals("15 - 20 min", EstimatedTimeCalculator.calculate(OrderStatus.PICKED_UP))
        assertEquals("5 - 10 min", EstimatedTimeCalculator.calculate(OrderStatus.IN_DELIVERY))
        assertEquals("Entregado", EstimatedTimeCalculator.calculate(OrderStatus.DELIVERED))
        assertEquals("Cancelado", EstimatedTimeCalculator.calculate(OrderStatus.CANCELLED))
    }

    @Test
    fun fraccionDeProgreso_avanzaMonotonicamente() {
        val p1 = EstimatedTimeCalculator.progressFraction(OrderStatus.PREPARING)
        val p2 = EstimatedTimeCalculator.progressFraction(OrderStatus.PICKED_UP)
        val p3 = EstimatedTimeCalculator.progressFraction(OrderStatus.IN_DELIVERY)
        val p4 = EstimatedTimeCalculator.progressFraction(OrderStatus.DELIVERED)

        assertTrue(p1 < p2)
        assertTrue(p2 < p3)
        assertTrue(p3 < p4)
        assertEquals(1.0f, p4, 0.01f)
    }
}
