package com.example.appdelivery.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantListScreen(
    vm: CatalogViewModel,
    onOpenMenu: (Long) -> Unit,
    onSearch: () -> Unit,
    onOpenMap: () -> Unit,
    onBack: () -> Unit
) {
    val restaurants by vm.restaurants.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restaurantes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar platillos")
                    }
                    IconButton(onClick = onOpenMap) {
                        Icon(Icons.Default.Map, contentDescription = "Ver mapa de restaurantes cercanos")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(restaurants, key = { it.id }) { r ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onOpenMenu(r.id) }) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(r.name, style = MaterialTheme.typography.titleMedium)
                        Text("${r.cuisine} · ${r.address}", style = MaterialTheme.typography.bodySmall)
                        Text(r.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
