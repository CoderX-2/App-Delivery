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
    onOpenAvailableOrders: () -> Unit = {},
    onLogout: () -> Unit
) {
    val courier by vm.currentCourier.collectAsStateWithLifecycle()

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
                            text = if (isAvail) "Recibirás pedidos en tiempo real" else "No recibirás solicitudes de entrega",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = isAvail,
                        onCheckedChange = { vm.setAvailability(it) }
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Gestión de entregas", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Consulta los pedidos listos en restaurantes para aceptar entregas y actualizar su estado en tiempo real.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = onOpenAvailableOrders,
                        enabled = isAvail,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text(if (isAvail) "Ver pedidos disponibles" else "Conéctate para ver pedidos")
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
