package com.example.appdelivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appdelivery.data.entity.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(review: Review): Long

    @Query("SELECT * FROM reviews WHERE orderId = :orderId LIMIT 1")
    suspend fun findByOrderId(orderId: String): Review?

    @Query("SELECT * FROM reviews WHERE orderId = :orderId LIMIT 1")
    fun observeByOrderId(orderId: String): Flow<Review?>

    @Query("SELECT * FROM reviews WHERE restaurantId = :restaurantId ORDER BY createdAt DESC")
    fun observeByRestaurant(restaurantId: Long): Flow<List<Review>>

    @Query("SELECT AVG(restaurantRating) FROM reviews WHERE restaurantId = :restaurantId")
    suspend fun getAverageRestaurantRating(restaurantId: Long): Double?
}
