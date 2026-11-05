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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.demo.shared.explorer.DemoMapMode
import com.moinofactory.moinogeo.demo.shared.explorer.DemoMapSettings
import kotlin.math.roundToInt

@Composable
internal fun MapSettingsDialog(
    mode: DemoMapMode,
    settings: DemoMapSettings,
    onMapHorizontalScaleChange: (Float) -> Unit,
    onMapVerticalScaleChange: (Float) -> Unit,
    onMapCenterLatitudeChange: (Float) -> Unit,
    onMapCenterLongitudeChange: (Float) -> Unit,
    onMapRotationAngleChange: (Float) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${mode.title} map") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "These controls apply only to the ${mode.title} map. " +
                        "Horizontal and vertical stretch distort the projection " +
                        "inside the available canvas (base ${formatAspect(mode.baseAspectRatio)}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                MapSliderRow(
                    label = "Horizontal stretch",
                    valueLabel = "${(settings.horizontalScale * 100).roundToInt() / 100.0}×",
                    value = settings.horizontalScale,
                    valueRange = 0.5f..2.5f,
                    onValueChange = onMapHorizontalScaleChange,
                )
                MapSliderRow(
                    label = "Vertical stretch",
                    valueLabel = "${(settings.verticalScale * 100).roundToInt() / 100.0}×",
                    value = settings.verticalScale,
                    valueRange = 0.5f..2.5f,
                    onValueChange = onMapVerticalScaleChange,
                )
                Text(
                    text = "Effective aspect: ${formatAspect(settings.aspectRatio(mode.baseAspectRatio))}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                MapSliderRow(
                    label = "Center latitude",
                    valueLabel = "${(settings.centerLatitude * 10).roundToInt() / 10.0}°",
                    value = settings.centerLatitude,
                    valueRange = -90f..90f,
                    onValueChange = onMapCenterLatitudeChange,
                )
                MapSliderRow(
                    label = "Center longitude",
                    valueLabel = "${(settings.centerLongitude * 10).roundToInt() / 10.0}°",
                    value = settings.centerLongitude,
                    valueRange = -180f..180f,
                    onValueChange = onMapCenterLongitudeChange,
                )
                MapSliderRow(
                    label = "Rotation angle",
                    valueLabel = "${settings.rotationAngle.roundToInt()}°",
                    value = settings.rotationAngle,
                    valueRange = -180f..180f,
                    onValueChange = onMapRotationAngleChange,
                )
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

private fun formatAspect(ratio: Float): String {
    val rounded = (ratio * 100).roundToInt() / 100.0
    return "$rounded:1"
}
