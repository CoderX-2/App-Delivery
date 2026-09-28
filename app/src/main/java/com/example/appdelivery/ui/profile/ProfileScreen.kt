package com.example.appdelivery.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.appdelivery.data.entity.Address

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    onEditProfile: () -> Unit,
    onAddAddress: () -> Unit,
    onEditAddress: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val customer by vm.customer.collectAsStateWithLifecycle()
    val addresses by vm.addresses.collectAsStateWithLifecycle()
    var addressToDelete by remember { mutableStateOf<Address?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAddress) {
                Icon(Icons.Default.Add, contentDescription = "Agregar dirección")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(customer?.name ?: "", style = MaterialTheme.typography.titleLarge)
                        Text(customer?.email ?: "", style = MaterialTheme.typography.bodyMedium)
                        Text("Tel: ${customer?.phone ?: ""}", style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = onEditProfile, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Editar perfil")
                        }
                    }
                }
            }
            item {
                Text("Mis direcciones de entrega", style = MaterialTheme.typography.titleMedium)
            }
            if (addresses.isEmpty()) {
                item { Text("Aún no tienes direcciones. Toca + para agregar una.") }
            }
            items(addresses, key = { it.id }) { address ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(address.label, style = MaterialTheme.typography.titleSmall)
                            Text("${address.street}, ${address.city}")
                            if (address.reference.isNotBlank()) {
                                Text("Ref: ${address.reference}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        IconButton(onClick = { onEditAddress(address.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar dirección")
                        }
                        IconButton(onClick = { addressToDelete = address }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar dirección")
                        }
                    }
                }
            }
        }
    }

    addressToDelete?.let { address ->
        AlertDialog(
            onDismissRequest = { addressToDelete = null },
            title = { Text("Eliminar dirección") },
            text = { Text("¿Eliminar \"${address.label}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteAddress(address)
                    addressToDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { addressToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}
