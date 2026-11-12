package com.example.appdelivery.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "refund_requests",
    foreignKeys = [ForeignKey(
        entity = Order::class,
        parentColumns = ["id"],
        childColumns = ["orderId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["orderId"], unique = true)]
)
data class RefundRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val refundCode: String,
    val customerId: Long,
    val amount: Double,
    val reason: String,
    val status: String = "APPROVED",
    val createdAt: Long = System.currentTimeMillis()
)
