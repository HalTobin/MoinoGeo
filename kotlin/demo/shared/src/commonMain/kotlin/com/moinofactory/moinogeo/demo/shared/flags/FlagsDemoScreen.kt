package com.moinofactory.moinogeo.demo.shared.flags

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.core.MoinoGeoDatabase
import com.moinofactory.moinogeo.core.model.CountryEntity
import com.moinofactory.moinogeo.demo.shared.data.sampleCountries
import com.moinofactory.moinogeo.demo.shared.flags.components.FlagLibrarySelector
import com.moinofactory.moinogeo.demo.shared.flags.components.FlagOptionsBar
import com.moinofactory.moinogeo.demo.shared.flags.components.FlagSearchField
import com.moinofactory.moinogeo.demo.shared.flags.visualizers.FlagComparisonVisualizer
import com.moinofactory.moinogeo.demo.shared.flags.visualizers.FlagGridVisualizer
import com.moinofactory.moinogeo.demo.shared.flags.visualizers.FlagListVisualizer
import com.moinofactory.moinogeo.demo.shared.ui.StatusBanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Showcase for the swappable flag-icon architecture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagsDemoScreen(db: MoinoGeoDatabase) {
    var countries by remember { mutableStateOf<List<CountryEntity>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLibrary by remember { mutableStateOf(FlagLibraryType.LIPIS_RECTANGLE) }
    var selectedSource by remember { mutableStateOf(AssetSourceMode.LOCAL_ASSETS) }
    var selectedViewMode by remember { mutableStateOf(FlagViewMode.GRID) }
    var inspectedCountry by remember { mutableStateOf<CountryEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var statusInfo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val dbCountries = withContext(Dispatchers.IO) {
                db.countryDao().getAllCountries()
            }
            if (dbCountries.isNotEmpty()) {
                countries = dbCountries
                statusInfo = "Loaded ${dbCountries.size} countries from database"
            } else {
                countries = sampleCountries
                statusInfo = "Using sample country dataset (${sampleCountries.size} countries)"
            }
        } catch (_: Exception) {
            countries = sampleCountries
            statusInfo = "DB load deferred. Showing ${sampleCountries.size} sample countries."
        } finally {
            isLoading = false
        }
    }

    val filteredCountries = remember(countries, searchQuery) {
        if (searchQuery.isBlank()) {
            countries
        } else {
            countries.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.iso2.contains(searchQuery, ignoreCase = true) ||
                    it.iso3.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MoinoGeo Flag Libraries Demo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Swappable Flag Icons Architecture",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            statusInfo?.let { StatusBanner(message = it) }

            FlagLibrarySelector(
                selectedLibrary = selectedLibrary,
                selectedViewMode = selectedViewMode,
                onLibrarySelected = { library ->
                    selectedLibrary = library
                    if (selectedViewMode == FlagViewMode.COMPARE) {
                        selectedViewMode = FlagViewMode.GRID
                    }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlagOptionsBar(
                selectedViewMode = selectedViewMode,
                selectedSource = selectedSource,
                onViewModeSelected = { selectedViewMode = it },
                onSourceToggle = {
                    selectedSource = if (selectedSource == AssetSourceMode.CDN) {
                        AssetSourceMode.LOCAL_ASSETS
                    } else {
                        AssetSourceMode.CDN
                    }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlagSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                filteredCountries.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No countries match '$searchQuery'",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                else -> {
                    Text(
                        text = "Visualising ${filteredCountries.size} flags (${selectedSource.label}):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    val activeProvider = selectedLibrary.getProvider(selectedSource)

                    when (selectedViewMode) {
                        FlagViewMode.GRID -> {
                            FlagGridVisualizer(
                                countries = filteredCountries,
                                provider = activeProvider,
                                onCountryClick = { inspectedCountry = it },
                            )
                        }
                        FlagViewMode.LIST -> {
                            FlagListVisualizer(
                                countries = filteredCountries,
                                provider = activeProvider,
                                onCountryClick = { inspectedCountry = it },
                            )
                        }
                        FlagViewMode.COMPARE -> {
                            FlagComparisonVisualizer(
                                countries = filteredCountries,
                                sourceMode = selectedSource,
                                onCountryClick = { inspectedCountry = it },
                            )
                        }
                    }
                }
            }
        }
    }

    inspectedCountry?.let { country ->
        FlagDetailDialog(
            country = country,
            sourceMode = selectedSource,
            onDismiss = { inspectedCountry = null },
        )
    }
}
