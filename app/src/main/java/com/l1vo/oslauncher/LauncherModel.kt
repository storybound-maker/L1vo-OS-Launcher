package com.l1vo.oslauncher
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

var L1voGreen=Color(0xFF3E6B34)
var L1voDeepGreen=Color(0xFF315A2A)
var L1voInk=Color(0xFF18241F)
var L1voPanel=Color(0xFFF4F4E9)
val L1voDark=Color(0xFF101612)

data class L1voThemePalette(val accent:Color,val deep:Color,val ink:Color,val panel:Color,val highlight:Color)
fun l1voThemePalette(name:String,tone:Float=1f):L1voThemePalette{
    val base=when(name.lowercase()){
        "autumn"->L1voThemePalette(Color(0xFFD4772F),Color(0xFF8A4A22),Color(0xFF3A2418),Color(0xFFF3E7D7),Color(0xFFE6A15D))
        "cherry blossom"->L1voThemePalette(Color(0xFFD85A82),Color(0xFFB43E67),Color(0xFF3A2029),Color(0xFFF8E9EF),Color(0xFFF0A7BF))
        "northern lights"->L1voThemePalette(Color(0xFF65D6A0),Color(0xFF5A3D8A),Color(0xFFEAE1FF),Color(0xFF211A32),Color(0xFF8D7BE8))
        "winter"->L1voThemePalette(Color(0xFF6CA9D8),Color(0xFF4D83B1),Color(0xFF203040),Color(0xFFEAF4FC),Color(0xFFAED7F3))
        else->L1voThemePalette(Color(0xFF3E6B34),Color(0xFF315A2A),Color(0xFF18241F),Color(0xFFF4F4E9),Color(0xFF7E9E74))
    }
    val t=tone.coerceIn(.35f,1f)
    fun blend(c:Color,amount:Float)=Color(
        c.red*amount+(1-amount)*.5f,
        c.green*amount+(1-amount)*.5f,
        c.blue*amount+(1-amount)*.5f,
        1f
    )
    return base.copy(accent=blend(base.accent,t),deep=blend(base.deep,t),highlight=blend(base.highlight,t))
}
fun applyL1voTheme(name:String,tone:Float=1f){
    val x=l1voThemePalette(name,tone)
    L1voGreen=x.accent;L1voDeepGreen=x.deep;L1voInk=x.ink;L1voPanel=x.panel
}

const val PREFS="l1vo_launcher"
const val WALLPAPER="wallpaper";const val WALLPAPER_PLAYLIST="wallpaper_playlist";const val WALLPAPER_ALL="wallpaper_all";const val WALLPAPER_PLAYLIST_ALL="wallpaper_playlist_all";const val WALLPAPER_INTERVAL="wallpaper_interval";const val WALLPAPER_INTERVAL_ALL="wallpaper_interval_all";const val WALLPAPER_INTERVAL_MAIN="wallpaper_interval_main";const val WALLPAPER_INTERVAL_HOME="wallpaper_interval_home";const val WALLPAPER_INTERVAL_HUB="wallpaper_interval_hub";const val WALLPAPER_MODE="wallpaper_mode";const val WALLPAPER_MAIN="wallpaper_main";const val WALLPAPER_HOME="wallpaper_home";const val WALLPAPER_HUB="wallpaper_hub";const val WALLPAPER_PLAYLIST_MAIN="wallpaper_playlist_main";const val WALLPAPER_PLAYLIST_HOME="wallpaper_playlist_home";const val WALLPAPER_PLAYLIST_HUB="wallpaper_playlist_hub";const val WALLPAPER_MODE_MAIN="wallpaper_mode_main";const val WALLPAPER_MODE_HOME="wallpaper_mode_home";const val WALLPAPER_MODE_HUB="wallpaper_mode_hub"
const val FONT_SIZE="font_size";const val FONT_COLOR="font_color";const val HIGHLIGHT_SHAPE="highlight_shape";const val HIGHLIGHT_SIZE="highlight_size";const val APPHUB_NAV="apphub_nav";const val APP_SIZE="app_size";const val APPHUB_COLUMNS="apphub_columns";const val APPHUB_HSPACE="apphub_hspace";const val APPHUB_VSPACE="apphub_vspace";const val LEAU_PACKAGE="com.liv.ol1viapa";const val ANIMATIONS="animations";const val DARK_THEME="dark_theme";const val FONT="font";const val NOTIFICATIONS="notifications";const val PILL_APP="pill_app";const val ACCOUNT_NAME="account_name"
const val L1VO_THEME="l1vo_theme";const val L1VO_THEME_TONE="l1vo_theme_tone"

data class LaunchableApp(val label:String,val packageName:String,val intent:Intent,val icon:Bitmap)
data class QuickSlot(val id:String,val label:String,val packageName:String?,val kind:SlotKind)
enum class SlotKind{HOME,SETTINGS,GALLERY,CALLS,APP}
