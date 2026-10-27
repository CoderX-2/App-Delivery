package com.example.appdelivery.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.appdelivery.data.model.DeliveryMode

enum class OrderStatus(val displayName: String) {
    PENDING("Pendiente de preparación"),
    PREPARING("En preparación"),
    IN_DELIVERY("En camino"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado")
}

@Entity(
    tableName = "orders",
    foreignKeys = [ForeignKey(
        entity = Customer::class,
        parentColumns = ["id"],
        childColumns = ["customerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("customerId")]
)
data class Order(
    @PrimaryKey val id: String, // UUID
    val customerId: Long,
    val restaurantId: Long,
    val restaurantName: String,
    val deliveryMode: DeliveryMode,
    val deliveryAddress: String?,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val paymentMethod: String,
    val paymentTransactionId: String,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_items",
    foreignKeys = [ForeignKey(
        entity = Order::class,
        parentColumns = ["id"],
        childColumns = ["orderId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("orderId")]
)
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val dishId: Long,
    val name: String,
    val price: Double,
    val quantity: Int
)

data class OrderWithItems(
    @Embedded val order: Order,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val items: List<OrderItem>
)
