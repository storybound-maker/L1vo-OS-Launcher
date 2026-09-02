package com.l1vo.oslauncher

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable fun AppHub(apps:List<LaunchableApp>,ink:Color,onBack:()->Unit,onLeau:()->Unit,onWallpaper:()->Unit,onOpen:(LaunchableApp)->Unit,onL1vo:()->Unit){
    val context=LocalContext.current
    Column(Modifier.fillMaxSize().padding(horizontal=18.dp,vertical=16.dp)){
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)};Column(Modifier.weight(1f)){Text("APP HUB",color=ink,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Light);Text("All apps",color=L1voGreen,style=MaterialTheme.typography.bodySmall)};IconButton(onClick=onWallpaper){Icon(Icons.Outlined.Wallpaper,"Wallpaper",tint=L1voGreen)}}
        Spacer(Modifier.height(14.dp))
        LazyVerticalGrid(columns=GridCells.Fixed(4),modifier=Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=10.dp)){
            item(span={GridItemSpan(maxLineSpan)}){HubSection("L1vo","L1vo services",ink)}
            item(span={GridItemSpan(maxLineSpan)}){ServicePanel(onL1vo,onLeau,ink)}
            item(span={GridItemSpan(maxLineSpan)}){HubSection("System","Core device controls",ink)}
            item{SystemCard("Settings",Icons.Outlined.Settings){launch(context,Intent(Settings.ACTION_SETTINGS))}}
            item{SystemCard("Phone",Icons.Outlined.Call){launch(context,Intent(Intent.ACTION_DIAL))}}
            item{SystemCard("Gallery",Icons.Outlined.Collections){launch(context,Intent(Intent.ACTION_VIEW).apply{type="image/*"})}}
            item{SystemCard("Wallpaper",Icons.Outlined.Wallpaper,onWallpaper)}
            item(span={GridItemSpan(maxLineSpan)}){HubSection("Apps","Installed apps",ink)}
            items(apps,key={it.packageName}){AppIcon(it,onOpen,ink)}
        }
        Spacer(Modifier.height(8.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End,verticalAlignment=Alignment.CenterVertically){Surface(onClick=onLeau,shape=RoundedCornerShape(24.dp),color=L1voInk,shadowElevation=6.dp){Row(Modifier.padding(horizontal=16.dp,vertical=11.dp),verticalAlignment=Alignment.CenterVertically){Text("Leau",color=Color.White,fontWeight=FontWeight.Medium);Spacer(Modifier.width(8.dp));Text("⌒  ⌒",color=Color.White,fontWeight=FontWeight.Bold)}}}
    }
}

@Composable private fun HubSection(t:String,s:String,ink:Color){Column(Modifier.fillMaxWidth().padding(top=2.dp,bottom=2.dp)){Text(t.uppercase(),color=L1voGreen,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelLarge);Text(s,color=ink.copy(alpha=.58f),style=MaterialTheme.typography.bodySmall)}}
@Composable private fun ServicePanel(onL1vo:()->Unit,onLeau:()->Unit,ink:Color){Surface(onClick=onL1vo,color=L1voPanel.copy(alpha=.96f),shape=RoundedCornerShape(24.dp),shadowElevation=4.dp,modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Surface(shape=RoundedCornerShape(18.dp),color=L1voGreen.copy(alpha=.12f),modifier=Modifier.size(52.dp)){Box(contentAlignment=Alignment.Center){Icon(Icons.Outlined.Eco,"L1vo",tint=L1voDeepGreen)}};Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text("L1vo apps & features",color=ink,fontWeight=FontWeight.Medium);Text("Settings, Leau, Gallery and Phone",color=ink.copy(alpha=.58f),style=MaterialTheme.typography.bodySmall)};IconButton(onClick=onLeau){Icon(Icons.Outlined.ChevronRight,"Open",tint=L1voGreen)}}}}
@Composable private fun SystemCard(t:String,i:ImageVector,onClick:()->Unit){Surface(onClick=onClick,shape=RoundedCornerShape(18.dp),color=Color.White.copy(alpha=.84f),shadowElevation=3.dp,modifier=Modifier.fillMaxWidth().height(92.dp)){Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(i,t,tint=L1voDeepGreen,modifier=Modifier.size(26.dp));Spacer(Modifier.height(7.dp));Text(t,style=MaterialTheme.typography.labelSmall)}}}
@Composable private fun AppIcon(a:LaunchableApp,onOpen:(LaunchableApp)->Unit,ink:Color){Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.fillMaxWidth()){Surface(onClick={onOpen(a)},shape=RoundedCornerShape(18.dp),color=Color.White.copy(alpha=.88f),shadowElevation=2.dp,modifier=Modifier.size(62.dp)){Image(a.icon.asImageBitmap(),a.label,Modifier.padding(9.dp),contentScale=ContentScale.Fit)};Spacer(Modifier.height(4.dp));Text(a.label,color=ink,style=MaterialTheme.typography.labelSmall,maxLines=1)}}
@Composable fun L1voHub(ink:Color,onBack:()->Unit,onSettings:()->Unit,onLeau:()->Unit,onWallpaper:()->Unit){val context=LocalContext.current;Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp,vertical=18.dp)){Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)};Column(Modifier.weight(1f)){Text("L1vo",style=MaterialTheme.typography.headlineMedium,color=ink);Text("L1vo apps & features",color=L1voGreen)};IconButton(onClick=onWallpaper){Icon(Icons.Outlined.Wallpaper,"Wallpaper",tint=L1voGreen)}};Spacer(Modifier.height(18.dp));Feature("L1vo Settings","Native launcher settings",Icons.Outlined.Settings,ink,onSettings);Feature("Leau","L1vo assistant",Icons.Outlined.Eco,ink,onLeau);Feature("L1vo Gallery","Media space",Icons.Outlined.Collections,ink){launch(context,Intent(Intent.ACTION_VIEW).apply{type="image/*"})};Feature("L1vo Phone & Contacts","Calls and contacts",Icons.Outlined.Call,ink){launch(context,Intent(Intent.ACTION_DIAL))}}}
@Composable private fun Feature(t:String,s:String,i:ImageVector,ink:Color,onClick:()->Unit){Surface(onClick=onClick,color=L1voPanel.copy(alpha=.95f),shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth().padding(bottom=12.dp)){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically){Icon(i,t,tint=L1voDeepGreen,modifier=Modifier.size(28.dp));Spacer(Modifier.width(15.dp));Column{Text(t,color=ink,fontWeight=FontWeight.Medium);Text(s,color=ink.copy(alpha=.6f),style=MaterialTheme.typography.bodySmall)}}}}
