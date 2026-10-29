# Vector SVG Country Flags Extension Guide

Country flags are provided as an **optional visual extension** with a **swappable icon-set architecture**. Rather than locking applications into a single aesthetic, MoinoGeo separates the flag engine into a lightweight core abstraction (`kotlin/flags`) and multiple independent icon-set modules (`flags-lipis`, `flags-flagpack`, `flags-circle`).

Apps can easily swap between different icon sets by changing a dependency, or supply completely custom artwork.

---

## 1. Swappable Icon-Set Architecture

```
+-------------------------------------------------------------------------------+
|                      Flags Core (kotlin/flags)                                |
|  - FlagAssetProvider (interface)                                              |
|  - FlagIcon (Compose Multiplatform composable with Coil SVG decoder)          |
|  - Bundles NO assets (< 50 KB)                                                |
+-------------------------------------------------------------------------------+
                                       |
        +------------------------------+------------------------------+
        |                              |                              |
        v                              v                              v
+-----------------------+      +-----------------------+      +-----------------------+
|   flags-lipis         |      |   flags-flagpack      |      |   flags-circle        |
| - lipis/flag-icons    |      | - flagpack-core       |      | - circle-flags        |
| - Rectangular 4x3/1x1 |      | - Stylized rounded    |      | - Circular 1x1        |
| - ~1.8 MB             |      | - ~1.5 MB             |      | - ~1.6 MB             |
+-----------------------+      +-----------------------+      +-----------------------+
```

### Supported Icon Sets Out of the Box:
1. **`lipis/flag-icons` (`kotlin/flags-lipis`)**: Standard realistic rectangular (4×3 and 1×1) SVG country flags.
2. **`flagpack-core` by Yummygum (`kotlin/flags-flagpack`)**: Stylized, modern rounded-corner flag icons ideal for UI dropdowns and lists.
3. **`circle-flags` by HatScripts (`kotlin/flags-circle`)**: Circular 1×1 flag icons designed specifically for avatars, badges, and user profile UIs.

Developers can also implement `FlagAssetProvider` directly to supply their own local SVGs, vector drawables, or remote CDN URLs.

---

## 2. Asset Specifications & Optimization

- **Format:** SVG (Scalable Vector Graphics).
- **Naming Convention:** Lowercase ISO 3166-1 alpha-2 code (e.g., `us.svg`, `de.svg`, `jp.svg`, `fr.svg`, `ua.svg`).
- **Optimization Strategy:** Minified via `scripts/optimize_flags.py`:
  - Removal of XML metadata, comments, doctypes, and hidden elements.
  - Coordinate precision reduction.
  - Stripping redundant formatting whitespace.
- **Average Size:** ~1 KB to 3 KB per SVG icon.

---

## 3. Kotlin / Compose Multiplatform Integration

### Abstract Flag Asset Provider (Kotlin)

```kotlin
package com.moinofactory.moinogeo.flags

// Core interface in kotlin/flags
fun interface FlagAssetProvider {
    fun getFlagData(iso2Code: String): Any // Returns File, URL, Asset Path, or ImageModel
}
```

### Implementations in Icon-Set Modules

```kotlin
// In kotlin/flags-lipis
class LipisFlagAssetProvider(private val basePath: String = "file:///android_asset/flags-lipis") : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any = "$basePath/${iso2Code.lowercase()}.svg"
}

// In kotlin/flags-flagpack
class FlagpackAssetProvider(private val basePath: String = "file:///android_asset/flags-flagpack") : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any = "$basePath/${iso2Code.lowercase()}.svg"
}

// In kotlin/flags-circle
class CircleFlagAssetProvider(private val basePath: String = "file:///android_asset/flags-circle") : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any = "$basePath/${iso2Code.lowercase()}.svg"
}

// Custom CDN provider
class CdnFlagAssetProvider(private val baseUrl: String) : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any = "$baseUrl/${iso2Code.lowercase()}.svg"
}
```

### Composable Flag Component

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalPlatformContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder

@Composable
fun FlagIcon(
    iso2Code: String,
    modifier: Modifier = Modifier,
    provider: FlagAssetProvider,
    contentDescription: String? = "Flag of $iso2Code"
) {
    val context = LocalPlatformContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(provider.getFlagData(iso2Code))
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = contentDescription,
        modifier = modifier
    )
}
```

---

## 4. iOS & macOS SwiftUI Integration (Planned / Deferred)

*(Swift implementation is planned for a later phase; the architecture will mirror the Kotlin provider pattern using a Swift `FlagProvider` protocol and SwiftUI `FlagView`.)*
