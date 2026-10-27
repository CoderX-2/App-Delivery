package com.example.appdelivery.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items", indices = [Index(value = ["dishId"], unique = true)])
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dishId: Long,
    val restaurantId: Long,
    val restaurantName: String,
    val name: String,
    val price: Double,
    val quantity: Int
)
