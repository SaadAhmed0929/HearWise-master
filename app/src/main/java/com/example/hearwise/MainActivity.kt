package com.example.hearwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.hearwise.core.navigation.NavGraph
import com.example.hearwise.core.theme.HearWiseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            HearWiseTheme {
                Surface(modifier = Modifier) {
                    NavGraph()
                }
            }
        }
    }
}