package com.moinofactory.moinogeo.maps.core

fun interface TopographyProvider {
    fun getTopographyData(): TopographyData
}
