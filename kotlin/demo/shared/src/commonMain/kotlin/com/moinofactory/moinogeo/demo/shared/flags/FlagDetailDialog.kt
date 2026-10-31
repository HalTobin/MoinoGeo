package com.moinofactory.moinogeo.demo.shared.flags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moinofactory.moinogeo.core.model.CountryEntity
import com.moinofactory.moinogeo.demo.shared.ui.DetailRow
import com.moinofactory.moinogeo.flags.FlagIcon

/**
 * Inspection dialog for a single country and its flags across all libraries.
 */
@Composable
fun FlagDetailDialog(
    country: CountryEntity,
    sourceMode: AssetSourceMode,
    onDismiss: () -> Unit,
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
                    fontWeight = FontWeight.SemiBold,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    FlagLibraryType.entries.forEach { library ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FlagIcon(
                                iso2Code = country.iso2,
                                provider = library.getProvider(sourceMode),
                                modifier = Modifier.size(56.dp, 42.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = library.shortName,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
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
        },
    )
}
