package com.l1vo.oslauncher

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable fun L1voLauncherApp(){
    val c=LocalContext.current
    val p=remember{c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)}
    var page by remember{mutableStateOf("cube")}
    var wallpaperReturnPage by remember{mutableStateOf("cube")}
    var edit by remember{mutableStateOf<String?>(null)}
    var refresh by remember{mutableIntStateOf(0)}
    var wallpaper by remember{mutableStateOf(p.getString(WALLPAPER,null))}
    var dark by remember{mutableStateOf(p.getBoolean(DARK_THEME,false))}
    var refreshWall by remember{mutableIntStateOf(0)}
    val interval=p.getLong(WALLPAPER_INTERVAL,3600L)
    val useSystemWallpaper=p.getString(WALLPAPER_MODE,"static")=="system"
    val font=when(p.getString(FONT,"Sans")){"Serif"->FontFamily.Serif;"Mono"->FontFamily.Monospace;"Cursive"->FontFamily.Cursive;"Condensed"->FontFamily.SansSerif;else->FontFamily.SansSerif}
    val fontScale=p.getFloat(FONT_SIZE,1f)
    val configuredFontColor=p.getString(FONT_COLOR,"auto")?:"auto"
    fun targetWallpaper(target:String):String?=when(target){"home"->p.getString(WALLPAPER_HOME,null)?:wallpaper;"hub"->p.getString(WALLPAPER_HUB,null)?:wallpaper;else->p.getString(WALLPAPER_MAIN,null)?:wallpaper}
    fun targetPlaylist(target:String):List<String>{val raw=when(target){"home"->p.getString(WALLPAPER_PLAYLIST_HOME,null);"hub"->p.getString(WALLPAPER_PLAYLIST_HUB,null);else->p.getString(WALLPAPER_PLAYLIST_MAIN,null)};return raw?.split("\n")?.filter{it.isNotBlank()}?:emptyList()}
    val wallpaperTarget=when(page){"dashboard"->"home";"hub","leacher"->"hub";else->"main"}
    val activeWallpaper=targetWallpaper(wallpaperTarget)
    val activePlaylist=targetPlaylist(wallpaperTarget)
    val ink=if(configuredFontColor=="white")Color.White else if(configuredFontColor=="green")L1voGreen else if(configuredFontColor=="warm")Color(0xFFFFF4D6) else if(dark)Color(0xFFE9F0E9) else L1voInk
    val homeInk=ink
    val apps=remember(refresh){loadApps(c)}
    val slots=remember(refresh){loadSlots(c)}
    val anim=p.getBoolean(ANIMATIONS,true)
    val columns=p.getInt(APPHUB_COLUMNS,4)
    val hspace=p.getFloat(APPHUB_HSPACE,10f)
    val vspace=p.getFloat(APPHUB_VSPACE,14f)
    val appSize=p.getFloat(APP_SIZE,1f)
    val highlightShape=p.getString(HIGHLIGHT_SHAPE,"round")?:"round"
    val highlightSize=p.getFloat(HIGHLIGHT_SIZE,1f)
    MaterialTheme(
        colorScheme=if(dark) darkColorScheme(primary=L1voGreen,onPrimary=Color.White,secondary=L1voGreen,onSecondary=Color.White,background=L1voDark,surface=Color(0xFF18201B),onBackground=Color(0xFFE9F0E9),onSurface=Color(0xFFE9F0E9),surfaceVariant=Color(0xFF263129),onSurfaceVariant=Color(0xFFC7D2C9))
        else lightColorScheme(primary=L1voDeepGreen,onPrimary=Color.White,secondary=L1voGreen,onSecondary=Color.White,background=L1voPanel,surface=L1voPanel,onBackground=L1voInk,onSurface=L1voInk,surfaceVariant=Color(0xFFE7E9DF),onSurfaceVariant=L1voInk),
        typography=MaterialTheme.typography.copy(
            bodyLarge=MaterialTheme.typography.bodyLarge.copy(fontFamily=font,fontWeight=FontWeight.Medium,fontSize=MaterialTheme.typography.bodyLarge.fontSize*fontScale),
            bodyMedium=MaterialTheme.typography.bodyMedium.copy(fontFamily=font,fontWeight=FontWeight.Medium,fontSize=MaterialTheme.typography.bodyMedium.fontSize*fontScale),
            labelLarge=MaterialTheme.typography.labelLarge.copy(fontFamily=font,fontWeight=FontWeight.Medium),
            labelMedium=MaterialTheme.typography.labelMedium.copy(fontFamily=font,fontWeight=FontWeight.Medium),
            titleMedium=MaterialTheme.typography.titleMedium.copy(fontFamily=font,fontWeight=FontWeight.SemiBold)
        )
    ){
        Surface(Modifier.fillMaxSize(),color=if(useSystemWallpaper)Color.Transparent else Color.Transparent){
            WallpaperBackground(activeWallpaper,dark,activePlaylist,interval,useSystemWallpaper)
            when(page){
                "dashboard"->HomeDashboard(apps,slots,homeInk,{page="cube"},{page="hub"},{launchLeau(c)},anim)
                "hub"->AppHub(apps,ink,{page="cube"},{launchLeau(c)},{wallpaperReturnPage="hub";page="wallpaper"},{launch(c,it.intent)},{page="l1vo"},{page="leacher"},{page="settings"},columns,hspace,vspace,appSize,highlightShape,highlightSize)
                "leacher"->LeacherScreen(apps,ink){page="hub"}
                "l1vo"->L1voHub(ink,{page="hub"},{page="settings"},{launchLeau(c)},{wallpaperReturnPage="l1vo";page="wallpaper"})
                "settings"->L1voSettings(p,dark,{dark=it;p.edit().putBoolean(DARK_THEME,it).apply()},{p.edit().putString(FONT,it).apply();refresh++},{page="cube"},{wallpaperReturnPage="settings";page="wallpaper"},{edit=it},{launch(c,Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))},{launch(c,Intent(Settings.ACTION_HOME_SETTINGS))})
                "wallpaper"->WallpaperStudio(
                    ink,{page=wallpaperReturnPage},
                    {target,u->saveWallpaperTarget(p,target,u);wallpaper=u;refreshWall++;page=wallpaperReturnPage},
                    {items,target->savePlaylistTarget(p,target,items,interval);refreshWall++;},
                    {runCatching{c.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))}},
                    activePlaylist,interval,mapOf("main" to targetPlaylist("main"),"home" to targetPlaylist("home"),"hub" to targetPlaylist("hub"))
                )
                else->HomeCube(slots,apps,ink,{page="dashboard"},{page="hub"},{launchLeau(c)},{wallpaperReturnPage="cube";page="wallpaper"},{edit=it},anim)
            }
            edit?.let{id->SlotPicker(apps,{edit=null}){saveSlot(c,id,it);edit=null;refresh++}}
        }
    }
}

