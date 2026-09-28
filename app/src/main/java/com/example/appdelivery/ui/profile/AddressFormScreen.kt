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
import androidx.compose.ui.unit.dp
import com.example.appdelivery.ui.components.FormField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressFormScreen(vm: ProfileViewModel, addressId: Long?, onDone: () -> Unit) {
    var label by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }

    LaunchedEffect(addressId) {
        vm.clearErrors()
        if (addressId != null) {
            vm.getAddress(addressId)?.let {
                label = it.label
                street = it.street
                city = it.city
                reference = it.reference
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (addressId == null) "Nueva dirección" else "Editar dirección") },
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
            FormField(label, { label = it }, "Nombre (Casa, Trabajo...)", vm.formErrors["label"])
            FormField(street, { street = it }, "Calle y número", vm.formErrors["street"])
            FormField(city, { city = it }, "Ciudad", vm.formErrors["city"])
            FormField(reference, { reference = it }, "Referencia (opcional)", null)
            Button(
                onClick = { vm.saveAddress(addressId, label, street, city, reference, onDone) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text(if (addressId == null) "Guardar dirección" else "Guardar cambios") }
        }
    }
}
