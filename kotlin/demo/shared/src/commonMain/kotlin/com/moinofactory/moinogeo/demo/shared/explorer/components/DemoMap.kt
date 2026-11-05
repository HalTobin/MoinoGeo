package com.moinofactory.moinogeo.demo.shared.explorer.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.moinofactory.moinogeo.core.domain.model.GeoPlace
import com.moinofactory.moinogeo.core.domain.model.GeoPlaceType
import com.moinofactory.moinogeo.demo.shared.explorer.DemoMapMode
import com.moinofactory.moinogeo.demo.shared.explorer.DemoMapSettings
import com.moinofactory.moinogeo.demo.shared.explorer.DemoMapStyleSettings
import com.moinofactory.moinogeo.demo.shared.explorer.MapCameraRequest
import com.moinofactory.moinogeo.demo.shared.ui.icon.Icons
import com.moinofactory.moinogeo.demo.shared.ui.icon.azimuthalMap
import com.moinofactory.moinogeo.demo.shared.ui.icon.flatMap
import com.moinofactory.moinogeo.demo.shared.ui.icon.palette
import com.moinofactory.moinogeo.demo.shared.ui.icon.settings
import com.moinofactory.moinogeo.demo.shared.ui.icon.sphereMap
import com.moinofactory.moinogeo.maps.countries.DefaultBoundaryProvider
import com.moinofactory.moinogeo.maps.core.BoundaryFeature
import com.moinofactory.moinogeo.maps.core.GeoPoint
import com.moinofactory.moinogeo.maps.plane.AzimuthalMap
import com.moinofactory.moinogeo.maps.plane.AzimuthalMapState
import com.moinofactory.moinogeo.maps.plane.MapScope
import com.moinofactory.moinogeo.maps.plane.PlaneMap
import com.moinofactory.moinogeo.maps.plane.PlaneMapState
import com.moinofactory.moinogeo.maps.plane.rememberAzimuthalMapState
import com.moinofactory.moinogeo.maps.plane.rememberPlaneMapState
import com.moinofactory.moinogeo.maps.topography.DefaultTopographyProvider
import kotlin.math.roundToInt

private val HighlightFill = Color(0x66FF6D00)
private val HighlightBorder = Color(0xFFFF6D00)
private val FlyToAnimationMillis = 800

/**
 * Demo wrapper around [PlaneMap], [AzimuthalMap], and a Sphere placeholder using
 * bundled Natural Earth topography and countries. Each mode keeps its own camera.
 * The map fills the available canvas; stretch from [DemoMapSettings] is applied
 * through the projection, not by resizing the canvas.
 */
