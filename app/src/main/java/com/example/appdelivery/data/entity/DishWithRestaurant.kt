package com.example.appdelivery.data.entity

import androidx.room.Embedded

data class DishWithRestaurant(
    @Embedded val dish: Dish,
    val restaurantName: String
)
