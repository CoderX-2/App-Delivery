package com.example.appdelivery.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CustomerWithAddresses(
    @Embedded val customer: Customer,
    @Relation(parentColumn = "id", entityColumn = "customerId")
    val addresses: List<Address>
)
