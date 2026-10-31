package com.moinofactory.moinogeo.flags.lipis.rectangle

import com.moinofactory.moinogeo.flags.AndroidAssetFlagAssetProvider
import com.moinofactory.moinogeo.flags.ClasspathFlagAssetProvider
import com.moinofactory.moinogeo.flags.ComposeResourceFlagAssetProvider
import com.moinofactory.moinogeo.flags.FlagAssetProvider
import com.moinofactory.moinogeo.flags.UriFlagAssetProvider

/**
 * Rectangular (4×3) flag icons from [lipis/flag-icons](https://github.com/lipis/flag-icons).
 *
 * Optimized assets go in `src/commonMain/resources/flags-lipis-rectangle/` via
 * `scripts/flags/export_kotlin.py`.
 */
class LipisRectangleFlagAssetProvider(
    basePath: String = DEFAULT_ANDROID_ASSET_PATH,
) : FlagAssetProvider by UriFlagAssetProvider(basePath) {

    companion object {
        const val ASSET_DIRECTORY = "flags-lipis-rectangle"
        const val DEFAULT_ANDROID_ASSET_PATH = "file:///android_asset/$ASSET_DIRECTORY"

        fun androidAssets(): FlagAssetProvider =
            AndroidAssetFlagAssetProvider(ASSET_DIRECTORY)

        /** Bundled `commonMain/resources/flags-lipis-rectangle/<iso2>.svg`. */
        fun classpathAssets(): FlagAssetProvider =
            ClasspathFlagAssetProvider(ASSET_DIRECTORY)

        fun composeResources(resolve: (iso2Code: String) -> Any): FlagAssetProvider =
            ComposeResourceFlagAssetProvider(resolve)
    }
}
