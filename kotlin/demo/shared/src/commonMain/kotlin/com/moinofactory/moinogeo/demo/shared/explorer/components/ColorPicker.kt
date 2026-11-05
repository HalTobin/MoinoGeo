package com.moinofactory.moinogeo.demo.shared.explorer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val ColorEntryShape = RoundedCornerShape(10.dp)

/**
 * Compact color row that opens a dedicated color-picker dialog.
 */
@Composable
internal fun ColorPickerRow(
    label: String,
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }

    Surface(
        onClick = { showPicker = true },
        modifier = modifier.fillMaxWidth(),
        shape = ColorEntryShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = color.toHexRgb(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                ColorSwatch(color = color)
            }
        }
    }

    if (showPicker) {
        ColorPickerDialog(
            title = label,
            color = color,
            onColorChange = onColorChange,
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
internal fun ColorPickerDialog(
    title: String,
    color: Color,
    onColorChange: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            ColorPicker(
                color = color,
                onColorChange = onColorChange,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
    )
}

/**
 * Color picker with hue / saturation / value sliders and a `#RRGGBB` hex field.
 */
@Composable
internal fun ColorPicker(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hsv = remember(color) { color.toHsv() }
    var hue by remember(color) { mutableFloatStateOf(hsv.hue) }
    var saturation by remember(color) { mutableFloatStateOf(hsv.saturation) }
    var value by remember(color) { mutableFloatStateOf(hsv.value) }
    var hexText by remember(color) { mutableStateOf(color.toHexRgb()) }

    LaunchedEffect(color) {
        val next = color.toHsv()
        hue = next.hue
        saturation = next.saturation
        value = next.value
        hexText = color.toHexRgb()
    }

    fun publishFromHsv(h: Float, s: Float, v: Float) {
        val next = hsvToColor(h, s, v, color.alpha)
        hexText = next.toHexRgb()
        onColorChange(next)
    }

    val hexDigits = hexText.removePrefix("#")
    val hexInvalid = hexDigits.isNotEmpty() && hexDigits.length != 6

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ColorSwatch(color = color, size = 28.dp)
            OutlinedTextField(
                value = hexText,
                onValueChange = { raw ->
                    val cleaned = raw.trim().uppercase().removePrefix("#")
                        .filter { it in "0123456789ABCDEF" }
                        .take(6)
                    hexText = if (cleaned.isEmpty()) "" else "#$cleaned"
                    parseHexRgb(cleaned)?.let { parsed ->
                        onColorChange(parsed.copy(alpha = color.alpha))
                    }
                },
                modifier = Modifier.width(108.dp),
                textStyle = MaterialTheme.typography.labelLarge,
                singleLine = true,
                isError = hexInvalid,
                label = { Text("Hex", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("#RGB", style = MaterialTheme.typography.labelSmall) },
                supportingText = if (hexInvalid) {
                    { Text("6 hex digits", style = MaterialTheme.typography.labelSmall) }
                } else {
                    null
                },
            )
        }

        HsvSlider(
            label = "Hue",
            value = hue,
            valueRange = 0f..360f,
            valueLabel = "${hue.roundToInt()}°",
            trackBrush = Brush.horizontalGradient(HueGradientColors),
            onValueChange = { next ->
                hue = next
                publishFromHsv(next, saturation, value)
            },
        )
        HsvSlider(
            label = "Saturation",
            value = saturation,
            valueRange = 0f..1f,
            valueLabel = "${(saturation * 100).roundToInt()}%",
            trackBrush = Brush.horizontalGradient(
                listOf(
                    hsvToColor(hue, 0f, value),
                    hsvToColor(hue, 1f, value),
                ),
            ),
            onValueChange = { next ->
                saturation = next
                publishFromHsv(hue, next, value)
            },
        )
        HsvSlider(
            label = "Value",
            value = value,
            valueRange = 0f..1f,
            valueLabel = "${(value * 100).roundToInt()}%",
            trackBrush = Brush.horizontalGradient(
                listOf(
                    Color.Black,
                    hsvToColor(hue, saturation, 1f),
                ),
            ),
            onValueChange = { next ->
                value = next
                publishFromHsv(hue, saturation, next)
            },
        )
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    size: Dp = 24.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(6.dp),
            ),
    )
}

@Composable
private fun HsvSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    trackBrush: Brush,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(trackBrush),
            )
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onSurface,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                ),
            )
        }
    }
}

internal fun Color.toHexRgb(): String {
    val r = (red * 255f).roundToInt().coerceIn(0, 255)
    val g = (green * 255f).roundToInt().coerceIn(0, 255)
    val b = (blue * 255f).roundToInt().coerceIn(0, 255)
    return "#${r.toHexByte()}${g.toHexByte()}${b.toHexByte()}"
}

internal fun parseHexRgb(hex: String): Color? {
    val cleaned = hex.trim().removePrefix("#")
    if (cleaned.length != 6) return null
    val value = cleaned.toLongOrNull(16) ?: return null
    val r = ((value shr 16) and 0xFF) / 255f
    val g = ((value shr 8) and 0xFF) / 255f
    val b = (value and 0xFF) / 255f
    return Color(red = r, green = g, blue = b, alpha = 1f)
}

private fun Int.toHexByte(): String =
    toString(16).padStart(2, '0').uppercase()

private data class HsvColor(
    val hue: Float,
    val saturation: Float,
    val value: Float,
)

private fun Color.toHsv(): HsvColor {
    val r = red
    val g = green
    val b = blue
    val max = max(r, max(g, b))
    val min = min(r, min(g, b))
    val delta = max - min

    val hue = when {
        delta == 0f -> 0f
        max == r -> ((60f * ((g - b) / delta)) + 360f) % 360f
        max == g -> (60f * ((b - r) / delta)) + 120f
        else -> (60f * ((r - g) / delta)) + 240f
    }
    val saturation = if (max == 0f) 0f else delta / max
    return HsvColor(hue = hue, saturation = saturation, value = max)
}

private fun hsvToColor(
    hue: Float,
    saturation: Float,
    value: Float,
    alpha: Float = 1f,
): Color {
    val h = ((hue % 360f) + 360f) % 360f
    val c = value * saturation
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = value - c
    val (rp, gp, bp) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(
        red = (rp + m).coerceIn(0f, 1f),
        green = (gp + m).coerceIn(0f, 1f),
        blue = (bp + m).coerceIn(0f, 1f),
        alpha = alpha.coerceIn(0f, 1f),
    )
}

private val HueGradientColors = listOf(
    Color.Red,
    Color.Yellow,
    Color.Green,
    Color.Cyan,
    Color.Blue,
    Color.Magenta,
    Color.Red,
)
