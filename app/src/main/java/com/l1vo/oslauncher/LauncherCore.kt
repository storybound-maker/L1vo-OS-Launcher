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
import androidx.core.graphics.drawable.toBitmap
import java.util.Locale
import kotlinx.coroutines.delay

@Composable fun HomeCube(slots:List<QuickSlot>,apps:List<LaunchableApp>,ink:Color,onHome:()->Unit,onHub:()->Unit,onLeau:()->Unit,onWallpaper:()->Unit,onEdit:(String)->Unit,animations:Boolean){val c=LocalContext.current;Column(Modifier.fillMaxSize().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){Spacer(Modifier.height(24.dp));Text("L1vo",color=ink.copy(alpha=.7f));Spacer(Modifier.weight(1f));Card(shape=RoundedCornerShape(34.dp),colors=CardDefaults.cardColors(L1voPanel.copy(alpha=.9f)),elevation=CardDefaults.cardElevation(10.dp),modifier=Modifier.size(250.dp)){Column(Modifier.fillMaxSize()){Row(Modifier.weight(1f)){CubeTile(slots[0],apps,Modifier.weight(1f),c,onHome,onEdit,ink);CubeTile(slots[1],apps,Modifier.weight(1f),c,onHome,onEdit,ink)};Row(Modifier.weight(1f)){CubeTile(slots[2],apps,Modifier.weight(1f),c,onHome,onEdit,ink);CubeTile(slots[3],apps,Modifier.weight(1f),c,onHome,onEdit,ink)}}};Spacer(Modifier.weight(1f));Row(horizontalArrangement=Arrangement.spacedBy(18.dp),verticalAlignment=Alignment.CenterVertically){ActionCircle(Icons.Outlined.Apps,"App Hub",onHub);LeauButton(onLeau,animations);ActionCircle(Icons.Outlined.Wallpaper,"Wallpaper",onWallpaper)}}}
@OptIn(ExperimentalFoundationApi::class) @Composable private fun CubeTile(s:QuickSlot,apps:List<LaunchableApp>,m:Modifier,c:Context,onHome:()->Unit,onEdit:(String)->Unit,ink:Color){val app=apps.firstOrNull{it.packageName==s.packageName};val icon=when(s.kind){SlotKind.HOME->Icons.Outlined.Home;SlotKind.SETTINGS->Icons.Outlined.Settings;SlotKind.GALLERY->Icons.Outlined.Collections;SlotKind.CALLS->Icons.Outlined.Call;SlotKind.APP->Icons.Outlined.Apps};Box(m.combinedClickable(onClick={if(s.kind==SlotKind.HOME)onHome()else openSlot(c,s)},onLongClick={if(s.kind!=SlotKind.HOME)onEdit(s.id)}),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Surface(shape=CircleShape,color=Color.White.copy(alpha=.88f),modifier=Modifier.size(58.dp)){Box(contentAlignment=Alignment.Center){if(app!=null)Image(app.icon.asImageBitmap(),s.label,Modifier.size(34.dp))else Icon(icon,s.label,tint=L1voDeepGreen)}};Spacer(Modifier.height(6.dp));Text(s.label,color=ink,style=MaterialTheme.typography.labelMedium,maxLines=1)}}}
@Composable fun HomeDashboard(apps:List<LaunchableApp>,slots:List<QuickSlot>,ink:Color,onBack:()->Unit,onHub:()->Unit,onLeau:()->Unit,animations:Boolean){var now by remember{mutableLongStateOf(System.currentTimeMillis())};LaunchedEffect(Unit){while(true){now=System.currentTimeMillis();delay(1000)}};val quick=remember(apps,slots){(slots.drop(1).mapNotNull{s->apps.firstOrNull{it.packageName==s.packageName}}+apps).distinctBy{it.packageName}.take(4)};Column(Modifier.fillMaxSize().padding(18.dp)){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)};Spacer(Modifier.weight(1f));Text("HOME",color=L1voGreen,fontWeight=FontWeight.Bold)};Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally){Text(java.text.SimpleDateFormat("HH:mm",Locale.getDefault()).format(java.util.Date(now)),color=ink,style=MaterialTheme.typography.displayLarge,fontWeight=FontWeight.Light);Text(java.text.SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(java.util.Date(now)),color=ink.copy(alpha=.68f))};Spacer(Modifier.height(18.dp));LazyVerticalGrid(columns=GridCells.Fixed(2),modifier=Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){items(quick,key={it.packageName}){a->DashboardCard(a,ink)}};Surface(onClick=onHub,color=L1voDeepGreen,shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth().height(62.dp)){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Apps,"App Hub",tint=Color.White);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("APP HUB",color=Color.White,fontWeight=FontWeight.Bold);Text("Everything installed",color=Color.White.copy(alpha=.7f),style=MaterialTheme.typography.bodySmall)};Icon(Icons.Outlined.ChevronRight,"Open",tint=Color.White)}};Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Leau",color=ink.copy(alpha=.7f),modifier=Modifier.weight(1f));LeauButton(onLeau,animations)}}}
@Composable private fun DashboardCard(a:LaunchableApp,ink:Color){val c=LocalContext.current;Surface(onClick={launch(c,a.intent)},color=Color.White.copy(alpha=.9f),shape=RoundedCornerShape(24.dp),shadowElevation=5.dp,modifier=Modifier.height(130.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.SpaceBetween){Surface(shape=RoundedCornerShape(16.dp),color=L1voGreen.copy(alpha=.1f),modifier=Modifier.size(52.dp)){Image(a.icon.asImageBitmap(),a.label,Modifier.padding(9.dp),contentScale=ContentScale.Fit)};Text(a.label,color=ink,fontWeight=FontWeight.SemiBold,maxLines=1)}}}
@Composable private fun LeauButton(onClick:()->Unit,animations:Boolean){val t=rememberInfiniteTransition(label="leau");val p by t.animateFloat(.94f,1.06f,infiniteRepeatable(tween(1700),RepeatMode.Reverse),label="pulse");Surface(onClick=onClick,modifier=Modifier.size(66.dp).scale(if(animations)p else 1f),shape=CircleShape,color=L1voInk,shadowElevation=8.dp){Box(contentAlignment=Alignment.Center){Text("⌒  ⌒",color=Color.White,fontWeight=FontWeight.Bold)}}}
@Composable private fun ActionCircle(icon:ImageVector,label:String,onClick:()->Unit){Surface(onClick=onClick,modifier=Modifier.size(48.dp),shape=CircleShape,color=Color.White.copy(alpha=.86f),shadowElevation=3.dp){Box(contentAlignment=Alignment.Center){Icon(icon,label,tint=L1voDeepGreen)}}}
@Composable fun SlotPicker(apps:List<LaunchableApp>,onDismiss:()->Unit,onSelect:(LaunchableApp)->Unit){AlertDialog(onDismissRequest=onDismiss,title={Text("Choose an app")},text={Column(Modifier.height(420.dp).verticalScroll(rememberScrollState())){apps.forEach{a->Surface(onClick={onSelect(a)},color=Color.Transparent,modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically){Image(a.icon.asImageBitmap(),null,Modifier.size(42.dp).padding(6.dp));Spacer(Modifier.width(12.dp));Text(a.label)}}}}},confirmButton={TextButton(onClick=onDismiss){Text("Cancel")}})}
fun loadSlots(c:Context):List<QuickSlot>{val p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);fun x(id:String,l:String,k:SlotKind):QuickSlot{val pkg=p.getString("slot_$id",null);return if(pkg==null)QuickSlot(id,l,null,k)else QuickSlot(id,appLabel(c,pkg),pkg,SlotKind.APP)};return listOf(QuickSlot("home","Home",null,SlotKind.HOME),x("settings","Settings",SlotKind.SETTINGS),x("gallery","Gallery",SlotKind.GALLERY),x("calls","Calls",SlotKind.CALLS))}
fun saveSlot(c:Context,id:String,a:LaunchableApp){c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString("slot_$id",a.packageName).apply()}
fun openSlot(c:Context,s:QuickSlot){when{ s.packageName!=null->c.packageManager.getLaunchIntentForPackage(s.packageName)?.let{launch(c,it)};s.kind==SlotKind.GALLERY->launch(c,Intent(Intent.ACTION_VIEW).apply{type="image/*"});s.kind==SlotKind.CALLS->launch(c,Intent(Intent.ACTION_DIAL))}}
fun launchLeau(c:Context){val pm=c.packageManager;runCatching{c.startActivity(Intent("com.liv.ol1viapa.OPEN_ASSISTANT").setPackage(LEAU_PACKAGE))}.onFailure{pm.getLaunchIntentForPackage(LEAU_PACKAGE)?.let{c.startActivity(it)}?:android.widget.Toast.makeText(c,"Leau Assistant is not installed yet",0).show()}}
fun launch(c:Context,i:Intent){runCatching{c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}.onFailure{android.widget.Toast.makeText(c,"Unable to open app",0).show()}}
fun loadApps(c:Context):List<LaunchableApp>{val pm=c.packageManager;val q=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);return pm.queryIntentActivities(q,PackageManager.MATCH_ALL).mapNotNull{info->val l=info.loadLabel(pm)?.toString()?.takeIf{it.isNotBlank()}?:return@mapNotNull null;val icon=info.loadIcon(pm)?.let{drawableToBitmap(it)}?:return@mapNotNull null;LaunchableApp(l,info.activityInfo.packageName,Intent(q).setClassName(info.activityInfo.packageName,info.activityInfo.name),icon)}.distinctBy{it.packageName}.sortedBy{it.label.lowercase(Locale.getDefault())}}
fun appLabel(c:Context,pkg:String)=runCatching{c.packageManager.getApplicationLabel(c.packageManager.getApplicationInfo(pkg,0)).toString()}.getOrDefault("App")
fun drawableToBitmap(d:Drawable)=d.toBitmap(96,96,Bitmap.Config.ARGB_8888)
