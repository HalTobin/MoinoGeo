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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.demo.shared.explorer.DemoMapMode
import com.moinofactory.moinogeo.demo.shared.explorer.DemoMapSettings
import com.moinofactory.moinogeo.demo.shared.explorer.DemoSphereBackgroundKind
import com.moinofactory.moinogeo.demo.shared.explorer.DemoSphereLightingKind
import com.moinofactory.moinogeo.demo.shared.explorer.DemoSphereSettings
import kotlin.math.roundToInt

@Composable
internal fun MapSettingsDialog(
    mode: DemoMapMode,
    settings: DemoMapSettings,
    sphereSettings: DemoSphereSettings,
    onMapHorizontalScaleChange: (Float) -> Unit,
    onMapVerticalScaleChange: (Float) -> Unit,
    onMapVerticalWarpChange: (Float) -> Unit,
    onMapCenterLatitudeChange: (Float) -> Unit,
    onMapCenterLongitudeChange: (Float) -> Unit,
    onMapRotationAngleChange: (Float) -> Unit,
    onApplyPreset: (DemoMapSettings) -> Unit,
    onSphereBackgroundKindChange: (DemoSphereBackgroundKind) -> Unit,
    onSphereSolidColorChange: (Color) -> Unit,
    onSphereLightingKindChange: (DemoSphereLightingKind) -> Unit,
    onSphereSunLatitudeChange: (Float) -> Unit,
    onSphereSunLongitudeChange: (Float) -> Unit,
    onSphereSunAmbientChange: (Float) -> Unit,
    onSphereAuraEnabledChange: (Boolean) -> Unit,
    onSphereAuraColorChange: (Color) -> Unit,
    onSphereAuraIntensityChange: (Float) -> Unit,
    onSphereAuraThicknessChange: (Float) -> Unit,
    onSphereAuraBloomChange: (Float) -> Unit,
    onResetSphereSettings: () -> Unit,
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
                when (mode) {
                    DemoMapMode.Sphere -> SphereSettingsContent(
                        settings = sphereSettings,
                        onBackgroundKindChange = onSphereBackgroundKindChange,
                        onSolidColorChange = onSphereSolidColorChange,
                        onLightingKindChange = onSphereLightingKindChange,
                        onSunLatitudeChange = onSphereSunLatitudeChange,
                        onSunLongitudeChange = onSphereSunLongitudeChange,
                        onSunAmbientChange = onSphereSunAmbientChange,
                        onAuraEnabledChange = onSphereAuraEnabledChange,
                        onAuraColorChange = onSphereAuraColorChange,
                        onAuraIntensityChange = onSphereAuraIntensityChange,
                        onAuraThicknessChange = onSphereAuraThicknessChange,
                        onAuraBloomChange = onSphereAuraBloomChange,
                    )
                    DemoMapMode.Flat,
                    DemoMapMode.Azimuthal,
                    -> ProjectionSettingsContent(
                        mode = mode,
                        settings = settings,
                        onMapHorizontalScaleChange = onMapHorizontalScaleChange,
                        onMapVerticalScaleChange = onMapVerticalScaleChange,
                        onMapVerticalWarpChange = onMapVerticalWarpChange,
                        onMapCenterLatitudeChange = onMapCenterLatitudeChange,
                        onMapCenterLongitudeChange = onMapCenterLongitudeChange,
                        onMapRotationAngleChange = onMapRotationAngleChange,
                    )
                }
            }
        },
        dismissButton = {
            when (mode) {
                DemoMapMode.Flat -> {
                    Row(horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onApplyPreset(DemoMapSettings.Linear) }) {
                            Text("Linear")
                        }
                        TextButton(onClick = { onApplyPreset(DemoMapSettings.Standard) }) {
                            Text("Standard")
                        }
                    }
                }
                DemoMapMode.Azimuthal -> {
                    TextButton(onClick = { onApplyPreset(DemoMapSettings.Linear) }) {
                        Text("Default")
                    }
                }
                DemoMapMode.Sphere -> {
                    TextButton(onClick = onResetSphereSettings) {
                        Text("Default")
                    }
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
private fun ProjectionSettingsContent(
    mode: DemoMapMode,
    settings: DemoMapSettings,
    onMapHorizontalScaleChange: (Float) -> Unit,
    onMapVerticalScaleChange: (Float) -> Unit,
    onMapVerticalWarpChange: (Float) -> Unit,
    onMapCenterLatitudeChange: (Float) -> Unit,
    onMapCenterLongitudeChange: (Float) -> Unit,
    onMapRotationAngleChange: (Float) -> Unit,
) {
    val isFlat = mode == DemoMapMode.Flat
    Text(
        text = if (isFlat) {
            "These controls apply only to the Flat map. " +
                "Uniform stretch changes aspect; vertical falloff " +
                "stretches exponentially away from the equator " +
                "(0 = linear). Standard matches common world-map rendering."
        } else {
            "These controls apply only to the ${mode.title} map. " +
                "Horizontal and vertical stretch distort the projection " +
                "inside the available canvas " +
                "(base ${formatAspect(mode.baseAspectRatio)})."
        },
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
    if (isFlat) {
        MapSliderRow(
            label = "Vertical falloff",
            valueLabel = formatWarp(settings.verticalWarp),
            value = settings.verticalWarp,
            valueRange = -3f..3f,
            onValueChange = onMapVerticalWarpChange,
        )
    }
    Text(
        text = "Effective aspect: ${formatAspect(settings.aspectRatio(mode.baseAspectRatio))}",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
    if (!isFlat) {
        MapSliderRow(
            label = "Center latitude",
            valueLabel = "${(settings.centerLatitude * 10).roundToInt() / 10.0}°",
            value = settings.centerLatitude,
            valueRange = -90f..90f,
            onValueChange = onMapCenterLatitudeChange,
        )
    }
    MapSliderRow(
        label = "Center longitude",
        valueLabel = "${(settings.centerLongitude * 10).roundToInt() / 10.0}°",
        value = settings.centerLongitude,
        valueRange = -180f..180f,
        onValueChange = onMapCenterLongitudeChange,
    )
    if (!isFlat) {
        MapSliderRow(
            label = "Rotation angle",
            valueLabel = "${settings.rotationAngle.roundToInt()}°",
            value = settings.rotationAngle,
            valueRange = -180f..180f,
            onValueChange = onMapRotationAngleChange,
        )
    }
}

@Composable
private fun SphereSettingsContent(
    settings: DemoSphereSettings,
    onBackgroundKindChange: (DemoSphereBackgroundKind) -> Unit,
    onSolidColorChange: (Color) -> Unit,
    onLightingKindChange: (DemoSphereLightingKind) -> Unit,
    onSunLatitudeChange: (Float) -> Unit,
    onSunLongitudeChange: (Float) -> Unit,
    onSunAmbientChange: (Float) -> Unit,
    onAuraEnabledChange: (Boolean) -> Unit,
    onAuraColorChange: (Color) -> Unit,
    onAuraIntensityChange: (Float) -> Unit,
    onAuraThicknessChange: (Float) -> Unit,
    onAuraBloomChange: (Float) -> Unit,
) {
    Text(
        text = "Sphere controls cover lighting, backdrop, and the atmospheric aura around the globe.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(12.dp))

    Text(text = "Lighting", style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DemoSphereLightingKind.entries.forEach { kind ->
            FilterChip(
                selected = settings.lightingKind == kind,
                onClick = { onLightingKindChange(kind) },
                label = { Text(kind.title) },
            )
        }
    }
    if (settings.lightingKind == DemoSphereLightingKind.Directional) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aim the sun by subsolar latitude / longitude for day–night cycles.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MapSliderRow(
            label = "Sun latitude",
            valueLabel = "${settings.sunLatitude.roundToInt()}°",
            value = settings.sunLatitude,
            valueRange = -90f..90f,
            onValueChange = onSunLatitudeChange,
        )
        MapSliderRow(
            label = "Sun longitude",
            valueLabel = "${settings.sunLongitude.roundToInt()}°",
            value = settings.sunLongitude,
            valueRange = -180f..180f,
            onValueChange = onSunLongitudeChange,
        )
        MapSliderRow(
            label = "Night ambient",
            valueLabel = "${(settings.sunAmbient * 100).roundToInt()}%",
            value = settings.sunAmbient,
            valueRange = 0f..0.5f,
            onValueChange = onSunAmbientChange,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text(text = "Background", style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DemoSphereBackgroundKind.entries.forEach { kind ->
            FilterChip(
                selected = settings.backgroundKind == kind,
                onClick = { onBackgroundKindChange(kind) },
                label = { Text(kind.title) },
            )
        }
    }

    if (settings.backgroundKind == DemoSphereBackgroundKind.Solid) {
        Spacer(modifier = Modifier.height(8.dp))
        ColorPickerRow(
            label = "Backdrop color",
            color = settings.solidColor,
            onColorChange = onSolidColorChange,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text(text = "Aura", style = MaterialTheme.typography.bodyMedium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = "Enabled", style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = settings.auraEnabled,
            onCheckedChange = onAuraEnabledChange,
        )
    }

    if (settings.auraEnabled) {
        ColorPickerRow(
            label = "Aura color",
            color = settings.auraColor,
            onColorChange = onAuraColorChange,
        )
        MapSliderRow(
            label = "Intensity",
            valueLabel = "${(settings.auraIntensity * 100).roundToInt()}%",
            value = settings.auraIntensity,
            valueRange = 0.05f..2f,
            onValueChange = onAuraIntensityChange,
        )
        MapSliderRow(
            label = "Thickness",
            valueLabel = "${(settings.auraThickness * 100).roundToInt() / 100.0}",
            value = settings.auraThickness,
            valueRange = 0.01f..0.2f,
            onValueChange = onAuraThicknessChange,
        )
        MapSliderRow(
            label = "Bloom",
            valueLabel = "${(settings.auraBloom * 100).roundToInt()}%",
            value = settings.auraBloom,
            valueRange = 0f..0.6f,
            onValueChange = onAuraBloomChange,
        )
    }
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

private fun formatWarp(warp: Float): String {
    val rounded = (warp * 100).roundToInt() / 100.0
    return if (rounded == 0.0) "linear" else rounded.toString()
}
