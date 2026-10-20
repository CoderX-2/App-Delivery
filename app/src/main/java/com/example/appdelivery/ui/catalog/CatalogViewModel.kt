package com.example.appdelivery.ui.catalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdelivery.data.db.AppDatabase
import com.example.appdelivery.data.entity.Restaurant
import com.example.appdelivery.data.entity.RestaurantWithDishes
import com.example.appdelivery.data.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CatalogViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    private val repo = CatalogRepository(db.restaurantDao(), db.dishDao())

    val restaurants: StateFlow<List<Restaurant>> = repo.observeRestaurants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeMenu(restaurantId: Long): Flow<RestaurantWithDishes?> = repo.observeMenu(restaurantId)
}
