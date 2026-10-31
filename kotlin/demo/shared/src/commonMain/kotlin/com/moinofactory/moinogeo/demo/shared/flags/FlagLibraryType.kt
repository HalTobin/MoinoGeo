package com.moinofactory.moinogeo.demo.shared.flags

import com.moinofactory.moinogeo.flags.FlagAssetProvider
import com.moinofactory.moinogeo.flags.circle.CircleFlagAssetProvider
import com.moinofactory.moinogeo.flags.flagpack.FlagpackAssetProvider
import com.moinofactory.moinogeo.flags.lipis.rectangle.LipisRectangleFlagAssetProvider
import com.moinofactory.moinogeo.flags.lipis.square.LipisSquareFlagAssetProvider

/**
 * Flag libraries available in `@kotlin/flags`.
 */
enum class FlagLibraryType(
    val title: String,
    val shortName: String,
    val description: String,
    val cdnBaseUrl: String,
    private val localProvider: () -> FlagAssetProvider,
    private val cdnProvider: (basePath: String) -> FlagAssetProvider,
) {
    LIPIS_RECTANGLE(
        title = "Lipis Rectangle (4×3)",
        shortName = "Lipis 4x3",
        description = "Standard realistic 4×3 rectangular vector flags (lipis/flag-icons)",
        cdnBaseUrl = "https://cdn.jsdelivr.net/gh/lipis/flag-icons@7.5.0/flags/4x3",
        localProvider = { LipisRectangleFlagAssetProvider.classpathAssets() },
        cdnProvider = { LipisRectangleFlagAssetProvider(it) },
    ),
    LIPIS_SQUARE(
        title = "Lipis Square (1×1)",
        shortName = "Lipis 1x1",
        description = "1×1 square vector flags (lipis/flag-icons)",
        cdnBaseUrl = "https://cdn.jsdelivr.net/gh/lipis/flag-icons@7.5.0/flags/1x1",
        localProvider = { LipisSquareFlagAssetProvider.classpathAssets() },
        cdnProvider = { LipisSquareFlagAssetProvider(it) },
    ),
    FLAGPACK(
        title = "Yummygum Flagpack",
        shortName = "Flagpack",
        description = "Stylized rounded rectangular flags (Yummygum/flagpack-core)",
        cdnBaseUrl = "https://cdn.jsdelivr.net/gh/Yummygum/flagpack-core@v2.0.0/svg_l",
        localProvider = { FlagpackAssetProvider.classpathAssets() },
        cdnProvider = { FlagpackAssetProvider(it) },
    ),
    CIRCLE(
        title = "HatScripts Circle (1×1)",
        shortName = "Circle",
        description = "Circular 1×1 avatar flags (HatScripts/circle-flags)",
        cdnBaseUrl = "https://cdn.jsdelivr.net/gh/HatScripts/circle-flags@gh-pages/flags",
        localProvider = { CircleFlagAssetProvider.classpathAssets() },
        cdnProvider = { CircleFlagAssetProvider(it) },
    );

    fun getProvider(source: AssetSourceMode): FlagAssetProvider = when (source) {
        AssetSourceMode.CDN -> cdnProvider(cdnBaseUrl)
        AssetSourceMode.LOCAL_ASSETS -> localProvider()
    }
}
