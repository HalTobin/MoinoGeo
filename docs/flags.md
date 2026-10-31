# Vector SVG Country Flags Extension Guide

Country flags are provided as an **optional visual extension** with a **swappable icon-set architecture**. Rather than locking applications into a single aesthetic, MoinoGeo separates the flag engine into a lightweight core abstraction (`kotlin/flags`) and multiple independent icon-set modules (`flags-lipis`, `flags-flagpack`, `flags-circle`).

Apps can easily swap between different icon sets by changing a dependency, or supply completely custom artwork by implementing `FlagAssetProvider`.

---

## 1. Swappable Icon-Set Architecture

```
+-------------------------------------------------------------------------------+
|                      Flags Core (kotlin/flags)                                |
|  - FlagAssetProvider (interface)                                              |
|  - UriFlagAssetProvider / AndroidAssetFlagAssetProvider                       |
|  - ComposeResourceFlagAssetProvider (CMP composeResources / custom resolvers) |
|  - FlagIcon (Compose Multiplatform composable with Coil SVG decoder)          |
|  - Bundles NO assets (< 50 KB)                                                |
+-------------------------------------------------------------------------------+
                                       |
        +------------------------------+------------------------------+
        |                              |                              |
        v                              v                              v
+-----------------------+      +-----------------------+      +-----------------------+
|   flags-lipis         |      |   flags-flagpack      |      |   flags-circle        |
| - lipis/flag-icons    |      | - Yummygum/flagpack   |      | - HatScripts/circle-  |
| - Rectangular 4x3/1x1 |      |   -core               |      |   flags               |
| - ~1.8 MB             |      | - Stylized rounded    |      | - Circular 1x1        |
|                       |      | - ~1.5 MB             |      | - ~1.6 MB             |
+-----------------------+      +-----------------------+      +-----------------------+
```

### Supported Icon Sets Out of the Box
1. **`lipis/flag-icons` (`kotlin/flags-lipis`)**: Standard realistic rectangular (4×3 and 1×1) SVG country flags.
2. **`Yummygum/flagpack-core` (`kotlin/flags-flagpack`)**: Stylized, modern rounded-corner flag icons ideal for UI dropdowns and lists.
3. **`HatScripts/circle-flags` (`kotlin/flags-circle`)**: Circular 1×1 flag icons designed specifically for avatars, badges, and user profile UIs.

> **Status:** Icon-set modules are scaffolded with providers and resource directories. Populate SVG assets via `scripts/optimize_flags.py` (see [Maintenance](maintenance.md)).

Developers can also implement `FlagAssetProvider` directly to supply their own local SVGs, vector drawables, or remote CDN URLs — no icon-set module required.

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

### Gradle Dependencies

```kotlin
// Always needed for FlagIcon + FlagAssetProvider
implementation(projects.flags)

// Pick ONE planned icon set (optional — or bring your own provider)
implementation(projects.flagsLipis)      // lipis/flag-icons
// implementation(projects.flagsFlagpack) // Yummygum/flagpack-core
// implementation(projects.flagsCircle)   // HatScripts/circle-flags
```

With typesafe project accessors the modules are `projects.flags`, `projects.flagsLipis`, `projects.flagsFlagpack`, and `projects.flagsCircle`. Without accessors use `project(":flags")`, `project(":flags-lipis")`, etc.

### Abstract Flag Asset Provider (Core)

```kotlin
package com.moinofactory.moinogeo.flags

// Core interface in kotlin/flags — implement manually for custom flag sets
fun interface FlagAssetProvider {
    fun getFlagData(iso2Code: String): Any // File, URL, ByteArray, Asset path, ImageModel, …
}
```

### Android Assets vs Compose Multiplatform Resources

The core module supports both packaging styles through small helpers:

| Packaging | Helper | Typical Coil model |
| :--- | :--- | :--- |
| **Android `assets/`** | `AndroidAssetFlagAssetProvider("flags-lipis")` | `file:///android_asset/flags-lipis/us.svg` |
| **CMP `composeResources`** | `ComposeResourceFlagAssetProvider { … }` | `ByteArray` from `Res.readBytes("files/flags/us.svg")` |
| **CDN / custom URI** | `UriFlagAssetProvider("https://…")` | Absolute URL string |

**Android assets example:**

```kotlin
val provider = AndroidAssetFlagAssetProvider("flags-lipis")
// or from an icon-set module:
val provider = LipisFlagAssetProvider.androidAssets()
```

**Compose Multiplatform `composeResources` example:**

```kotlin
import moinogeo.flags.generated.resources.Res

val provider = ComposeResourceFlagAssetProvider { iso2 ->
    Res.readBytes("files/flags/$iso2.svg")
}

// or via an icon-set factory:
val provider = LipisFlagAssetProvider.composeResources { iso2 ->
    Res.readBytes("files/flags-lipis/$iso2.svg")
}
```

**Custom CDN / manual implementation:**

```kotlin
class CdnFlagAssetProvider(
    private val baseUrl: String,
) : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any =
        "$baseUrl/${iso2Code.lowercase()}.svg"
}
```

### Icon-Set Module Providers

```kotlin
// kotlin/flags-lipis — lipis/flag-icons
class LipisFlagAssetProvider(
    basePath: String = "file:///android_asset/flags-lipis",
) : FlagAssetProvider

// kotlin/flags-flagpack — Yummygum/flagpack-core
class FlagpackAssetProvider(
    basePath: String = "file:///android_asset/flags-flagpack",
) : FlagAssetProvider

// kotlin/flags-circle — HatScripts/circle-flags
class CircleFlagAssetProvider(
    basePath: String = "file:///android_asset/flags-circle",
) : FlagAssetProvider
```

### Composable Flag Component

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder
import com.moinofactory.moinogeo.flags.FlagAssetProvider
import com.moinofactory.moinogeo.flags.FlagIcon

@Composable
fun ExampleFlagRow(iso2Code: String, provider: FlagAssetProvider) {
    FlagIcon(
        iso2Code = iso2Code,
        provider = provider,
        modifier = Modifier, // size as needed
    )
}
```

`FlagIcon` always requires an explicit `provider` — the core module ships **no** default assets.

---

## 4. iOS & macOS SwiftUI Integration (Planned / Deferred)

*(Swift implementation is planned for a later phase; the architecture will mirror the Kotlin provider pattern using a Swift `FlagProvider` protocol and SwiftUI `FlagView`.)*
