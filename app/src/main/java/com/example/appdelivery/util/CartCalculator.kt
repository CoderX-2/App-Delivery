package com.example.appdelivery.util

import com.example.appdelivery.data.entity.Address
import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.entity.Restaurant
import com.example.appdelivery.data.model.DeliveryMode

data class CartTotals(
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val distanceKm: Double? = null
)

object CartCalculator {
    const val BASE_DELIVERY_FEE = 20.0
    const val FEE_PER_KM = 5.0

    fun calculate(
        items: List<CartItem>,
        mode: DeliveryMode,
        restaurant: Restaurant?,
        address: Address?
    ): CartTotals {
        val subtotal = items.sumOf { it.price * it.quantity }
        if (items.isEmpty()) {
            return CartTotals(subtotal = 0.0, deliveryFee = 0.0, total = 0.0, distanceKm = null)
        }

        if (mode == DeliveryMode.PICKUP) {
            return CartTotals(subtotal = subtotal, deliveryFee = 0.0, total = subtotal, distanceKm = null)
        }

        var distanceKm: Double? = null
        val fee = if (restaurant != null && address?.latitude != null && address?.longitude != null) {
            val dist = GeoUtils.distanceKm(
                GeoPoint(restaurant.latitude, restaurant.longitude),
                GeoPoint(address.latitude, address.longitude)
            )
            distanceKm = dist
            BASE_DELIVERY_FEE + (dist * FEE_PER_KM)
        } else {
            BASE_DELIVERY_FEE
        }

        return CartTotals(
            subtotal = subtotal,
            deliveryFee = fee,
            total = subtotal + fee,
            distanceKm = distanceKm
        )
    }
}
