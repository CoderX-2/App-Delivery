package com.example.appdelivery.ui.order

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appdelivery.data.entity.Address
import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.entity.Restaurant
import com.example.appdelivery.data.model.DeliveryMode
import com.example.appdelivery.data.payment.PaymentResult
import com.example.appdelivery.util.CartTotals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSummaryScreen(
    items: List<CartItem>,
    totals: CartTotals,
    deliveryMode: DeliveryMode,
    selectedAddress: Address?,
    restaurant: Restaurant?,
    paymentResult: PaymentResult.Success,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen del pedido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Restaurante", style = MaterialTheme.typography.labelMedium)
                    Text(
                        restaurant?.name ?: items.firstOrNull()?.restaurantName ?: "Restaurante",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        if (deliveryMode == DeliveryMode.DELIVERY) "Entrega a domicilio" else "Recoger en sucursal",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        if (deliveryMode == DeliveryMode.DELIVERY) {
                            selectedAddress?.let { "${it.label} - ${it.street}, ${it.city} (${it.reference})" } ?: "Domicilio"
                        } else {
                            restaurant?.address ?: "En mostrador"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Método de pago", style = MaterialTheme.typography.labelMedium)
                    Text(
                        paymentResult.method.displayName + (paymentResult.last4?.let { " (****$it)" } ?: ""),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Transacción: ${paymentResult.transactionId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Productos", style = MaterialTheme.typography.titleSmall)
                    items.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.quantity}x ${item.name}", style = MaterialTheme.typography.bodyMedium)
                            Text("$" + "%.2f".format(item.price * item.quantity), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                        Text("$" + "%.2f".format(totals.subtotal), style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Envío", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (deliveryMode == DeliveryMode.PICKUP) "Gratis ($0.00)" else "$" + "%.2f".format(totals.deliveryFee),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "$" + "%.2f".format(totals.total),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Button(
                onClick = {
                    isSubmitting = true
                    onConfirm()
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(if (isSubmitting) "Confirmando pedido..." else "Confirmar y ordenar")
            }
        }
    }
}
