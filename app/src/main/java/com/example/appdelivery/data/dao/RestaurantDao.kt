package com.example.appdelivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.appdelivery.data.entity.Restaurant
import com.example.appdelivery.data.entity.RestaurantWithDishes
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Insert
    suspend fun insert(restaurant: Restaurant): Long

    @Query("SELECT * FROM restaurants ORDER BY name")
    fun observeAll(): Flow<List<Restaurant>>

    @Query("SELECT * FROM restaurants WHERE id = :id")
    suspend fun getById(id: Long): Restaurant?

    @Transaction
    @Query("SELECT * FROM restaurants WHERE id = :id")
    fun observeWithDishes(id: Long): Flow<RestaurantWithDishes?>
}
