package com.moinofactory.moinogeo.jvm

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.moinofactory.moinogeo.demo.shared.MoinoGeoAppScreen
import com.moinofactory.moinogeo.demo.shared.createDatabase

fun main() = application {
    val db = createDatabase()
    Window(
        onCloseRequest = ::exitApplication,
        title = "MoinoGeo JVM Demo",
    ) {
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                MoinoGeoAppScreen(db)
            }
        }
    }
}
