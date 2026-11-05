package com.moinofactory.moinogeo.demo.shared.explorer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.demo.shared.explorer.DemoMapStylePreset
import com.moinofactory.moinogeo.demo.shared.explorer.DemoMapStyleSettings
import com.moinofactory.moinogeo.demo.shared.explorer.displayName
import com.moinofactory.moinogeo.maps.core.TopoZone

@Composable
internal fun MapStyleDialog(
    settings: DemoMapStyleSettings,
    onPresetSelected: (DemoMapStylePreset) -> Unit,
    onShowBordersChange: (Boolean) -> Unit,
    onShowTopographyChange: (Boolean) -> Unit,
    onBordersScaleWithZoomChange: (Boolean) -> Unit,
    onZoneColorChange: (TopoZone, Color) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Map style") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "These options apply to every map mode.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Preset",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DemoMapStylePreset.entries.forEach { preset ->
                        FilterChip(
                            selected = settings.preset == preset,
                            onClick = { onPresetSelected(preset) },
                            label = { Text(preset.title) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                StyleSwitchRow(
                    label = "Show topography",
                    checked = settings.showTopography,
                    onCheckedChange = onShowTopographyChange,
                )
                StyleSwitchRow(
                    label = "Show borders",
                    checked = settings.showBorders,
                    onCheckedChange = onShowBordersChange,
                )
                StyleSwitchRow(
                    label = "Borders scale with zoom",
                    checked = settings.bordersScaleWithZoom,
                    onCheckedChange = onBordersScaleWithZoomChange,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Topography colors",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Tap a zone to edit its color (HSV + hex).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    TopoZone.entries.forEach { zone ->
                        val color = settings.zoneColors[zone] ?: Color.Gray
                        ColorPickerRow(
                            label = zone.displayName(),
                            color = color,
                            onColorChange = { onZoneColorChange(zone, it) },
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onReset) {
                Text("Default")
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
private fun StyleSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
