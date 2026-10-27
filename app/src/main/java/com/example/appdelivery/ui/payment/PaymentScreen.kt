package com.example.appdelivery.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appdelivery.data.payment.PaymentMethod
import com.example.appdelivery.data.payment.PaymentResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    vm: PaymentViewModel,
    totalAmount: Double,
    onBack: () -> Unit,
    onPaymentSuccess: (PaymentResult.Success) -> Unit
) {
    val method by vm.selectedMethod.collectAsStateWithLifecycle()
    val isProcessing by vm.isProcessing.collectAsStateWithLifecycle()
    val lastResult by vm.lastResult.collectAsStateWithLifecycle()

    if (lastResult is PaymentResult.Failed) {
        val fail = lastResult as PaymentResult.Failed
        AlertDialog(
            onDismissRequest = { vm.dismissResult() },
            title = { Text("Error en el pago") },
            text = { Text(fail.reason) },
            confirmButton = {
                TextButton(onClick = { vm.dismissResult() }) { Text("Reintentar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Método de pago") },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total a pagar:", style = MaterialTheme.typography.titleMedium)
                    Text("$" + "%.2f".format(totalAmount), style = MaterialTheme.typography.headlineSmall)
                }
            }

            Text("Selecciona una opción:", style = MaterialTheme.typography.titleMedium)

            PaymentMethod.values().forEach { m ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { vm.setMethod(m) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = method == m, onClick = { vm.setMethod(m) })
                        Text(m.displayName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            if (method == PaymentMethod.CARD) {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Datos de la tarjeta", style = MaterialTheme.typography.titleSmall)

                        OutlinedTextField(
                            value = vm.cardNumber,
                            onValueChange = { if (it.length <= 19) vm.cardNumber = it },
                            label = { Text("Número de tarjeta") },
                            isError = vm.errors["cardNumber"] != null,
                            supportingText = vm.errors["cardNumber"]?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = vm.cardHolder,
                            onValueChange = { vm.cardHolder = it },
                            label = { Text("Nombre del titular") },
                            isError = vm.errors["cardHolder"] != null,
                            supportingText = vm.errors["cardHolder"]?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = vm.expiry,
                                onValueChange = { if (it.length <= 5) vm.expiry = it },
                                label = { Text("Vence (MM/AA)") },
                                placeholder = { Text("12/28") },
                                isError = vm.errors["expiry"] != null,
                                supportingText = vm.errors["expiry"]?.let { { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = vm.cvv,
                                onValueChange = { if (it.length <= 4) vm.cvv = it },
                                label = { Text("CVV") },
                                isError = vm.errors["cvv"] != null,
                                supportingText = vm.errors["cvv"]?.let { { Text(it) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else if (method == PaymentMethod.TRANSFER) {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Datos para SPEI", style = MaterialTheme.typography.titleSmall)
                        Text("Banco: BBVA Bancomer", style = MaterialTheme.typography.bodyMedium)
                        Text("CLABE: 012180001234567890", style = MaterialTheme.typography.bodyMedium)
                        Text("Beneficiario: App Delivery S.A. de C.V.", style = MaterialTheme.typography.bodyMedium)
                        Text("Al confirmar, se registrará el pedido en espera de validación.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Pago en efectivo", style = MaterialTheme.typography.titleSmall)
                        Text("Pagarás al repartidor al momento de recibir tus platillos.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Button(
                onClick = { vm.submitPayment(totalAmount, onPaymentSuccess) },
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(end = 8.dp))
                    Text("Procesando...")
                } else {
                    Text(if (method == PaymentMethod.CARD) "Pagar ahora" else "Confirmar método de pago")
                }
            }
        }
    }
}
