package com.moinofactory.moinogeo.flags

/**
 * Pluggable source of flag artwork keyed by ISO 3166-1 alpha-2.
 *
 * Implement this yourself to supply custom SVGs, drawables, or CDN URLs.
 * Return any Coil-compatible model (URI/path [String], [ByteArray], file path, etc.).
 */
fun interface FlagAssetProvider {
    fun getFlagData(iso2Code: String): Any
}

/**
 * Resolves flags from a base URI or path prefix.
 *
 * Works for:
 * - Remote CDN: `https://cdn.example.com/flags`
 * - Android assets: `file:///android_asset/flags-lipis`
 * - Local / file URIs: `file:///…`
 */
open class UriFlagAssetProvider(
    private val baseUri: String,
) : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any {
        val code = iso2Code.lowercase()
        val base = baseUri.trimEnd('/')
        return "$base/$code.svg"
    }
}

/**
 * Loads flags from the Android `assets/` directory via Coil's asset URI scheme.
 *
 * Example: assets under `src/main/assets/flags/us.svg` →
 * `AndroidAssetFlagAssetProvider("flags")`.
 */
class AndroidAssetFlagAssetProvider(
    assetDirectory: String = "flags",
) : FlagAssetProvider by UriFlagAssetProvider(
    "file:///android_asset/${assetDirectory.trim('/')}"
)

/**
 * Loads flags via a custom resolver — use this with Compose Multiplatform
 * `composeResources` (or any classpath / generated resource API).
 *
 * Example with CMP compose resources:
 * ```kotlin
 * ComposeResourceFlagAssetProvider { iso2 ->
 *     Res.readBytes("files/flags/$iso2.svg")
 * }
 * ```
 *
 * The resolver receives a lowercase ISO2 code and must return a Coil model
 * (typically [ByteArray] from `Res.readBytes`, or a resource URI).
 */
class ComposeResourceFlagAssetProvider(
    private val resolve: (iso2Code: String) -> Any,
) : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any = resolve(iso2Code.lowercase())
}
