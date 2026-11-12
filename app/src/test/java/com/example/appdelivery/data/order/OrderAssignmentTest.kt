package com.example.appdelivery.data.order

import com.example.appdelivery.data.dao.CartDao
import com.example.appdelivery.data.dao.OrderDao
import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.entity.Order
import com.example.appdelivery.data.entity.OrderItem
import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.data.entity.OrderWithItems
import com.example.appdelivery.data.model.DeliveryMode
import com.example.appdelivery.data.repository.OrderRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeOrderDaoForAssignment : OrderDao {
    val orders = mutableListOf<Order>()
    val items = mutableListOf<OrderItem>()
    val availableFlow = MutableStateFlow<List<OrderWithItems>>(emptyList())

    override suspend fun insertOrder(order: Order) {
        orders.add(order)
        refresh()
    }
    override suspend fun insertItems(items: List<OrderItem>) { this.items.addAll(items) }
    override fun observeByCustomer(customerId: Long): Flow<List<OrderWithItems>> = MutableStateFlow(emptyList())
    override suspend fun getById(orderId: String): OrderWithItems? = orders.firstOrNull { it.id == orderId }?.let { OrderWithItems(it, emptyList()) }
    override suspend fun updateStatus(orderId: String, status: String) {}

    override fun observeById(orderId: String): Flow<OrderWithItems?> = MutableStateFlow(orders.firstOrNull { it.id == orderId }?.let { OrderWithItems(it, emptyList()) })
    override fun observeAvailableOrders(): Flow<List<OrderWithItems>> = availableFlow
    override fun observeAssignedOrders(courierId: Long): Flow<List<OrderWithItems>> = MutableStateFlow(emptyList())

    override suspend fun assignCourier(orderId: String, courierId: Long): Int {
        val idx = orders.indexOfFirst { it.id == orderId }
        if (idx != -1 && orders[idx].courierId == null) {
            orders[idx] = orders[idx].copy(courierId = courierId, status = OrderStatus.PREPARING)
            refresh()
            return 1
        }
        return 0
    }

    private fun refresh() {
        availableFlow.value = orders
            .filter { it.deliveryMode == DeliveryMode.DELIVERY && it.courierId == null }
            .map { OrderWithItems(it, emptyList()) }
    }
}

private class FakeCartDaoDummy : CartDao {
    override fun observeAll(): Flow<List<CartItem>> = MutableStateFlow(emptyList())
    override suspend fun getAll(): List<CartItem> = emptyList()
    override suspend fun findByDish(dishId: Long): CartItem? = null
    override suspend fun upsert(item: CartItem) {}
    override suspend fun setQuantity(dishId: Long, quantity: Int) {}
    override suspend fun delete(dishId: Long) {}
    override suspend fun clear() {}
}

class OrderAssignmentTest {
    private val orderDao = FakeOrderDaoForAssignment()
    private val repo = OrderRepositoryImpl(orderDao, FakeCartDaoDummy())

    private fun sampleOrder(id: String, courierId: Long? = null) = Order(
        id = id, customerId = 1L, courierId = courierId, restaurantId = 1L,
        restaurantName = "Tacos", deliveryMode = DeliveryMode.DELIVERY,
        deliveryAddress = "Calle 1", subtotal = 100.0, deliveryFee = 25.0,
        total = 125.0, paymentMethod = "Efectivo", paymentTransactionId = "CASH-1",
        status = OrderStatus.PENDING
    )

    @Test
    fun aceptarPedido_sinRepartidor_asignaExitosamente() = runTest {
        orderDao.insertOrder(sampleOrder("ORD-001"))
        val accepted = repo.acceptOrder("ORD-001", courierId = 10L)
        assertTrue(accepted)
        assertEquals(10L, orderDao.orders.first().courierId)
        assertEquals(OrderStatus.PREPARING, orderDao.orders.first().status)
    }

    @Test
    fun aceptarPedido_yaTomadoPorOtroRepartidor_fallaYEvitaDobleAsignacion() = runTest {
        orderDao.insertOrder(sampleOrder("ORD-002"))
        val primerRepartidor = repo.acceptOrder("ORD-002", courierId = 10L)
        assertTrue(primerRepartidor)

        // Segundo repartidor intenta tomar el mismo pedido (AD-98)
        val segundoRepartidor = repo.acceptOrder("ORD-002", courierId = 20L)
        assertFalse(segundoRepartidor)
        assertEquals(10L, orderDao.orders.first().courierId)
    }
}
