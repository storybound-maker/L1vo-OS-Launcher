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

@Composable fun L1voLauncherApp(){
 val c=LocalContext.current; val p=remember{c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)}
 var page by remember{mutableStateOf("cube")}; var edit by remember{mutableStateOf<String?>(null)}; var refresh by remember{mutableIntStateOf(0)}
 var wallpaper by remember{mutableStateOf(p.getString(WALLPAPER,null))}; var dark by remember{mutableStateOf(p.getBoolean(DARK_THEME,false))}
 val font=when(p.getString(FONT,"Sans")){"Serif"->FontFamily.Serif;"Mono"->FontFamily.Monospace;else->FontFamily.SansSerif}; val ink=if(dark)Color(0xFFE9F0E9)else L1voInk
 val apps=remember(refresh){loadApps(c)}; val slots=remember(refresh){loadSlots(c)}; val anim=p.getBoolean(ANIMATIONS,true)
 MaterialTheme(typography=MaterialTheme.typography.copy(bodyLarge=MaterialTheme.typography.bodyLarge.copy(fontFamily=font),bodyMedium=MaterialTheme.typography.bodyMedium.copy(fontFamily=font),titleMedium=MaterialTheme.typography.titleMedium.copy(fontFamily=font),headlineMedium=MaterialTheme.typography.headlineMedium.copy(fontFamily=font))){
  Surface(Modifier.fillMaxSize(),color=if(dark)L1voDark else L1voPanel){WallpaperBackground(wallpaper,dark);when(page){
   "dashboard"->HomeDashboard(apps,slots,ink,{page="cube"},{page="hub"},{launchLeau(c)},anim)
   "hub"->AppHub(apps,ink,{page="cube"},{launchLeau(c)},{page="wallpaper"},{launch(c,it.intent)},{page="l1vo"})
   "l1vo"->L1voHub(ink,{page="hub"},{page="settings"},{launchLeau(c)},{page="wallpaper"})
   "settings"->L1voSettings(p,dark,{dark=it;p.edit().putBoolean(DARK_THEME,it).apply()},{p.edit().putString(FONT,it).apply();refresh++},{page="cube"},{page="wallpaper"},{edit=it},{launch(c,Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))},{launch(c,Intent(Settings.ACTION_HOME_SETTINGS))})
   "wallpaper"->WallpaperStudio(ink,{page="cube"}){u->wallpaper=u;p.edit().putString(WALLPAPER,u).apply();page="cube"}
   else->HomeCube(slots,apps,ink,{page="dashboard"},{page="hub"},{launchLeau(c)},{page="wallpaper"},{edit=it},anim)
  };edit?.let{id->SlotPicker(apps,{edit=null}){saveSlot(c,id,it);edit=null;refresh++}}}
 }
}
