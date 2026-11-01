package com.moinofactory.moinogeo.maps.core

import androidx.compose.ui.graphics.Color

data class BorderStyle(
    val lineColor: Color = Color(0xFF333333),
    val lineWidth: Float = 1.5f,
    val dashPattern: List<Float>? = null
)
