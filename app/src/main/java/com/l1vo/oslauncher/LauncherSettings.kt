package com.l1vo.oslauncher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun L1voSettings(
    p:android.content.SharedPreferences,dark:Boolean,onTheme:(Boolean)->Unit,onFont:(String)->Unit,
    onBack:()->Unit,onWallpaper:()->Unit,onCube:(String)->Unit,onAccessibility:()->Unit,onDefaultLauncher:()->Unit
){
    val ink=if(dark) Color(0xFFE9F0E9) else L1voInk
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
        Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack,modifier=Modifier.offset(y=8.dp).size(56.dp)){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)};Column(Modifier.weight(1f)){Text("L1vo",color=ink,style=MaterialTheme.typography.headlineMedium);Text("Settings",color=L1voGreen)};Icon(Icons.Outlined.Settings,"Settings",tint=L1voGreen)}
        Spacer(Modifier.height(18.dp))
        Group("Account",Icons.Outlined.Person,ink){Item("L1vo account",p.getString(ACCOUNT_NAME,"Guest")?:"Guest",Icons.Outlined.Person,ink){};Toggle("Notifications",if(notif)"Enabled"else"Disabled",Icons.Outlined.Notifications,notif,{notif=it;p.edit().putBoolean(NOTIFICATIONS,it).apply()},ink)}
        Group("Appearance",Icons.Outlined.Palette,ink){
            Toggle("Dark theme",if(dark)"On"else"Off",Icons.Outlined.Palette,dark,onTheme,ink)
            Item("Wallpaper","Open Wallpaper Studio",Icons.Outlined.Wallpaper,ink,onWallpaper)
            Choice("Font family",font,listOf("Sans","Serif","Mono","Cursive","Condensed"),ink){font=it;onFont(it)}
            SliderItem("Text size",fontSize,.80f,1.35f,ink){fontSize=it;p.edit().putFloat(FONT_SIZE,it).apply()}
            Choice("Font color",fontColor,listOf("auto","green","white","warm"),ink){fontColor=it;p.edit().putString(FONT_COLOR,it).apply()}
        }
        Group("App presentation",Icons.Outlined.Apps,ink){
            Choice("Highlight shape",highlight,listOf("round","square","diamond","none"),ink){highlight=it;p.edit().putString(HIGHLIGHT_SHAPE,it).apply()}
            SliderItem("Highlight size",highlightSize,.75f,1.35f,ink){highlightSize=it;p.edit().putFloat(HIGHLIGHT_SIZE,it).apply()}
            SliderItem("App size",appSize,.75f,1.40f,ink){appSize=it;p.edit().putFloat(APP_SIZE,it).apply()}
            Choice("App Hub columns",columns.toString(),listOf("3","4","5","6"),ink){columns=it.toInt();p.edit().putInt(APPHUB_COLUMNS,columns).apply()}
            SliderItem("App Hub horizontal spacing",hspace,4f,24f,ink){hspace=it;p.edit().putFloat(APPHUB_HSPACE,it).apply()}
            SliderItem("App Hub vertical spacing",vspace,6f,28f,ink){vspace=it;p.edit().putFloat(APPHUB_VSPACE,it).apply()}
            Text("These controls change the app grid without changing the L1vo Home/Cube layout.",color=ink.copy(alpha=.62f),style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(horizontal=14.dp,vertical=8.dp))
        }
        Group("Home & Cube",Icons.Outlined.Tune,ink){Item("Settings slot","Customize upper-right cube section",Icons.Outlined.Settings,ink){onCube("settings")};Item("Gallery slot","Customize lower-left cube section",Icons.Outlined.Collections,ink){onCube("gallery")};Item("Calls slot","Customize lower-right cube section",Icons.Outlined.Call,ink){onCube("calls")}}
        Group("Accessibility",Icons.Outlined.Visibility,ink){Toggle("Animations",if(anim)"Enabled"else"Reduced",Icons.Outlined.Visibility,anim,{anim=it;p.edit().putBoolean(ANIMATIONS,it).apply()},ink);Toggle("Pill app",if(pill)"Enabled"else"Disabled",Icons.Outlined.Apps,pill,{pill=it;p.edit().putBoolean(PILL_APP,it).apply()},ink);Item("Android accessibility","Open system controls",Icons.Outlined.Visibility,ink,onAccessibility)}
        Group("Launcher",Icons.Outlined.Home,ink){Item("Default launcher","Choose L1vo as device home",Icons.Outlined.Home,ink,onDefaultLauncher)}
        Group("About",Icons.Outlined.Info,ink){Item("L1vo OS Launcher","Version 0.1.0",Icons.Outlined.Info,ink){}}
    }
}
@Composable private fun Group(t:String,i:ImageVector,ink:Color,content:@Composable ColumnScope.()->Unit){Column{Row(verticalAlignment=Alignment.CenterVertically){Icon(i,t,tint=L1voGreen,modifier=Modifier.size(20.dp));Spacer(Modifier.width(8.dp));Text(t.uppercase(Locale.getDefault()),color=L1voGreen,fontWeight=FontWeight.Bold)};Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface.copy(alpha=.95f)),shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth().padding(vertical=8.dp)){Column(content=content)}}}
@Composable private fun Item(t:String,s:String,i:ImageVector,ink:Color,onClick:()->Unit){Surface(onClick=onClick,color=Color.Transparent,modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(i,t,tint=L1voGreen);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(t,color=ink);Text(s,color=ink.copy(alpha=.62f),style=MaterialTheme.typography.bodySmall)}}}}
@Composable private fun Toggle(t:String,s:String,i:ImageVector,checked:Boolean,onChange:(Boolean)->Unit,ink:Color){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(i,t,tint=L1voGreen);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(t,color=ink);Text(s,color=ink.copy(alpha=.62f),style=MaterialTheme.typography.bodySmall)};Switch(checked=checked,onCheckedChange=onChange)}}
@Composable private fun Choice(t:String,value:String,options:List<String>,ink:Color,onChange:(String)->Unit){Column(Modifier.fillMaxWidth().padding(14.dp)){Text(t,color=ink,fontWeight=FontWeight.Medium);Spacer(Modifier.height(7.dp));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){options.forEach{v->FilterChip(selected=value==v,onClick={onChange(v)},label={Text(v,style=MaterialTheme.typography.labelSmall)},modifier=Modifier.weight(1f))}}}}
@Composable private fun SliderItem(t:String,value:Float,min:Float,max:Float,ink:Color,onChange:(Float)->Unit){Column(Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=8.dp)){Row{Text(t,color=ink,modifier=Modifier.weight(1f));Text(String.format(Locale.US,"%.2f",value),color=ink.copy(alpha=.65f),style=MaterialTheme.typography.bodySmall)};Slider(value=value,onValueChange=onChange,valueRange=min..max);}}
