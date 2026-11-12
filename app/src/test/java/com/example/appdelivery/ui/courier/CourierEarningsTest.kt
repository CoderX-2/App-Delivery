package com.example.appdelivery.ui.courier

import com.example.appdelivery.data.entity.Order
import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.data.model.DeliveryMode
import com.example.appdelivery.util.CourierEarningsCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class CourierEarningsTest {

    private fun order(id: String, status: OrderStatus, fee: Double, createdAt: Long) = Order(
        id = id, customerId = 1L, courierId = 10L, restaurantId = 1L,
        restaurantName = "Tacos", deliveryMode = DeliveryMode.DELIVERY,
        deliveryAddress = "Casa", subtotal = 100.0, deliveryFee = fee, total = 100.0 + fee,
        paymentMethod = "Tarjeta", paymentTransactionId = "TX-1", status = status,
        createdAt = createdAt
    )

    @Test
    fun calcularGanancias_soloSumaPedidosEntregados() {
        val now = System.currentTimeMillis()
        val orders = listOf(
            order("O1", OrderStatus.DELIVERED, 35.0, now),
            order("O2", OrderStatus.DELIVERED, 40.0, now),
            order("O3", OrderStatus.IN_DELIVERY, 50.0, now), // En camino: NO debe sumar
            order("O4", OrderStatus.CANCELLED, 30.0, now)    // Cancelado: NO debe sumar
        )

        val summary = CourierEarningsCalculator.calculate(orders, now)

        assertEquals(2, summary.completedCount)
        assertEquals(75.0, summary.todayTotal, 0.001)
        assertEquals(75.0, summary.weekTotal, 0.001)
        assertEquals(75.0, summary.allTimeTotal, 0.001)
    }

    @Test
    fun calcularGanancias_separaIngresosPorFecha() {
        val now = System.currentTimeMillis()
        val oneDay = 24L * 60 * 60 * 1000
        val tenDaysAgo = now - (10L * oneDay)

        val orders = listOf(
            order("O_HOY", OrderStatus.DELIVERED, 50.0, now),
            order("O_ANTIGUO", OrderStatus.DELIVERED, 80.0, tenDaysAgo)
        )

        val summary = CourierEarningsCalculator.calculate(orders, now)

        assertEquals(2, summary.completedCount)
        assertEquals(50.0, summary.todayTotal, 0.001)
        assertEquals(50.0, summary.weekTotal, 0.001)
        assertEquals(130.0, summary.allTimeTotal, 0.001)
    }
}
