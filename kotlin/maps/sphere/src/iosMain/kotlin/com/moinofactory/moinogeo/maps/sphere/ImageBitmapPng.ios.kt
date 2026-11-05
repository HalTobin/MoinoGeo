package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

internal actual fun ImageBitmap.encodeToPng(): ByteArray {
    val skia = asSkiaBitmap()
    val image = Image.makeFromBitmap(skia)
    return image.encodeToData(EncodedImageFormat.PNG)?.bytes ?: ByteArray(0)
}
