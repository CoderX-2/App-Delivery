package com.example.appdelivery.ui.courier

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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appdelivery.data.entity.OrderWithItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableOrdersScreen(
    vm: CourierViewModel,
    onOrderAccepted: (String) -> Unit,
    onBack: () -> Unit
) {
    val orders by vm.availableOrders.collectAsStateWithLifecycle()
    val actionMessage by vm.orderActionMessage.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var dismissedOrderIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbar.showSnackbar(it)
            vm.clearActionMessage()
        }
    }

    val visibleOrders = orders.filterNot { it.order.id in dismissedOrderIds }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Pedidos disponibles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (visibleOrders.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No hay pedidos disponibles por el momento", style = MaterialTheme.typography.titleMedium)
                    Text("Los nuevos pedidos aparecerán aquí automáticamente.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleOrders, key = { it.order.id }) { item ->
                    val o = item.order
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("#${o.id}", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Envío: $" + "%.2f".format(o.deliveryFee),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                                Text(" Recoger en: ${o.restaurantName}", style = MaterialTheme.typography.bodyMedium)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                                Text(" Entregar en: ${o.deliveryAddress ?: "Dirección cliente"}", style = MaterialTheme.typography.bodyMedium)
                            }

                            HorizontalDivider(Modifier.padding(vertical = 4.dp))

                            Text("Contenido: ${item.items.joinToString { "${it.quantity}x ${it.name}" }}", style = MaterialTheme.typography.bodySmall)

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { dismissedOrderIds = dismissedOrderIds + o.id },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Rechazar")
                                }
                                Button(
                                    onClick = {
                                        vm.acceptOrder(o.id) {
                                            onOrderAccepted(o.id)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Aceptar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
