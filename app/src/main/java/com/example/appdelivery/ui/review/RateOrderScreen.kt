package com.example.appdelivery.ui.review

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.appdelivery.data.db.AppDatabase
import com.example.appdelivery.data.review.ReviewRepository
import com.example.appdelivery.data.review.ReviewRepositoryImpl
import com.example.appdelivery.ui.order.OrderViewModel
import kotlinx.coroutines.launch

class ReviewViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    val repo: ReviewRepository = ReviewRepositoryImpl(db.reviewDao(), db.orderDao())

    var isSubmitting by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun submit(
        orderId: String,
        restaurantRating: Int,
        restaurantComment: String,
        courierRating: Int?,
        courierComment: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isSubmitting = true
            errorMessage = null
            val res = repo.submitReview(orderId, restaurantRating, restaurantComment, courierRating, courierComment)
            isSubmitting = false
            res.onSuccess { onSuccess() }
                .onFailure { errorMessage = it.message ?: "Error al enviar reseña" }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateOrderScreen(
    orderId: String,
    orderVm: OrderViewModel,
    reviewVm: ReviewViewModel,
    onBack: () -> Unit
) {
    val orderWithItems by remember(orderId) { orderVm.observeOrder(orderId) }
        .collectAsStateWithLifecycle(initialValue = null)

    var restaurantStars by remember { mutableIntStateOf(5) }
    var restaurantComment by remember { mutableStateOf("") }
    var courierStars by remember { mutableIntStateOf(5) }
    var courierComment by remember { mutableStateOf("") }

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(reviewVm.errorMessage) {
        reviewVm.errorMessage?.let {
            snackbar.showSnackbar(it)
            reviewVm.errorMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Calificar pedido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        val order = orderWithItems?.order
        if (order == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("¿Qué te pareció ${order.restaurantName}?", style = MaterialTheme.typography.titleMedium)
                        StarRatingBar(rating = restaurantStars, onRatingChanged = { restaurantStars = it })
                        OutlinedTextField(
                            value = restaurantComment,
                            onValueChange = { restaurantComment = it },
                            label = { Text("Comentario sobre la comida (opcional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (order.courierId != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("¿Cómo fue el servicio de entrega?", style = MaterialTheme.typography.titleMedium)
                            StarRatingBar(rating = courierStars, onRatingChanged = { courierStars = it })
                            OutlinedTextField(
                                value = courierComment,
                                onValueChange = { courierComment = it },
                                label = { Text("Comentario sobre el repartidor (opcional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        reviewVm.submit(
                            orderId = order.id,
                            restaurantRating = restaurantStars,
                            restaurantComment = restaurantComment,
                            courierRating = if (order.courierId != null) courierStars else null,
                            courierComment = courierComment,
                            onSuccess = onBack
                        )
                    },
                    enabled = !reviewVm.isSubmitting,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(if (reviewVm.isSubmitting) "Guardando calificación..." else "Enviar calificación")
                }
            }
        }
    }
}

@Composable
fun StarRatingBar(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFB300) else Color.Gray,
                modifier = Modifier.size(36.dp).clickable { onRatingChanged(i) }
            )
        }
    }
}
