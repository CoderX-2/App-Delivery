package com.example.appdelivery.data.review

import com.example.appdelivery.data.dao.OrderDao
import com.example.appdelivery.data.dao.ReviewDao
import com.example.appdelivery.data.entity.Order
import com.example.appdelivery.data.entity.OrderItem
import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.data.entity.OrderWithItems
import com.example.appdelivery.data.entity.Review
import com.example.appdelivery.data.model.DeliveryMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeReviewDao : ReviewDao {
    val reviews = mutableListOf<Review>()
    override suspend fun insert(review: Review): Long {
        reviews.add(review)
        return reviews.size.toLong()
    }
    override suspend fun findByOrderId(orderId: String): Review? = reviews.firstOrNull { it.orderId == orderId }
    override fun observeByOrderId(orderId: String): Flow<Review?> = MutableStateFlow(null)
    override fun observeByRestaurant(restaurantId: Long): Flow<List<Review>> = MutableStateFlow(reviews.filter { it.restaurantId == restaurantId })
    override suspend fun getAverageRestaurantRating(restaurantId: Long): Double? {
        val matches = reviews.filter { it.restaurantId == restaurantId }
        return if (matches.isEmpty()) null else matches.map { it.restaurantRating }.average()
    }
}

private class FakeOrderDaoForReview : OrderDao {
    val orders = mutableListOf<Order>()
    override suspend fun insertOrder(order: Order) { orders.add(order) }
    override suspend fun insertItems(items: List<OrderItem>) {}
    override fun observeByCustomer(customerId: Long): Flow<List<OrderWithItems>> = MutableStateFlow(emptyList())
    override suspend fun getById(orderId: String): OrderWithItems? = orders.firstOrNull { it.id == orderId }?.let { OrderWithItems(it, emptyList()) }
    override fun observeById(orderId: String): Flow<OrderWithItems?> = MutableStateFlow(null)
    override suspend fun updateStatus(orderId: String, status: String) {}
    override fun observeAvailableOrders(): Flow<List<OrderWithItems>> = MutableStateFlow(emptyList())
    override fun observeAssignedOrders(courierId: Long): Flow<List<OrderWithItems>> = MutableStateFlow(emptyList())
    override suspend fun assignCourier(orderId: String, courierId: Long): Int = 1
}

class ReviewRepositoryTest {
    private val reviewDao = FakeReviewDao()
    private val orderDao = FakeOrderDaoForReview()
    private val repo = ReviewRepositoryImpl(reviewDao, orderDao)

    private fun createOrder(id: String, status: OrderStatus) = Order(
        id = id, customerId = 1L, courierId = 10L, restaurantId = 5L, restaurantName = "R",
        deliveryMode = DeliveryMode.DELIVERY, deliveryAddress = "Casa",
        subtotal = 100.0, deliveryFee = 20.0, total = 120.0,
        paymentMethod = "Tarjeta", paymentTransactionId = "TX-1", status = status
    )

    @Test
    fun calificarPedidoEntregado_exitoso() = runTest {
        orderDao.insertOrder(createOrder("ORD-REV-1", OrderStatus.DELIVERED))
        val res = repo.submitReview("ORD-REV-1", 5, "Excelente comida", 4, "Rápido")

        assertTrue(res.isSuccess)
        assertEquals(1, reviewDao.reviews.size)
        assertEquals(5, reviewDao.reviews.first().restaurantRating)
        assertEquals(4, reviewDao.reviews.first().courierRating)
    }

    @Test
    fun calificarPedidoNoEntregado_esRechazado() = runTest {
        orderDao.insertOrder(createOrder("ORD-REV-2", OrderStatus.IN_DELIVERY))
        val res = repo.submitReview("ORD-REV-2", 5, "Bien", 5, "Bien")

        assertTrue(res.isFailure)
        assertTrue(reviewDao.reviews.isEmpty())
    }

    @Test
    fun calificarMismoPedidoDosVeces_esRechazado() = runTest {
        orderDao.insertOrder(createOrder("ORD-REV-3", OrderStatus.DELIVERED))
        repo.submitReview("ORD-REV-3", 4, "Ok", null, "")
        val segundaCalificacion = repo.submitReview("ORD-REV-3", 5, "Mejor", null, "")

        assertTrue(segundaCalificacion.isFailure)
        assertEquals(1, reviewDao.reviews.size)
    }
}
