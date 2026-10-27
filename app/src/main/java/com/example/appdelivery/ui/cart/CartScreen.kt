package com.example.appdelivery.ui.cart

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.model.DeliveryMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    vm: CartViewModel,
    onBack: () -> Unit,
    onAddAddress: () -> Unit = {},
    onProceedToPayment: () -> Unit = {}
) {
    val items by vm.items.collectAsStateWithLifecycle()
    val deliveryMode by vm.deliveryMode.collectAsStateWithLifecycle()
    val selectedAddress by vm.selectedAddress.collectAsStateWithLifecycle()
    val addresses by vm.addresses.collectAsStateWithLifecycle()
    val restaurant by vm.currentRestaurant.collectAsStateWithLifecycle()
    val totals by vm.totals.collectAsStateWithLifecycle()
    var showAddressDialog by remember { mutableStateOf(false) }

    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text("Elige una dirección") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    addresses.forEach { addr ->
                        Card(
                            onClick = {
                                vm.selectAddress(addr)
                                showAddressDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(addr.label, style = MaterialTheme.typography.titleSmall)
                                Text("${addr.street}, ${addr.city}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) { Text("Cerrar") }
            }
        )
    }

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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Selector de modalidad
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = deliveryMode == DeliveryMode.DELIVERY,
                        onClick = { vm.setDeliveryMode(DeliveryMode.DELIVERY) },
                        label = { Text("A domicilio") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = deliveryMode == DeliveryMode.PICKUP,
                        onClick = { vm.setDeliveryMode(DeliveryMode.PICKUP) },
                        label = { Text("Recoger en tienda") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sección según modalidad
                if (deliveryMode == DeliveryMode.DELIVERY) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Entrega en:", style = MaterialTheme.typography.labelLarge)
                                TextButton(onClick = onAddAddress) { Text("+ Nueva") }
                            }
                            if (addresses.isEmpty()) {
                                Text("No tienes direcciones registradas", color = MaterialTheme.colorScheme.error)
                                Button(onClick = onAddAddress, modifier = Modifier.padding(top = 4.dp)) {
                                    Text("Agregar dirección")
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { showAddressDialog = true },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(selectedAddress?.label ?: "Seleccionar", style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            selectedAddress?.let { "${it.street}, ${it.city}" } ?: "Toca para elegir",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    TextButton(onClick = { showAddressDialog = true }) { Text("Cambiar") }
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column(Modifier.padding(start = 12.dp)) {
                                Text("Recoger en sucursal:", style = MaterialTheme.typography.labelMedium)
                                Text(restaurant?.name ?: items.first().restaurantName, style = MaterialTheme.typography.titleSmall)
                                Text(restaurant?.address ?: "Dirección disponible al confirmar", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.dishId }) { item ->
                        CartRow(item, vm)
                    }
                }

                // Desglose de precios (AD-76)
                HorizontalDivider()
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                        Text("$" + "%.2f".format(totals.subtotal), style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val feeLabel = if (deliveryMode == DeliveryMode.PICKUP) {
                            "Costo de entrega (Recogida)"
                        } else {
                            totals.distanceKm?.let { "Costo de envío (%.1f km)".format(it) } ?: "Costo de envío base"
                        }
                        Text(feeLabel, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (deliveryMode == DeliveryMode.PICKUP) "Gratis ($0.00)" else "$" + "%.2f".format(totals.deliveryFee),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (deliveryMode == DeliveryMode.PICKUP) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "$" + "%.2f".format(totals.total),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = onProceedToPayment,
                        enabled = items.isNotEmpty() && vm.isModeValid(),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Continuar al pago")
                    }
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
