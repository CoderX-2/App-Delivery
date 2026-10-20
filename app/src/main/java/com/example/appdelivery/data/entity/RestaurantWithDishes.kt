package com.example.appdelivery.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RestaurantWithDishes(
    @Embedded val restaurant: Restaurant,
    @Relation(parentColumn = "id", entityColumn = "restaurantId")
    val dishes: List<Dish>
)
