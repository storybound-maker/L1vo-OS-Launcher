package com.l1vo.oslauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT)
        setContent { L1voLauncherApp() }
    }
}
