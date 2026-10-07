package com.l1vo.oslauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun L1voSettings(
    p:android.content.SharedPreferences,dark:Boolean,onTheme:(Boolean)->Unit,onFont:(String)->Unit,
    onBack:()->Unit,onWallpaper:()->Unit,onCube:(String)->Unit,onAccessibility:()->Unit,onDefaultLauncher:()->Unit
){
    val ink=if(dark)Color.White else MaterialTheme.colorScheme.onSurface
    var section by rememberSaveable{mutableStateOf<String?>(null)}
    var anim by remember{mutableStateOf(p.getBoolean(ANIMATIONS,true))}
    var notif by remember{mutableStateOf(p.getBoolean(NOTIFICATIONS,true))}
    var pill by remember{mutableStateOf(p.getBoolean(PILL_APP,true))}
    var font by remember{mutableStateOf(p.getString(FONT,"Sans")?:"Sans")}
    var fontSize by remember{mutableFloatStateOf(p.getFloat(FONT_SIZE,1f))}
    var fontColor by remember{mutableStateOf(p.getString(FONT_COLOR,"auto")?:"auto")}
    var highlight by remember{mutableStateOf(p.getString(HIGHLIGHT_SHAPE,"round")?:"round")}
    var highlightSize by remember{mutableFloatStateOf(p.getFloat(HIGHLIGHT_SIZE,1f))}
    var appSize by remember{mutableFloatStateOf(p.getFloat(APP_SIZE,1f))}
    var columns by remember{mutableIntStateOf(p.getInt(APPHUB_COLUMNS,4))}
    var hspace by remember{mutableFloatStateOf(p.getFloat(APPHUB_HSPACE,10f))}
    var vspace by remember{mutableFloatStateOf(p.getFloat(APPHUB_VSPACE,14f))}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(top=20.dp+WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),bottom=24.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){
            IconButton(onClick={if(section==null)onBack()else section=null},modifier=Modifier.size(56.dp)){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)}
            Column(Modifier.weight(1f)){Text(if(section==null)"L1vo Settings" else section!!,color=ink,style=MaterialTheme.typography.headlineMedium);Text("Launcher control center",color=L1voGreen)}
            Icon(Icons.Outlined.Settings,"Settings",tint=L1voGreen)
        }
        Spacer(Modifier.height(16.dp))
        if(section==null){
            listOf(
                Triple("Accounts","Account and notification options",Icons.Outlined.Person),
                Triple("Appearance","Theme, fonts, colors and wallpaper",Icons.Outlined.Palette),
                Triple("App presentation","App Hub layout, sizing, highlights and swipe pages",Icons.Outlined.Apps),
                Triple("Home & Cube","Home Hub and cube controls",Icons.Outlined.Tune),
                Triple("Accessibility","Animation, interaction and Android accessibility",Icons.Outlined.Visibility),
                Triple("Launcher","Default launcher and launcher behavior",Icons.Outlined.Home),
                Triple("About","L1vo OS Launcher information",Icons.Outlined.Info)
            ).forEach{(t,s,i)->MainSettingRow(t,s,i,ink){section=t}}
        }else when(section){
            "Accounts"->AccountsSettings(p,ink,notif,{notif=it;p.edit().putBoolean(NOTIFICATIONS,it).apply()})
            "Appearance"->AppearanceSettings(p,dark,ink,font,fontSize,fontColor,{font=it;onFont(it)},{fontSize=it;p.edit().putFloat(FONT_SIZE,it).apply()},{fontColor=it;p.edit().putString(FONT_COLOR,it).apply()},{onTheme(!dark)},onWallpaper)
            "App presentation"->AppPresentationSettings(p,ink,columns,hspace,vspace,appSize,highlight,highlightSize,{columns=it;p.edit().putInt(APPHUB_COLUMNS,it).apply()},{hspace=it;p.edit().putFloat(APPHUB_HSPACE,it).apply()},{vspace=it;p.edit().putFloat(APPHUB_VSPACE,it).apply()},{appSize=it;p.edit().putFloat(APP_SIZE,it).apply()},{highlight=it;p.edit().putString(HIGHLIGHT_SHAPE,it).apply()},{highlightSize=it;p.edit().putFloat(HIGHLIGHT_SIZE,it).apply()})
            "Home & Cube"->HomeCubeSettings(ink,onCube,onWallpaper)
            "Accessibility"->AccessibilitySettings(p,ink,anim,pill,{anim=it;p.edit().putBoolean(ANIMATIONS,it).apply()},{pill=it;p.edit().putBoolean(PILL_APP,it).apply()},onAccessibility)
            "Launcher"->LauncherSettingsPage(ink,onDefaultLauncher)
            "About"->AboutSettings(ink)
        }
    }
}
@Composable private fun MainSettingRow(t:String,s:String,i:ImageVector,ink:Color,onClick:()->Unit){Surface(onClick=onClick,color=MaterialTheme.colorScheme.surface.copy(alpha=.96f),shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth().padding(vertical=6.dp)){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically){Icon(i,t,tint=L1voGreen,modifier=Modifier.size(28.dp));Spacer(Modifier.width(15.dp));Column(Modifier.weight(1f)){Text(t,color=ink,fontWeight=FontWeight.SemiBold,style=MaterialTheme.typography.titleMedium);Text(s,color=ink.copy(alpha=.62f),style=MaterialTheme.typography.bodySmall)};Icon(Icons.Outlined.ChevronRight,"Open",tint=ink.copy(alpha=.55f))}}}
@Composable private fun AccountsSettings(p:android.content.SharedPreferences,ink:Color,notif:Boolean,onNotif:(Boolean)->Unit){Text("Account",color=ink,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Item("L1vo account",p.getString(ACCOUNT_NAME,"Guest")?:"Guest",Icons.Outlined.Person,ink){};Toggle("Notifications",if(notif)"Enabled"else"Disabled",Icons.Outlined.Notifications,notif,onNotif,ink)}
@Composable private fun AppearanceSettings(p:android.content.SharedPreferences,dark:Boolean,ink:Color,font:String,size:Float,color:String,onFont:(String)->Unit,onSize:(Float)->Unit,onColor:(String)->Unit,onTheme:()->Unit,onWallpaper:()->Unit){Toggle("Dark mode",if(dark)"On — text is white"else"Off",Icons.Outlined.DarkMode,dark,{onTheme()},ink);Item("Wallpaper","Open Wallpaper Studio",Icons.Outlined.Wallpaper,ink,onWallpaper);Choice("Font family",font,listOf("Sans","Serif","Mono","Cursive","Condensed"),ink,onFont);SliderItem("Text size",size,.80f,1.35f,ink,onSize);Choice("Universal font color",color,listOf("auto","green","white","warm"),ink,onColor)}
@Composable private fun AppPresentationSettings(p:android.content.SharedPreferences,ink:Color,columns:Int,hspace:Float,vspace:Float,appSize:Float,highlight:String,highlightSize:Float,onColumns:(Int)->Unit,onH:(Float)->Unit,onV:(Float)->Unit,onSize:(Float)->Unit,onHighlight:(String)->Unit,onHighlightSize:(Float)->Unit){
    Text("App Hub layout preview",color=ink,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)
    Row(Modifier.fillMaxWidth().padding(vertical=10.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(3,4,5,6).forEach{n->Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){Surface(onClick={onColumns(n)},shape=RoundedCornerShape(12.dp),color=if(columns==n)L1voGreen.copy(alpha=.22f) else MaterialTheme.colorScheme.surfaceVariant,modifier=Modifier.fillMaxWidth().height(72.dp)){Row(Modifier.padding(8.dp),horizontalArrangement=Arrangement.spacedBy(3.dp)){repeat(n){Box(Modifier.weight(1f).height(56.dp).background(L1voGreen.copy(alpha=.55f),RoundedCornerShape(3.dp)))}}};Text(n.toString()+" cols",color=ink,style=MaterialTheme.typography.labelSmall)}}}
    Spacer(Modifier.height(8.dp));SliderItem("App size",appSize,.75f,1.40f,ink,onSize);SliderItem("Highlight size",highlightSize,.75f,1.35f,ink,onHighlightSize);SliderItem("Horizontal spacing",hspace,4f,24f,ink,onH);SliderItem("Vertical spacing",vspace,6f,28f,ink,onV);Choice("Highlight shape",highlight,listOf("round","square","diamond","none"),ink,onHighlight)
    Text("Swipe left/right in App Hub to move between app pages.",color=ink.copy(alpha=.65f),style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(14.dp))
}
@Composable private fun HomeCubeSettings(ink:Color,onCube:(String)->Unit,onWallpaper:()->Unit){Item("Wallpaper","Home/Cube wallpaper",Icons.Outlined.Wallpaper,ink,onWallpaper);Item("Settings cube slot","Customize the settings face",Icons.Outlined.Settings,ink){onCube("settings")};Item("Gallery cube slot","Customize the gallery face",Icons.Outlined.Collections,ink){onCube("gallery")};Item("Calls cube slot","Customize the calls face",Icons.Outlined.Call,ink){onCube("calls")};Item("Home Hub widgets","Real Android widgets are managed from Home Hub",Icons.Outlined.Widgets,ink){}}
@Composable private fun AccessibilitySettings(p:android.content.SharedPreferences,ink:Color,anim:Boolean,pill:Boolean,onAnim:(Boolean)->Unit,onPill:(Boolean)->Unit,onAccessibility:()->Unit){Toggle("Animations",if(anim)"Enabled"else"Reduced",Icons.Outlined.Animation,anim,onAnim,ink);Toggle("Pill app",if(pill)"Enabled"else"Disabled",Icons.Outlined.Apps,pill,onPill,ink);Item("Android accessibility","Open system controls",Icons.Outlined.Visibility,ink,onAccessibility)}
@Composable private fun LauncherSettingsPage(ink:Color,onDefaultLauncher:()->Unit){Item("Default launcher","Choose L1vo as device home",Icons.Outlined.Home,ink,onDefaultLauncher)}
@Composable private fun AboutSettings(ink:Color){Item("L1vo OS Launcher","Version 0.1.0",Icons.Outlined.Info,ink){};Item("Design","L1vo launcher control system",Icons.Outlined.Palette,ink){}}
@Composable private fun Item(t:String,s:String,i:ImageVector,ink:Color,onClick:()->Unit){Surface(onClick=onClick,color=MaterialTheme.colorScheme.surface.copy(alpha=.96f),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().padding(vertical=5.dp)){Row(Modifier.padding(15.dp),verticalAlignment=Alignment.CenterVertically){Icon(i,t,tint=L1voGreen);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(t,color=ink);Text(s,color=ink.copy(alpha=.62f),style=MaterialTheme.typography.bodySmall)};Icon(Icons.Outlined.ChevronRight,"Open",tint=ink.copy(alpha=.45f))}}}
@Composable private fun Toggle(t:String,s:String,i:ImageVector,checked:Boolean,onChange:(Boolean)->Unit,ink:Color){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(i,t,tint=L1voGreen);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(t,color=ink);Text(s,color=ink.copy(alpha=.62f),style=MaterialTheme.typography.bodySmall)};Switch(checked=checked,onCheckedChange=onChange)}}
@Composable private fun Choice(t:String,value:String,options:List<String>,ink:Color,onChange:(String)->Unit){Column(Modifier.fillMaxWidth().padding(12.dp)){Text(t,color=ink,fontWeight=FontWeight.Medium);Spacer(Modifier.height(7.dp));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){options.forEach{v->FilterChip(selected=value==v,onClick={onChange(v)},label={Text(v,style=MaterialTheme.typography.labelSmall)},modifier=Modifier.weight(1f))}}}}
@Composable private fun SliderItem(t:String,value:Float,min:Float,max:Float,ink:Color,onChange:(Float)->Unit){Column(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=8.dp)){Row{Text(t,color=ink,modifier=Modifier.weight(1f));Text(String.format(Locale.US,"%.2f",value),color=ink.copy(alpha=.65f),style=MaterialTheme.typography.bodySmall)};Slider(value=value,onValueChange=onChange,valueRange=min..max)}}
