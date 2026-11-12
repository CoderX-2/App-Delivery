package com.example.appdelivery.data.repository

import com.example.appdelivery.data.dao.CartDao
import com.example.appdelivery.data.dao.OrderDao
import com.example.appdelivery.data.dao.RefundDao
import com.example.appdelivery.data.entity.Order
import com.example.appdelivery.data.entity.OrderItem
import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.data.entity.OrderWithItems
import com.example.appdelivery.data.entity.RefundRequest
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class OrderRepositoryImpl(
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
    private val refundDao: RefundDao
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

    override fun observeOrder(orderId: String): Flow<OrderWithItems?> =
        orderDao.observeById(orderId)

    override fun observeAvailableOrders(): Flow<List<OrderWithItems>> =
        orderDao.observeAvailableOrders()

    override fun observeAssignedOrders(courierId: Long): Flow<List<OrderWithItems>> =
        orderDao.observeAssignedOrders(courierId)

    override fun observeCompletedDeliveries(courierId: Long): Flow<List<OrderWithItems>> =
        orderDao.observeCompletedDeliveries(courierId)

    override suspend fun acceptOrder(orderId: String, courierId: Long): Boolean {
        val rowsAffected = orderDao.assignCourier(orderId, courierId)
        return rowsAffected > 0
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        orderDao.updateStatus(orderId, newStatus)
    }

    override suspend fun cancelOrder(orderId: String, reason: String): Result<String> {
        val orderWithItems = orderDao.getById(orderId)
            ?: return Result.failure(IllegalArgumentException("Pedido no encontrado"))
        val order = orderWithItems.order

        // Regla AD-126: Solo se permite cancelar antes de que el repartidor recoja el pedido
        if (order.status != OrderStatus.PENDING && order.status != OrderStatus.PREPARING) {
            return Result.failure(IllegalStateException("No es posible cancelar: el pedido ya está en camino o fue entregado"))
        }

        orderDao.updateStatus(orderId, OrderStatus.CANCELLED.name)
        val refundCode = "REF-" + UUID.randomUUID().toString().take(8).uppercase()
        val refund = RefundRequest(
            orderId = orderId,
            refundCode = refundCode,
            customerId = order.customerId,
            amount = order.total,
            reason = reason,
            status = "APPROVED"
        )
        refundDao.insert(refund)
        return Result.success(refundCode)
    }

    override suspend fun getRefund(orderId: String): RefundRequest? =
        refundDao.findByOrderId(orderId)
}
