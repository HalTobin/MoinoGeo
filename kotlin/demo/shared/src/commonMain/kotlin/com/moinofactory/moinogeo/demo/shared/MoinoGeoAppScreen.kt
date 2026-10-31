package com.moinofactory.moinogeo.demo.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moinofactory.moinogeo.core.MoinoGeoDatabase
import com.moinofactory.moinogeo.core.model.CountryEntity
import com.moinofactory.moinogeo.flags.FlagAssetProvider
import com.moinofactory.moinogeo.flags.FlagIcon
import com.moinofactory.moinogeo.flags.circle.CircleFlagAssetProvider
import com.moinofactory.moinogeo.flags.flagpack.FlagpackAssetProvider
import com.moinofactory.moinogeo.flags.lipis.rectangle.LipisRectangleFlagAssetProvider
import com.moinofactory.moinogeo.flags.lipis.square.LipisSquareFlagAssetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Flag Libraries available in `@kotlin/flags`.
 */
enum class FlagLibraryType(
    val title: String,
    val shortName: String,
    val description: String,
    val cdnBaseUrl: String,
    private val localProvider: () -> FlagAssetProvider,
    private val cdnProvider: (basePath: String) -> FlagAssetProvider,
) {
    LIPIS_RECTANGLE(
        title = "Lipis Rectangle (4×3)",
        shortName = "Lipis 4x3",
        description = "Standard realistic 4×3 rectangular vector flags (lipis/flag-icons)",
        cdnBaseUrl = "https://cdn.jsdelivr.net/gh/lipis/flag-icons@7.5.0/flags/4x3",
        localProvider = { LipisRectangleFlagAssetProvider.classpathAssets() },
        cdnProvider = { LipisRectangleFlagAssetProvider(it) },
    ),
    LIPIS_SQUARE(
        title = "Lipis Square (1×1)",
        shortName = "Lipis 1x1",
        description = "1×1 square vector flags (lipis/flag-icons)",
        cdnBaseUrl = "https://cdn.jsdelivr.net/gh/lipis/flag-icons@7.5.0/flags/1x1",
        localProvider = { LipisSquareFlagAssetProvider.classpathAssets() },
        cdnProvider = { LipisSquareFlagAssetProvider(it) },
    ),
    FLAGPACK(
        title = "Yummygum Flagpack",
        shortName = "Flagpack",
        description = "Stylized rounded rectangular flags (Yummygum/flagpack-core)",
        cdnBaseUrl = "https://cdn.jsdelivr.net/gh/Yummygum/flagpack-core@v2.0.0/svg_l",
        localProvider = { FlagpackAssetProvider.classpathAssets() },
        cdnProvider = { FlagpackAssetProvider(it) },
    ),
    CIRCLE(
        title = "HatScripts Circle (1×1)",
        shortName = "Circle",
        description = "Circular 1×1 avatar flags (HatScripts/circle-flags)",
        cdnBaseUrl = "https://cdn.jsdelivr.net/gh/HatScripts/circle-flags@gh-pages/flags",
        localProvider = { CircleFlagAssetProvider.classpathAssets() },
        cdnProvider = { CircleFlagAssetProvider(it) },
    );

    fun getProvider(source: AssetSourceMode): FlagAssetProvider = when (source) {
        AssetSourceMode.CDN -> cdnProvider(cdnBaseUrl)
        AssetSourceMode.LOCAL_ASSETS -> localProvider()
    }
}

/**
 * Source of assets for resolving SVG files.
 */
enum class AssetSourceMode(val label: String, val description: String) {
    CDN("Remote CDN (jsDelivr)", "Loads optimized SVGs from remote CDN"),
    LOCAL_ASSETS("Bundled Resources", "Loads SVGs embedded in the flag library modules")
}

/**
 * Display modes for visualising flags.
 */
