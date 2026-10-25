# Vector SVG Country Flags Extension Guide

As part of MoinoGeo's mobile platform wrapper, vector country flags are provided as an **optional visual extension module** for ISO 3166-1 alpha-2 country records. Vectors provide lossless scaling on high-DPI mobile screens while maintaining a minimal asset footprint (~1.8 MB total).

The flag module is **completely unbundlable and abstractable**. Apps that do not render flag icons can omit the `flags/` directory to save ~1.8 MB, and apps requiring custom visual aesthetics can provide **custom flag artwork** on top of MoinoGeo's Kotlin/Swift flag components.

---

## 1. Modular Visual Extension & Custom Art Overriding

For aesthetic, branding, or geopolitical/legal compliance reasons, developers may need to customize flag artwork, supply alternative flag designs (e.g., circular flags, waving flags, 3D flags), or use a proprietary flag set.

MoinoGeo decouples its Kotlin/Swift flag UI components from the default bundled SVG assets via an abstract **Asset Provider** protocol/interface.

```
+-----------------------------------------------------------------------+
|                    MoinoGeo Flag UI Extension                         |
+-----------------------------------------------------------------------+
                                   |
                  +----------------+----------------+
                  v                                 v
   +------------------------------+  +------------------------------+
   | Default Asset Provider       |  | Custom Flag Asset Provider   |
   | - Resolves bundled SVGs      |  | - Resolves custom asset directory|
   |   (e.g., assets/flags/us.svg)|  | - Resolves remote CDN URLs    |
   |                              |  | - Resolves alternative artwork|
   +------------------------------+  +------------------------------+
```

### Key Modularity Capabilities:
1. **Omit Flag Vectors:** Strip the `flags/` directory from assets if your app does not display flags, saving ~1.8 MB binary size.
2. **Custom Artwork Injection:** Implement `FlagAssetProvider` (Kotlin) or `FlagProvider` (Swift) to redirect asset loading to custom paths, local bundles, or custom vector repositories while retaining MoinoGeo's native mobile UI components.

---

## 2. Asset Specifications & Optimization

- **Format:** SVG (Scalable Vector Graphics).
- **Naming Convention:** Lowercase ISO 3166-1 alpha-2 code (e.g., `us.svg`, `de.svg`, `jp.svg`, `fr.svg`, `ua.svg`).
- **Optimization Strategy:** Minified via `scripts/optimize_flags.py` using SVGO rules:
  - Removal of XML metadata, comments, doctypes, and hidden elements.
  - Coordinate precision reduction.
  - Stripping redundant formatting whitespace.
- **Average Size:** ~1 KB to 3 KB per SVG icon.

---

## 3. Android Jetpack Compose Integration

Render bundled SVG flags or custom flag art in Jetpack Compose using Coil with `SvgDecoder` and abstract asset resolution.

### Abstract Flag Asset Provider (Kotlin)

```kotlin
// Abstract interface for resolving flag image model/data
fun interface FlagAssetProvider {
    fun getFlagData(iso2Code: String): Any // Returns File, URL, Asset Path, or Drawable Res ID
}

// Default implementation loading bundled assets
class DefaultAssetFlagProvider(private val assetSubdirectory: String = "flags") : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any {
        return "file:///android_asset/$assetSubdirectory/${iso2Code.lowercase()}.svg"
    }
}

// Custom implementation loading custom artwork (e.g. custom directory or remote CDN)
class CustomArtFlagProvider(private val baseUrl: String) : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any {
        return "$baseUrl/${iso2Code.lowercase()}-custom.svg"
    }
}
```

### Composable Flag Component
```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

@Composable
fun ComposableFlagIcon(
    iso2Code: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    provider: FlagAssetProvider = DefaultAssetFlagProvider()
) {
    val context = LocalContext.current
    val flagModel = provider.getFlagData(iso2Code)

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(flagModel)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier
    )
}
```

---

## 4. iOS & macOS SwiftUI Integration

Render bundled SVG vector flags or custom flag art in SwiftUI using modular `FlagProvider`.

### Abstract Flag Provider Protocol (Swift)

```swift
import Foundation

public protocol FlagProvider {
    func flagURL(for iso2Code: String) -> URL?
}

// Default provider targeting bundled resources
public struct DefaultFlagProvider: FlagProvider {
    public init() {}
    public func flagURL(for iso2Code: String) -> URL? {
        MoinoGeoBundle.flagURL(for: iso2Code)
    }
}

// Custom provider loading custom artwork from a custom bundle or local path
public struct CustomDirectoryFlagProvider: FlagProvider {
    private let directoryURL: URL

    public init(directoryURL: URL) {
        self.directoryURL = directoryURL
    }

    public func flagURL(for iso2Code: String) -> URL? {
        directoryURL.appendingPathComponent("\(iso2Code.lowercased()).svg")
    }
}
```

### SwiftUI SVG Flag Component
```swift
import SwiftUI
import WebKit

public struct SVGFlagView: UIViewRepresentable {
    public let iso2Code: String
    public let provider: FlagProvider

    public init(iso2Code: String, provider: FlagProvider = DefaultFlagProvider()) {
        self.iso2Code = iso2Code
        self.provider = provider
    }

    public func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.isOpaque = false
        webView.backgroundColor = .clear
        webView.scrollView.isScrollEnabled = false
        return webView
    }

    public func updateUIView(_ uiView: WKWebView, context: Context) {
        guard let url = provider.flagURL(for: iso2Code),
              let svgData = try? Data(contentsOf: url) else { return }

        uiView.load(
            svgData,
            mimeType: "image/svg+xml",
            characterEncodingName: "UTF-8",
            baseURL: url.deletingLastPathComponent()
        )
    }
}
```
