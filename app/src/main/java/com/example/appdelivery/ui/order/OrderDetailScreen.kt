package com.example.appdelivery.ui.order

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
import com.example.appdelivery.data.entity.OrderWithItems
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    orderVm: OrderViewModel,
    onOpenOrders: () -> Unit,
    onGoToHome: () -> Unit,
    onBack: () -> Unit
) {
    var orderWithItems by remember { mutableStateOf<OrderWithItems?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(orderId) {
        orderWithItems = orderVm.getOrder(orderId)
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento de pedido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val order = orderWithItems?.order
            val items = orderWithItems?.items.orEmpty()

            if (order == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Pedido no encontrado")
                }
            } else {
                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.createdAt))

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
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pedido #${order.id}", style = MaterialTheme.typography.titleMedium)
                                Text(dateStr, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                "Estado: ${order.status.displayName}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Restaurante:", style = MaterialTheme.typography.labelMedium)
                            Text(order.restaurantName, style = MaterialTheme.typography.titleSmall)
                            Text("Entrega en:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
                            Text(order.deliveryAddress ?: "Sucursal", style = MaterialTheme.typography.bodyMedium)
                            Text("Pago:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
                            Text(order.paymentMethod, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Platillos ordenados", style = MaterialTheme.typography.titleSmall)
                            items.forEach { item ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${item.quantity}x ${item.name}", style = MaterialTheme.typography.bodyMedium)
                                    Text("$" + "%.2f".format(item.price * item.quantity), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total pagado", style = MaterialTheme.typography.titleMedium)
                                Text("$" + "%.2f".format(order.total), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    Button(
                        onClick = onOpenOrders,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Ver mis pedidos")
                    }

                    OutlinedButton(
                        onClick = onGoToHome,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver a restaurantes")
                    }
                }
            }
        }
    }
}
