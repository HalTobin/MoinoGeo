package com.moinofactory.moinogeo.demo.shared.explorer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.demo.shared.flags.FlagLibraryType
import com.moinofactory.moinogeo.demo.shared.flags.FlagViewMode
import kotlin.math.roundToInt

@Composable
internal fun FlagSettingsDialog(
    selectedLibrary: FlagLibraryType,
    selectedViewMode: FlagViewMode,
    mapVerticalScale: Float,
    mapCenterLatitude: Float,
    mapCenterLongitude: Float,
    mapBordersScaleWithZoom: Boolean,
    onLibrarySelected: (FlagLibraryType) -> Unit,
    onViewModeSelected: (FlagViewMode) -> Unit,
    onMapVerticalScaleChange: (Float) -> Unit,
    onMapCenterLatitudeChange: (Float) -> Unit,
    onMapCenterLongitudeChange: (Float) -> Unit,
    onMapBordersScaleWithZoomChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Flag library",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FlagLibraryType.entries) { library ->
                        FilterChip(
                            selected = selectedLibrary == library &&
                                selectedViewMode != FlagViewMode.COMPARE,
                            onClick = { onLibrarySelected(library) },
                            label = { Text(library.shortName) },
                        )
                    }
                }
                Text(
                    text = if (selectedViewMode == FlagViewMode.COMPARE) {
                        "Comparing all flag libraries side-by-side"
                    } else {
                        selectedLibrary.description
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Country list layout",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(FlagViewMode.entries) { mode ->
                        FilterChip(
                            selected = selectedViewMode == mode,
                            onClick = { onViewModeSelected(mode) },
                            label = { Text(mode.title) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Plane map",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))

                MapSliderRow(
                    label = "Vertical stretch",
                    valueLabel = "${(mapVerticalScale * 100).roundToInt() / 100.0}×",
                    value = mapVerticalScale,
                    valueRange = 0.5f..2.5f,
                    onValueChange = onMapVerticalScaleChange,
                )
                MapSliderRow(
                    label = "Center latitude",
                    valueLabel = "${(mapCenterLatitude * 10).roundToInt() / 10.0}°",
                    value = mapCenterLatitude,
                    valueRange = -90f..90f,
                    onValueChange = onMapCenterLatitudeChange,
                )
                MapSliderRow(
                    label = "Center longitude",
                    valueLabel = "${(mapCenterLongitude * 10).roundToInt() / 10.0}°",
                    value = mapCenterLongitude,
                    valueRange = -180f..180f,
                    onValueChange = onMapCenterLongitudeChange,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Borders scale with zoom",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(
                        checked = mapBordersScaleWithZoom,
                        onCheckedChange = onMapBordersScaleWithZoomChange,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
    )
}

@Composable
private fun MapSliderRow(
    label: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}
