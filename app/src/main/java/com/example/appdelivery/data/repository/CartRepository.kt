package com.example.appdelivery.data.repository

import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.entity.Dish
import kotlinx.coroutines.flow.Flow

sealed class AddResult {
    object Added : AddResult()
    object Unavailable : AddResult()
    data class DifferentRestaurant(val currentRestaurantName: String) : AddResult()
}

interface CartRepository {
    fun observeCart(): Flow<List<CartItem>>
    suspend fun add(dish: Dish, restaurantName: String): AddResult
    suspend fun replaceCartWith(dish: Dish, restaurantName: String)
    suspend fun setQuantity(dishId: Long, quantity: Int)
    suspend fun remove(dishId: Long)
    suspend fun clear()
}
