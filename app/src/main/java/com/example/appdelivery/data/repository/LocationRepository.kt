package com.example.appdelivery.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.example.appdelivery.util.GeoPoint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class LocationRepository(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    /** Devuelve la ubicación actual o null si no se pudo obtener. Requiere permiso concedido. */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GeoPoint? = suspendCancellableCoroutine { cont ->
        val cts = CancellationTokenSource()
        cont.invokeOnCancellation { cts.cancel() }
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                cont.resume(loc?.let { GeoPoint(it.latitude, it.longitude) })
            }
            .addOnFailureListener { cont.resume(null) }
    }
}
