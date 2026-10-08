package com.l1vo.oslauncher

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    val names=listOf("weather" to "Weather","calendar" to "Calendar","notes" to "Notes","maps" to "Maps")
    LazyColumn(
        modifier=Modifier.fillMaxSize(),
        contentPadding=PaddingValues(start=20.dp,end=20.dp,top=18.dp+WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),bottom=28.dp),
        verticalArrangement=Arrangement.spacedBy(8.dp)
    ){
        item{
            Row(verticalAlignment=Alignment.CenterVertically){
                IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)}
                Column(Modifier.weight(1f)){Text("Home Hub",color=ink,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.SemiBold);Text("Personal home screen settings",color=L1voGreen)}
                Icon(Icons.Outlined.Widgets,"Widgets",tint=L1voGreen)
            }
        }
        item{Text("Widgets",color=ink,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge)}
        item{Text("Clock is permanent. Widgets can be moved, removed, and restored here.",color=ink.copy(alpha=.65f),style=MaterialTheme.typography.bodySmall)}
        item{
            Button(onClick=onPickWidget,modifier=Modifier.fillMaxWidth().height(54.dp)){
                Icon(Icons.Outlined.Add,"Add widget");Spacer(Modifier.width(8.dp));Text("ADD ANDROID WIDGET")
            }
        }
        items(names,key={it.first}){(key,label)->
            val enabled=p.getBoolean("home_system_$key",true)
            Surface(shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surface.copy(alpha=.96f),modifier=Modifier.fillMaxWidth()){
                Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){
                    Icon(when(key){"weather"->Icons.Outlined.WbSunny;"calendar"->Icons.Outlined.CalendarMonth;"notes"->Icons.Outlined.EditNote;else->Icons.Outlined.Map},label,tint=L1voGreen)
                    Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(label,color=ink,fontWeight=FontWeight.Medium);Text("System widget",color=ink.copy(alpha=.6f),style=MaterialTheme.typography.bodySmall)}
                    Switch(checked=enabled,onCheckedChange={p.edit().putBoolean("home_system_$key",it).apply()})
                }
            }
        }
        item{
            Surface(shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surface.copy(alpha=.92f),modifier=Modifier.fillMaxWidth()){
                Column(Modifier.padding(15.dp)){
                    Text("Drag & delete",color=ink,fontWeight=FontWeight.Bold)
                    Text("Long-press a widget and drag it toward the red × at the bottom. The × grows/highlights when the widget reaches it. Release to delete; releasing anywhere else keeps the widget and hides the ×.",color=ink.copy(alpha=.65f),style=MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
