package com.moinofactory.moinogeo.demo.shared.flags.visualizers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moinofactory.moinogeo.core.model.CountryEntity
import com.moinofactory.moinogeo.flags.FlagAssetProvider
import com.moinofactory.moinogeo.flags.FlagIcon

/**
 * List layout showing each flag with country attributes.
 */
@Composable
fun FlagListVisualizer(
    countries: List<CountryEntity>,
    provider: FlagAssetProvider,
    onCountryClick: (CountryEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(countries) { country ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCountryClick(country) },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                ) {
                    FlagIcon(
                        iso2Code = country.iso2,
                        provider = provider,
                        modifier = Modifier.size(48.dp, 36.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${country.name} (${country.iso2})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Capital: ${country.capital ?: "N/A"} | Region: ${country.region ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
