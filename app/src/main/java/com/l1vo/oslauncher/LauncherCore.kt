package com.l1vo.oslauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import java.util.Locale
import kotlinx.coroutines.delay

@Composable fun HomeCube(slots:List<QuickSlot>,apps:List<LaunchableApp>,ink:Color,onHome:()->Unit,onHub:()->Unit,onLeau:()->Unit,onWallpaper:()->Unit,onEdit:(String)->Unit,animations:Boolean){val c=LocalContext.current;Column(Modifier.fillMaxSize().padding(horizontal=20.dp),horizontalAlignment=Alignment.CenterHorizontally){Spacer(Modifier.height(30.dp));Text("L1vo",color=ink.copy(alpha=.72f),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Light);Spacer(Modifier.weight(1f));Column(verticalArrangement=Arrangement.spacedBy(7.dp),horizontalAlignment=Alignment.CenterHorizontally){Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){CubeTile(slots[0],apps,Modifier.size(116.dp),c,onHome,onEdit,ink);CubeTile(slots[1],apps,Modifier.size(116.dp),c,onHome,onEdit,ink)};Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){CubeTile(slots[2],apps,Modifier.size(116.dp),c,onHome,onEdit,ink);CubeTile(slots[3],apps,Modifier.size(116.dp),c,onHome,onEdit,ink)}};Spacer(Modifier.weight(1f));Row(horizontalArrangement=Arrangement.spacedBy(18.dp),verticalAlignment=Alignment.CenterVertically){ActionCircle(Icons.Outlined.Apps,"App Hub",onHub);LeauButton(onLeau,animations);ActionCircle(Icons.Outlined.Wallpaper,"Wallpaper",onWallpaper)};Spacer(Modifier.height(20.dp))}}

@OptIn(ExperimentalFoundationApi::class) @Composable private fun CubeTile(s:QuickSlot,apps:List<LaunchableApp>,m:Modifier,c:Context,onHome:()->Unit,onEdit:(String)->Unit,ink:Color){val app=apps.firstOrNull{it.packageName==s.packageName};val icon=when(s.kind){SlotKind.HOME->Icons.Outlined.Home;SlotKind.SETTINGS->Icons.Outlined.Settings;SlotKind.GALLERY->Icons.Outlined.Collections;SlotKind.CALLS->Icons.Outlined.Call;SlotKind.APP->Icons.Outlined.Apps};Surface(shape=RoundedCornerShape(30.dp),color=Color.White.copy(alpha=.86f),shadowElevation=7.dp,modifier=m.combinedClickable(onClick={if(s.kind==SlotKind.HOME)onHome()else openSlot(c,s)},onLongClick={if(s.kind!=SlotKind.HOME)onEdit(s.id)})){Column(Modifier.fillMaxSize().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){if(app!=null)Image(app.icon.asImageBitmap(),s.label,Modifier.size(38.dp),contentScale=ContentScale.Fit)else Icon(icon,s.label,tint=L1voDeepGreen,modifier=Modifier.size(38.dp));Spacer(Modifier.height(8.dp));Text(s.label,color=ink,style=MaterialTheme.typography.labelMedium,maxLines=1)}}}

@Composable fun HomeDashboard(apps:List<LaunchableApp>,slots:List<QuickSlot>,ink:Color,onBack:()->Unit,onHub:()->Unit,onLeau:()->Unit,animations:Boolean){var now by remember{mutableLongStateOf(System.currentTimeMillis())};LaunchedEffect(Unit){while(true){now=System.currentTimeMillis();delay(1000)}};val c=LocalContext.current;Column(Modifier.fillMaxSize().padding(horizontal=20.dp,vertical=18.dp)){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)};Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){Text("HOME HUB",color=ink,fontWeight=FontWeight.Medium,letterSpacing=2.sp);Text(java.text.SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(java.util.Date(now)),color=ink.copy(alpha=.62f),style=MaterialTheme.typography.bodySmall)};IconButton(onClick=onLeau){Icon(Icons.Outlined.Face,"Leau",tint=L1voGreen)}};Spacer(Modifier.height(16.dp));Text(java.text.SimpleDateFormat("HH:mm",Locale.getDefault()).format(java.util.Date(now)),color=ink,style=MaterialTheme.typography.displayLarge,fontWeight=FontWeight.Light,modifier=Modifier.fillMaxWidth(),textAlign=androidx.compose.ui.text.style.TextAlign.Center);Spacer(Modifier.height(18.dp));LazyVerticalGrid(columns=GridCells.Fixed(2),modifier=Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=12.dp)){item{HubTile("Weather","72°  Clear",Icons.Outlined.WbSunny,ink){}};item{HubTile("Calendar","Today",Icons.Outlined.CalendarMonth,ink){}};item{HubTile("Notes","Quick notes",Icons.Outlined.EditNote,ink){}};item{HubTile("Maps","Explore",Icons.Outlined.Map,ink){}}};Surface(onClick=onHub,color=L1voDeepGreen,shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth().height(64.dp),shadowElevation=5.dp){Row(Modifier.padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Apps,"App Hub",tint=Color.White);Spacer(Modifier.width(12.dp));Text("APP HUB",color=Color.White,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Icon(Icons.Outlined.ChevronRight,"Open",tint=Color.White)}};Spacer(Modifier.height(12.dp));Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("L1vo",color=ink.copy(alpha=.55f),modifier=Modifier.weight(1f),style=MaterialTheme.typography.labelMedium);Surface(onClick={launchLeau(c)},shape=CircleShape,color=Color.White.copy(alpha=.86f),modifier=Modifier.size(46.dp)){Box(contentAlignment=Alignment.Center){Icon(Icons.Outlined.Person,"Account",tint=L1voDeepGreen)}}}}}

