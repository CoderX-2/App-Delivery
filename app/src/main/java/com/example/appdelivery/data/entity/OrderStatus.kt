package com.example.appdelivery.data.entity

enum class OrderStatus(val displayName: String) {
    PENDING("Pendiente de preparación"),
    PREPARING("En preparación en restaurante"),
    PICKED_UP("Recogido por repartidor"),
    IN_DELIVERY("En camino al domicilio"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado");

    fun nextDeliveryStatus(): OrderStatus? = when (this) {
        PREPARING -> PICKED_UP
        PICKED_UP -> IN_DELIVERY
        IN_DELIVERY -> DELIVERED
        else -> null
    }

    fun actionLabel(): String? = when (this) {
        PREPARING -> "Marcar pedido como recogido"
        PICKED_UP -> "Iniciar viaje al domicilio"
        IN_DELIVERY -> "Confirmar entrega al cliente"
        else -> null
    }
}
