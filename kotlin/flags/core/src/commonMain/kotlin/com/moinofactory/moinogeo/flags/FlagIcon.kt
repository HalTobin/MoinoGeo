package com.moinofactory.moinogeo.flags

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder

/**
 * Renders a country flag via [FlagAssetProvider] and Coil's SVG decoder.
 *
 * The core module bundles no flag assets — pass a provider from an icon-set
 * module (`:flags:lipis-flag-icons-rectangle`, `:flags:lipis-flag-icons-square`,
 * `:flags:yummygum-flagpack`, `:flags:hatscripts-flags-circle`) or your own
 * [FlagAssetProvider] / [ComposeResourceFlagAssetProvider] /
 * [AndroidAssetFlagAssetProvider] / [MappedFlagAssetProvider] /
 * [AndroidResourceFlagAssetProvider] implementation.
 *
 * @param fallback Optional Coil model used when [provider] returns `null` or
 * when the primary image fails to load (e.g. missing SVG, CDN 404).
 */
@Composable
fun FlagIcon(
    iso2Code: String,
    provider: FlagAssetProvider,
    modifier: Modifier = Modifier,
    contentDescription: String? = "Flag of $iso2Code",
    fallback: Any? = null,
) {
    val context = LocalPlatformContext.current
    val primaryData = remember(iso2Code, provider) { provider.getFlagData(iso2Code) }
    var loadFailed by remember(iso2Code, provider, fallback) {
        mutableStateOf(value = false)
    }

    val data = when {
        loadFailed -> fallback
        else -> primaryData ?: fallback
    } ?: return

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(data)
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        onError = {
            if ((fallback != null) && !loadFailed && (data != fallback)) {
                loadFailed = true
            }
        },
    )
}
