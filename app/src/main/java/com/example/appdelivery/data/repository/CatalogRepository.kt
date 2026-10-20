package com.example.appdelivery.data.repository

import com.example.appdelivery.data.dao.DishDao
import com.example.appdelivery.data.dao.RestaurantDao
import com.example.appdelivery.data.entity.Dish
import com.example.appdelivery.data.entity.DishWithRestaurant
import com.example.appdelivery.data.entity.Restaurant
import com.example.appdelivery.data.entity.RestaurantWithDishes
import kotlinx.coroutines.flow.Flow

class CatalogRepository(
    private val restaurantDao: RestaurantDao,
    private val dishDao: DishDao
) {
    fun observeRestaurants(): Flow<List<Restaurant>> = restaurantDao.observeAll()

    fun observeMenu(restaurantId: Long): Flow<RestaurantWithDishes?> =
        restaurantDao.observeWithDishes(restaurantId)

    fun observeDishes(restaurantId: Long): Flow<List<Dish>> =
        dishDao.observeByRestaurant(restaurantId)

    fun observeCategories(): Flow<List<String>> = dishDao.observeCategories()

    fun searchDishes(
        query: String,
        category: String?,
        minPrice: Double,
        maxPrice: Double,
        onlyAvailable: Boolean
    ): Flow<List<DishWithRestaurant>> =
        dishDao.search(query, category, minPrice, maxPrice, onlyAvailable)
}
