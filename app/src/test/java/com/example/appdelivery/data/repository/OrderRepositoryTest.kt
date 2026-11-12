package com.example.appdelivery.data.repository

import com.example.appdelivery.data.dao.CartDao
import com.example.appdelivery.data.dao.OrderDao
import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.entity.Order
import com.example.appdelivery.data.entity.OrderItem
import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.data.entity.OrderWithItems
import com.example.appdelivery.data.model.DeliveryMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeOrderDao : OrderDao {
    val orders = mutableListOf<Order>()
    val items = mutableListOf<OrderItem>()

    override suspend fun insertOrder(order: Order) { orders.add(order) }
    override suspend fun insertItems(items: List<OrderItem>) { this.items.addAll(items) }
    override fun observeByCustomer(customerId: Long): Flow<List<OrderWithItems>> =
        MutableStateFlow(orders.filter { it.customerId == customerId }.map { o ->
            OrderWithItems(o, items.filter { it.orderId == o.id })
        })
    override suspend fun getById(orderId: String): OrderWithItems? =
        orders.firstOrNull { it.id == orderId }?.let { o ->
            OrderWithItems(o, items.filter { it.orderId == o.id })
        }
    override suspend fun updateStatus(orderId: String, status: String) {
        val idx = orders.indexOfFirst { it.id == orderId }
        if (idx != -1) {
            orders[idx] = orders[idx].copy(status = OrderStatus.valueOf(status))
        }
    }
    override fun observeById(orderId: String): Flow<OrderWithItems?> = MutableStateFlow(orders.firstOrNull { it.id == orderId }?.let { OrderWithItems(it, emptyList()) })
    override fun observeAvailableOrders(): Flow<List<OrderWithItems>> = MutableStateFlow(emptyList())
    override fun observeAssignedOrders(courierId: Long): Flow<List<OrderWithItems>> = MutableStateFlow(emptyList())
    override suspend fun assignCourier(orderId: String, courierId: Long): Int = 1
}

private class FakeCartDaoForOrder : CartDao {
    val items = mutableListOf(
        CartItem(1, 10, 1, "R", "Tacos", 85.0, 2)
    )
    override fun observeAll(): Flow<List<CartItem>> = MutableStateFlow(items)
    override suspend fun getAll(): List<CartItem> = items
    override suspend fun findByDish(dishId: Long): CartItem? = items.firstOrNull { it.dishId == dishId }
    override suspend fun upsert(item: CartItem) { items.add(item) }
    override suspend fun setQuantity(dishId: Long, quantity: Int) {}
    override suspend fun delete(dishId: Long) {}
    override suspend fun clear() { items.clear() }
}

class OrderRepositoryTest {
    private val orderDao = FakeOrderDao()
    private val cartDao = FakeCartDaoForOrder()
    private val repo = OrderRepositoryImpl(orderDao, cartDao)

    @Test
    fun placeOrder_guardaOrdenYVaciaCarrito() = runTest {
        val order = Order(
            id = "ORD-123", customerId = 1L, restaurantId = 1L, restaurantName = "Tacos",
            deliveryMode = DeliveryMode.DELIVERY, deliveryAddress = "Casa",
            subtotal = 170.0, deliveryFee = 25.0, total = 195.0,
            paymentMethod = "Tarjeta", paymentTransactionId = "TX-1", status = OrderStatus.PENDING
        )
        val items = listOf(OrderItem(id = 1, orderId = "ORD-123", dishId = 10, name = "Tacos", price = 85.0, quantity = 2))

        val orderId = repo.placeOrder(order, items)

        assertEquals("ORD-123", orderId)
        assertEquals(1, orderDao.orders.size)
        assertEquals(1, orderDao.items.size)
        assertTrue(cartDao.items.isEmpty())
    }

    @Test
    fun getOrder_recuperaOrdenConItemsEInmutabilidad() = runTest {
        val order = Order(
            id = "ORD-456", customerId = 1L, restaurantId = 2L, restaurantName = "Pizza",
            deliveryMode = DeliveryMode.PICKUP, deliveryAddress = null,
            subtotal = 140.0, deliveryFee = 0.0, total = 140.0,
            paymentMethod = "Efectivo", paymentTransactionId = "CASH-1", status = OrderStatus.PENDING
        )
        val items = listOf(OrderItem(id = 2, orderId = "ORD-456", dishId = 20, name = "Pizza Margarita", price = 140.0, quantity = 1))
        repo.placeOrder(order, items)

        val retrieved = repo.getOrder("ORD-456")
        assertNotNull(retrieved)
        assertEquals(OrderStatus.PENDING, retrieved!!.order.status)
        assertEquals(1, retrieved.items.size)
        assertEquals("Pizza Margarita", retrieved.items[0].name)
        assertEquals(140.0, retrieved.items[0].price, 0.001)
    }
}
