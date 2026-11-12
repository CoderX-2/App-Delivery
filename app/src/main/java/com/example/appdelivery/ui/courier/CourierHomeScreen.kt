package com.example.appdelivery.ui.courier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.TwoWheeler
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourierHomeScreen(
    vm: CourierViewModel,
    onOpenAvailableOrders: () -> Unit,
    onOpenDeliveryDetail: (String) -> Unit = {},
    onOpenEarnings: () -> Unit = {},
    onLogout: () -> Unit
) {
    val courier by vm.currentCourier.collectAsStateWithLifecycle()
    val availableOrders by vm.availableOrders.collectAsStateWithLifecycle()
    val assignedOrders by vm.assignedOrders.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel del Repartidor") },
                actions = {
                    IconButton(onClick = { vm.logout(onLogout) }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
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
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = "¡Hola, ${courier?.name ?: "Repartidor"}!",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${courier?.email ?: ""} · ${courier?.phone ?: ""}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Vehículo: ${courier?.vehicleType?.displayName ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            val isAvail = courier?.isAvailable == true
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAvail) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAvail) "Conectado para entregas" else "Desconectado",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isAvail) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isAvail) "Recibirás solicitudes de entrega en tiempo real" else "Conéctate para recibir pedidos",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = isAvail,
                        onCheckedChange = { vm.setAvailability(it) }
                    )
                }
            }

            // Entregas activas asignadas a este repartidor
            if (assignedOrders.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeliveryDining, contentDescription = null)
                            Text(" Entrega en curso", style = MaterialTheme.typography.titleMedium)
                        }
                        val active = assignedOrders.first()
                        Text("Pedido #${active.order.id} · ${active.order.restaurantName}", style = MaterialTheme.typography.bodyMedium)
                        Text("Estado actual: ${active.order.status.displayName}", style = MaterialTheme.typography.labelMedium)
                        Button(
                            onClick = { onOpenDeliveryDetail(active.order.id) },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text("Gestionar entrega")
                        }
                    }
                }
            }

            // Pedidos disponibles
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pedidos disponibles", style = MaterialTheme.typography.titleMedium)
                        Text("${availableOrders.size} listos", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        "Hay pedidos listos en restaurantes esperando repartidor.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = onOpenAvailableOrders,
                        enabled = isAvail,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text(if (isAvail) "Ver pedidos disponibles (${availableOrders.size})" else "Conéctate para ver pedidos")
                    }
                }
            }

            
            // Resumen de Ganancias (AD-134)
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenEarnings
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Mis Ganancias", style = MaterialTheme.typography.titleMedium)
                        Text("Consulta tus ingresos de hoy, semana y desglose", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = onOpenEarnings) {
                        Text("Ver")
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            OutlinedButton(
                onClick = { vm.logout(onLogout) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
