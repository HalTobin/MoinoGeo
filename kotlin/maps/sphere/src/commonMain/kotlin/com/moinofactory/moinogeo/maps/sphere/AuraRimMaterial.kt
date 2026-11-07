package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.erkko68.filament.Material
import io.github.erkko68.filament.MaterialInstance
import io.github.erkko68.filament.VertexBuffer.VertexAttribute
import io.github.erkko68.filament.compose.scene.LinearColor
import io.github.erkko68.filament.compose.scene.rememberMaterial
import io.github.erkko68.filament.compose.scene.rememberMaterialInstance
import io.github.erkko68.filament.filamat.MaterialBuilder

/**
 * Unlit additive Fresnel rim for the globe's silhouette:
 * bright along the edges/horizon of the sphere, transparent across the face
 * so land and ocean details remain completely visible.
 */
internal object InnerAuraMaterial {
    private val cachedPayload: ByteArray by lazy { compile() }

    fun payload(): ByteArray = cachedPayload

    private fun compile(): ByteArray {
        MaterialBuilder.init()
        return try {
            val pkg = MaterialBuilder()
                .name("InnerAura")
                .platform(MaterialBuilder.Platform.ALL)
                .targetApi(MaterialBuilder.TargetApi.ALL)
                .optimization(MaterialBuilder.Optimization.PERFORMANCE)
                .shading(MaterialBuilder.Shading.UNLIT)
                .blending(MaterialBuilder.BlendingMode.ADD)
                .depthWrite(enable = false)
                .doubleSided(doubleSided = false)
                .require(VertexAttribute.TANGENTS)
                .parameter("color", MaterialBuilder.UniformType.FLOAT3)
                .parameter("intensity", MaterialBuilder.UniformType.FLOAT)
                .parameter("power", MaterialBuilder.UniformType.FLOAT)
                .material(Fragment)
                .build()
            check(pkg.isValid) { "InnerAura material package is invalid" }
            check(pkg.data.isNotEmpty()) { "InnerAura material package is empty" }
            pkg.data
        } finally {
            MaterialBuilder.shutdown()
        }
    }

    private val Fragment = """
        void material(inout MaterialInputs material) {
            prepareMaterial(material);
            float ndotv = clamp(abs(dot(getWorldGeometricNormalVector(), getWorldViewVector())), 0.0, 1.0);
            float edge = 1.0 - ndotv;
            float rim = pow(edge, materialParams.power);
            float soft = pow(edge, max(1.0, materialParams.power * 0.45)) * 0.25;
            float a = (rim + soft) * materialParams.intensity * 1.5;
            material.baseColor.rgb = materialParams.color * a;
        }
    """.trimIndent()
}

/**
 * Unlit additive atmospheric glow surrounding the sphere into space:
 * rendered on the inside/back faces of an outer sphere with an inverted hull.
 *
 * It starts with peak intensity at the silhouette horizon of the globe and smoothly
 * fades outward into space towards the outer radius, leaving the globe interior untouched.
 */
internal object OuterAuraMaterial {
    private val cachedPayload: ByteArray by lazy { compile() }

    fun payload(): ByteArray = cachedPayload

    private fun compile(): ByteArray {
        MaterialBuilder.init()
        return try {
            val pkg = MaterialBuilder()
                .name("OuterAura")
                .platform(MaterialBuilder.Platform.ALL)
                .targetApi(MaterialBuilder.TargetApi.ALL)
                .optimization(MaterialBuilder.Optimization.PERFORMANCE)
                .shading(MaterialBuilder.Shading.UNLIT)
                .blending(MaterialBuilder.BlendingMode.ADD)
                .culling(MaterialBuilder.CullingMode.FRONT)
                .depthWrite(enable = false)
                .depthCulling(enable = true)
                .doubleSided(doubleSided = false)
                .require(VertexAttribute.TANGENTS)
                .parameter("color", MaterialBuilder.UniformType.FLOAT3)
                .parameter("intensity", MaterialBuilder.UniformType.FLOAT)
                .parameter("globeRadius", MaterialBuilder.UniformType.FLOAT)
                .parameter("outerRadius", MaterialBuilder.UniformType.FLOAT)
                .parameter("falloff", MaterialBuilder.UniformType.FLOAT)
                .material(Fragment)
                .build()
            check(pkg.isValid) { "OuterAura material package is invalid" }
            check(pkg.data.isNotEmpty()) { "OuterAura material package is empty" }
            pkg.data
        } finally {
            MaterialBuilder.shutdown()
        }
    }

