package com.example.appdelivery.util

import com.example.appdelivery.data.entity.OrderStatus

object EstimatedTimeCalculator {
    fun calculate(status: OrderStatus): String = when (status) {
        OrderStatus.PENDING -> "35 - 45 min"
        OrderStatus.PREPARING -> "25 - 35 min"
        OrderStatus.PICKED_UP -> "15 - 20 min"
        OrderStatus.IN_DELIVERY -> "5 - 10 min"
        OrderStatus.DELIVERED -> "Entregado"
        OrderStatus.CANCELLED -> "Cancelado"
    }

    fun progressFraction(status: OrderStatus): Float = when (status) {
        OrderStatus.PENDING -> 0.1f
        OrderStatus.PREPARING -> 0.35f
        OrderStatus.PICKED_UP -> 0.65f
        OrderStatus.IN_DELIVERY -> 0.90f
        OrderStatus.DELIVERED -> 1.0f
        OrderStatus.CANCELLED -> 0.0f
    }
}
