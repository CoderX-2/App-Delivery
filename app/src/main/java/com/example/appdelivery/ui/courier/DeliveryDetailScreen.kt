package com.example.appdelivery.ui.courier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
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
import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.ui.order.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailScreen(
    orderId: String,
    courierVm: CourierViewModel,
    orderVm: OrderViewModel,
    onBack: () -> Unit
) {
    val orderWithItems by remember(orderId) { orderVm.observeOrder(orderId) }
        .collectAsStateWithLifecycle(initialValue = null)
    val actionMessage by courierVm.orderActionMessage.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showConfirmDeliveredDialog by remember { mutableStateOf(false) }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbar.showSnackbar(it)
            courierVm.clearActionMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Entrega") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        val order = orderWithItems?.order
        val items = orderWithItems?.items.orEmpty()

        if (order == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (showConfirmDeliveredDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDeliveredDialog = false },
                    title = { Text("Confirmar entrega") },
                    text = { Text("¿Confirmas que entregaste el pedido al cliente?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showConfirmDeliveredDialog = false
                            courierVm.advanceOrderStatus(order.id, order.status)
                        }) { Text("Sí, confirmar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDeliveredDialog = false }) { Text("Cancelar") }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Pedido #${order.id}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Estado actual: ${order.status.displayName}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Puntos de la ruta", style = MaterialTheme.typography.titleSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(" Sucursal: ${order.restaurantName}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(" Cliente: ${order.deliveryAddress ?: "Domicilio"}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Detalles del pedido", style = MaterialTheme.typography.titleSmall)
                        items.forEach { item ->
                            Text("• ${item.quantity}x ${item.name}", style = MaterialTheme.typography.bodyMedium)
                        }
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pago:", style = MaterialTheme.typography.bodyMedium)
                            Text(order.paymentMethod, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ganancia envío:", style = MaterialTheme.typography.titleSmall)
                            Text("$" + "%.2f".format(order.deliveryFee), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                val actionLabel = order.status.actionLabel()
                if (actionLabel != null) {
                    Button(
                        onClick = {
                            if (order.status == OrderStatus.IN_DELIVERY) {
                                showConfirmDeliveredDialog = true
                            } else {
                                courierVm.advanceOrderStatus(order.id, order.status)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text(actionLabel)
                    }
                } else if (order.status == OrderStatus.DELIVERED) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(" ¡Pedido entregado con éxito!", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver al panel")
                }
            }
        }
    }
}
