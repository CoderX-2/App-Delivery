package com.example.appdelivery.data.repository

import com.example.appdelivery.data.dao.CartDao
import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.entity.Dish
import kotlinx.coroutines.flow.Flow

class CartRepositoryImpl(private val dao: CartDao) : CartRepository {

    override fun observeCart(): Flow<List<CartItem>> = dao.observeAll()

    override suspend fun add(dish: Dish, restaurantName: String): AddResult {
        if (!dish.available) return AddResult.Unavailable
        val current = dao.getAll()
        if (current.isNotEmpty() && current.first().restaurantId != dish.restaurantId) {
            return AddResult.DifferentRestaurant(current.first().restaurantName)
        }
        val existing = dao.findByDish(dish.id)
        if (existing != null) dao.setQuantity(dish.id, existing.quantity + 1)
        else dao.upsert(newItem(dish, restaurantName))
        return AddResult.Added
    }

    override suspend fun replaceCartWith(dish: Dish, restaurantName: String) {
        dao.clear()
        dao.upsert(newItem(dish, restaurantName))
    }

    override suspend fun setQuantity(dishId: Long, quantity: Int) {
        if (quantity <= 0) dao.delete(dishId) else dao.setQuantity(dishId, quantity)
    }

    override suspend fun remove(dishId: Long) = dao.delete(dishId)

    override suspend fun clear() = dao.clear()

    private fun newItem(dish: Dish, restaurantName: String) = CartItem(
        dishId = dish.id,
        restaurantId = dish.restaurantId,
        restaurantName = restaurantName,
        name = dish.name,
        price = dish.price,
        quantity = 1
    )
}
