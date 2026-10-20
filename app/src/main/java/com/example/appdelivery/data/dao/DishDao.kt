package com.example.appdelivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appdelivery.data.entity.Dish
import com.example.appdelivery.data.entity.DishWithRestaurant
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Insert
    suspend fun insert(dish: Dish): Long

    @Query("SELECT * FROM dishes WHERE restaurantId = :restaurantId ORDER BY category, name")
    fun observeByRestaurant(restaurantId: Long): Flow<List<Dish>>

    @Query("SELECT DISTINCT category FROM dishes ORDER BY category")
    fun observeCategories(): Flow<List<String>>

    @Query(
        """
        SELECT d.*, r.name AS restaurantName
        FROM dishes d
        INNER JOIN restaurants r ON r.id = d.restaurantId
        WHERE (:query = '' OR d.name LIKE '%' || :query || '%' OR d.description LIKE '%' || :query || '%')
          AND (:category IS NULL OR d.category = :category)
          AND d.price BETWEEN :minPrice AND :maxPrice
          AND (:onlyAvailable = 0 OR d.available = 1)
        ORDER BY d.name
        """
    )
    fun search(
        query: String,
        category: String?,
        minPrice: Double,
        maxPrice: Double,
        onlyAvailable: Boolean
    ): Flow<List<DishWithRestaurant>>
}
