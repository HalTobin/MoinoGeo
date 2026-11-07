package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import com.moinofactory.moinogeo.maps.countries.DefaultBoundaryProvider
import com.moinofactory.moinogeo.maps.core.BoundaryProvider
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.core.MapStyle
import com.moinofactory.moinogeo.maps.core.TopoZone
import com.moinofactory.moinogeo.maps.core.TopographyProvider
import com.moinofactory.moinogeo.maps.topography.DefaultTopographyProvider
import io.github.erkko68.filament.compose.FilamentSceneScope
import io.github.erkko68.filament.compose.FilamentSceneView
import io.github.erkko68.filament.compose.scene.Bloom
import io.github.erkko68.filament.compose.scene.Direction
import io.github.erkko68.filament.compose.scene.DirectionalLight
import io.github.erkko68.filament.compose.scene.IndirectLight
import io.github.erkko68.filament.compose.scene.LightIntensity
import io.github.erkko68.filament.compose.scene.LinearColor
import io.github.erkko68.filament.compose.scene.Position
import io.github.erkko68.filament.compose.scene.PostProcessing
import io.github.erkko68.filament.compose.scene.Projection
import io.github.erkko68.filament.compose.scene.Skybox
import io.github.erkko68.filament.compose.scene.SphericalHarmonics
import io.github.erkko68.filament.compose.scene.primitives.Sphere
import io.github.erkko68.filament.compose.scene.rememberCameraState
import io.github.erkko68.filament.compose.scene.rememberColorMaterialInstance
import io.github.erkko68.filament.compose.scene.rememberTexturedMaterialInstance
import io.github.erkko68.filament.compose.scene.rememberTexture
import io.github.erkko68.filament.compose.scene.rememberUnlitColorMaterialInstance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 3D globe map backed by Filament KMP. Base layers come from [mapStyle] + providers;
 * sphere-only look (background, aura, FOV) from [sphereStyle]. Overlays are declared in
 * [content] via [SphereMapScope] (`Marker`, `Circle`, `Area`).
 */
