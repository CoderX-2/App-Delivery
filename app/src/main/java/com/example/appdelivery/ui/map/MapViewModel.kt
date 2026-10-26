package com.example.appdelivery.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdelivery.data.db.AppDatabase
import com.example.appdelivery.data.repository.CatalogRepository
import com.example.appdelivery.data.repository.LocationRepository
import com.example.appdelivery.util.GeoPoint
import com.example.appdelivery.util.GeoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LocationStatus { IDLE, LOADING, READY, UNAVAILABLE }

class MapViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    private val catalog = CatalogRepository(db.restaurantDao(), db.dishDao())
    private val locationRepo = LocationRepository(app)

    private val _userLocation = MutableStateFlow<GeoPoint?>(null)
    val userLocation: StateFlow<GeoPoint?> = _userLocation.asStateFlow()

    private val _status = MutableStateFlow(LocationStatus.IDLE)
    val status: StateFlow<LocationStatus> = _status.asStateFlow()

    /** Restaurantes dentro del radio, ordenados del más cercano al más lejano. */
    val nearby: StateFlow<List<NearbyRestaurant>> = combine(
        catalog.observeRestaurants(), _userLocation
    ) { restaurants, location ->
        if (location == null) emptyList()
        else restaurants
            .map { NearbyRestaurant(it, GeoUtils.distanceKm(location, GeoPoint(it.latitude, it.longitude))) }
            .filter { it.distanceKm <= RADIUS_KM }
            .sortedBy { it.distanceKm }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Llamar solo cuando el permiso de ubicación ya fue concedido. */
    fun loadLocation() {
        if (_status.value == LocationStatus.LOADING) return
        viewModelScope.launch {
            _status.value = LocationStatus.LOADING
            val location = locationRepo.getCurrentLocation()
            _userLocation.value = location
            _status.value = if (location == null) LocationStatus.UNAVAILABLE else LocationStatus.READY
        }
    }

    companion object {
        const val RADIUS_KM = 5.0
    }
}
