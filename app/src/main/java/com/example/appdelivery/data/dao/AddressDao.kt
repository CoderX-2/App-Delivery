package com.example.appdelivery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.appdelivery.data.entity.Address
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Insert
    suspend fun insert(address: Address): Long

    @Update
    suspend fun update(address: Address)

    @Delete
    suspend fun delete(address: Address)

    @Query("SELECT * FROM addresses WHERE customerId = :customerId ORDER BY id DESC")
    fun observeByCustomer(customerId: Long): Flow<List<Address>>

    @Query("SELECT * FROM addresses WHERE id = :id")
    suspend fun getById(id: Long): Address?
}