    private val Fragment = """
        void material(inout MaterialInputs material) {
            prepareMaterial(material);
            float ndotv = clamp(abs(dot(getWorldGeometricNormalVector(), getWorldViewVector())), 0.0, 1.0);
            // On a sphere centered at (0,0,0) with radius R_outer, the perpendicular
            // distance from the origin to the view ray is b = R_outer * sin(theta).
            float b = materialParams.outerRadius * sqrt(max(0.0, 1.0 - ndotv * ndotv));
            float span = max(0.0001, materialParams.outerRadius - materialParams.globeRadius);
            float t = clamp((b - materialParams.globeRadius) / span, 0.0, 1.0);
            float glow = (b > materialParams.globeRadius) ? pow(1.0 - t, materialParams.falloff) : 0.0;
            float a = glow * materialParams.intensity * 1.5;
            material.baseColor.rgb = materialParams.color * a;
        }
    """.trimIndent()
}

/** Legacy alias for [InnerAuraMaterial] */
@Suppress("unused")
internal typealias AuraRimMaterial = InnerAuraMaterial

/**
 * Inner contour-aura instance. [thickness] widens the rim falloff;
 * [intensity] scales brightness.
 */
@Composable
internal fun rememberInnerAuraMaterialInstance(
    color: Color,
    intensity: Float,
    thickness: Float,
): MaterialInstance? {
    val material: Material? = rememberMaterial(key = "aura-rim-inner") {
        InnerAuraMaterial.payload()
    }
    val clampedThickness = thickness.coerceIn(0.01f, 0.25f)
    val thicknessFactor = (clampedThickness - 0.01f) / 0.24f
    // Thin → power ~5.5; thick → softer edge band power ~2.5
    val power = (5.5f - (thicknessFactor * 3.0f)).coerceIn(2.0f, 7.0f)
    val (r, g, b) = LinearColor.fromComposeColor(color)
    return rememberMaterialInstance(
        material,
        color,
        intensity,
        power,
    ) {
        setParameter("color", r, g, b)
        setParameter("intensity", intensity.coerceIn(0f, 5f))
        setParameter("power", power)
    }
}

/**
 * Outer atmospheric glow instance. Renders in the space immediately surrounding
 * the globe, fading out towards [outerRadius].
 */
@Composable
internal fun rememberOuterAuraMaterialInstance(
    color: Color,
    intensity: Float,
    globeRadius: Float,
    outerRadius: Float,
    thickness: Float,
): MaterialInstance? {
    val material: Material? = rememberMaterial(key = "aura-halo-outer") {
        OuterAuraMaterial.payload()
    }
    val clampedThickness = thickness.coerceIn(0.01f, 0.25f)
    val thicknessFactor = (clampedThickness - 0.01f) / 0.24f
    val falloff = (3.5f - (thicknessFactor * 1.7f)).coerceIn(1.5f, 4.5f)
    val (r, g, b) = LinearColor.fromComposeColor(color)
    return rememberMaterialInstance(
        material,
        color,
        intensity,
        globeRadius,
        outerRadius,
        falloff,
    ) {
        setParameter("color", r, g, b)
        setParameter("intensity", intensity.coerceIn(0f, 5f))
        setParameter("globeRadius", globeRadius)
        setParameter("outerRadius", outerRadius)
        setParameter("falloff", falloff)
    }
}

/** Legacy alias */
@Suppress("unused")
@Composable
internal fun rememberAuraRimMaterialInstance(
    color: Color,
    intensity: Float,
    thickness: Float,
): MaterialInstance? = rememberInnerAuraMaterialInstance(color, intensity, thickness)
