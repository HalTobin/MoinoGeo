package com.moinofactory.moinogeo.maps.topography

import com.moinofactory.moinogeo.maps.core.TopographyData
import kotlinx.serialization.json.Json

internal object TopographyDataJson {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun decode(text: String): TopographyData {
        if (text.isBlank()) return TopographyData()
        return runCatching { json.decodeFromString(TopographyData.serializer(), text) }
            .getOrDefault(TopographyData())
    }

    fun decodeBytes(bytes: ByteArray): TopographyData =
        decode(bytes.decodeToString())
}
