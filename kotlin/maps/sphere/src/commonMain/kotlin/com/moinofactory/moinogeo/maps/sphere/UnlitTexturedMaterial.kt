package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.runtime.Composable
import io.github.erkko68.filament.Material
import io.github.erkko68.filament.MaterialInstance
import io.github.erkko68.filament.Texture
import io.github.erkko68.filament.TextureSampler
import io.github.erkko68.filament.VertexBuffer.VertexAttribute
import io.github.erkko68.filament.compose.scene.rememberMaterial
import io.github.erkko68.filament.compose.scene.rememberMaterialInstance
import io.github.erkko68.filament.filamat.MaterialBuilder

/**
 * UNLIT textured surface: samples the `albedo` sampler as base colour and ignores scene lighting.
 * Used for [SphereLighting.Uniform] so the globe map reads flat with no terminator.
 */
internal object UnlitTexturedMaterial {
    private val cachedPayload: ByteArray by lazy { compile() }

    fun payload(): ByteArray = cachedPayload

    private fun compile(): ByteArray {
        MaterialBuilder.init()
        return try {
            val pkg = MaterialBuilder()
                .name("UnlitTextured")
                .platform(MaterialBuilder.Platform.ALL)
                .targetApi(MaterialBuilder.TargetApi.ALL)
                .optimization(MaterialBuilder.Optimization.PERFORMANCE)
                .shading(MaterialBuilder.Shading.UNLIT)
                .require(VertexAttribute.UV0)
                .parameter("albedo", MaterialBuilder.SamplerType.SAMPLER_2D)
                .material(Fragment)
                .build()
            check(pkg.isValid) { "UnlitTextured material package is invalid" }
            check(pkg.data.isNotEmpty()) { "UnlitTextured material package is empty" }
            pkg.data
        } finally {
            MaterialBuilder.shutdown()
        }
    }

    private val Fragment = """
        void material(inout MaterialInputs material) {
            prepareMaterial(material);
            material.baseColor = texture(materialParams_albedo, getUV0());
        }
    """.trimIndent()
}

@Composable
internal fun rememberUnlitTexturedMaterialInstance(
    texture: Texture?,
    sampler: TextureSampler = TextureSampler(
        TextureSampler.MinFilter.LINEAR_MIPMAP_LINEAR,
        TextureSampler.MagFilter.LINEAR,
        TextureSampler.WrapMode.REPEAT,
    ),
): MaterialInstance? {
    if (texture == null) return null
    val material: Material? = rememberMaterial(key = "globe-unlit-textured") {
        UnlitTexturedMaterial.payload()
    }
    return rememberMaterialInstance(material, texture) {
        setParameter("albedo", texture, sampler)
    }
}
