package com.example.appdelivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appdelivery.data.entity.CartItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY id")
    fun observeAll(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items ORDER BY id")
    suspend fun getAll(): List<CartItem>

    @Query("SELECT * FROM cart_items WHERE dishId = :dishId LIMIT 1")
    suspend fun findByDish(dishId: Long): CartItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItem)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE dishId = :dishId")
    suspend fun setQuantity(dishId: Long, quantity: Int)

    @Query("DELETE FROM cart_items WHERE dishId = :dishId")
    suspend fun delete(dishId: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}
