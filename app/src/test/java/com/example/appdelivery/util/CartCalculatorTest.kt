package com.example.appdelivery.util

import com.example.appdelivery.data.entity.Address
import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.entity.Restaurant
import com.example.appdelivery.data.model.DeliveryMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CartCalculatorTest {

    private fun item(id: Long, price: Double, qty: Int) = CartItem(
        id = id, dishId = id, restaurantId = 1L, restaurantName = "R",
        name = "Platillo $id", price = price, quantity = qty
    )

    private val restaurant = Restaurant(
        id = 1L, name = "Tacos El Güero", description = "d", cuisine = "c",
        address = "Av. Hidalgo 12", latitude = 19.6490, longitude = -99.2130
    )

    private val addressWithCoords = Address(
        id = 1L, customerId = 1L, label = "Casa", street = "Reforma 123",
        city = "Izcalli", latitude = 19.6450, longitude = -99.2150
    )

    private val addressWithoutCoords = Address(
        id = 2L, customerId = 1L, label = "Oficina", street = "Juarez 1",
        city = "Izcalli", latitude = null, longitude = null
    )

    @Test
    fun carritoVacio_todosLosTotalesSonCero() {
        val totals = CartCalculator.calculate(emptyList(), DeliveryMode.DELIVERY, restaurant, addressWithCoords)
        assertEquals(0.0, totals.subtotal, 0.001)
        assertEquals(0.0, totals.deliveryFee, 0.001)
        assertEquals(0.0, totals.total, 0.001)
        assertNull(totals.distanceKm)
    }

    @Test
    fun modalidadRecogida_costoEnvioEsCeroYTotalIgualSubtotal() {
        val items = listOf(item(1, 100.0, 2), item(2, 50.0, 1)) // 250.0
        val totals = CartCalculator.calculate(items, DeliveryMode.PICKUP, restaurant, addressWithCoords)
        assertEquals(250.0, totals.subtotal, 0.001)
        assertEquals(0.0, totals.deliveryFee, 0.001)
        assertEquals(250.0, totals.total, 0.001)
    }

    @Test
    fun modalidadEntrega_conCoordenadas_calculaBaseMasDistancia() {
        val items = listOf(item(1, 100.0, 1)) // Subtotal 100
        val totals = CartCalculator.calculate(items, DeliveryMode.DELIVERY, restaurant, addressWithCoords)
        assertEquals(100.0, totals.subtotal, 0.001)
        assertTrue(totals.deliveryFee > 20.0) // Mayor a base por los km calculados
        assertEquals(totals.subtotal + totals.deliveryFee, totals.total, 0.001)
        assertTrue(totals.distanceKm != null && totals.distanceKm!! > 0)
    }

    @Test
    fun modalidadEntrega_sinCoordenadas_aplicaTarifaBase() {
        val items = listOf(item(1, 80.0, 1))
        val totals = CartCalculator.calculate(items, DeliveryMode.DELIVERY, restaurant, addressWithoutCoords)
        assertEquals(80.0, totals.subtotal, 0.001)
        assertEquals(20.0, totals.deliveryFee, 0.001)
        assertEquals(100.0, totals.total, 0.001)
    }
}
