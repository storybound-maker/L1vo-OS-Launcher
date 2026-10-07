package com.l1vo.oslauncher

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

@Composable fun L1voLauncherApp(){
 val c=LocalContext.current; val p=remember{c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)}
 var page by remember{mutableStateOf("cube")}; var wallpaperReturnPage by remember{mutableStateOf("cube")}; var edit by remember{mutableStateOf<String?>(null)}; var refresh by remember{mutableIntStateOf(0)}
 var wallpaper by remember{mutableStateOf(p.getString(WALLPAPER,null))}; var dark by remember{mutableStateOf(p.getBoolean(DARK_THEME,false))}; var playlist by remember{mutableStateOf(p.getString(WALLPAPER_PLAYLIST,null)?.split("\n")?.filter{it.isNotBlank()}?:emptyList())}; val intervalSeconds=p.getLong(WALLPAPER_INTERVAL,3600L); val useSystemWallpaper=p.getString(WALLPAPER_MODE,"static")=="system"
 val font=when(p.getString(FONT,"Sans")){"Serif"->FontFamily.Serif;"Mono"->FontFamily.Monospace;else->FontFamily.SansSerif}; val ink=rememberAdaptiveInk(wallpaper,dark); val homeInk=rememberHomeAdaptiveInk(wallpaper,dark)
 val apps=remember(refresh){loadApps(c)}; val slots=remember(refresh){loadSlots(c)}; val anim=p.getBoolean(ANIMATIONS,true)
 MaterialTheme(colorScheme=if(dark) darkColorScheme(primary=L1voGreen,onPrimary=Color.White,secondary=L1voGreen,onSecondary=Color.White,background=L1voDark,surface=Color(0xFF18201B),onBackground=Color(0xFFE9F0E9),onSurface=Color(0xFFE9F0E9),surfaceVariant=Color(0xFF263129),onSurfaceVariant=Color(0xFFC7D2C9)) else lightColorScheme(primary=L1voDeepGreen,onPrimary=Color.White,secondary=L1voGreen,onSecondary=Color.White,background=L1voPanel,surface=L1voPanel,onBackground=L1voInk,onSurface=L1voInk),typography=MaterialTheme.typography.copy(bodyLarge=MaterialTheme.typography.bodyLarge.copy(fontFamily=font,fontWeight=FontWeight.Medium),bodyMedium=MaterialTheme.typography.bodyMedium.copy(fontFamily=font,fontWeight=FontWeight.Medium),labelLarge=MaterialTheme.typography.labelLarge.copy(fontFamily=font,fontWeight=FontWeight.Medium),labelMedium=MaterialTheme.typography.labelMedium.copy(fontFamily=font,fontWeight=FontWeight.SemiBold),labelSmall=MaterialTheme.typography.labelSmall.copy(fontFamily=font,fontWeight=FontWeight.SemiBold),titleMedium=MaterialTheme.typography.titleMedium.copy(fontFamily=font,fontWeight=FontWeight.SemiBold),headlineMedium=MaterialTheme.typography.headlineMedium.copy(fontFamily=font,fontWeight=FontWeight.SemiBold))){
  Surface(Modifier.fillMaxSize(),color=if(useSystemWallpaper) Color.Transparent else if(dark)L1voDark else L1voPanel){WallpaperBackground(wallpaper,dark,playlist,intervalSeconds,useSystemWallpaper);when(page){
   "dashboard"->HomeDashboard(apps,slots,homeInk,{page="cube"},{page="hub"},{launchLeau(c)},anim)
   "hub"->AppHub(apps,ink,{page="cube"},{launchLeau(c)},{wallpaperReturnPage="hub";page="wallpaper"},{launch(c,it.intent)},{page="l1vo"},{page="leacher"},{page="settings"})
   "leacher"->LeacherScreen(apps,ink){page="hub"}
   "l1vo"->L1voHub(ink,{page="hub"},{page="settings"},{launchLeau(c)},{wallpaperReturnPage="l1vo";page="wallpaper"})
   "settings"->L1voSettings(p,dark,{dark=it;p.edit().putBoolean(DARK_THEME,it).apply()},{p.edit().putString(FONT,it).apply();refresh++},{page="cube"},{wallpaperReturnPage="settings";page="wallpaper"},{edit=it},{launch(c,Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))},{launch(c,Intent(Settings.ACTION_HOME_SETTINGS))})
   "wallpaper"->WallpaperStudio(ink,{page=wallpaperReturnPage},{u->wallpaper=u;playlist=emptyList();p.edit().putString(WALLPAPER,u).putString(WALLPAPER_PLAYLIST,"").putString(WALLPAPER_MODE,"static").apply();page=wallpaperReturnPage},{items->playlist=items;p.edit().putString(WALLPAPER_PLAYLIST,items.joinToString("\n")).putString(WALLPAPER_MODE,"static").apply();page=wallpaperReturnPage},{p.edit().putString(WALLPAPER_MODE,"system").apply();runCatching{c.startActivity(Intent(android.app.WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))}},{playlist,intervalSeconds})
   else->HomeCube(slots,apps,ink,{page="dashboard"},{page="hub"},{launchLeau(c)},{wallpaperReturnPage="cube";page="wallpaper"},{edit=it},anim)
  };edit?.let{id->SlotPicker(apps,{edit=null}){saveSlot(c,id,it);edit=null;refresh++}}}
 }
}
