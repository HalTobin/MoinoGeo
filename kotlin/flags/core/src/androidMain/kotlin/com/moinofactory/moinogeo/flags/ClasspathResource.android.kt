package com.moinofactory.moinogeo.flags

actual fun readClasspathResource(path: String): ByteArray? {
    val normalized = path.trimStart('/')
    val loaders = listOfNotNull(
        Thread.currentThread().contextClassLoader,
        ClasspathFlagAssetProvider::class.java.classLoader,
        ClassLoader.getSystemClassLoader(),
    )
    for (loader in loaders) {
        loader.getResourceAsStream(normalized)?.use { return it.readBytes() }
    }
    return null
}
