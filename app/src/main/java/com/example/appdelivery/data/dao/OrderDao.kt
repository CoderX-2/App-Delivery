package com.example.appdelivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.appdelivery.data.entity.Order
import com.example.appdelivery.data.entity.OrderItem
import com.example.appdelivery.data.entity.OrderWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<OrderItem>)

    @Transaction
    suspend fun insertFullOrder(order: Order, items: List<OrderItem>) {
        insertOrder(order)
        insertItems(items)
    }

    @Transaction
    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun observeByCustomer(customerId: Long): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getById(orderId: String): OrderWithItems?

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateStatus(orderId: String, status: String)

    // Consultas para repartidores (AD-18, AD-24)
    @Transaction
    @Query("SELECT * FROM orders WHERE deliveryMode = 'DELIVERY' AND courierId IS NULL AND status IN ('PENDING', 'PREPARING') ORDER BY createdAt DESC")
    fun observeAvailableOrders(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE courierId = :courierId AND status NOT IN ('DELIVERED', 'CANCELLED') ORDER BY createdAt DESC")
    fun observeAssignedOrders(courierId: Long): Flow<List<OrderWithItems>>

    @Query("UPDATE orders SET courierId = :courierId, status = 'PREPARING' WHERE id = :orderId AND courierId IS NULL")
    suspend fun assignCourier(orderId: String, courierId: Long): Int
}