private fun saveWallpaperTarget(p:android.content.SharedPreferences,target:String,u:String){
    val e=p.edit()
    fun save(suffix:String){e.putString(if(suffix=="main")WALLPAPER_MAIN else if(suffix=="home")WALLPAPER_HOME else WALLPAPER_HUB,u).putString(if(suffix=="main")WALLPAPER_PLAYLIST_MAIN else if(suffix=="home")WALLPAPER_PLAYLIST_HOME else WALLPAPER_PLAYLIST_HUB,"")}
    if(target=="all"){save("main");save("home");save("hub")}else save(target)
    e.putString(WALLPAPER,u).putString(WALLPAPER_MODE,"static").apply()
}
private fun savePlaylistTarget(p:android.content.SharedPreferences,target:String,items:List<String>,interval:Long){
    val raw=items.joinToString("\n");val e=p.edit()
    fun save(suffix:String){e.putString(if(suffix=="main")WALLPAPER_PLAYLIST_MAIN else if(suffix=="home")WALLPAPER_PLAYLIST_HOME else WALLPAPER_PLAYLIST_HUB,raw);if(items.isNotEmpty())e.putString(if(suffix=="main")WALLPAPER_MAIN else if(suffix=="home")WALLPAPER_HOME else WALLPAPER_HUB,items.first())}
    if(target=="all"){save("main");save("home");save("hub")}else save(target)
    e.putLong(WALLPAPER_INTERVAL,interval).putString(WALLPAPER_PLAYLIST,raw).putString(WALLPAPER_MODE,"static").apply()
}
