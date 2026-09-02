package com.l1vo.oslauncher
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
val L1voGreen=Color(0xFF3E6B34);val L1voDeepGreen=Color(0xFF315A2A);val L1voInk=Color(0xFF18241F);val L1voPanel=Color(0xFFF4F4E9);val L1voDark=Color(0xFF101612)
const val PREFS="l1vo_launcher";const val WALLPAPER="wallpaper";const val LEAU_PACKAGE="com.liv.ol1viapa";const val ANIMATIONS="animations";const val DARK_THEME="dark_theme";const val FONT="font";const val NOTIFICATIONS="notifications";const val PILL_APP="pill_app";const val ACCOUNT_NAME="account_name"
data class LaunchableApp(val label:String,val packageName:String,val intent:Intent,val icon:Bitmap)
data class QuickSlot(val id:String,val label:String,val packageName:String?,val kind:SlotKind)
enum class SlotKind{HOME,SETTINGS,GALLERY,CALLS,APP}
