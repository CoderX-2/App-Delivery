package com.example.appdelivery.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.appdelivery.data.entity.OrderStatus

object NotificationHelper {
    const val CHANNEL_ID = "delivery_orders_channel"
    private const val CHANNEL_NAME = "Actualizaciones de Pedidos"

    fun notificationText(orderId: String, status: OrderStatus): Pair<String, String> {
        val title = "Pedido #$orderId"
        val message = when (status) {
            OrderStatus.PENDING -> "Hemos recibido tu pedido y está pendiente de confirmación."
            OrderStatus.PREPARING -> "El restaurante está preparando tus platillos."
            OrderStatus.PICKED_UP -> "¡El repartidor ya recogió tu pedido en el restaurante!"
            OrderStatus.IN_DELIVERY -> "Tu pedido está en camino a tu domicilio."
            OrderStatus.DELIVERED -> "¡Tu pedido ha sido entregado! Que disfrutes tu comida."
            OrderStatus.CANCELLED -> "Tu pedido ha sido cancelado."
        }
        return Pair(title, message)
    }

    fun showOrderNotification(context: Context, orderId: String, status: OrderStatus) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre el estado y avance de pedidos"
            }
            manager.createNotificationChannel(channel)
        }

        val (title, message) = notificationText(orderId, status)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Usamos el hash del ID para el notificationId
        manager.notify(orderId.hashCode(), builder.build())
    }
}
