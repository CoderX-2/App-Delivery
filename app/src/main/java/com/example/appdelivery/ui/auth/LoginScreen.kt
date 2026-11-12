package com.example.appdelivery.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appdelivery.ui.components.FormField
import com.example.appdelivery.ui.courier.CourierViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    onCourierLoginSuccess: () -> Unit = {},
    onGoToCourierRegister: () -> Unit = {},
    vm: AuthViewModel = viewModel(),
    courierVm: CourierViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Cliente, 1: Repartidor
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val customerState = vm.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text("App Delivery", style = MaterialTheme.typography.headlineLarge)
        Text("Inicia sesión para continuar", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0; email = ""; password = ""; courierVm.clearErrors() },
                text = { Text("Cliente") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1; email = ""; password = ""; courierVm.clearErrors() },
                text = { Text("Repartidor") }
            )
        }

        Spacer(Modifier.height(20.dp))

        if (selectedTab == 0) {
            FormField(email, { email = it }, "Correo electrónico", customerState.fieldErrors["email"], KeyboardType.Email)
            FormField(password, { password = it }, "Contraseña", customerState.fieldErrors["password"], isPassword = true)

            customerState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { vm.login(email, password, onLoginSuccess) },
                enabled = !customerState.loading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (customerState.loading) "Entrando..." else "Iniciar sesión como cliente") }

            TextButton(onClick = onGoToRegister, modifier = Modifier.fillMaxWidth()) {
                Text("¿No tienes cuenta? Regístrate como cliente")
            }
        } else {
            FormField(email, { email = it }, "Correo de repartidor", null, KeyboardType.Email)
            FormField(password, { password = it }, "Contraseña", null, isPassword = true)

            courierVm.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            }

            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text("Repartidor demo:", style = MaterialTheme.typography.labelSmall)
                    Text("repartidor@delivery.com / 123456", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { courierVm.login(email, password, onCourierLoginSuccess) },
                enabled = !courierVm.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (courierVm.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(end = 8.dp))
                    Text("Entrando...")
                } else {
                    Text("Ingresar como repartidor")
                }
            }

            TextButton(onClick = onGoToCourierRegister, modifier = Modifier.fillMaxWidth()) {
                Text("¿Quieres repartir con nosotros? Regístrate aquí")
            }
        }
    }
}
