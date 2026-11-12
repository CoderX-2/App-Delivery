package com.example.appdelivery.data.review

import com.example.appdelivery.data.dao.OrderDao
import com.example.appdelivery.data.dao.ReviewDao
import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.data.entity.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    suspend fun submitReview(
        orderId: String,
        restaurantRating: Int,
        restaurantComment: String,
        courierRating: Int?,
        courierComment: String
    ): Result<Long>

    fun observeReviewByOrder(orderId: String): Flow<Review?>
    fun observeRestaurantReviews(restaurantId: Long): Flow<List<Review>>
    suspend fun getAverageRestaurantRating(restaurantId: Long): Double?
}

class ReviewRepositoryImpl(
    private val reviewDao: ReviewDao,
    private val orderDao: OrderDao
) : ReviewRepository {

    override suspend fun submitReview(
        orderId: String,
        restaurantRating: Int,
        restaurantComment: String,
        courierRating: Int?,
        courierComment: String
    ): Result<Long> {
        val orderWithItems = orderDao.getById(orderId)
            ?: return Result.failure(IllegalArgumentException("Pedido no encontrado"))
        val order = orderWithItems.order

        // Regla AD-113: Solo se puede calificar si está entregado
        if (order.status != OrderStatus.DELIVERED) {
            return Result.failure(IllegalStateException("Solo se pueden calificar pedidos entregados"))
        }

        if (reviewDao.findByOrderId(orderId) != null) {
            return Result.failure(IllegalStateException("Este pedido ya fue calificado"))
        }

        val review = Review(
            orderId = orderId,
            customerId = order.customerId,
            restaurantId = order.restaurantId,
            courierId = order.courierId,
            restaurantRating = restaurantRating.coerceIn(1, 5),
            restaurantComment = restaurantComment.trim(),
            courierRating = courierRating?.coerceIn(1, 5),
            courierComment = courierComment.trim()
        )

        val id = reviewDao.insert(review)
        return Result.success(id)
    }

    override fun observeReviewByOrder(orderId: String): Flow<Review?> =
        reviewDao.observeByOrderId(orderId)

    override fun observeRestaurantReviews(restaurantId: Long): Flow<List<Review>> =
        reviewDao.observeByRestaurant(restaurantId)

    override suspend fun getAverageRestaurantRating(restaurantId: Long): Double? =
        reviewDao.getAverageRestaurantRating(restaurantId)
}
