package com.moinofactory.moinogeo.maps.countries

import com.moinofactory.moinogeo.maps.core.BoundaryData
import kotlinx.serialization.json.Json

internal object BoundaryDataJson {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun decode(text: String): BoundaryData {
        if (text.isBlank()) return BoundaryData()
        return runCatching { json.decodeFromString(BoundaryData.serializer(), text) }
            .getOrDefault(BoundaryData())
    }

    fun decodeBytes(bytes: ByteArray): BoundaryData =
        decode(bytes.decodeToString())
}
