package com.moinofactory.moinogeo.maps.sphere

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

internal actual fun ImageBitmap.encodeToPng(): ByteArray {
    val awt = toAwtImage()
    val stream = ByteArrayOutputStream()
    ImageIO.write(awt, "png", stream)
    return stream.toByteArray()
}
