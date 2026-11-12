package com.example.appdelivery.data.repository

import com.example.appdelivery.data.entity.Order
import com.example.appdelivery.data.entity.OrderItem
import com.example.appdelivery.data.entity.OrderWithItems
import com.example.appdelivery.data.entity.RefundRequest
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun placeOrder(order: Order, items: List<OrderItem>): String
    fun observeOrders(customerId: Long): Flow<List<OrderWithItems>>
    suspend fun getOrder(orderId: String): OrderWithItems?
    fun observeOrder(orderId: String): Flow<OrderWithItems?>
    
    fun observeAvailableOrders(): Flow<List<OrderWithItems>>
    fun observeAssignedOrders(courierId: Long): Flow<List<OrderWithItems>>
    suspend fun acceptOrder(orderId: String, courierId: Long): Boolean
    suspend fun updateOrderStatus(orderId: String, newStatus: String)
    
    // Cancelaciones y reembolsos (AD-126, AD-129)
    suspend fun cancelOrder(orderId: String, reason: String): Result<String>
    suspend fun getRefund(orderId: String): RefundRequest?
}
