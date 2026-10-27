package com.example.appdelivery.ui.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdelivery.data.db.AppDatabase
import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.entity.Dish
import com.example.appdelivery.data.repository.AddResult
import com.example.appdelivery.data.repository.CartRepository
import com.example.appdelivery.data.repository.CartRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PendingAdd(val dish: Dish, val restaurantName: String, val currentRestaurantName: String)

class CartViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: CartRepository = CartRepositoryImpl(AppDatabase.getInstance(app).cartDao())

    val items: StateFlow<List<CartItem>> = repo.observeCart()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val message = MutableStateFlow<String?>(null)
    val conflict = MutableStateFlow<PendingAdd?>(null)

    fun add(dish: Dish, restaurantName: String) {
        viewModelScope.launch {
            when (val r = repo.add(dish, restaurantName)) {
                AddResult.Added -> message.value = "Agregado: ${dish.name}"
                AddResult.Unavailable -> message.value = "Platillo no disponible"
                is AddResult.DifferentRestaurant ->
                    conflict.value = PendingAdd(dish, restaurantName, r.currentRestaurantName)
            }
        }
    }

    fun confirmReplace() {
        val p = conflict.value ?: return
        conflict.value = null
        viewModelScope.launch {
            repo.replaceCartWith(p.dish, p.restaurantName)
            message.value = "Carrito nuevo: ${p.dish.name}"
        }
    }

    fun dismissConflict() { conflict.value = null }
    fun clearMessage() { message.value = null }
    fun increment(item: CartItem) { viewModelScope.launch { repo.setQuantity(item.dishId, item.quantity + 1) } }
    fun decrement(item: CartItem) { viewModelScope.launch { repo.setQuantity(item.dishId, item.quantity - 1) } }
    fun remove(item: CartItem) { viewModelScope.launch { repo.remove(item.dishId) } }
    fun clear() { viewModelScope.launch { repo.clear() } }
}
