package com.example.appdelivery.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appdelivery.data.entity.Dish

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(vm: CatalogViewModel, restaurantId: Long, onBack: () -> Unit) {
    val menu by remember(restaurantId) { vm.observeMenu(restaurantId) }
        .collectAsStateWithLifecycle(initialValue = null)
    val grouped = menu?.dishes.orEmpty().groupBy { it.category }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(menu?.restaurant?.name ?: "Menú") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            menu?.restaurant?.let { r ->
                item {
                    Text("${r.cuisine} · ${r.address}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            grouped.forEach { (category, dishes) ->
                item(key = "cat-$category") {
                    Text(category, style = MaterialTheme.typography.titleMedium)
                }
                items(dishes, key = { it.id }) { dish -> DishRow(dish) }
            }
        }
    }
}

@Composable
private fun DishRow(dish: Dish) {
    Card(modifier = Modifier.fillMaxWidth().alpha(if (dish.available) 1f else 0.5f)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dish.name, style = MaterialTheme.typography.titleSmall)
                Text(dish.description, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$" + "%.2f".format(dish.price), style = MaterialTheme.typography.titleSmall)
                if (!dish.available) {
                    Text("No disponible", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
