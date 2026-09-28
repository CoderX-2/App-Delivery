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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appdelivery.ui.components.FormField

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val state = vm.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))

        FormField(name, { name = it }, "Nombre completo", state.fieldErrors["name"])
        FormField(email, { email = it }, "Correo electrónico", state.fieldErrors["email"], KeyboardType.Email)
        FormField(phone, { phone = it }, "Teléfono (10 dígitos)", state.fieldErrors["phone"], KeyboardType.Phone)
        FormField(password, { password = it }, "Contraseña", state.fieldErrors["password"], isPassword = true)
        FormField(confirm, { confirm = it }, "Confirmar contraseña", state.fieldErrors["confirm"], isPassword = true)

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
        }
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { vm.register(name, email, phone, password, confirm, onRegisterSuccess) },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (state.loading) "Registrando..." else "Registrarme") }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}
