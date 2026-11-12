package com.example.appdelivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appdelivery.data.entity.RefundRequest

@Dao
interface RefundDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(refund: RefundRequest): Long

    @Query("SELECT * FROM refund_requests WHERE orderId = :orderId LIMIT 1")
    suspend fun findByOrderId(orderId: String): RefundRequest?
}
