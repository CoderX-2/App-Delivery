package com.example.appdelivery.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "addresses",
    foreignKeys = [ForeignKey(
        entity = Customer::class,
        parentColumns = ["id"],
        childColumns = ["customerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("customerId")]
)
data class Address(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val label: String,
    val street: String,
    val city: String,
    val reference: String = ""
)