@Composable
internal fun DemoMap(
    selectedPlace: GeoPlace?,
    mode: DemoMapMode,
    flatSettings: DemoMapSettings,
    azimuthalSettings: DemoMapSettings,
    sphereSettings: DemoMapSettings,
    styleSettings: DemoMapStyleSettings,
    onSelectMode: (DemoMapMode) -> Unit,
    modifier: Modifier = Modifier,
    cameraRequest: MapCameraRequest? = null,
    onCameraRequestConsumed: () -> Unit = {},
    onOpenMapSettings: (() -> Unit)? = null,
    onOpenMapStyleSettings: (() -> Unit)? = null,
) {
    val boundaryProvider = remember { DefaultBoundaryProvider() }
    val topographyProvider = remember { DefaultTopographyProvider() }
    val boundaryData = remember(boundaryProvider) { boundaryProvider.getBoundaryData() }
    val planeState = rememberPlaneMapState()
    val azimuthalState = rememberAzimuthalMapState()
    var clickedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var overlayClickLabel by remember { mutableStateOf<String?>(null) }

    BindPlaneCamera(planeState, flatSettings)
    BindAzimuthalCamera(azimuthalState, azimuthalSettings)
    AnimateCameraRequest(
        request = cameraRequest,
        mode = mode,
        planeState = planeState,
        azimuthalState = azimuthalState,
        onConsumed = onCameraRequestConsumed,
    )

    val selectedCountryFeature: BoundaryFeature? = remember(selectedPlace, boundaryData) {
        val place = selectedPlace ?: return@remember null
        if (place.type != GeoPlaceType.COUNTRY) return@remember null
        val iso = place.iso2?.takeIf { it.isNotBlank() } ?: return@remember null
        boundaryData.features.firstOrNull { feature ->
            feature.isoCode.equals(iso, ignoreCase = true)
        }
    }

    val markerPoint: GeoPoint? = remember(selectedPlace, selectedCountryFeature) {
        if (selectedCountryFeature != null) return@remember null
        val place = selectedPlace ?: return@remember null
        val lat = place.latitude ?: return@remember null
        val lon = place.longitude ?: return@remember null
        GeoPoint(latitude = lat, longitude = lon)
    }

    val mapStyle = styleSettings.toMapStyle()

    Column(modifier = modifier) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 1.dp,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (mode) {
                    DemoMapMode.Flat -> PlaneMap(
                        modifier = Modifier.fillMaxSize(),
                        mapStyle = mapStyle,
                        boundaryProvider = boundaryProvider,
                        topographyProvider = topographyProvider,
                        state = planeState,
                        onMapClick = { point ->
                            clickedPoint = point
                            overlayClickLabel = null
                        },
                    ) {
                        PlaceOverlays(
                            selectedPlace = selectedPlace,
                            selectedCountryFeature = selectedCountryFeature,
                            markerPoint = markerPoint,
                            onOverlayClick = { label, point ->
                                overlayClickLabel = label
                                clickedPoint = point
                            },
                        )
                    }

                    DemoMapMode.Azimuthal -> AzimuthalMap(
                        modifier = Modifier.fillMaxSize(),
                        mapStyle = mapStyle,
                        boundaryProvider = boundaryProvider,
                        topographyProvider = topographyProvider,
                        state = azimuthalState,
                        onMapClick = { point ->
                            clickedPoint = point
                            overlayClickLabel = null
                        },
                    ) {
                        PlaceOverlays(
                            selectedPlace = selectedPlace,
                            selectedCountryFeature = selectedCountryFeature,
                            markerPoint = markerPoint,
                            onOverlayClick = { label, point ->
                                overlayClickLabel = label
                                clickedPoint = point
                            },
                        )
                    }

                    DemoMapMode.Sphere -> SphereMapPlaceholder(
                        modifier = Modifier.fillMaxSize(),
                        backgroundColor = mapStyle.backgroundColor,
                    )
                }

                if (onOpenMapStyleSettings != null) {
                    MapOverlayIconButton(
                        onClick = onOpenMapStyleSettings,
                        contentDescription = "Map Style",
                        imageVector = Icons.palette,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                    )
                }

                MapModeSelector(
                    selected = mode,
                    onSelect = onSelectMode,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .width(168.dp),
                )

                if (onOpenMapSettings != null) {
                    MapOverlayIconButton(
                        onClick = onOpenMapSettings,
                        contentDescription = "Map Settings",
                        imageVector = Icons.settings,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when {
                overlayClickLabel != null -> "Clicked: $overlayClickLabel"
                clickedPoint != null -> {
                    val lat = (clickedPoint!!.latitude * 10000).roundToInt() / 10000.0
                    val lon = (clickedPoint!!.longitude * 10000).roundToInt() / 10000.0
                    "Clicked: $lat°, $lon°"
                }
                else -> "Tap the map to inspect a location"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun SphereMapPlaceholder(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor,
        ) {}
        Text(
            text = "3D SPHERE MAP",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MapOverlayIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            tonalElevation = 3.dp,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.padding(8.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun MapModeSelector(
    selected: DemoMapMode,
    onSelect: (DemoMapMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = DemoMapMode.entries
    val selectedIndex = modes.indexOf(selected).coerceAtLeast(0)
    val trackInset = 3.dp
    // Gray track + white thumb keeps the indicator readable over the map.
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.94f)
    val indicatorColor = MaterialTheme.colorScheme.surface
    val selectedContentColor = MaterialTheme.colorScheme.onSurface
    val unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f)

    Surface(
        modifier = modifier.height(40.dp),
        shape = CircleShape,
        color = trackColor,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(trackInset)
                .selectableGroup(),
        ) {
            val segmentWidth = (maxWidth / modes.size).coerceAtLeast(0.dp)
            val indicatorOffset by animateDpAsState(
                targetValue = segmentWidth * selectedIndex,
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutSlowInEasing,
                ),
                label = "MapModeIndicator",
            )

            Box(
                modifier = Modifier
                    .zIndex(0f)
                    .offset(x = indicatorOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .shadow(elevation = 2.dp, shape = CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(indicatorColor),
            )

            Row(
                modifier = Modifier
                    .zIndex(1f)
                    .fillMaxSize(),
            ) {
                modes.forEach { mode ->
                    val isSelected = selected == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .selectable(
                                selected = isSelected,
                                onClick = { onSelect(mode) },
                                role = Role.RadioButton,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = mode.icon,
                            contentDescription = mode.title,
                            modifier = Modifier.size(22.dp),
                            tint = if (isSelected) {
                                selectedContentColor
                            } else {
                                unselectedContentColor
                            },
                        )
                    }
                }
            }
        }
    }
}

private val DemoMapMode.icon: ImageVector
    get() = when (this) {
        DemoMapMode.Flat -> Icons.flatMap
        DemoMapMode.Azimuthal -> Icons.azimuthalMap
        DemoMapMode.Sphere -> Icons.sphereMap
    }

@Composable
private fun AnimateCameraRequest(
    request: MapCameraRequest?,
    mode: DemoMapMode,
    planeState: PlaneMapState,
    azimuthalState: AzimuthalMapState,
    onConsumed: () -> Unit,
) {
    LaunchedEffect(request?.id, mode) {
        val target = request ?: return@LaunchedEffect
        val animationSpec = tween<Float>(
            durationMillis = FlyToAnimationMillis,
            easing = FastOutSlowInEasing,
        )
        when (mode) {
            DemoMapMode.Flat -> planeState.animateTo(
                point = target.point,
                zoom = target.zoom,
                animationSpec = animationSpec,
            )
            DemoMapMode.Azimuthal -> azimuthalState.animateTo(
                point = target.point,
                zoom = target.zoom,
                animationSpec = animationSpec,
            )
            DemoMapMode.Sphere -> Unit
        }
        onConsumed()
    }
}

@Composable
private fun BindPlaneCamera(state: PlaneMapState, settings: DemoMapSettings) {
    // Stretch / falloff live on the projection so canvas size can change freely.
    // Flat map keeps equator pivot and no rotation.
    LaunchedEffect(Unit) {
        state.centerLatitude = 0.0
        state.rotationAngle = 0f
    }
    LaunchedEffect(settings.horizontalScale, settings.verticalScale) {
        state.verticalScale = settings.projectionVerticalScale()
    }
    LaunchedEffect(settings.verticalWarp) {
        state.verticalWarp = settings.verticalWarp
    }
    LaunchedEffect(settings.centerLongitude) {
        state.setCenter(
            GeoPoint(
                latitude = 0.0,
                longitude = settings.centerLongitude.toDouble(),
            ),
        )
    }
}

@Composable
private fun BindAzimuthalCamera(state: AzimuthalMapState, settings: DemoMapSettings) {
    // Stretch lives on the projection so canvas size can change freely.
    LaunchedEffect(settings.horizontalScale, settings.verticalScale) {
        state.verticalScale = settings.projectionVerticalScale()
    }
    LaunchedEffect(settings.rotationAngle) {
        state.rotationAngle = settings.rotationAngle
    }
    LaunchedEffect(settings.centerLatitude, settings.centerLongitude) {
        state.setCenter(
            GeoPoint(
                latitude = settings.centerLatitude.toDouble(),
                longitude = settings.centerLongitude.toDouble(),
            ),
        )
    }
}

@Composable
private fun MapScope.PlaceOverlays(
    selectedPlace: GeoPlace?,
    selectedCountryFeature: BoundaryFeature?,
    markerPoint: GeoPoint?,
    onOverlayClick: (label: String, point: GeoPoint?) -> Unit,
) {
    selectedCountryFeature?.let { feature ->
        Area(
            geometries = feature.geometries,
            fillColor = HighlightFill,
            borderColor = HighlightBorder,
            borderWidth = 2.5f,
            onClick = {
                onOverlayClick(selectedPlace?.name ?: feature.isoCode, null)
            },
        )
    }

    if (markerPoint != null && selectedPlace != null) {
        Marker(
            position = markerPoint,
            onClick = {
                onOverlayClick(selectedPlace.name, markerPoint)
            },
        ) {
            Text(
                text = selectedPlace.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
