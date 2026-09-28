package com.example.appdelivery.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appdelivery.ui.components.FormField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(vm: ProfileViewModel, onDone: () -> Unit) {
    val customer by vm.customer.collectAsStateWithLifecycle()
    var name by remember(customer?.id) { mutableStateOf(customer?.name ?: "") }
    var phone by remember(customer?.id) { mutableStateOf(customer?.phone ?: "") }

    LaunchedEffect(Unit) { vm.clearErrors() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FormField(name, { name = it }, "Nombre completo", vm.formErrors["name"])
            FormField(phone, { phone = it }, "Teléfono (10 dígitos)", vm.formErrors["phone"], KeyboardType.Phone)
            OutlinedTextField(
                value = customer?.email ?: "",
                onValueChange = {},
                label = { Text("Correo electrónico (no editable)") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { vm.updateProfile(name, phone, onDone) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text("Guardar cambios") }
        }
    }
}
