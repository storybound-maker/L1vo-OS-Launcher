package com.l1vo.oslauncher

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable fun L1voLauncherApp(){
    val c=LocalContext.current
    val activity=c as? MainActivity
    val p=remember{c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)}
    var page by remember{mutableStateOf("cube")}
    var wallpaperReturnPage by remember{mutableStateOf("cube")}
    var edit by remember{mutableStateOf<String?>(null)}
    var editingApp by remember{mutableStateOf<LaunchableApp?>(null)}
    var refresh by remember{mutableIntStateOf(0)}
    var wallpaper by remember{mutableStateOf(p.getString(WALLPAPER,null))}
    var dark by remember{mutableStateOf(p.getBoolean(DARK_THEME,false))}
    var vibee by remember{mutableStateOf(false)}
    var settingsReturnPage by remember{mutableStateOf("cube")}
    var settingsWallpaperTarget by remember{mutableStateOf<String?>(null)}
    val themeName=p.getString(L1VO_THEME,"default")?:"default"
    val themeTone=p.getFloat(L1VO_THEME_TONE,1f)
    applyL1voTheme(themeName,themeTone)
    var intervalAll by remember{mutableLongStateOf(p.getLong(WALLPAPER_INTERVAL_ALL,p.getLong(WALLPAPER_INTERVAL,3600L)))}
    var intervalMain by remember{mutableLongStateOf(p.getLong(WALLPAPER_INTERVAL_MAIN,p.getLong(WALLPAPER_INTERVAL,3600L)))}
    var intervalHome by remember{mutableLongStateOf(p.getLong(WALLPAPER_INTERVAL_HOME,p.getLong(WALLPAPER_INTERVAL,3600L)))}
    var intervalHub by remember{mutableLongStateOf(p.getLong(WALLPAPER_INTERVAL_HUB,p.getLong(WALLPAPER_INTERVAL,3600L)))}
    var refreshWall by remember{mutableIntStateOf(0)}
    val interval=when(when(page){"dashboard"->"home";"hub","leacher"->"hub";else->"main"}){"home"->intervalHome;"hub"->intervalHub;else->intervalMain}
    val useSystemWallpaper=p.getString(WALLPAPER_MODE,"static")=="system"
    val font=when(p.getString(FONT,"Sans")){"Serif"->FontFamily.Serif;"Mono"->FontFamily.Monospace;"Cursive"->FontFamily.Cursive;"Condensed"->FontFamily.SansSerif;else->FontFamily.SansSerif}
    val fontScale=p.getFloat(FONT_SIZE,1f)
    val configuredFontColor=p.getString(FONT_COLOR,"auto")?:"auto"
    fun targetWallpaper(target:String):String?=when(target){"all"->p.getString(WALLPAPER_ALL,null)?.takeIf{it.isNotBlank()}?:wallpaper;"home"->p.getString(WALLPAPER_HOME,null)?.takeIf{it.isNotBlank()}?:p.getString(WALLPAPER_ALL,null)?.takeIf{it.isNotBlank()}?:wallpaper;"hub"->p.getString(WALLPAPER_HUB,null)?.takeIf{it.isNotBlank()}?:p.getString(WALLPAPER_ALL,null)?.takeIf{it.isNotBlank()}?:wallpaper;else->p.getString(WALLPAPER_MAIN,null)?.takeIf{it.isNotBlank()}?:p.getString(WALLPAPER_ALL,null)?.takeIf{it.isNotBlank()}?:wallpaper}
    fun targetPlaylist(target:String):List<String>{val raw=when(target){"all"->p.getString(WALLPAPER_PLAYLIST_ALL,null);"home"->p.getString(WALLPAPER_PLAYLIST_HOME,null)?.takeIf{it.isNotBlank()}?:p.getString(WALLPAPER_PLAYLIST_ALL,null);"hub"->p.getString(WALLPAPER_PLAYLIST_HUB,null)?.takeIf{it.isNotBlank()}?:p.getString(WALLPAPER_PLAYLIST_ALL,null);else->p.getString(WALLPAPER_PLAYLIST_MAIN,null)?.takeIf{it.isNotBlank()}?:p.getString(WALLPAPER_PLAYLIST_ALL,null)};return raw?.split("\n")?.filter{it.isNotBlank()}?.ifEmpty{listOfNotNull(targetWallpaper(target))}?:listOfNotNull(targetWallpaper(target))}
    val wallpaperTarget=settingsWallpaperTarget ?: when(page){"dashboard"->"home";"hub","leacher"->"hub";else->"main"}
    val activeWallpaper=targetWallpaper(wallpaperTarget)
    val activePlaylist=targetPlaylist(wallpaperTarget)
    val ink=if(dark)Color.White else if(configuredFontColor=="white")Color.White else if(configuredFontColor=="green")L1voGreen else if(configuredFontColor=="warm")Color(0xFFFFF4D6) else L1voInk
    val homeInk=if(page=="dashboard")Color.White else ink
    BackHandler {
        when {
            vibee -> vibee=false
            edit!=null -> edit=null
            editingApp!=null -> { editingApp=null; page="hub" }
            page=="dashboard" -> page="cube"
            page=="hub" -> page="cube"
            page=="l1vo" -> page="hub"
            page=="leacher" -> page="hub"
            page=="home_settings" -> page="dashboard"
            page=="app_editor" -> { editingApp=null; page="hub" }
            page=="wallpaper" -> { page=wallpaperReturnPage; settingsWallpaperTarget=null }
            page=="settings" -> { page=settingsReturnPage; settingsWallpaperTarget=null }
            else -> Unit
        }
    }
    val apps=remember(refresh){loadApps(c)}
    val slots=remember(refresh){loadSlots(c)}
    val anim=p.getBoolean(ANIMATIONS,true)
    val columns=p.getInt(APPHUB_COLUMNS,4)
    val appHubNavigation=p.getString(APPHUB_NAV,"scroll")?:"scroll"
    val hspace=p.getFloat(APPHUB_HSPACE,10f)
    val vspace=p.getFloat(APPHUB_VSPACE,14f)
    val appSize=p.getFloat(APP_SIZE,1f)
    val highlightShape=p.getString(HIGHLIGHT_SHAPE,"round")?:"round"
    val highlightSize=p.getFloat(HIGHLIGHT_SIZE,1f)
    MaterialTheme(
        colorScheme=if(dark) darkColorScheme(primary=L1voGreen,onPrimary=Color.White,secondary=L1voGreen,onSecondary=Color.White,background=L1voDark,surface=Color(0xFF18201B),onBackground=ink,onSurface=ink,surfaceVariant=Color(0xFF263129),onSurfaceVariant=ink)
        else lightColorScheme(primary=L1voDeepGreen,onPrimary=Color.White,secondary=L1voGreen,onSecondary=Color.White,background=L1voPanel,surface=L1voPanel,onBackground=ink,onSurface=ink,surfaceVariant=Color(0xFFE7E9DF),onSurfaceVariant=ink),
        typography=MaterialTheme.typography.copy(
            bodyLarge=MaterialTheme.typography.bodyLarge.copy(fontFamily=font,fontWeight=FontWeight.Medium,fontSize=MaterialTheme.typography.bodyLarge.fontSize*fontScale),
            bodyMedium=MaterialTheme.typography.bodyMedium.copy(fontFamily=font,fontWeight=FontWeight.Medium,fontSize=MaterialTheme.typography.bodyMedium.fontSize*fontScale),
            labelLarge=MaterialTheme.typography.labelLarge.copy(fontFamily=font,fontWeight=FontWeight.Medium),
            labelMedium=MaterialTheme.typography.labelMedium.copy(fontFamily=font,fontWeight=FontWeight.Medium),
            titleMedium=MaterialTheme.typography.titleMedium.copy(fontFamily=font,fontWeight=FontWeight.SemiBold)
        )
    ){
        Surface(Modifier.fillMaxSize(),color=if(useSystemWallpaper)Color.Transparent else Color.Transparent){
            WallpaperBackground(activeWallpaper,dark || page=="dashboard" || page=="home_settings",activePlaylist,interval,useSystemWallpaper)
            if(page in setOf("settings","home_settings","app_editor","wallpaper","l1vo")) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=if(dark) .32f else .20f)))
            when(page){
                "dashboard"->HomeDashboard(apps,slots,homeInk,{page="cube"},{page="hub"},{vibee=true},{page="home_settings"},anim)
                "hub"->AppHub(apps,ink,{page="cube"},{vibee=true},{wallpaperReturnPage="hub";page="wallpaper"},{launch(c,it.intent);page="hub"},{page="l1vo"},{page="leacher"},{settingsReturnPage="hub";settingsWallpaperTarget="hub";page="settings"},{editingApp=it;page="app_editor"},columns,appHubNavigation,hspace,vspace,appSize,highlightShape,highlightSize)
                "leacher"->LeacherScreen(apps,ink){page="hub"}
                "home_settings"->HomeHubSettings(c,ink,{page="dashboard"},{activity?.pickHomeWidget() ?: Unit})
                "app_editor"->editingApp?.let{AppEditorScreen(it,ink,{editingApp=null;page="hub"}){refresh++;editingApp=null}}
                "l1vo"->L1voHub(ink,{page="hub"},{page="settings"},{vibee=true},{wallpaperReturnPage="l1vo";page="wallpaper"})
                "settings"->L1voSettings(p,dark,{dark=it;p.edit().putBoolean(DARK_THEME,it).apply()},{p.edit().putString(FONT,it).apply();refresh++},{page="cube"},{wallpaperReturnPage="settings";page="wallpaper"},{edit=it},{launch(c,Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))},{launch(c,Intent(Settings.ACTION_HOME_SETTINGS))},{refresh++})
                "wallpaper"->WallpaperStudio(
                    ink,{page=wallpaperReturnPage},
                    {target,u->saveWallpaperTarget(p,target,u);wallpaper=u;refreshWall++;page=wallpaperReturnPage},
                    {items,target,newInterval->savePlaylistTarget(p,target,items,newInterval);when(target){"home"->intervalHome=newInterval;"hub"->intervalHub=newInterval;"main"->intervalMain=newInterval;"all"->{intervalAll=newInterval}};refreshWall++;},
                    {p.edit().putString(WALLPAPER_MODE,"system").apply();runCatching{c.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))}},
                    activePlaylist,interval,mapOf("all" to targetPlaylist("all"),"main" to targetPlaylist("main"),"home" to targetPlaylist("home"),"hub" to targetPlaylist("hub")),mapOf("all" to intervalAll,"main" to intervalMain,"home" to intervalHome,"hub" to intervalHub)
                )
                else->HomeCube(slots,apps,ink,{page="dashboard"},{page="hub"},{vibee=true},{wallpaperReturnPage="cube";page="wallpaper"},{edit=it},anim)
            }
            edit?.let{id->SlotPicker(apps,{edit=null}){saveSlot(c,id,it);edit=null;refresh++}}
            if(vibee) VibeeOverlay(apps,ink,{vibee=false})
        }
    }
}

