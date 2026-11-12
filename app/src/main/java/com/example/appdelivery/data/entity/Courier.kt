package com.example.appdelivery.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class VehicleType(val displayName: String) {
    MOTORCYCLE("Motocicleta"),
    BICYCLE("Bicicleta"),
    CAR("Automóvil")
}

@Entity(
    tableName = "couriers",
    indices = [Index(value = ["email"], unique = true)]
)
data class Courier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    val vehicleType: VehicleType,
    val isAvailable: Boolean = true,
    val passwordHash: String,
    val salt: String
)
