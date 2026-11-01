package com.moinofactory.moinogeo.maps.topography

/** Reads a file packaged under `src/commonMain/resources/`. */
internal expect fun readClasspathResource(path: String): ByteArray?
