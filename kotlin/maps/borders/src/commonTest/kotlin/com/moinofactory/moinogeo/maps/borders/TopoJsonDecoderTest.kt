package com.moinofactory.moinogeo.maps.borders

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TopoJsonDecoderTest {
    @Test
    fun testDefaultBoundaryProvider() {
        val provider = DefaultBoundaryProvider()
        val data = provider.getBoundaryData()
        assertTrue(data.features.isNotEmpty())
    }

    @Test
    fun testEmptyTopoJsonDecoding() {
        val data = TopoJsonDecoder.decodeTopoJson("")
        assertEquals(0, data.features.size)
    }
}
