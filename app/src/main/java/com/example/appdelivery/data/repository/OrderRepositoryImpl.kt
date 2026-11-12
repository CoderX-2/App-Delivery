package com.example.appdelivery.data.repository

import com.example.appdelivery.data.dao.CartDao
import com.example.appdelivery.data.dao.OrderDao
import com.example.appdelivery.data.entity.Order
import com.example.appdelivery.data.entity.OrderItem
import com.example.appdelivery.data.entity.OrderWithItems
import kotlinx.coroutines.flow.Flow

class OrderRepositoryImpl(
    private val orderDao: OrderDao,
    private val cartDao: CartDao
) : OrderRepository {

    override suspend fun placeOrder(order: Order, items: List<OrderItem>): String {
        orderDao.insertFullOrder(order, items)
        cartDao.clear()
        return order.id
    }

    override fun observeOrders(customerId: Long): Flow<List<OrderWithItems>> =
        orderDao.observeByCustomer(customerId)

    override suspend fun getOrder(orderId: String): OrderWithItems? =
        orderDao.getById(orderId)

    override fun observeAvailableOrders(): Flow<List<OrderWithItems>> =
        orderDao.observeAvailableOrders()

    override fun observeAssignedOrders(courierId: Long): Flow<List<OrderWithItems>> =
        orderDao.observeAssignedOrders(courierId)

    override suspend fun acceptOrder(orderId: String, courierId: Long): Boolean {
        val rowsAffected = orderDao.assignCourier(orderId, courierId)
        return rowsAffected > 0
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        orderDao.updateStatus(orderId, newStatus)
    }
}
