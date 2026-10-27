package com.example.appdelivery.ui.order

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdelivery.data.db.AppDatabase
import com.example.appdelivery.data.entity.Address
import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.entity.Order
import com.example.appdelivery.data.entity.OrderItem
import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.data.entity.OrderWithItems
import com.example.appdelivery.data.entity.Restaurant
import com.example.appdelivery.data.model.DeliveryMode
import com.example.appdelivery.data.payment.PaymentResult
import com.example.appdelivery.data.repository.OrderRepository
import com.example.appdelivery.data.repository.OrderRepositoryImpl
import com.example.appdelivery.data.session.SessionManager
import com.example.appdelivery.util.CartTotals
import java.util.UUID
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
class OrderViewModel(
    app: Application,
    private val repo: OrderRepository = OrderRepositoryImpl(
        AppDatabase.getInstance(app).orderDao(),
        AppDatabase.getInstance(app).cartDao()
    )
) : AndroidViewModel(app) {

    private val session = SessionManager(app)

    val orders: StateFlow<List<OrderWithItems>> = session.userId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.observeOrders(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingPaymentResult = MutableStateFlow<PaymentResult.Success?>(null)
    val latestOrderId = MutableStateFlow<String?>(null)

    fun placeOrder(
        items: List<CartItem>,
        totals: CartTotals,
        mode: DeliveryMode,
        restaurant: Restaurant?,
        address: Address?,
        payment: PaymentResult.Success,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val customerId = session.userId.first() ?: return@launch
            val orderId = "ORD-" + UUID.randomUUID().toString().take(8).uppercase()

            val deliveryText = if (mode == DeliveryMode.DELIVERY) {
                address?.let { "${it.label} - ${it.street}, ${it.city} (${it.reference})" } ?: "Domicilio"
            } else {
                "Recogida en sucursal: ${restaurant?.address ?: "Mostrador"}"
            }

            val order = Order(
                id = orderId,
                customerId = customerId,
                restaurantId = items.firstOrNull()?.restaurantId ?: 1L,
                restaurantName = restaurant?.name ?: items.firstOrNull()?.restaurantName ?: "Restaurante",
                deliveryMode = mode,
                deliveryAddress = deliveryText,
                subtotal = totals.subtotal,
                deliveryFee = totals.deliveryFee,
                total = totals.total,
                paymentMethod = payment.method.displayName + (payment.last4?.let { " (****$it)" } ?: ""),
                paymentTransactionId = payment.transactionId,
                status = OrderStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )

            val orderItems = items.map {
                OrderItem(
                    orderId = orderId,
                    dishId = it.dishId,
                    name = it.name,
                    price = it.price,
                    quantity = it.quantity
                )
            }

            repo.placeOrder(order, orderItems)
            latestOrderId.value = orderId
            pendingPaymentResult.value = null
            onSuccess(orderId)
        }
    }

    suspend fun getOrder(orderId: String): OrderWithItems? = repo.getOrder(orderId)
}
