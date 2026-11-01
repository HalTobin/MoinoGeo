package com.moinofactory.moinogeo.maps.countries

/**
 * Reads a file packaged under `src/commonMain/resources/` (or equivalent
 * classpath / JAR entry) as raw bytes.
 */
internal expect fun readClasspathResource(path: String): ByteArray?
