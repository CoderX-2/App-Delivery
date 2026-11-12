package com.example.appdelivery.ui.courier

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdelivery.data.courier.CourierRepository
import com.example.appdelivery.data.courier.CourierRepositoryImpl
import com.example.appdelivery.data.db.AppDatabase
import com.example.appdelivery.data.entity.Courier
import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.data.entity.OrderWithItems
import com.example.appdelivery.data.entity.VehicleType
import com.example.appdelivery.data.repository.OrderRepository
import com.example.appdelivery.data.repository.OrderRepositoryImpl
import com.example.appdelivery.data.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CourierViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    private val courierRepo: CourierRepository = CourierRepositoryImpl(db.courierDao())
    private val orderRepo: OrderRepository = OrderRepositoryImpl(db.orderDao(), db.cartDao())
    private val session = SessionManager(app)

    val currentCourier: StateFlow<Courier?> = session.courierId
        .flatMapLatest { id -> if (id == null) flowOf(null) else courierRepo.observeCourier(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val availableOrders: StateFlow<List<OrderWithItems>> = orderRepo.observeAvailableOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignedOrders: StateFlow<List<OrderWithItems>> = session.courierId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else orderRepo.observeAssignedOrders(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orderActionMessage = MutableStateFlow<String?>(null)

    var formErrors by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set

    fun clearErrors() {
        formErrors = emptyMap()
        errorMessage = null
    }

    fun clearActionMessage() {
        orderActionMessage.value = null
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        clearErrors()
        if (email.isBlank() || pass.isBlank()) {
            errorMessage = "Ingresa correo y contraseña"
            return
        }
        viewModelScope.launch {
            isLoading = true
            val res = courierRepo.login(email, pass)
            isLoading = false
            res.onSuccess { courier ->
                session.saveCourier(courier.id)
                onSuccess()
            }.onFailure {
                errorMessage = it.message ?: "Credenciales incorrectas"
            }
        }
    }

    fun register(
        name: String, email: String, phone: String,
        vehicleType: VehicleType, pass: String, confirm: String,
        onSuccess: () -> Unit
    ) {
        clearErrors()
        val errors = mutableMapOf<String, String>()
        if (name.trim().length < 2) errors["name"] = "Nombre obligatorio"
        if (!email.contains("@") || !email.contains(".")) errors["email"] = "Correo inválido"
        if (phone.trim().length < 10) errors["phone"] = "Teléfono de al menos 10 dígitos"
        if (pass.length < 6) errors["password"] = "Mínimo 6 caracteres"
        if (pass != confirm) errors["confirm"] = "Las contraseñas no coinciden"

        formErrors = errors
        if (errors.isNotEmpty()) return

        viewModelScope.launch {
            isLoading = true
            val res = courierRepo.register(name, email, phone, vehicleType, pass)
            isLoading = false
            res.onSuccess { id ->
                session.saveCourier(id)
                onSuccess()
            }.onFailure {
                errorMessage = it.message ?: "Error al registrar repartidor"
            }
        }
    }

    fun setAvailability(isAvailable: Boolean) {
        val courier = currentCourier.value ?: return
        viewModelScope.launch {
            courierRepo.setAvailability(courier.id, isAvailable)
        }
    }

    fun acceptOrder(orderId: String, onAccepted: () -> Unit) {
        viewModelScope.launch {
            val courierId = session.courierId.first() ?: return@launch
            val success = orderRepo.acceptOrder(orderId, courierId)
            if (success) {
                orderActionMessage.value = "¡Pedido aceptado! Ahora puedes recogerlo."
                onAccepted()
            } else {
                orderActionMessage.value = "Lo sentimos, otro repartidor ya tomó este pedido."
            }
        }
    }

    fun advanceOrderStatus(orderId: String, currentStatus: OrderStatus) {
        val next = currentStatus.nextDeliveryStatus() ?: return
        viewModelScope.launch {
            orderRepo.updateOrderStatus(orderId, next.name)
            orderActionMessage.value = "Estado actualizado a: ${next.displayName}"
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            session.clear()
            onSuccess()
        }
    }
}
