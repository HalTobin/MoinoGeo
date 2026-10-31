package com.moinofactory.moinogeo.demo.shared.flags

import com.moinofactory.moinogeo.flags.FlagAssetProvider
import com.moinofactory.moinogeo.flags.circle.CircleFlagAssetProvider
import com.moinofactory.moinogeo.flags.flagpack.FlagpackAssetProvider
import com.moinofactory.moinogeo.flags.lipis.rectangle.LipisRectangleFlagAssetProvider
import com.moinofactory.moinogeo.flags.lipis.square.LipisSquareFlagAssetProvider
import com.moinofactory.moinogeo.flags.withFallback

/**
 * Flag libraries available in `@kotlin/flags`.
 */
enum class FlagLibraryType(
    val title: String,
    val shortName: String,
    val description: String,
    private val providerFactory: () -> FlagAssetProvider,
) {
    LIPIS_RECTANGLE(
        title = "Lipis Rectangle (4×3)",
        shortName = "Lipis 4x3",
        description = "Standard realistic 4×3 rectangular vector flags (lipis/flag-icons)",
        providerFactory = { LipisRectangleFlagAssetProvider.classpathAssets() },
    ),
    LIPIS_SQUARE(
        title = "Lipis Square (1×1)",
        shortName = "Lipis 1x1",
        description = "1×1 square vector flags (lipis/flag-icons)",
        providerFactory = { LipisSquareFlagAssetProvider.classpathAssets() },
    ),
    FLAGPACK(
        title = "Yummygum Flagpack",
        shortName = "Flagpack",
        description = "Stylized rounded rectangular flags (Yummygum/flagpack-core)",
        providerFactory = { FlagpackAssetProvider.classpathAssets() },
    ),
    CIRCLE(
        title = "HatScripts Circle (1×1)",
        shortName = "Circle",
        description = "Circular 1×1 avatar flags (HatScripts/circle-flags)",
        providerFactory = { CircleFlagAssetProvider.classpathAssets() },
    );

    /**
     * @param fallbackIso2 Optional unknown/placeholder code (default `xx`) used when
     * the requested country SVG is missing. Pass `null` to disable provider-level fallback.
     */
    fun getProvider(fallbackIso2: String? = DEFAULT_FALLBACK_ISO2): FlagAssetProvider {
        val primary = providerFactory()
        if (fallbackIso2 == null) return primary
        val fallbackData = primary.getFlagData(fallbackIso2) ?: return primary
        return primary.withFallback(fallbackData)
    }

    /** Coil model for FlagIcon load-error fallback (same pack's `xx` artwork). */
    fun getFallbackData(fallbackIso2: String = DEFAULT_FALLBACK_ISO2): Any? =
        providerFactory().getFlagData(fallbackIso2)

    companion object {
        const val DEFAULT_FALLBACK_ISO2 = "xx"
    }
}
