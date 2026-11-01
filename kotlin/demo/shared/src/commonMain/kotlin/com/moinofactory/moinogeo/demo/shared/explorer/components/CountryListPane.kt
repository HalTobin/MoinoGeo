package com.moinofactory.moinogeo.demo.shared.explorer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.core.domain.model.Country
import com.moinofactory.moinogeo.demo.shared.flags.FlagLibraryType
import com.moinofactory.moinogeo.demo.shared.flags.FlagViewMode
import com.moinofactory.moinogeo.demo.shared.flags.visualizers.FlagComparisonVisualizer
import com.moinofactory.moinogeo.demo.shared.flags.visualizers.FlagGridVisualizer
import com.moinofactory.moinogeo.demo.shared.flags.visualizers.FlagListVisualizer

@Composable
internal fun CountryListPane(
    countries: List<Country>,
    isLoading: Boolean,
    selectedLibrary: FlagLibraryType,
    selectedViewMode: FlagViewMode,
    onCountryClick: (Country) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        countries.isEmpty() -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No countries available",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        else -> {
            val provider = remember(selectedLibrary) { selectedLibrary.getProvider() }
            val fallback = remember(selectedLibrary) { selectedLibrary.getFallbackData() }

            Box(modifier = modifier.fillMaxSize()) {
                Text(
                    text = "${countries.size} countries",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                when (selectedViewMode) {
                    FlagViewMode.GRID -> {
                        FlagGridVisualizer(
                            countries = countries,
                            provider = provider,
                            onCountryClick = onCountryClick,
                            fallback = fallback,
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }
                    FlagViewMode.LIST -> {
                        FlagListVisualizer(
                            countries = countries,
                            provider = provider,
                            onCountryClick = onCountryClick,
                            fallback = fallback,
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }
                    FlagViewMode.COMPARE -> {
                        FlagComparisonVisualizer(
                            countries = countries,
                            onCountryClick = onCountryClick,
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }
                }
            }
        }
    }
}
