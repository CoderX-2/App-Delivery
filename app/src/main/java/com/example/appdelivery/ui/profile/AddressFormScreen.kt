package com.example.appdelivery.ui.profile

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appdelivery.ui.components.FormField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressFormScreen(vm: ProfileViewModel, addressId: Long?, onDone: () -> Unit) {
    var label by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var isLocating by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            scope.launch {
                isLocating = true
                val loc = vm.getCurrentLocation()
                if (loc != null) {
                    latitude = loc.latitude
                    longitude = loc.longitude
                }
                isLocating = false
            }
        }
    }

    LaunchedEffect(addressId) {
        vm.clearErrors()
        if (addressId != null) {
            vm.getAddress(addressId)?.let {
                label = it.label
                street = it.street
                city = it.city
                reference = it.reference
                latitude = it.latitude
                longitude = it.longitude
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

            OutlinedButton(
                onClick = {
                    locationLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Text(
                    text = if (isLocating) " Obteniendo ubicación..." else " Usar mi ubicación actual",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (latitude != null && longitude != null) {
                Text(
                    text = "Coordenadas: %.4f, %.4f".format(latitude, longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Button(
                onClick = {
                    vm.saveAddress(
                        addressId = addressId,
                        label = label,
                        street = street,
                        city = city,
                        reference = reference,
                        onSuccess = onDone,
                        latitude = latitude,
                        longitude = longitude
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text(if (addressId == null) "Guardar dirección" else "Guardar cambios") }
        }
    }
}
