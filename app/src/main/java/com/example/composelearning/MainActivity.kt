package com.example.composelearning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.composelearning.animcompose.RealisticExplosionScreen
import com.example.composelearning.ui.theme.ComposeLearningTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeLearningTheme {
                RealisticExplosionScreen()
            }
        }
    }
}