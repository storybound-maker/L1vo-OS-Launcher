package com.l1vo.oslauncher

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeHubSettings(context:Context,ink:Color,onBack:()->Unit,onPickWidget:()->Unit){
    val p=context.getSharedPreferences(PREFS,0)
    val names=listOf("weather" to "Weather", "calendar" to "Calendar", "notes" to "Notes", "maps" to "Maps")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(top=18.dp+WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),bottom=24.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)};Column(Modifier.weight(1f)){Text("Home Hub",color=ink,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.SemiBold);Text("Personal home screen settings",color=L1voGreen)};Icon(Icons.Outlined.Widgets,"Widgets",tint=L1voGreen)}}
        Spacer(Modifier.height(18.dp))
        Text("Widgets",color=ink,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("The clock stays permanent. These widgets and any Android widgets can be rearranged, removed and restored.",color=ink.copy(alpha=.65f),style=MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        Button(onClick=onPickWidget,modifier=Modifier.fillMaxWidth().height(54.dp)){Icon(Icons.Outlined.Add,"Add widget");Spacer(Modifier.width(8.dp));Text("ADD ANDROID WIDGET")}
        Spacer(Modifier.height(14.dp))
        names.forEach{(key,label)->var enabled by remember{mutableStateOf(p.getBoolean("home_system_$key",true))};Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surface.copy(alpha=.96f),modifier=Modifier.fillMaxWidth().padding(vertical=4.dp)){Row(Modifier.padding(15.dp),verticalAlignment=Alignment.CenterVertically){Icon(when(key){"weather"->Icons.Outlined.WbSunny;"calendar"->Icons.Outlined.CalendarMonth;"notes"->Icons.Outlined.EditNote;else->Icons.Outlined.Map},label,tint=L1voGreen);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(label,color=ink,fontWeight=FontWeight.Medium);Text("Home Hub system widget",color=ink.copy(alpha=.6f),style=MaterialTheme.typography.bodySmall)};Switch(checked=enabled,onCheckedChange={enabled=it;p.edit().putBoolean("home_system_$key",it).apply()})}}}
        Spacer(Modifier.height(12.dp))
        Text("Drag & delete",color=ink,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)
        Text("Long-press a widget on Home Hub to move it. While moving, use the red DELETE area at the bottom to remove it.",color=ink.copy(alpha=.65f),style=MaterialTheme.typography.bodySmall)
    }