@Composable
fun SphereMap(
    modifier: Modifier = Modifier,
    mapStyle: MapStyle = MapStyle.Default,
    sphereStyle: SphereMapStyle = SphereMapStyle.Default,
    boundaryProvider: BoundaryProvider = remember { DefaultBoundaryProvider() },
    topographyProvider: TopographyProvider = remember { DefaultTopographyProvider() },
    state: SphereMapState = rememberSphereMapState(
        initialFieldOfView = sphereStyle.fieldOfView,
    ),
    onMapClick: ((GeoPoint) -> Unit)? = null,
    content: @Composable SphereMapScope.() -> Unit = {},
) {
    val boundaryData = remember(boundaryProvider) { boundaryProvider.getBoundaryData() }
    val topoData = remember(topographyProvider) { topographyProvider.getTopographyData() }
    val gestureScope = rememberCoroutineScope()
    val highlights = remember { mutableStateListOf<HighlightPolygon>() }
    val highlightRegistrar = remember(highlights) {
        SphereHighlightRegistrar { highlight ->
            highlights.add(highlight)
            return@SphereHighlightRegistrar { highlights.remove(highlight) }
        }
    }

    val mergedStyle = remember(mapStyle, sphereStyle) {
        SphereMapStyle(
            mapStyle = mapStyle,
            globeRadius = sphereStyle.globeRadius,
            fieldOfView = sphereStyle.fieldOfView,
            background = sphereStyle.background,
            aura = sphereStyle.aura,
            lighting = sphereStyle.lighting,
        )
    }

    var bakedGlobe by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(topoData, boundaryData, mapStyle, highlights.toList()) {
        bakedGlobe = withContext(Dispatchers.Default) {
            EquirectTextureBaker.bake(
                topography = topoData,
                boundaries = boundaryData,
                mapStyle = mapStyle,
                highlightPolygons = highlights.toList(),
            )
        }
    }

    val skyBitmap = remember(mergedStyle.background) {
        when (val bg = mergedStyle.background) {
            is SphereBackground.Solid -> null
            is SphereBackground.Starfield -> EquirectTextureBaker.bakeStarfield(bg)
            is SphereBackground.Image -> bg.bitmap
        }
    }

    val eye = state.eyePosition()
    val cameraState = rememberCameraState(
        initialEye = Position(eye.x, eye.y, eye.z),
        initialTarget = Position(0f, 0f, 0f),
        initialProjection = Projection.Perspective(fovDegrees = state.fieldOfView.toDouble()),
    )
    LaunchedEffect(state.center, state.zoom, state.fieldOfView) {
        val e = state.eyePosition()
        cameraState.eye = Position(e.x, e.y, e.z)
        cameraState.target = Position(0f, 0f, 0f)
        cameraState.projection = Projection.Perspective(fovDegrees = state.fieldOfView.toDouble())
    }

    val bloom = if (mergedStyle.aura.enabled && mergedStyle.aura.bloomStrength > 0.001f) {
        Bloom(
            strength = (mergedStyle.aura.bloomStrength * mergedStyle.aura.intensity)
                .coerceIn(0f, 0.5f),
            levels = 6,
        )
    } else {
        null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { state.viewportSize = it.toSize() }
            .sphereGestures(state, gestureScope, onMapClick),
    ) {
        FilamentSceneView(
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState,
            postProcessing = PostProcessing(bloom = bloom),
        ) {
            SphereSceneBackground(mergedStyle.background, skyBitmap)

            val lighting = mergedStyle.lighting
            if (lighting is SphereLighting.Directional) {
                val ambient = lighting.ambient.coerceIn(0f, 1f)
                if (ambient > 0.001f) {
                    // Isotropic fill so the night side stays faintly readable.
                    IndirectLight(
                        irradiance = UniformIrradiance,
                        intensity = 30_000f * ambient,
                    )
                }
                val sunStrength = lighting.intensity.coerceIn(0f, 5f)
                DirectionalLight(
                    direction = Direction(
                        lighting.directionX,
                        lighting.directionY,
                        lighting.directionZ,
                    ),
                    intensity = LightIntensity.LuminousPower(100_000f * sunStrength),
                    shadow = null,
                )
            }

            val fallbackColor = mapStyle.zoneStyles[TopoZone.WATER]?.fillColor
                ?: mapStyle.backgroundColor
            val fallbackLinear = LinearColor.fromComposeColor(fallbackColor)
            val globePng = remember(bakedGlobe) {
                bakedGlobe?.encodeToPng()?.takeIf { it.isNotEmpty() }
            }
            val globeTexture = rememberTexture(key = globePng ?: Unit) {
                globePng ?: ByteArray(0)
            }
            val globeMaterial = when (mergedStyle.lighting) {
                is SphereLighting.Uniform -> {
                    rememberUnlitTexturedMaterialInstance(globeTexture)
                        ?: rememberUnlitColorMaterialInstance(fallbackLinear)
                }
                is SphereLighting.Directional -> {
                    rememberTexturedMaterialInstance(globeTexture, roughness = 1f)
                        ?: rememberColorMaterialInstance(fallbackLinear, roughness = 1f)
                }
            }

            Sphere(
                material = globeMaterial,
                radius = mergedStyle.globeRadius,
                rings = 64,
                segments = 128,
                castShadows = false,
                receiveShadows = false,
            )

            if (mergedStyle.aura.enabled) {
                val auraThickness = mergedStyle.aura.thickness.coerceIn(0.01f, 0.25f)
                val outerRadius = mergedStyle.globeRadius * (1f + auraThickness * 2.2f)

                // Atmospheric outer glow halo radiating into space around the perimeter
                val outerMaterial = rememberOuterAuraMaterialInstance(
                    color = mergedStyle.aura.color,
                    intensity = mergedStyle.aura.intensity,
                    globeRadius = mergedStyle.globeRadius,
                    outerRadius = outerRadius,
                    thickness = auraThickness,
                )
                Sphere(
                    material = outerMaterial,
                    radius = outerRadius,
                    rings = 64,
                    segments = 128,
                    castShadows = false,
                    receiveShadows = false,
                )

                // Atmospheric inner rim glow along the globe silhouette
                val innerMaterial = rememberInnerAuraMaterialInstance(
                    color = mergedStyle.aura.color,
                    intensity = mergedStyle.aura.intensity,
                    thickness = auraThickness,
                )
                Sphere(
                    material = innerMaterial,
                    radius = mergedStyle.globeRadius * 1.001f,
                    rings = 64,
                    segments = 128,
                    castShadows = false,
                    receiveShadows = false,
                )
            }
        }

        val projection = remember(
            state.viewportSize,
            state.center,
            state.zoom,
            state.fieldOfView,
            mergedStyle.globeRadius,
        ) {
            state.projection()
        }

        ProvideSphereMapLocals(
            projection = projection,
            highlightRegistrar = highlightRegistrar,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SphereMapScopeInstance.content()
            }
        }
    }
}

@Composable
private fun FilamentSceneScope.SphereSceneBackground(
    background: SphereBackground,
    skyBitmap: ImageBitmap?,
) {
    when (background) {
        is SphereBackground.Solid -> {
            Skybox(color = LinearColor.fromComposeColor(background.color))
        }
        is SphereBackground.Starfield -> {
            // Cubemap skyboxes need KTX; use a solid void + textured skydome sphere instead.
            Skybox(color = LinearColor.fromComposeColor(background.voidColor))
            val png = remember(skyBitmap) {
                skyBitmap?.encodeToPng()?.takeIf { it.isNotEmpty() }
            }
            if (png != null) {
                val texture = rememberTexture(key = png) { png }
                val material = rememberTexturedMaterialInstance(texture)
                Sphere(
                    material = material,
                    radius = 50f,
                    rings = 24,
                    segments = 48,
                    castShadows = false,
                    receiveShadows = false,
                )
            }
        }
        is SphereBackground.Image -> {
            Skybox(color = LinearColor.fromComposeColor(Color(0xFF02040A)))
            val png = remember(skyBitmap) {
                skyBitmap?.encodeToPng()?.takeIf { it.isNotEmpty() }
            }
            if (png != null) {
                val texture = rememberTexture(key = png) { png }
                val material = rememberTexturedMaterialInstance(texture)
                Sphere(
                    material = material,
                    radius = 50f,
                    rings = 24,
                    segments = 48,
                    castShadows = false,
                    receiveShadows = false,
                )
            }
        }
    }
}

/** Single-band (constant) irradiance — even fill with no directional bias. */
private val UniformIrradiance = SphericalHarmonics(
    bands = 1,
    coefficients = floatArrayOf(1f, 1f, 1f),
)
