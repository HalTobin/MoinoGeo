package com.moinofactory.moinogeo.demo.shared.flags

/**
 * Source of assets for resolving SVG files.
 */
enum class AssetSourceMode(val label: String, val description: String) {
    CDN("Remote CDN (jsDelivr)", "Loads optimized SVGs from remote CDN"),
    LOCAL_ASSETS("Bundled Resources", "Loads SVGs embedded in the flag library modules"),
}
