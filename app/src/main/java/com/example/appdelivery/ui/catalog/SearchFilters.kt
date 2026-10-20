package com.example.appdelivery.ui.catalog

data class SearchFilters(
    val query: String = "",
    val category: String? = null,
    val minPrice: Float = 0f,
    val maxPrice: Float = MAX_PRICE,
    val onlyAvailable: Boolean = false
) {
    val isActive: Boolean
        get() = query.isNotBlank() || category != null || minPrice > 0f ||
            maxPrice < MAX_PRICE || onlyAvailable

    companion object {
        const val MAX_PRICE = 200f
    }
}
