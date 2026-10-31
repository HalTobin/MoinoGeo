package com.moinofactory.moinogeo.flags

/**
 * Pluggable source of flag artwork keyed by ISO 3166-1 alpha-2.
 *
 * Implement this yourself to supply custom SVGs, drawables, bitmaps, or CDN URLs.
 * Return any Coil-compatible model (URI/path [String], [ByteArray], file path,
 * Android `@DrawableRes`/`@RawRes` [Int], Bitmap / ImageBitmap, etc.), or `null`
 * when the flag is unavailable so a [FallbackFlagAssetProvider] / [FlagIcon]
 * fallback can take over.
 */
fun interface FlagAssetProvider {
    fun getFlagData(iso2Code: String): Any?
}

/**
 * Tries [primary], then an optional secondary [fallback] provider, then optional
 * static [fallbackData] (any Coil model).
 */
class FallbackFlagAssetProvider(
    private val primary: FlagAssetProvider,
    private val fallback: FlagAssetProvider? = null,
    private val fallbackData: Any? = null,
) : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any? =
        primary.getFlagData(iso2Code)
            ?: fallback?.getFlagData(iso2Code)
            ?: fallbackData
}

/** Chains another provider when this one returns `null`. */
fun FlagAssetProvider.withFallback(fallback: FlagAssetProvider): FlagAssetProvider =
    FallbackFlagAssetProvider(primary = this, fallback = fallback)

/** Uses a fixed Coil model when this provider returns `null`. */
fun FlagAssetProvider.withFallback(fallbackData: Any): FlagAssetProvider =
    FallbackFlagAssetProvider(primary = this, fallbackData = fallbackData)

/**
 * Resolves flags from a base URI or path prefix.
 *
 * Works for:
 * - Remote CDN: `https://cdn.example.com/flags`
 * - Android assets: `file:///android_asset/flags-lipis`
 * - Local / file URIs: `file:///…`
 *
 * Always returns a URI string (cannot detect remote 404s). Pair with
 * [FlagIcon]'s `fallback` for load-error recovery.
 */
open class UriFlagAssetProvider(
    private val baseUri: String,
    private val extension: String = "svg",
) : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any {
        val code = iso2Code.lowercase()
        val base = baseUri.trimEnd('/')
        val ext = extension.trimStart('.')
        return "$base/$code.$ext"
    }
}

/**
 * Loads flags from a local filesystem directory via `file://` URIs.
 *
 * Example: files at `/data/flags/us.svg` →
 * `FileFlagAssetProvider("/data/flags")`.
 */
class FileFlagAssetProvider(
    directoryPath: String,
    extension: String = "svg",
) : FlagAssetProvider by UriFlagAssetProvider(
    baseUri = "file://${directoryPath.trimEnd('/')}",
    extension = extension,
)

/**
 * Loads flags from the Android `assets/` directory via Coil's asset URI scheme.
 *
 * Example: assets under `src/main/assets/flags/us.svg` →
 * `AndroidAssetFlagAssetProvider("flags")`.
 */
class AndroidAssetFlagAssetProvider(
    assetDirectory: String = "flags",
    extension: String = "svg",
) : FlagAssetProvider by UriFlagAssetProvider(
    baseUri = "file:///android_asset/${assetDirectory.trim('/')}",
    extension = extension,
)

/**
 * Resolves Android `@DrawableRes` / `@RawRes` resource IDs (Coil model = [Int]).
 *
 * Intended for Android targets. Returns `null` when [resolveResId] yields `null`
 * or `0`.
 *
 * ```kotlin
 * AndroidResourceFlagAssetProvider { iso2 ->
 *     resources.getIdentifier("flag_$iso2", "drawable", packageName)
 * }
 * ```
 */
class AndroidResourceFlagAssetProvider(
    private val resolveResId: (iso2Code: String) -> Int?,
) : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any? {
        val id = resolveResId(iso2Code.lowercase()) ?: return null
        return id.takeIf { it != 0 }
    }
}

/**
 * Supplies preloaded / in-memory Coil models keyed by ISO2
 * (Bitmap, ImageBitmap, Image, ByteArray PNG/SVG, URI String, resource Int, …).
 */
class MappedFlagAssetProvider(
    modelsByIso2: Map<String, Any>,
) : FlagAssetProvider {
    private val models = modelsByIso2.mapKeys { (key, _) -> key.lowercase() }

    override fun getFlagData(iso2Code: String): Any? = models[iso2Code.lowercase()]
}

/**
 * General resolver for any Coil-compatible model (bitmap, URL, bytes, resource ID, …).
 */
class ResolvingFlagAssetProvider(
    private val resolve: (iso2Code: String) -> Any?,
) : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any? = resolve(iso2Code.lowercase())
}

/**
 * Loads flags via a custom resolver — use this with Compose Multiplatform
 * `composeResources` (or any classpath / generated resource API).
 *
 * Example with CMP compose resources:
 * ```kotlin
 * ComposeResourceFlagAssetProvider { iso2 ->
 *     runCatching { Res.readBytes("files/flags/$iso2.svg") }.getOrNull()
 * }
 * ```
 *
 * The resolver receives a lowercase ISO2 code and must return a Coil model
 * (typically [ByteArray] from `Res.readBytes`, or a resource URI), or `null`
 * when missing.
 */
class ComposeResourceFlagAssetProvider(
    private val resolve: (iso2Code: String) -> Any?,
) : FlagAssetProvider by ResolvingFlagAssetProvider(resolve)

/**
 * Loads flags packaged under `src/commonMain/resources/<directory>/`.
 *
 * Example: resources at `flags-lipis-square/us.svg` →
 * `ClasspathFlagAssetProvider("flags-lipis-square")`.
 *
 * Returns `null` when the resource is missing so callers can apply a fallback.
 */
class ClasspathFlagAssetProvider(
    private val assetDirectory: String,
    private val extension: String = "svg",
) : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any? {
        val directory = assetDirectory.trim('/')
        val ext = extension.trimStart('.')
        val path = "$directory/${iso2Code.lowercase()}.$ext"
        return readClasspathResource(path)
    }
}
