package com.moinofactory.moinogeo.demo.shared.explorer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun MapSettingsDialog(
    mapVerticalScale: Float,
    mapCenterLatitude: Float,
    mapCenterLongitude: Float,
    mapRotationAngle: Float,
    mapBordersScaleWithZoom: Boolean,
    onMapVerticalScaleChange: (Float) -> Unit,
    onMapCenterLatitudeChange: (Float) -> Unit,
    onMapCenterLongitudeChange: (Float) -> Unit,
    onMapRotationAngleChange: (Float) -> Unit,
    onMapBordersScaleWithZoomChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Map Settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
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
                MapSliderRow(
                    label = "Rotation angle",
                    valueLabel = "${mapRotationAngle.roundToInt()}°",
                    value = mapRotationAngle,
                    valueRange = -180f..180f,
                    onValueChange = onMapRotationAngleChange,
                )

                Spacer(modifier = Modifier.height(8.dp))

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
