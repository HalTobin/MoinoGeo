package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SphereMap(
    modifier: Modifier = Modifier,
    sphereStyle: SphereMapStyle = SphereMapStyle.Default
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(sphereStyle.mapStyle.backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D47A1))
                    .border(
                        width = (sphereStyle.atmosphereThickness / 3f).dp,
                        color = sphereStyle.atmosphereColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    val center = center

                    for (i in 1..3) {
                        val yOffset = radius * (i / 4f)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            radius = radius * (1 - (i / 4f) * (i / 4f)),
                            center = center.copy(y = center.y + yOffset * 0.5f),
                            style = Stroke(width = 1f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            radius = radius * (1 - (i / 4f) * (i / 4f)),
                            center = center.copy(y = center.y - yOffset * 0.5f),
                            style = Stroke(width = 1f)
                        )
                    }

                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = radius,
                        style = Stroke(width = 2f)
                    )
                }

                Text(
                    text = "SPHERE MAP",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3D Sphere Configuration Placeholder",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Globe Radius: ${sphereStyle.globeRadius} px",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Camera Distance: ${sphereStyle.cameraDistance} px",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Tilt Angle: ${sphereStyle.tiltAngle}° | Rotation Speed: ${sphereStyle.rotationSpeed}x",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Lighting: ${if (sphereStyle.enableLighting) "Enabled" else "Disabled"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
