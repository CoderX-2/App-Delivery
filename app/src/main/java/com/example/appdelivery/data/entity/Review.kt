package com.example.appdelivery.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    foreignKeys = [ForeignKey(
        entity = Order::class,
        parentColumns = ["id"],
        childColumns = ["orderId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["orderId"], unique = true), Index("restaurantId"), Index("courierId")]
)
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val customerId: Long,
    val restaurantId: Long,
    val courierId: Long?,
    val restaurantRating: Int, // 1..5
    val restaurantComment: String = "",
    val courierRating: Int? = null, // 1..5
    val courierComment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
