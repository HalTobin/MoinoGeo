package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.ui.graphics.ImageBitmap

/** Encodes an [ImageBitmap] to PNG bytes for Filament `rememberTexture` loaders. */
internal expect fun ImageBitmap.encodeToPng(): ByteArray
