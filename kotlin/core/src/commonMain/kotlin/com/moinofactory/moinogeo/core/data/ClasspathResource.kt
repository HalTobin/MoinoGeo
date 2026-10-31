package com.moinofactory.moinogeo.core.data

/**
 * Reads a file packaged under `src/commonMain/resources/` (or equivalent
 * classpath / JAR entry) as raw bytes.
 *
 * Returns null when the resource is missing or unavailable on this platform.
 */
internal expect fun readClasspathResource(path: String): ByteArray?

internal fun requireBundledDatabaseBytes(): ByteArray {
    return readClasspathResource(MoinoGeoDatabase.RESOURCE_PATH)
        ?: error(
            "Bundled database not found on classpath: ${MoinoGeoDatabase.RESOURCE_PATH}. " +
                "Run: python3 scripts/regions/export_kotlin.py",
        )
}
