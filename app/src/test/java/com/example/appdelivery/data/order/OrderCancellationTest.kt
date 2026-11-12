package com.example.appdelivery.data.order

import com.example.appdelivery.data.dao.CartDao
import com.example.appdelivery.data.dao.OrderDao
import com.example.appdelivery.data.dao.RefundDao
import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.entity.Order
import com.example.appdelivery.data.entity.OrderItem
import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.data.entity.OrderWithItems
import com.example.appdelivery.data.entity.RefundRequest
import com.example.appdelivery.data.model.DeliveryMode
import com.example.appdelivery.data.repository.OrderRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeOrderDaoForCancel : OrderDao {
    val orders = mutableListOf<Order>()
    override suspend fun insertOrder(order: Order) { orders.add(order) }
    override suspend fun insertItems(items: List<OrderItem>) {}
    override fun observeByCustomer(customerId: Long): Flow<List<OrderWithItems>> = MutableStateFlow(emptyList())
    override suspend fun getById(orderId: String): OrderWithItems? = orders.firstOrNull { it.id == orderId }?.let { OrderWithItems(it, emptyList()) }
    override fun observeById(orderId: String): Flow<OrderWithItems?> = MutableStateFlow(null)
    override suspend fun updateStatus(orderId: String, status: String) {
        val idx = orders.indexOfFirst { it.id == orderId }
        if (idx != -1) orders[idx] = orders[idx].copy(status = OrderStatus.valueOf(status))
    }
    override fun observeAvailableOrders(): Flow<List<OrderWithItems>> = MutableStateFlow(emptyList())
    override fun observeAssignedOrders(courierId: Long): Flow<List<OrderWithItems>> = MutableStateFlow(emptyList())
    override fun observeCompletedDeliveries(courierId: Long): Flow<List<OrderWithItems>> = MutableStateFlow(emptyList())
    override suspend fun assignCourier(orderId: String, courierId: Long): Int = 1
}

private class FakeRefundDaoForCancel : RefundDao {
    val refunds = mutableListOf<RefundRequest>()
    override suspend fun insert(refund: RefundRequest): Long {
        refunds.add(refund)
        return refunds.size.toLong()
    }
    override suspend fun findByOrderId(orderId: String): RefundRequest? = refunds.firstOrNull { it.orderId == orderId }
}

private class FakeCartDummy : CartDao {
    override fun observeAll(): Flow<List<CartItem>> = MutableStateFlow(emptyList())
    override suspend fun getAll(): List<CartItem> = emptyList()
    override suspend fun findByDish(dishId: Long): CartItem? = null
    override suspend fun upsert(item: CartItem) {}
    override suspend fun setQuantity(dishId: Long, quantity: Int) {}
    override suspend fun delete(dishId: Long) {}
    override suspend fun clear() {}
}

class OrderCancellationTest {
    private val orderDao = FakeOrderDaoForCancel()
    private val refundDao = FakeRefundDaoForCancel()
    private val repo = OrderRepositoryImpl(orderDao, FakeCartDummy(), refundDao)

    private fun createOrder(id: String, status: OrderStatus) = Order(
        id = id, customerId = 1L, restaurantId = 1L, restaurantName = "R",
        deliveryMode = DeliveryMode.DELIVERY, deliveryAddress = "Casa",
        subtotal = 100.0, deliveryFee = 20.0, total = 120.0,
        paymentMethod = "Tarjeta", paymentTransactionId = "TX-1", status = status
    )

    @Test
    fun cancelarPedidoEnPreparacion_autorizaReembolsoYCambiaEstado() = runTest {
        orderDao.insertOrder(createOrder("ORD-CANCEL-1", OrderStatus.PREPARING))
        val res = repo.cancelOrder("ORD-CANCEL-1", "Demora en el restaurante")

        assertTrue(res.isSuccess)
        assertEquals(OrderStatus.CANCELLED, orderDao.orders.first().status)
        assertEquals(1, refundDao.refunds.size)
        assertEquals("ORD-CANCEL-1", refundDao.refunds.first().orderId)
        assertEquals(120.0, refundDao.refunds.first().amount, 0.001)
    }

    @Test
    fun cancelarPedidoYaRecogido_esRechazado() = runTest {
        orderDao.insertOrder(createOrder("ORD-CANCEL-2", OrderStatus.PICKED_UP))
        val res = repo.cancelOrder("ORD-CANCEL-2", "Cambié de opinión")

        assertTrue(res.isFailure)
        assertEquals(OrderStatus.PICKED_UP, orderDao.orders.first().status)
        assertTrue(refundDao.refunds.isEmpty())
    }
}