@Composable private fun HubTile(title:String,subtitle:String,icon:ImageVector,ink:Color,onClick:()->Unit){Surface(onClick=onClick,color=Color.White.copy(alpha=.82f),shape=RoundedCornerShape(26.dp),shadowElevation=4.dp,modifier=Modifier.fillMaxWidth().height(142.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.SpaceBetween){Surface(shape=RoundedCornerShape(16.dp),color=L1voGreen.copy(alpha=.12f),modifier=Modifier.size(48.dp)){Box(contentAlignment=Alignment.Center){Icon(icon,title,tint=L1voDeepGreen)}};Column{Text(title,color=ink,fontWeight=FontWeight.SemiBold);Text(subtitle,color=ink.copy(alpha=.58f),style=MaterialTheme.typography.bodySmall)}}}}
@Composable private fun LeauButton(onClick:()->Unit,animations:Boolean){val t=rememberInfiniteTransition(label="leau");val p by t.animateFloat(.94f,1.06f,infiniteRepeatable(tween(1700),RepeatMode.Reverse),label="pulse");Surface(onClick=onClick,modifier=Modifier.size(66.dp).scale(if(animations)p else 1f),shape=CircleShape,color=L1voInk,shadowElevation=8.dp){Box(contentAlignment=Alignment.Center){Text("⌒  ⌒",color=Color.White,fontWeight=FontWeight.Bold)}}}
@Composable private fun ActionCircle(icon:ImageVector,label:String,onClick:()->Unit){Surface(onClick=onClick,modifier=Modifier.size(48.dp),shape=CircleShape,color=Color.White.copy(alpha=.86f),shadowElevation=3.dp){Box(contentAlignment=Alignment.Center){Icon(icon,label,tint=L1voDeepGreen)}}}
@Composable fun SlotPicker(apps:List<LaunchableApp>,onDismiss:()->Unit,onSelect:(LaunchableApp)->Unit){AlertDialog(onDismissRequest=onDismiss,title={Text("Choose an app")},text={Column(Modifier.height(420.dp).verticalScroll(rememberScrollState())){apps.forEach{a->Surface(onClick={onSelect(a)},color=Color.Transparent,modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically){Image(a.icon.asImageBitmap(),null,Modifier.size(42.dp).padding(6.dp));Spacer(Modifier.width(12.dp));Text(a.label)}}}}},confirmButton={TextButton(onClick=onDismiss){Text("Cancel")}})}
fun loadSlots(c:Context):List<QuickSlot>{val p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);fun x(id:String,l:String,k:SlotKind):QuickSlot{val pkg=p.getString("slot_$id",null);return if(pkg==null)QuickSlot(id,l,null,k)else QuickSlot(id,appLabel(c,pkg),pkg,SlotKind.APP)};return listOf(QuickSlot("home","Home",null,SlotKind.HOME),x("settings","Settings",SlotKind.SETTINGS),x("gallery","Gallery",SlotKind.GALLERY),x("calls","Calls",SlotKind.CALLS))}
fun saveSlot(c:Context,id:String,a:LaunchableApp){c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString("slot_$id",a.packageName).apply()}
fun openSlot(c:Context,s:QuickSlot){when{s.packageName!=null->c.packageManager.getLaunchIntentForPackage(s.packageName)?.let{launch(c,it)};s.kind==SlotKind.GALLERY->launch(c,Intent(Intent.ACTION_VIEW).apply{type="image/*"});s.kind==SlotKind.CALLS->launch(c,Intent(Intent.ACTION_DIAL))}}
fun launchLeau(c:Context){val pm=c.packageManager;runCatching{c.startActivity(Intent("com.liv.ol1viapa.OPEN_ASSISTANT").setPackage(LEAU_PACKAGE))}.onFailure{pm.getLaunchIntentForPackage(LEAU_PACKAGE)?.let{c.startActivity(it)}?:android.widget.Toast.makeText(c,"Leau Assistant is not installed yet",0).show()}}
fun launch(c:Context,i:Intent){runCatching{if(c is android.app.Activity)c.startActivity(i)else c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}.onFailure{android.widget.Toast.makeText(c,"Unable to open app",0).show()}}
fun loadApps(c:Context):List<LaunchableApp>{val pm=c.packageManager;val q=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);return pm.queryIntentActivities(q,PackageManager.MATCH_ALL).mapNotNull{info->val l=info.loadLabel(pm)?.toString()?.takeIf{it.isNotBlank()}?:return@mapNotNull null;val icon=info.loadIcon(pm)?.let{drawableToBitmap(it)}?:return@mapNotNull null;LaunchableApp(l,info.activityInfo.packageName,Intent(q).setClassName(info.activityInfo.packageName,info.activityInfo.name),icon)}.distinctBy{it.packageName}.sortedBy{it.label.lowercase(Locale.getDefault())}}
fun appLabel(c:Context,pkg:String)=runCatching{c.packageManager.getApplicationLabel(c.packageManager.getApplicationInfo(pkg,0)).toString()}.getOrDefault("App")
fun drawableToBitmap(d:Drawable)=d.toBitmap(96,96,Bitmap.Config.ARGB_8888)
