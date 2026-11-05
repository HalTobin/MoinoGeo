package com.moinofactory.moinogeo.demo.shared.flags.visualizers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.core.domain.model.Country
import com.moinofactory.moinogeo.demo.shared.flags.CountryContextClickable
import com.moinofactory.moinogeo.flags.FlagAssetProvider
import com.moinofactory.moinogeo.flags.FlagIcon

/**
 * Grid layout for browsing flags efficiently.
 */
@Composable
internal fun FlagGridVisualizer(
    countries: List<Country>,
    provider: FlagAssetProvider,
    onCountryClick: (Country) -> Unit,
    onCountryGoTo: (Country) -> Unit,
    modifier: Modifier = Modifier,
    fallback: Any? = null,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 90.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(countries) { country ->
            CountryContextClickable(
                country = country,
                onClick = { onCountryClick(country) },
                onGoTo = { onCountryGoTo(country) },
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    ) {
                        FlagIcon(
                            iso2Code = country.iso2,
                            provider = provider,
                            modifier = Modifier.size(52.dp, 38.dp),
                            fallback = fallback,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = country.iso2,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = country.name,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
