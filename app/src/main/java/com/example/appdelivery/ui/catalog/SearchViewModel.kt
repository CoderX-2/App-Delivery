package com.example.appdelivery.ui.catalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdelivery.data.db.AppDatabase
import com.example.appdelivery.data.entity.DishWithRestaurant
import com.example.appdelivery.data.repository.CatalogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    private val repo = CatalogRepository(db.restaurantDao(), db.dishDao())

    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters.asStateFlow()

    val categories: StateFlow<List<String>> = repo.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val results: StateFlow<List<DishWithRestaurant>> = _filters
        .debounce(200)
        .flatMapLatest { f ->
            val max = if (f.maxPrice >= SearchFilters.MAX_PRICE) Double.MAX_VALUE else f.maxPrice.toDouble()
            repo.searchDishes(f.query.trim(), f.category, f.minPrice.toDouble(), max, f.onlyAvailable)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(query: String) = _filters.update { it.copy(query = query) }

    fun onCategorySelected(category: String?) = _filters.update { it.copy(category = category) }

    fun onPriceRangeChange(min: Float, max: Float) =
        _filters.update { it.copy(minPrice = min, maxPrice = max) }

    fun onOnlyAvailableChange(value: Boolean) = _filters.update { it.copy(onlyAvailable = value) }

    fun clearFilters() {
        _filters.value = SearchFilters()
    }
}
