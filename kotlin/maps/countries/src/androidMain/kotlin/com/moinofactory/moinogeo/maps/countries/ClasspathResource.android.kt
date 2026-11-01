package com.moinofactory.moinogeo.maps.countries

internal actual fun readClasspathResource(path: String): ByteArray? {
    val normalized = path.trimStart('/')
    val loaders = listOfNotNull(
        Thread.currentThread().contextClassLoader,
        DefaultBoundaryProvider::class.java.classLoader,
        ClassLoader.getSystemClassLoader(),
    )
    for (loader in loaders) {
        loader.getResourceAsStream(normalized)?.use { return it.readBytes() }
    }
    return null
}
