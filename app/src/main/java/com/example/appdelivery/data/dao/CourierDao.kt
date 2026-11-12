package com.example.appdelivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appdelivery.data.entity.Courier
import kotlinx.coroutines.flow.Flow

@Dao
interface CourierDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(courier: Courier): Long

    @Query("SELECT * FROM couriers WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): Courier?

    @Query("SELECT * FROM couriers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Courier?

    @Query("SELECT * FROM couriers WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<Courier?>

    @Query("UPDATE couriers SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun setAvailability(id: Long, isAvailable: Boolean)
}
