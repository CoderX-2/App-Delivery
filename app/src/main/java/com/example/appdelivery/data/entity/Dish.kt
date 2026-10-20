package com.example.appdelivery.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dishes",
    foreignKeys = [ForeignKey(
        entity = Restaurant::class,
        parentColumns = ["id"],
        childColumns = ["restaurantId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("restaurantId")]
)
data class Dish(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val restaurantId: Long,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val available: Boolean = true
)
