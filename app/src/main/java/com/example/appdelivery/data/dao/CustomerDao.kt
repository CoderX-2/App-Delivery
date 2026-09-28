package com.example.appdelivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.appdelivery.data.entity.Customer
import com.example.appdelivery.data.entity.CustomerWithAddresses
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Query("SELECT * FROM customers WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): Customer?

    @Query("SELECT * FROM customers WHERE id = :id")
    fun observeById(id: Long): Flow<Customer?>

    @Transaction
    @Query("SELECT * FROM customers WHERE id = :id")
    fun observeWithAddresses(id: Long): Flow<CustomerWithAddresses?>
}
