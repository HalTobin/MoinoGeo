package com.moinofactory.moinogeo.maps

fun interface BoundaryProvider {
    fun getBoundaryJson(): String
}
