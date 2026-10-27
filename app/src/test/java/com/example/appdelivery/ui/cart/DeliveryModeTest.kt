package com.example.appdelivery.ui.cart

import com.example.appdelivery.data.entity.Address
import com.example.appdelivery.data.model.DeliveryMode
import com.example.appdelivery.util.GeoPoint
import com.example.appdelivery.util.GeoUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeliveryModeTest {

    @Test
    fun deliveryMode_enumContieneValoresEsperados() {
        assertEquals(DeliveryMode.DELIVERY, DeliveryMode.valueOf("DELIVERY"))
        assertEquals(DeliveryMode.PICKUP, DeliveryMode.valueOf("PICKUP"))
    }

    @Test
    fun address_conCoordenadas_almacenaLatitudYLongitud() {
        val address = Address(
            id = 1L,
            customerId = 10L,
            label = "Casa",
            street = "Reforma 123",
            city = "Cuautitlán Izcalli",
            reference = "Portón negro",
            latitude = 19.6450,
            longitude = -99.2150
        )
        assertNotNull(address.latitude)
        assertNotNull(address.longitude)
        assertEquals(19.6450, address.latitude!!, 0.0001)
        assertEquals(-99.2150, address.longitude!!, 0.0001)
    }

    @Test
    fun calcularDistancia_entreRestauranteYDireccion_esMayorACero() {
        val restaurantCoord = GeoPoint(19.6490, -99.2130) // Tacos El Güero
        val addressCoord = GeoPoint(19.6450, -99.2150)    // Casa de prueba
        val distance = GeoUtils.distanceKm(restaurantCoord, addressCoord)
        assertTrue(distance > 0.0)
        assertTrue(distance < 5.0) // Cercano en Izcalli
    }
}
