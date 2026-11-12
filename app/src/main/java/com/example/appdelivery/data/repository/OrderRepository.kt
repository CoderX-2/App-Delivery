package com.example.appdelivery.data.repository

import com.example.appdelivery.data.entity.Order
import com.example.appdelivery.data.entity.OrderItem
import com.example.appdelivery.data.entity.OrderWithItems
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun placeOrder(order: Order, items: List<OrderItem>): String
    fun observeOrders(customerId: Long): Flow<List<OrderWithItems>>
    suspend fun getOrder(orderId: String): OrderWithItems?
    
    // Repartidores (AD-18, AD-24)
    fun observeAvailableOrders(): Flow<List<OrderWithItems>>
    fun observeAssignedOrders(courierId: Long): Flow<List<OrderWithItems>>
    suspend fun acceptOrder(orderId: String, courierId: Long): Boolean
    suspend fun updateOrderStatus(orderId: String, newStatus: String)
}
