# Vector SVG Country Flags Guide

MoinoGeo bundles vector country flags for all ISO 3166-1 alpha-2 entities. Vectors provide lossless scaling on high-DPI mobile screens while maintaining a minimal asset footprint (~1.8 MB total).

---

## 1. Asset Specifications & Optimization

- **Format:** SVG (Scalable Vector Graphics).
- **Naming Convention:** Lowercase ISO 3166-1 alpha-2 code (e.g., `us.svg`, `de.svg`, `jp.svg`, `fr.svg`, `ua.svg`).
- **Optimization Strategy:** Minified via `scripts/optimize_flags.py` using SVGO rules:
  - Removal of XML metadata, comments, doctypes, and hidden elements.
  - Coordinate precision reduction.
  - Stripping redundant formatting whitespace.
- **Average Size:** ~1 KB to 3 KB per SVG icon.

---

## 2. Android Jetpack Compose Integration

Render bundled SVG flags in Jetpack Compose using [Coil](https://coil-kt.github.io/coil/) with `SvgDecoder`.

### Gradle Dependency
```kotlin
// build.gradle.kts
dependencies {
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-svg:2.5.0")
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assetPath = "file:///android_asset/flags/${iso2Code.lowercase()}.svg"

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(assetPath)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier
    )
}
```

---

## 3. iOS & macOS SwiftUI Integration

Render bundled SVG vector flags losslessly in SwiftUI using a lightweight `WKWebView` wrapper or `Image(uiImage:)` / `SVGKit`.

### SwiftUI SVG Representable Component
```swift
import SwiftUI
import WebKit

// WebView wrapper for losslessly rendering bundled SVG vector flags in SwiftUI
public struct SVGFlagView: UIViewRepresentable {
    public let iso2Code: String

    public init(iso2Code: String) {
        self.iso2Code = iso2Code
    }

    public func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.isOpaque = false
        webView.backgroundColor = .clear
        webView.scrollView.isScrollEnabled = false
        return webView
    }

    public func updateUIView(_ uiView: WKWebView, context: Context) {
        guard let url = MoinoGeoBundle.flagURL(for: iso2Code),
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
