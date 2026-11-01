package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.ui.graphics.Color
import com.moinofactory.moinogeo.maps.core.MapStyle

data class SphereMapStyle(
    val mapStyle: MapStyle = MapStyle.Default,
    val globeRadius: Float = 200f,
    val cameraDistance: Float = 1000f,
    val atmosphereColor: Color = Color(0x6688CCFF),
    val atmosphereThickness: Float = 15f,
    val rotationSpeed: Float = 1.0f,
    val tiltAngle: Float = 23.5f,
    val enableLighting: Boolean = true,
    val fieldOfView: Float = 45f
) {
    companion object {
        val Default = SphereMapStyle()
    }
}
