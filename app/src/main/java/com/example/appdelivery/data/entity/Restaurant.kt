package com.example.appdelivery.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val cuisine: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)
