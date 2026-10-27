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
        cartDao.clear() // Vacía el carrito atómicamente tras registrar el pedido (AD-86)
        return order.id
    }

    override fun observeOrders(customerId: Long): Flow<List<OrderWithItems>> =
        orderDao.observeByCustomer(customerId)

    override suspend fun getOrder(orderId: String): OrderWithItems? =
        orderDao.getById(orderId)
}
