package com.example.appdelivery.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.util.EstimatedTimeCalculator
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
    val orderWithItems by remember(orderId) { orderVm.observeOrder(orderId) }
        .collectAsStateWithLifecycle(initialValue = null)

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
        val order = orderWithItems?.order
        val items = orderWithItems?.items.orEmpty()

        if (order == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.createdAt))
            val estimatedTime = EstimatedTimeCalculator.calculate(order.status)
            val progressFraction = EstimatedTimeCalculator.progressFraction(order.status)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tarjeta de Estado y Tiempo Estimado (AD-106)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pedido #${order.id}", style = MaterialTheme.typography.titleMedium)
                            Text(dateStr, style = MaterialTheme.typography.bodySmall)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = " Entrega estimada: $estimatedTime",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Estado: ${order.status.displayName}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        )
                    }
                }

                // Línea de Progreso con las Etapas (AD-107)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Etapas de la entrega", style = MaterialTheme.typography.titleSmall)
                        DeliveryStepRow(
                            stepName = "1. Preparando en restaurante",
                            isCompleted = order.status in listOf(OrderStatus.PREPARING, OrderStatus.PICKED_UP, OrderStatus.IN_DELIVERY, OrderStatus.DELIVERED),
                            isCurrent = order.status == OrderStatus.PREPARING
                        )
                        DeliveryStepRow(
                            stepName = "2. Recogido por repartidor",
                            isCompleted = order.status in listOf(OrderStatus.PICKED_UP, OrderStatus.IN_DELIVERY, OrderStatus.DELIVERED),
                            isCurrent = order.status == OrderStatus.PICKED_UP
                        )
                        DeliveryStepRow(
                            stepName = "3. En camino a tu domicilio",
                            isCompleted = order.status in listOf(OrderStatus.IN_DELIVERY, OrderStatus.DELIVERED),
                            isCurrent = order.status == OrderStatus.IN_DELIVERY
                        )
                        DeliveryStepRow(
                            stepName = "4. Pedido entregado",
                            isCompleted = order.status == OrderStatus.DELIVERED,
                            isCurrent = order.status == OrderStatus.DELIVERED
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Detalles de entrega:", style = MaterialTheme.typography.labelMedium)
                        Text(order.restaurantName, style = MaterialTheme.typography.titleSmall)
                        Text(order.deliveryAddress ?: "Sucursal", style = MaterialTheme.typography.bodyMedium)
                        Text("Pago: ${order.paymentMethod}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                            Text("$" + "%.2f".format(order.total), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Button(onClick = onOpenOrders, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text("Ver todos mis pedidos")
                }

                OutlinedButton(onClick = onGoToHome, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver al inicio")
                }
            }
        }
    }
}

@Composable
private fun DeliveryStepRow(stepName: String, isCompleted: Boolean, isCurrent: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val color = when {
            isCompleted -> MaterialTheme.colorScheme.primary
            isCurrent -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.outlineVariant
        }
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
            }
        }
        Text(
            text = "  $stepName",
            style = if (isCurrent) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (isCompleted || isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
        )
    }
}
