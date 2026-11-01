package com.moinofactory.moinogeo.maps.core

import androidx.compose.ui.graphics.Color

data class ZoneStyle(
    val fillColor: Color,
    val strokeColor: Color? = null,
    val strokeWidth: Float = 0f,
    val patternName: String? = null
)
