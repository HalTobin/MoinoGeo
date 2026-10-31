package com.moinofactory.moinogeo.demo.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.core.MoinoGeoDatabase
import com.moinofactory.moinogeo.core.model.CountryEntity
import com.moinofactory.moinogeo.flags.FlagIcon
import com.moinofactory.moinogeo.flags.lipis.rectangle.LipisRectangleFlagAssetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoinoGeoAppScreen(db: MoinoGeoDatabase) {
    var countries by remember { mutableStateOf<List<CountryEntity>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            countries = withContext(Dispatchers.IO) {
                db.countryDao().getAllCountries()
            }
        } catch (e: Exception) {
            errorMessage = "Database load error (Ensure moinogeo.db is available): ${e.message}"
        }
    }

    val filteredCountries = countries.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.iso2.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MoinoGeo Demo (KMP / CMP)") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Country or ISO2") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Text(
                    text = "Countries (${filteredCountries.size}):",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCountries) { country ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FlagIcon(
                                    iso2Code = country.iso2,
                                    // Demo CDN until optimize_flags.py populates local SVGs
                                    provider = LipisRectangleFlagAssetProvider(
                                        "https://cdn.jsdelivr.net/gh/lipis/flag-icons@7.5.0/flags/4x3"
                                    ),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1.0f)) {
                                    Text(
                                        text = "${country.name} (${country.iso2})",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Region: ${country.region ?: "N/A"} | Capital: ${country.capital ?: "N/A"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
