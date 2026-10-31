package com.moinofactory.moinogeo.flags

/**
 * Reads a file packaged under `src/commonMain/resources/` (or equivalent
 * classpath / JAR entry) as raw bytes.
 *
 * Returns null when the resource is missing or unavailable on this platform.
 */
expect fun readClasspathResource(path: String): ByteArray?
