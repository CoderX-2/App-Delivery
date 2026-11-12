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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appdelivery.data.entity.OrderStatus
import com.example.appdelivery.data.entity.RefundRequest
import com.example.appdelivery.util.EstimatedTimeCalculator
import kotlinx.coroutines.launch
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
    onGoToRate: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val orderWithItems by remember(orderId) { orderVm.observeOrder(orderId) }
        .collectAsStateWithLifecycle(initialValue = null)

    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("Cambié de opinión") }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var refundInfo by remember { mutableStateOf<RefundRequest?>(null) }

    LaunchedEffect(orderWithItems?.order?.status) {
        if (orderWithItems?.order?.status == OrderStatus.CANCELLED) {
            refundInfo = orderVm.getRefund(orderId)
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar pedido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Solo puedes cancelar antes de que el repartidor recoja tu comida. Se procesará el reembolso de inmediato.")
                    Text("Motivo de cancelación:", style = MaterialTheme.typography.labelMedium)
                    listOf("Cambié de opinión", "Tardó demasiado", "Error en la dirección").forEach { r ->
                        TextButton(
                            onClick = { cancelReason = r },
                            colors = if (cancelReason == r) ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                     else ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Text("• $r")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        orderVm.cancelOrder(
                            orderId = orderId,
                            reason = cancelReason,
                            onCancelled = { code ->
                                scope.launch {
                                    snackbar.showSnackbar("Pedido cancelado. Reembolso registrado: $code")
                                }
                            },
                            onError = { err ->
                                scope.launch { snackbar.showSnackbar(err) }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirmar cancelación")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Cerrar") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pedido #${order.id}", style = MaterialTheme.typography.titleMedium)
                            Text(dateStr, style = MaterialTheme.typography.bodySmall)
                        }

                        if (order.status != OrderStatus.CANCELLED) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = " Entrega estimada: $estimatedTime",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = "Estado: ${order.status.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (order.status == OrderStatus.CANCELLED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        if (order.status != OrderStatus.CANCELLED) {
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            )
                        }
                    }
                }

                // Info de reembolso si cancelado (AD-129)
                if (order.status == OrderStatus.CANCELLED && refundInfo != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Reembolso Autorizado", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                            Text("Código: ${refundInfo?.refundCode}", style = MaterialTheme.typography.bodyMedium)
                            Text("Monto: $" + "%.2f".format(refundInfo?.amount ?: 0.0), style = MaterialTheme.typography.titleMedium)
                            Text("Motivo: ${refundInfo?.reason}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (order.status != OrderStatus.CANCELLED) {
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

                // Botón Calificar si ya fue entregado (AD-112)
                if (order.status == OrderStatus.DELIVERED) {
                    Button(
                        onClick = { onGoToRate(order.id) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("Calificar restaurante y repartidor")
                    }
                }

                // Botón Cancelar si está en etapa previa a recogida (AD-126, AD-127)
                if (order.status == OrderStatus.PENDING || order.status == OrderStatus.PREPARING) {
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar pedido")
                    }
                }

                Button(onClick = onOpenOrders, modifier = Modifier.fillMaxWidth()) {
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
