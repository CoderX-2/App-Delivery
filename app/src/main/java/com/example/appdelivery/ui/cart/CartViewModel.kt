package com.example.appdelivery.ui.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdelivery.data.db.AppDatabase
import com.example.appdelivery.data.entity.Address
import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.entity.Dish
import com.example.appdelivery.data.entity.Restaurant
import com.example.appdelivery.data.model.DeliveryMode
import com.example.appdelivery.data.repository.AddResult
import com.example.appdelivery.data.repository.CartRepository
import com.example.appdelivery.data.repository.CartRepositoryImpl
import com.example.appdelivery.data.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PendingAdd(val dish: Dish, val restaurantName: String, val currentRestaurantName: String)

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    private val repo: CartRepository = CartRepositoryImpl(db.cartDao())
    private val addressDao = db.addressDao()
    private val restaurantDao = db.restaurantDao()
    private val session = SessionManager(app)

    val items: StateFlow<List<CartItem>> = repo.observeCart()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val message = MutableStateFlow<String?>(null)
    val conflict = MutableStateFlow<PendingAdd?>(null)

    val deliveryMode = MutableStateFlow(DeliveryMode.DELIVERY)
    val selectedAddress = MutableStateFlow<Address?>(null)

    val addresses: StateFlow<List<Address>> = session.userId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else addressDao.observeByCustomer(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentRestaurant: StateFlow<Restaurant?> = items
        .flatMapLatest { list ->
            val rId = list.firstOrNull()?.restaurantId
            flow<Restaurant?> {
                emit(if (rId != null) restaurantDao.getById(rId) else null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            addresses.collect { list ->
                if (selectedAddress.value == null || list.none { it.id == selectedAddress.value?.id }) {
                    selectedAddress.value = list.firstOrNull()
                }
            }
        }
    }

    fun setDeliveryMode(mode: DeliveryMode) {
        deliveryMode.value = mode
    }

    fun selectAddress(address: Address) {
        selectedAddress.value = address
    }

    fun isModeValid(): Boolean =
        if (deliveryMode.value == DeliveryMode.DELIVERY) selectedAddress.value != null else true

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