enum class FlagViewMode(val title: String) {
    GRID("Grid Gallery"),
    LIST("Country List"),
    COMPARE("Compare All 4")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoinoGeoAppScreen(db: MoinoGeoDatabase) {
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
        } catch (e: Exception) {
            countries = sampleCountries
            statusInfo = "DB load deferred. Showing ${sampleCountries.size} sample countries."
        } finally {
            isLoading = false
        }
    }

    val filteredCountries = remember(countries, searchQuery) {
        if (searchQuery.isBlank()) countries
        else countries.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.iso2.contains(searchQuery, ignoreCase = true) ||
            it.iso3.contains(searchQuery, ignoreCase = true)
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
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Swappable Flag Icons Architecture",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Status bar
            statusInfo?.let { status ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // 1. Flag Library Swapper Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "1. Active Flag Library (Icon Set):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Library Chips Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FlagLibraryType.entries.forEach { library ->
                            FilterChip(
                                selected = selectedLibrary == library && selectedViewMode != FlagViewMode.COMPARE,
                                onClick = {
                                    selectedLibrary = library
                                    if (selectedViewMode == FlagViewMode.COMPARE) {
                                        selectedViewMode = FlagViewMode.GRID
                                    }
                                },
                                label = {
                                    Text(
                                        text = library.shortName,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text(
                        text = if (selectedViewMode == FlagViewMode.COMPARE)
                            "Comparing all 4 flag libraries side-by-side"
                        else
                            selectedLibrary.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Options Bar: View Mode & Asset Source
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Mode Tabs
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FlagViewMode.entries.forEach { mode ->
                        ElevatedFilterChip(
                            selected = selectedViewMode == mode,
                            onClick = { selectedViewMode = mode },
                            label = { Text(mode.title, fontSize = 12.sp) }
                        )
                    }
                }

                // Asset Source Toggle
                AssistChip(
                    onClick = {
                        selectedSource = if (selectedSource == AssetSourceMode.CDN)
                            AssetSourceMode.LOCAL_ASSETS
                        else
                            AssetSourceMode.CDN
                    },
                    label = { Text(selectedSource.name, fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search country or ISO code...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Main Flag Visualizer Content
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredCountries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No countries match '$searchQuery'",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Text(
                    text = "Visualising ${filteredCountries.size} flags (${selectedSource.label}):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val activeProvider = selectedLibrary.getProvider(selectedSource)

                when (selectedViewMode) {
                    FlagViewMode.GRID -> {
                        FlagGridVisualizer(
                            countries = filteredCountries,
                            provider = activeProvider,
                            onCountryClick = { inspectedCountry = it }
                        )
                    }
                    FlagViewMode.LIST -> {
                        FlagListVisualizer(
                            countries = filteredCountries,
                            provider = activeProvider,
                            onCountryClick = { inspectedCountry = it }
                        )
                    }
                    FlagViewMode.COMPARE -> {
                        FlagComparisonVisualizer(
                            countries = filteredCountries,
                            sourceMode = selectedSource,
                            onCountryClick = { inspectedCountry = it }
                        )
                    }
                }
            }
        }
    }

    // Country & Flags Detail Inspection Dialog
    inspectedCountry?.let { country ->
        FlagDetailDialog(
            country = country,
            sourceMode = selectedSource,
            onDismiss = { inspectedCountry = null }
        )
    }
}

/**
 * Grid layout to visualising all available flags efficiently.
 */
@Composable
private fun FlagGridVisualizer(
    countries: List<CountryEntity>,
    provider: FlagAssetProvider,
    onCountryClick: (CountryEntity) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 90.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(countries) { country ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCountryClick(country) }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(10.dp).fillMaxWidth()
                ) {
                    FlagIcon(
                        iso2Code = country.iso2,
                        provider = provider,
                        modifier = Modifier.size(52.dp, 38.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = country.iso2,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = country.name,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * List layout showing flag with detailed country attributes.
 */
@Composable
private fun FlagListVisualizer(
    countries: List<CountryEntity>,
    provider: FlagAssetProvider,
    onCountryClick: (CountryEntity) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(countries) { country ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCountryClick(country) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp).fillMaxWidth()
                ) {
                    FlagIcon(
                        iso2Code = country.iso2,
                        provider = provider,
                        modifier = Modifier.size(48.dp, 36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${country.name} (${country.iso2})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Capital: ${country.capital ?: "N/A"} | Region: ${country.region ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    country.emoji?.let { emoji ->
                        Text(text = emoji, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}

/**
 * Comparison layout rendering each country flag with all 4 flag libraries side-by-side!
 */
@Composable
private fun FlagComparisonVisualizer(
    countries: List<CountryEntity>,
    sourceMode: AssetSourceMode,
    onCountryClick: (CountryEntity) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(countries) { country ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCountryClick(country) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${country.name} (${country.iso2})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FlagLibraryType.entries.forEach { library ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(56.dp, 40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(2.dp)
                                ) {
                                    FlagIcon(
                                        iso2Code = country.iso2,
                                        provider = library.getProvider(sourceMode),
                                        modifier = Modifier.size(48.dp, 34.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = library.shortName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
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

/**
 * Inspection dialog for a single country and its flags rendered across all libraries.
 */
@Composable
private fun FlagDetailDialog(
    country: CountryEntity,
    sourceMode: AssetSourceMode,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                country.emoji?.let {
                    Text(text = it, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = country.name, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Flag Rendering across all 4 Libraries:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FlagLibraryType.entries.forEach { library ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FlagIcon(
                                iso2Code = country.iso2,
                                provider = library.getProvider(sourceMode),
                                modifier = Modifier.size(56.dp, 42.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = library.shortName,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DetailRow("ISO Alpha-2:", country.iso2)
                    DetailRow("ISO Alpha-3:", country.iso3)
                    country.capital?.let { DetailRow("Capital:", it) }
                    country.region?.let { DetailRow("Region:", it) }
                    country.currency?.let { DetailRow("Currency:", it) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Sample fallback dataset of countries for standalone visualising when DB is empty.
 */
private val sampleCountries = listOf(
    CountryEntity(1, "United States", "US", "USA", "840", "1", "Washington D.C.", "USD", "$", ".us", "Americas", "Northern America", 38.0, -97.0, "🇺🇸", null),
    CountryEntity(2, "France", "FR", "FRA", "250", "33", "Paris", "EUR", "€", ".fr", "Europe", "Western Europe", 46.0, 2.0, "🇫🇷", null),
    CountryEntity(3, "Germany", "DE", "DEU", "276", "49", "Berlin", "EUR", "€", ".de", "Europe", "Western Europe", 51.0, 9.0, "🇩🇪", null),
    CountryEntity(4, "Japan", "JP", "JPN", "392", "81", "Tokyo", "JPY", "¥", ".jp", "Asia", "Eastern Asia", 36.0, 138.0, "🇯🇵", null),
    CountryEntity(5, "Brazil", "BR", "BRA", "076", "55", "Brasília", "BRL", "R$", ".br", "Americas", "South America", -10.0, -55.0, "🇧🇷", null),
    CountryEntity(6, "United Kingdom", "GB", "GBR", "826", "44", "London", "GBP", "£", ".uk", "Europe", "Northern Europe", 54.0, -2.0, "🇬🇧", null),
    CountryEntity(7, "Canada", "CA", "CAN", "124", "1", "Ottawa", "CAD", "$", ".ca", "Americas", "Northern America", 60.0, -95.0, "🇨🇦", null),
    CountryEntity(8, "Australia", "AU", "AUS", "036", "61", "Canberra", "AUD", "$", ".au", "Oceania", "Australia and New Zealand", -27.0, 133.0, "🇦🇺", null),
    CountryEntity(9, "Italy", "IT", "ITA", "380", "39", "Rome", "EUR", "€", ".it", "Europe", "Southern Europe", 42.8, 12.8, "🇮🇹", null),
    CountryEntity(10, "Spain", "ES", "ESP", "724", "34", "Madrid", "EUR", "€", ".es", "Europe", "Southern Europe", 40.0, -4.0, "🇪🇸", null),
    CountryEntity(11, "India", "IN", "IND", "356", "91", "New Delhi", "INR", "₹", ".in", "Asia", "Southern Asia", 20.0, 77.0, "🇮🇳", null),
    CountryEntity(12, "China", "CN", "CHN", "156", "86", "Beijing", "CNY", "¥", ".cn", "Asia", "Eastern Asia", 35.0, 105.0, "🇨🇳", null),
    CountryEntity(13, "South Korea", "KR", "KOR", "410", "82", "Seoul", "KRW", "₩", ".kr", "Asia", "Eastern Asia", 37.0, 127.5, "🇰🇷", null),
    CountryEntity(14, "Mexico", "MX", "MEX", "484", "52", "Mexico City", "MXN", "$", ".mx", "Americas", "Central America", 23.0, -102.0, "🇲🇽", null),
    CountryEntity(15, "South Africa", "ZA", "ZAF", "710", "27", "Pretoria", "ZAR", "R", ".za", "Africa", "Southern Africa", -29.0, 24.0, "🇿🇦", null),
    CountryEntity(16, "Netherlands", "NL", "NLD", "528", "31", "Amsterdam", "EUR", "€", ".nl", "Europe", "Western Europe", 52.5, 5.75, "🇳🇱", null),
    CountryEntity(17, "Switzerland", "CH", "CHE", "756", "41", "Bern", "CHF", "CHF", ".ch", "Europe", "Western Europe", 47.0, 8.0, "🇨🇭", null),
    CountryEntity(18, "Argentina", "AR", "ARG", "032", "54", "Buenos Aires", "ARS", "$", ".ar", "Americas", "South America", -34.0, -64.0, "🇦🇷", null),
    CountryEntity(19, "Sweden", "SE", "SWE", "752", "46", "Stockholm", "SEK", "kr", ".se", "Europe", "Northern Europe", 62.0, 15.0, "🇸🇪", null),
    CountryEntity(20, "Ukraine", "UA", "UKR", "804", "380", "Kyiv", "UAH", "₴", ".ua", "Europe", "Eastern Europe", 49.0, 32.0, "🇺🇦", null),
)
