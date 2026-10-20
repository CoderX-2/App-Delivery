package com.example.appdelivery.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appdelivery.data.entity.DishWithRestaurant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    vm: SearchViewModel,
    onOpenMenu: (Long) -> Unit,
    onBack: () -> Unit
) {
    val filters by vm.filters.collectAsStateWithLifecycle()
    val results by vm.results.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar platillos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = filters.query,
                onValueChange = vm::onQueryChange,
                label = { Text("Buscar platillo") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = filters.category == null,
                        onClick = { vm.onCategorySelected(null) },
                        label = { Text("Todas") }
                    )
                }
                items(categories, key = { it }) { category ->
                    FilterChip(
                        selected = filters.category == category,
                        onClick = {
                            vm.onCategorySelected(if (filters.category == category) null else category)
                        },
                        label = { Text(category) }
                    )
                }
            }

            val maxLabel = if (filters.maxPrice >= SearchFilters.MAX_PRICE) "+" else ""
            Text(
                "Precio: \$${filters.minPrice.toInt()} - \$${filters.maxPrice.toInt()}$maxLabel",
                style = MaterialTheme.typography.labelLarge
            )
            RangeSlider(
                value = filters.minPrice..filters.maxPrice,
                onValueChange = { range -> vm.onPriceRangeChange(range.start, range.endInclusive) },
                valueRange = 0f..SearchFilters.MAX_PRICE,
                steps = 39
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Solo disponibles", modifier = Modifier.weight(1f))
                Switch(checked = filters.onlyAvailable, onCheckedChange = vm::onOnlyAvailableChange)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${results.size} resultado(s)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                if (filters.isActive) {
                    TextButton(onClick = vm::clearFilters) { Text("Limpiar filtros") }
                }
            }

            if (results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Sin resultados", style = MaterialTheme.typography.titleMedium)
                        Text("Prueba con otra búsqueda o cambia los filtros.")
                        if (filters.isActive) {
                            OutlinedButton(onClick = vm::clearFilters) { Text("Limpiar filtros") }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(results, key = { it.dish.id }) { item ->
                        ResultRow(item, onClick = { onOpenMenu(item.dish.restaurantId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(item: DishWithRestaurant, onClick: () -> Unit) {
    val dish = item.dish
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (dish.available) 1f else 0.5f)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dish.name, style = MaterialTheme.typography.titleSmall)
                Text("${item.restaurantName} · ${dish.category}", style = MaterialTheme.typography.bodySmall)
                Text(dish.description, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("\$" + "%.2f".format(dish.price), style = MaterialTheme.typography.titleSmall)
                if (!dish.available) {
                    Text(
                        "No disponible",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
