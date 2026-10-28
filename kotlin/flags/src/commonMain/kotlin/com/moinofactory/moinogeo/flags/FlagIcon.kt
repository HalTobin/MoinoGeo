package com.moinofactory.moinogeo.flags

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage

@Composable
fun FlagIcon(
    iso2Code: String,
    modifier: Modifier = Modifier,
    provider: FlagAssetProvider = DefaultFlagAssetProvider(),
    contentDescription: String? = "Flag of $iso2Code"
) {
    AsyncImage(
        model = provider.getFlagData(iso2Code),
        contentDescription = contentDescription,
        modifier = modifier
    )
}