private fun saveWallpaperTarget(p:android.content.SharedPreferences,target:String,u:String){
    val e=p.edit()
    if(target=="all") e.putString(WALLPAPER_ALL,u).putString(WALLPAPER_PLAYLIST_ALL,"") else if(target=="main") e.putString(WALLPAPER_MAIN,u).putString(WALLPAPER_PLAYLIST_MAIN,"") else if(target=="home") e.putString(WALLPAPER_HOME,u).putString(WALLPAPER_PLAYLIST_HOME,"") else e.putString(WALLPAPER_HUB,u).putString(WALLPAPER_PLAYLIST_HUB,"")
    e.putString(WALLPAPER,u).putString(WALLPAPER_MODE,"static").apply()
}
private fun savePlaylistTarget(p:android.content.SharedPreferences,target:String,items:List<String>,interval:Long){
    val raw=items.joinToString("\n");val e=p.edit()
    fun save(suffix:String){val playlistKey=if(suffix=="all")WALLPAPER_PLAYLIST_ALL else if(suffix=="main")WALLPAPER_PLAYLIST_MAIN else if(suffix=="home")WALLPAPER_PLAYLIST_HOME else WALLPAPER_PLAYLIST_HUB;val intervalKey=if(suffix=="all")WALLPAPER_INTERVAL_ALL else if(suffix=="main")WALLPAPER_INTERVAL_MAIN else if(suffix=="home")WALLPAPER_INTERVAL_HOME else WALLPAPER_INTERVAL_HUB;val wallpaperKey=if(suffix=="all")WALLPAPER_ALL else if(suffix=="main")WALLPAPER_MAIN else if(suffix=="home")WALLPAPER_HOME else WALLPAPER_HUB;e.putString(playlistKey,raw).putLong(intervalKey,interval);if(items.isNotEmpty())e.putString(wallpaperKey,items.first())}
    save(target)
    e.putLong(WALLPAPER_INTERVAL,interval).putString(WALLPAPER_PLAYLIST,raw).putString(WALLPAPER_MODE,"static").apply()
}
