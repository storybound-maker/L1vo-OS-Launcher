package com.l1vo.oslauncher

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
        window.setStatusBarColor(AndroidColor.TRANSPARENT)
        window.setNavigationBarColor(AndroidColor.TRANSPARENT)
        setContent { L1voLauncherApp() }
    }
}
