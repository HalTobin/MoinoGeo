package com.moinofactory.moinogeo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.moinofactory.moinogeo.demo.shared.MoinoGeoAppScreen
import com.moinofactory.moinogeo.demo.shared.createDatabase
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val db by lazy { createDatabase(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MoinoGeoAppScreen(db)
                }
            }
        }
    }
}
