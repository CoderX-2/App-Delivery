package com.example.appdelivery.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appdelivery.util.GeoPoint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private const val FINE = Manifest.permission.ACCESS_FINE_LOCATION
private const val COARSE = Manifest.permission.ACCESS_COARSE_LOCATION

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, FINE) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, COARSE) == PackageManager.PERMISSION_GRANTED

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    vm: MapViewModel,
    onOpenMenu: (Long) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val status by vm.status.collectAsStateWithLifecycle()
    val userLocation by vm.userLocation.collectAsStateWithLifecycle()
    val nearby by vm.nearby.collectAsStateWithLifecycle()
    val user = userLocation

    var granted by remember { mutableStateOf(hasLocationPermission(context)) }
    var denied by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = result[FINE] == true || result[COARSE] == true
        denied = !granted
    }

    // Pide el permiso al entrar si todavía no se tiene
    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(arrayOf(FINE, COARSE))
    }

    // Al volver a la pantalla (por ejemplo desde los ajustes) revisa permiso y ubicación
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        granted = hasLocationPermission(context)
        if (granted) {
            denied = false
            vm.loadLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restaurantes cercanos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                !granted -> {
                    if (denied) {
                        MessageState(
                            title = "Necesitamos tu ubicación",
                            text = "Para mostrar los restaurantes a 5 km de ti, permite el acceso a la ubicación.",
                            primaryLabel = "Conceder permiso",
                            onPrimary = { launcher.launch(arrayOf(FINE, COARSE)) },
                            secondaryLabel = "Abrir ajustes de la app",
                            onSecondary = {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    )
                                )
                            }
                        )
                    }
                }
                status == LocationStatus.UNAVAILABLE -> MessageState(
                    title = "No pudimos obtener tu ubicación",
                    text = "Verifica que el GPS esté activado e inténtalo de nuevo.",
                    primaryLabel = "Reintentar",
                    onPrimary = { vm.loadLocation() },
                    secondaryLabel = "Abrir ajustes de ubicación",
                    onSecondary = {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                )
                status == LocationStatus.READY && user != null ->
                    NearbyContent(user, nearby, onOpenMenu)
                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Obteniendo tu ubicación...")
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyContent(
    user: GeoPoint,
    nearby: List<NearbyRestaurant>,
    onOpenMenu: (Long) -> Unit
) {
    val userLatLng = LatLng(user.latitude, user.longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 12f)
    }
    LaunchedEffect(userLatLng) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxWidth().weight(1f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(zoomControlsEnabled = true)
        ) {
            Circle(
                center = userLatLng,
                radius = MapViewModel.RADIUS_KM * 1000,
                strokeColor = Color(0xFF3F51B5),
                strokeWidth = 3f,
                fillColor = Color(0x203F51B5)
            )
            nearby.forEach { item ->
                key(item.restaurant.id) {
                    Marker(
                        state = rememberMarkerState(
                            position = LatLng(item.restaurant.latitude, item.restaurant.longitude)
                        ),
                        title = item.restaurant.name,
                        snippet = "%.1f km · toca para ver el menú".format(item.distanceKm),
                        onInfoWindowClick = { onOpenMenu(item.restaurant.id) }
                    )
                }
            }
        }

        Text(
            text = if (nearby.isEmpty()) "No hay restaurantes dentro de 5 km"
            else "${nearby.size} restaurante(s) a menos de 5 km",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nearby, key = { it.restaurant.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenMenu(item.restaurant.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.restaurant.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${item.restaurant.cuisine} · ${item.restaurant.address}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text("%.1f km".format(item.distanceKm), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageState(
    title: String,
    text: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(text, textAlign = TextAlign.Center)
            Button(onClick = onPrimary) { Text(primaryLabel) }
            OutlinedButton(onClick = onSecondary) { Text(secondaryLabel) }
        }
    }
}
