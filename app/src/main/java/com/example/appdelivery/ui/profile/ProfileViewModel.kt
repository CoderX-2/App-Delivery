package com.example.appdelivery.ui.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdelivery.data.db.AppDatabase
import com.example.appdelivery.data.entity.Address
import com.example.appdelivery.data.entity.Customer
import com.example.appdelivery.data.repository.ProfileRepository
import com.example.appdelivery.data.session.SessionManager
import com.example.appdelivery.util.Validators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    private val repo = ProfileRepository(db.customerDao(), db.addressDao())
    private val session = SessionManager(app)

    val customer: StateFlow<Customer?> = session.userId
        .flatMapLatest { id -> if (id == null) flowOf(null) else repo.observeCustomer(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val addresses: StateFlow<List<Address>> = session.userId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.observeAddresses(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var formErrors by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    fun clearErrors() {
        formErrors = emptyMap()
    }

    fun updateProfile(name: String, phone: String, onSuccess: () -> Unit) {
        val errors = buildMap<String, String> {
            Validators.name(name)?.let { put("name", it) }
            Validators.phone(phone)?.let { put("phone", it) }
        }
        formErrors = errors
        if (errors.isNotEmpty()) return
        val current = customer.value ?: return
        viewModelScope.launch {
            repo.updateProfile(current, name, phone)
            onSuccess()
        }
    }

    suspend fun getAddress(id: Long): Address? = repo.getAddress(id)

    fun saveAddress(
        addressId: Long?, label: String, street: String, city: String,
        reference: String, onSuccess: () -> Unit
    ) {
        val errors = buildMap<String, String> {
            Validators.addressLabel(label)?.let { put("label", it) }
            Validators.street(street)?.let { put("street", it) }
            Validators.city(city)?.let { put("city", it) }
        }
        formErrors = errors
        if (errors.isNotEmpty()) return
        viewModelScope.launch {
            val ownerId = session.userId.first() ?: return@launch
            val address = Address(
                id = addressId ?: 0,
                customerId = ownerId,
                label = label.trim(),
                street = street.trim(),
                city = city.trim(),
                reference = reference.trim()
            )
            if (addressId == null) repo.addAddress(address) else repo.updateAddress(address)
            onSuccess()
        }
    }

    fun deleteAddress(address: Address) {
        viewModelScope.launch { repo.deleteAddress(address) }
    }
}
