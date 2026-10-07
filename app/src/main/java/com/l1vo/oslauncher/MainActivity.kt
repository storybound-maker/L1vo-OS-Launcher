package com.l1vo.oslauncher

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    lateinit var widgetHost: AppWidgetHost
    private val widgetHostId = 0x4C3156
    private val pickWidgetRequest = 4101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetHost = AppWidgetHost(this, widgetHostId)
        window.setBackgroundDrawable(ColorDrawable(AndroidColor.rgb(16,22,18)))
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setWindowAnimations(0)
        window.setStatusBarColor(AndroidColor.TRANSPARENT)
        window.setNavigationBarColor(AndroidColor.TRANSPARENT)
        setContent { L1voLauncherApp() }
        window.decorView.post { window.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT)) }
    }

    fun pickHomeWidget() {
        val id = widgetHost.allocateAppWidgetId()
        startActivityForResult(
            Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id),
            pickWidgetRequest
        )
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode==pickWidgetRequest){
            val id=data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID)
                ?:AppWidgetManager.INVALID_APPWIDGET_ID
            if(resultCode==RESULT_OK && id!=AppWidgetManager.INVALID_APPWIDGET_ID){
                val p=getSharedPreferences(PREFS,MODE_PRIVATE)
                val ids=p.getStringSet("home_widget_ids",emptySet())!!.toMutableSet()
                ids.add(id.toString())
                p.edit().putStringSet("home_widget_ids",ids).apply()
            }else if(id!=AppWidgetManager.INVALID_APPWIDGET_ID) widgetHost.deleteAppWidgetId(id)
        }
    }

    override fun onStart(){super.onStart();widgetHost.startListening()}
    override fun onStop(){widgetHost.stopListening();super.onStop()}

    override fun onResume() {
        super.onResume()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }
}
