package com.moinofactory.moinogeo.flags

import androidx.compose.runtime.Composable
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
 * [AndroidAssetFlagAssetProvider] implementation.
 */
@Composable
fun FlagIcon(
    iso2Code: String,
    provider: FlagAssetProvider,
    modifier: Modifier = Modifier,
    contentDescription: String? = "Flag of $iso2Code",
) {
    val context = LocalPlatformContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(provider.getFlagData(iso2Code))
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
