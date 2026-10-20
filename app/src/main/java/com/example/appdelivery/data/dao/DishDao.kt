package com.example.appdelivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appdelivery.data.entity.Dish
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Insert
    suspend fun insert(dish: Dish): Long

    @Query("SELECT * FROM dishes WHERE restaurantId = :restaurantId ORDER BY category, name")
    fun observeByRestaurant(restaurantId: Long): Flow<List<Dish>>
}
