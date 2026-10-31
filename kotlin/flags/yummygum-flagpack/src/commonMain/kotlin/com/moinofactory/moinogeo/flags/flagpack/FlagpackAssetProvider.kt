package com.moinofactory.moinogeo.flags.flagpack

import com.moinofactory.moinogeo.flags.AndroidAssetFlagAssetProvider
import com.moinofactory.moinogeo.flags.ComposeResourceFlagAssetProvider
import com.moinofactory.moinogeo.flags.FlagAssetProvider
import com.moinofactory.moinogeo.flags.UriFlagAssetProvider

/**
 * Flag icons from [Yummygum/flagpack-core](https://github.com/Yummygum/flagpack-core)
 * (stylized rounded-rectangle SVGs).
 *
 * Optimized assets are written to `src/commonMain/resources/flags-flagpack/` by
 * `scripts/flags/export_kotlin.py`. Until that pipeline has been run, this module
 * is a planned stub — wire [FlagpackAssetProvider] once SVGs are present,
 * or use [ComposeResourceFlagAssetProvider] / a custom [FlagAssetProvider].
 */
class FlagpackAssetProvider(
    basePath: String = DEFAULT_ANDROID_ASSET_PATH,
) : FlagAssetProvider by UriFlagAssetProvider(basePath) {

    companion object {
        const val ASSET_DIRECTORY = "flags-flagpack"
        const val DEFAULT_ANDROID_ASSET_PATH = "file:///android_asset/$ASSET_DIRECTORY"

        /** Android `assets/flags-flagpack/<iso2>.svg`. */
        fun androidAssets(): FlagAssetProvider =
            AndroidAssetFlagAssetProvider(ASSET_DIRECTORY)

        /**
         * Compose Multiplatform / custom resource resolver.
         * Pass a lambda that loads `flags-flagpack/<iso2>.svg` (e.g. via `Res.readBytes`).
         */
        fun composeResources(resolve: (iso2Code: String) -> Any): FlagAssetProvider =
            ComposeResourceFlagAssetProvider(resolve)
    }
}
