package com.example.appdelivery.ui.courier

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appdelivery.data.entity.VehicleType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourierRegisterScreen(
    vm: CourierViewModel,
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf(VehicleType.MOTORCYCLE) }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.clearErrors()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Repartidor") },
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Únete a nuestro equipo de repartidores", style = MaterialTheme.typography.titleMedium)

            vm.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                isError = vm.formErrors["name"] != null,
                supportingText = vm.formErrors["name"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = vm.formErrors["email"] != null,
                supportingText = vm.formErrors["email"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono de contacto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = vm.formErrors["phone"] != null,
                supportingText = vm.formErrors["phone"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Vehículo de entrega:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VehicleType.values().forEach { vt ->
                    FilterChip(
                        selected = vehicleType == vt,
                        onClick = { vehicleType = vt },
                        label = { Text(vt.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña (mínimo 6 caracteres)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = vm.formErrors["password"] != null,
                supportingText = vm.formErrors["password"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = vm.formErrors["confirm"] != null,
                supportingText = vm.formErrors["confirm"]?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    vm.register(name, email, phone, vehicleType, password, confirm, onRegisterSuccess)
                },
                enabled = !vm.isLoading,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            ) {
                if (vm.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(end = 8.dp))
                    Text("Registrando...")
                } else {
                    Text("Completar registro")
                }
            }
        }
    }
}
