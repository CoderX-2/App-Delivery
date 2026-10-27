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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appdelivery.data.entity.Dish
import com.example.appdelivery.ui.cart.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    vm: CatalogViewModel,
    cartVm: CartViewModel,
    restaurantId: Long,
    onOpenCart: () -> Unit,
    onBack: () -> Unit
) {
    val menu by remember(restaurantId) { vm.observeMenu(restaurantId) }
        .collectAsStateWithLifecycle(initialValue = null)
    val grouped = menu?.dishes.orEmpty().groupBy { it.category }
    val cartItems by cartVm.items.collectAsStateWithLifecycle()
    val message by cartVm.message.collectAsStateWithLifecycle()
    val conflict by cartVm.conflict.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val count = cartItems.sumOf { it.quantity }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            cartVm.clearMessage()
        }
    }

    conflict?.let { p ->
        AlertDialog(
            onDismissRequest = { cartVm.dismissConflict() },
            title = { Text("¿Cambiar de restaurante?") },
            text = { Text("Tu carrito tiene platillos de ${p.currentRestaurantName}. ¿Vaciarlo y agregar de ${p.restaurantName}?") },
            confirmButton = { TextButton(onClick = { cartVm.confirmReplace() }) { Text("Vaciar y agregar") } },
            dismissButton = { TextButton(onClick = { cartVm.dismissConflict() }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(menu?.restaurant?.name ?: "Menú") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenCart) {
                        BadgedBox(badge = { if (count > 0) Badge { Text("$count") } }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                        }
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
                items(dishes, key = { it.id }) { dish ->
                    DishRow(dish, onAdd = { menu?.restaurant?.let { r -> cartVm.add(dish, r.name) } })
                }
            }
        }
    }
}

@Composable
private fun DishRow(dish: Dish, onAdd: () -> Unit) {
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
                if (dish.available) {
                    Button(onClick = onAdd) { Text("Agregar") }
                } else {
                    Text("No disponible", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
