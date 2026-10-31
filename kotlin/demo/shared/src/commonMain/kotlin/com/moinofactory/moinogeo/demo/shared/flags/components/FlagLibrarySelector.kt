package com.moinofactory.moinogeo.demo.shared.flags.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.demo.shared.flags.FlagLibraryType
import com.moinofactory.moinogeo.demo.shared.flags.FlagViewMode

@Composable
fun FlagLibrarySelector(
    selectedLibrary: FlagLibraryType,
    selectedViewMode: FlagViewMode,
    onLibrarySelected: (FlagLibraryType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "1. Active Flag Library (Icon Set):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FlagLibraryType.entries.forEach { library ->
                    FilterChip(
                        selected = selectedLibrary == library && selectedViewMode != FlagViewMode.COMPARE,
                        onClick = { onLibrarySelected(library) },
                        label = {
                            Text(
                                text = library.shortName,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Text(
                text = if (selectedViewMode == FlagViewMode.COMPARE) {
                    "Comparing all 4 flag libraries side-by-side"
                } else {
                    selectedLibrary.description
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
