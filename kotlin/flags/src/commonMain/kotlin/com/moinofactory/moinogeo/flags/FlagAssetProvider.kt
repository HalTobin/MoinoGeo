package com.moinofactory.moinogeo.flags

fun interface FlagAssetProvider {
    fun getFlagData(iso2Code: String): Any
}

class DefaultFlagAssetProvider(private val assetBasePath: String = "file:///android_asset/flags") : FlagAssetProvider {
    override fun getFlagData(iso2Code: String): Any {
        val code = iso2Code.lowercase()
        return "$assetBasePath/$code.svg"
    }
}
