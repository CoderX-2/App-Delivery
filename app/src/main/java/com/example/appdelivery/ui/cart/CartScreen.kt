package com.example.appdelivery.ui.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appdelivery.data.entity.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(vm: CartViewModel, onBack: () -> Unit) {
    val items by vm.items.collectAsStateWithLifecycle()
    val subtotal = items.sumOf { it.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrito") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (items.isNotEmpty()) TextButton(onClick = { vm.clear() }) { Text("Vaciar") }
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Tu carrito está vacío")
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                Text(
                    "Restaurante: ${items.first().restaurantName}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.dishId }) { item ->
                        CartRow(item, vm)
                    }
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", style = MaterialTheme.typography.titleMedium)
                    Text("$" + "%.2f".format(subtotal), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun CartRow(item: CartItem, vm: CartViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall)
                Text("$" + "%.2f".format(item.price) + " c/u", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { vm.decrement(item) }) { Text("−", style = MaterialTheme.typography.titleLarge) }
            Text("${item.quantity}", style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = { vm.increment(item) }) { Text("+", style = MaterialTheme.typography.titleLarge) }
            IconButton(onClick = { vm.remove(item) }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}
