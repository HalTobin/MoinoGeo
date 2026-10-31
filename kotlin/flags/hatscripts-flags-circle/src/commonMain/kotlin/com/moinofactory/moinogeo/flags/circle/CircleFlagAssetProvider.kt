package com.moinofactory.moinogeo.flags.circle

import com.moinofactory.moinogeo.flags.AndroidAssetFlagAssetProvider
import com.moinofactory.moinogeo.flags.ComposeResourceFlagAssetProvider
import com.moinofactory.moinogeo.flags.FlagAssetProvider
import com.moinofactory.moinogeo.flags.UriFlagAssetProvider

/**
 * Flag icons from [HatScripts/circle-flags](https://github.com/HatScripts/circle-flags)
 * (circular 1×1 SVGs).
 *
 * Optimized assets are written to `src/commonMain/resources/flags-circle/` by
 * `scripts/flags/export_kotlin.py`. Until that pipeline has been run, this module
 * is a planned stub — wire [CircleFlagAssetProvider] once SVGs are present,
 * or use [ComposeResourceFlagAssetProvider] / a custom [FlagAssetProvider].
 */
class CircleFlagAssetProvider(
    basePath: String = DEFAULT_ANDROID_ASSET_PATH,
) : FlagAssetProvider by UriFlagAssetProvider(basePath) {

    companion object {
        const val ASSET_DIRECTORY = "flags-circle"
        const val DEFAULT_ANDROID_ASSET_PATH = "file:///android_asset/$ASSET_DIRECTORY"

        /** Android `assets/flags-circle/<iso2>.svg`. */
        fun androidAssets(): FlagAssetProvider =
            AndroidAssetFlagAssetProvider(ASSET_DIRECTORY)

        /**
         * Compose Multiplatform / custom resource resolver.
         * Pass a lambda that loads `flags-circle/<iso2>.svg` (e.g. via `Res.readBytes`).
         */
        fun composeResources(resolve: (iso2Code: String) -> Any): FlagAssetProvider =
            ComposeResourceFlagAssetProvider(resolve)
    }
}
