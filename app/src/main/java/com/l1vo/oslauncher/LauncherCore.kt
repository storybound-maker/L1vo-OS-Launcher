package com.l1vo.oslauncher

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.CalendarContract
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.appwidget.AppWidgetManager
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

@Composable fun HomeCube(slots:List<QuickSlot>,apps:List<LaunchableApp>,ink:Color,onHome:()->Unit,onHub:()->Unit,onLeau:()->Unit,onWallpaper:()->Unit,onEdit:(String)->Unit,animations:Boolean){val c=LocalContext.current;Column(Modifier.fillMaxSize().padding(horizontal=20.dp),horizontalAlignment=Alignment.CenterHorizontally){Spacer(Modifier.height(30.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()));Text("L1vo",color=ink.copy(alpha=.72f),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold);Spacer(Modifier.weight(1f));Column(verticalArrangement=Arrangement.spacedBy(7.dp),horizontalAlignment=Alignment.CenterHorizontally){Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){CubeTile(slots[0],apps,Modifier.size(116.dp),c,onHome,onEdit,ink);CubeTile(slots[1],apps,Modifier.size(116.dp),c,onHome,onEdit,ink)};Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){CubeTile(slots[2],apps,Modifier.size(116.dp),c,onHome,onEdit,ink);CubeTile(slots[3],apps,Modifier.size(116.dp),c,onHome,onEdit,ink)}};Spacer(Modifier.weight(1f));Row(horizontalArrangement=Arrangement.spacedBy(18.dp),verticalAlignment=Alignment.CenterVertically){ActionCircle(Icons.Outlined.Apps,"App Hub",onHub);LeauButton(onLeau,animations);ActionCircle(Icons.Outlined.Wallpaper,"Wallpaper",onWallpaper)};Spacer(Modifier.height(20.dp))}}

@OptIn(ExperimentalFoundationApi::class) @Composable private fun CubeTile(s:QuickSlot,apps:List<LaunchableApp>,m:Modifier,c:Context,onHome:()->Unit,onEdit:(String)->Unit,ink:Color){val app=apps.firstOrNull{it.packageName==s.packageName};val icon=when(s.kind){SlotKind.HOME->Icons.Outlined.Home;SlotKind.SETTINGS->Icons.Outlined.Settings;SlotKind.GALLERY->Icons.Outlined.Collections;SlotKind.CALLS->Icons.Outlined.Call;SlotKind.APP->Icons.Outlined.Apps};val appColor=if(app==null)MaterialTheme.colorScheme.onSurface else when(c.getSharedPreferences(PREFS,0).getString("app_color_"+app.packageName.replace(Regex("[^A-Za-z0-9_.-]"),"_"),"auto")){"white"->Color.White;"green"->L1voGreen;"warm"->Color(0xFFFFF4D6);else->MaterialTheme.colorScheme.onSurface};val appFontFamily=when(app?.let{appFont(c,it.packageName)}){"Serif"->FontFamily.Serif;"Mono"->FontFamily.Monospace;"Cursive"->FontFamily.Cursive;else->FontFamily.SansSerif};Surface(shape=RoundedCornerShape(30.dp),color=MaterialTheme.colorScheme.surface.copy(alpha=.94f),shadowElevation=7.dp,modifier=m.combinedClickable(onClick={if(s.kind==SlotKind.HOME)onHome()else openSlot(c,s)},onLongClick={if(s.kind!=SlotKind.HOME)onEdit(s.id)})){Column(Modifier.fillMaxSize().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){if(app!=null){CustomAppIcon(app,Modifier.size(38.dp));Spacer(Modifier.height(8.dp));Text(appDisplayName(c,app),color=appColor,fontFamily=appFontFamily,style=MaterialTheme.typography.labelMedium,maxLines=1)}else{Icon(icon,s.label,tint=L1voDeepGreen,modifier=Modifier.size(38.dp));Spacer(Modifier.height(8.dp));Text(s.label,color=MaterialTheme.colorScheme.onSurface,style=MaterialTheme.typography.labelMedium,maxLines=1)}}}}

@Composable
fun HomeDashboard(apps:List<LaunchableApp>,slots:List<QuickSlot>,ink:Color,onBack:()->Unit,onHub:()->Unit,onLeau:()->Unit,onHomeSettings:()->Unit,onPickWidget:()->Unit,animations:Boolean){
    val context=LocalContext.current
    val activity=context as? MainActivity
    val widgetHost=activity?.widgetHost
    val prefs=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
    var now by remember{mutableLongStateOf(System.currentTimeMillis())}
    var weather by remember{mutableStateOf("Tap to allow live weather")}
    var nextEvent by remember{mutableStateOf("No upcoming event")}
    var note by remember{mutableStateOf(prefs.getString("home_note","")?:"")}
    var showNote by remember{mutableStateOf(false)}
    var hasLocation by remember{mutableStateOf(context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)}
    val locationPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){hasLocation=it}
    var widgetIds by remember{mutableStateOf(prefs.getStringSet("home_widget_ids",emptySet())!!.mapNotNull{it.toIntOrNull()})}
    var offsets by remember(widgetIds){mutableStateOf(widgetIds.associateWith{id->Offset(prefs.getFloat("widget_x_$id",0f),prefs.getFloat("widget_y_$id",0f))})}
    var moving by remember{mutableStateOf(false)}
    var deleteArmed by remember{mutableStateOf(false)}
    val density=androidx.compose.ui.platform.LocalDensity.current
    val screenWidthPx=with(density){LocalConfiguration.current.screenWidthDp.dp.toPx()}
    val screenHeightPx=with(density){LocalConfiguration.current.screenHeightDp.dp.toPx()}
    val deleteCenterY=screenHeightPx-with(density){112.dp.toPx()}
    val enabledSystem=listOf("weather","calendar","notes","maps").filter{prefs.getBoolean("home_system_$it",true)}
    LaunchedEffect(Unit){while(true){delay(1000);now=System.currentTimeMillis();val latest=prefs.getStringSet("home_widget_ids",emptySet())!!.mapNotNull{it.toIntOrNull()};if(latest!=widgetIds)widgetIds=latest}}
    LaunchedEffect(hasLocation){if(hasLocation){val lm=context.getSystemService(Context.LOCATION_SERVICE) as LocationManager;val loc=runCatching{lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?:lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)}.getOrNull();if(loc!=null){weather=try{withContext(Dispatchers.IO){val json=URL("https://api.open-meteo.com/v1/forecast?latitude="+loc.latitude+"&longitude="+loc.longitude+"&current=temperature_2m,weather_code&timezone=auto").readText();val cur=JSONObject(json).getJSONObject("current");cur.getDouble("temperature_2m").toInt().toString()+"° • "+weatherCodeLabel(cur.getInt("weather_code"))}}catch(_:Exception){"Weather unavailable"}}else weather="Location unavailable"}}
    LaunchedEffect(Unit){if(context.checkSelfPermission(Manifest.permission.READ_CALENDAR)==PackageManager.PERMISSION_GRANTED){nextEvent=try{context.contentResolver.query(CalendarContract.Instances.CONTENT_URI.buildUpon().apply{appendPath(System.currentTimeMillis().toString());appendPath((System.currentTimeMillis()+7*24*60*60*1000).toString())}.build(),arrayOf(CalendarContract.Instances.EVENT_ID,CalendarContract.Instances.TITLE,CalendarContract.Instances.BEGIN),null,null,CalendarContract.Instances.BEGIN+" ASC")?.use{cur->if(cur.moveToFirst())cur.getString(cur.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)) else "No upcoming event"}?:"No upcoming event"}catch(_:Exception){"Calendar unavailable"}}}
    Box(Modifier.fillMaxSize()){
        Column(Modifier.fillMaxSize().padding(horizontal=20.dp).padding(top=18.dp+WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),bottom=18.dp)){
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                IconButton(onClick=onBack,modifier=Modifier.offset(y=8.dp).size(56.dp)){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)}
                Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){Text("HOME HUB",color=ink,fontWeight=FontWeight.Medium,letterSpacing=2.sp);Text(java.text.SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(java.util.Date(now)),color=ink.copy(alpha=.62f),style=MaterialTheme.typography.bodySmall)}
                IconButton(onClick=onHomeSettings){Icon(Icons.Outlined.Settings,"Home Hub settings",tint=L1voGreen)}
            }
            Spacer(Modifier.height(8.dp))
            Text(java.text.SimpleDateFormat("HH:mm",Locale.getDefault()).format(java.util.Date(now)),color=ink,style=MaterialTheme.typography.displayLarge,fontWeight=FontWeight.SemiBold,modifier=Modifier.fillMaxWidth(),textAlign=androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.weight(1f).fillMaxWidth().combinedClickable(onClick={},onLongClick=onPickWidget)){
                Column(Modifier.fillMaxSize().padding(top=8.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){
                        HomeSystemWidget("weather",weather,ink,context,{if(!hasLocation)locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)},{moving=true}){o,overlap->if(overlap)prefs.edit().putBoolean("home_system_weather",false).apply() else {prefs.edit().putFloat("system_x_weather",o.x).putFloat("system_y_weather",o.y).apply()};moving=false;deleteArmed=false}
                        HomeSystemWidget("calendar",nextEvent,ink,context,{launch(context,Intent(Intent.ACTION_VIEW).apply{data=android.net.Uri.parse("content://com.android.calendar/time/"+now)})},{moving=true}){o,overlap->if(overlap)prefs.edit().putBoolean("home_system_calendar",false).apply() else {prefs.edit().putFloat("system_x_calendar",o.x).putFloat("system_y_calendar",o.y).apply()};moving=false;deleteArmed=false}
                    }
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){
                        HomeSystemWidget("notes",if(note.isBlank())"Tap to write" else note,ink,context,{showNote=true},{moving=true}){o,overlap->if(overlap)prefs.edit().putBoolean("home_system_notes",false).apply() else {prefs.edit().putFloat("system_x_notes",o.x).putFloat("system_y_notes",o.y).apply()};moving=false;deleteArmed=false}
                        HomeSystemWidget("maps","Open live map",ink,context,{launch(context,Intent(Intent.ACTION_VIEW,android.net.Uri.parse("geo:0,0?q=My+Location")))},{moving=true}){o,overlap->if(overlap)prefs.edit().putBoolean("home_system_maps",false).apply() else {prefs.edit().putFloat("system_x_maps",o.x).putFloat("system_y_maps",o.y).apply()};moving=false;deleteArmed=false}
                    }
                    widgetIds.forEach{id->
                        val info=AppWidgetManager.getInstance(context).getAppWidgetInfo(id)
                        val pos=offsets[id]?:Offset.Zero
                        if(info!=null)Box(Modifier.fillMaxWidth().height(150.dp).offset{IntOffset((pos.x).roundToInt(),pos.y.roundToInt())}.pointerInput(id){
                            detectDragGesturesAfterLongPress(
                                onDragStart={moving=true;deleteArmed=false},
                                onDragEnd={
                                    if(deleteArmed){widgetHost?.deleteAppWidgetId(id);widgetIds=widgetIds.filterNot{it==id};prefs.edit().putStringSet("home_widget_ids",widgetIds.map{it.toString()}.toSet()).remove("widget_x_$id").remove("widget_y_$id").apply()}
                                    else prefs.edit().putFloat("widget_x_$id",offsets[id]?.x?:0f).putFloat("widget_y_$id",offsets[id]?.y?:0f).apply()
                                    moving=false;deleteArmed=false
                                }
                            ){change,dragAmount->
                                change.consume()
                                val next=(offsets[id]?:Offset.Zero)+dragAmount
                                offsets=offsets.toMutableMap().also{it[id]=next}
                                val cx=next.x+screenWidthPx/2f
                                val cy=next.y+with(density){75.dp.toPx()}
                                deleteArmed=kotlin.math.abs(cx-screenWidthPx/2f)<72.dp.toPx()&&kotlin.math.abs(cy-deleteCenterY)<72.dp.toPx()
                            }
                        }){
                            AndroidView(factory={widgetHost?.createView(context,id,info)},modifier=Modifier.fillMaxSize())
                        }
                    }
                }
            }
            Surface(onClick=onHub,color=L1voDeepGreen,shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth().height(64.dp),shadowElevation=5.dp){Row(Modifier.padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Apps,"App Hub",tint=Color.White);Spacer(Modifier.width(12.dp));Text("APP HUB",color=Color.White,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Icon(Icons.Outlined.ChevronRight,"Open",tint=Color.White)}}
        }
        if(moving)Box(Modifier.fillMaxSize(),contentAlignment=Alignment.BottomCenter){
            Surface(onClick={},color=if(deleteArmed)Color(0xFFE53935) else Color(0xFFB71C1C),shape=CircleShape,shadowElevation=if(deleteArmed)12.dp else 4.dp,modifier=Modifier.padding(bottom=82.dp)){Box(Modifier.size(if(deleteArmed)68.dp else 58.dp),contentAlignment=Alignment.Center){Text("×",color=Color.White,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.headlineSmall)}}
        }
    }
    if(showNote){var draft by remember(note){mutableStateOf(note)};AlertDialog(onDismissRequest={showNote=false},title={Text("Quick note")},text={OutlinedTextField(value=draft,onValueChange={draft=it},modifier=Modifier.fillMaxWidth(),minLines=4)},confirmButton={TextButton(onClick={note=draft;prefs.edit().putString("home_note",draft).apply();showNote=false}){Text("Save")}},dismissButton={TextButton(onClick={showNote=false}){Text("Cancel")}})}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable private fun HomeSystemWidget(key:String,subtitle:String,ink:Color,context:Context,onClick:()->Unit,onMoving:()->Unit,onMoved:(Offset,Boolean)->Unit){
    val density=androidx.compose.ui.platform.LocalDensity.current
    val screenWidthPx=with(density){LocalConfiguration.current.screenWidthDp.dp.toPx()}
    val screenHeightPx=with(density){LocalConfiguration.current.screenHeightDp.dp.toPx()}
    val deleteCenterY=screenHeightPx-with(density){112.dp.toPx()}
    var current by remember{mutableStateOf(Offset.Zero)}
    val icon=when(key){"weather"->Icons.Outlined.WbSunny;"calendar"->Icons.Outlined.CalendarMonth;"notes"->Icons.Outlined.EditNote;else->Icons.Outlined.Map}
    Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surface.copy(alpha=.94f),shadowElevation=3.dp,modifier=Modifier.fillMaxWidth(.48f).height(122.dp).pointerInput(key){
        detectDragGesturesAfterLongPress(onDragStart={onMoving()},onDragEnd={val cx=screenWidthPx/2f;val cy=deleteCenterY;onMoved(current,kotlin.math.abs(cx-screenWidthPx/2f)<72.dp.toPx()&&kotlin.math.abs(cy-deleteCenterY)<72.dp.toPx())}){change,amount->change.consume();current+=amount}
    }.combinedClickable(onClick=onClick,onLongClick=onMoving)){
        Row(Modifier.fillMaxSize().padding(14.dp),verticalAlignment=Alignment.CenterVertically){
            Surface(shape=RoundedCornerShape(14.dp),color=L1voGreen.copy(alpha=.10f),modifier=Modifier.size(44.dp)){Box(contentAlignment=Alignment.Center){Icon(icon,key,tint=L1voDeepGreen)}}
            Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(key.replaceFirstChar{it.uppercase()},color=ink,fontWeight=FontWeight.SemiBold);Text(subtitle,color=ink.copy(alpha=.62f),style=MaterialTheme.typography.bodySmall,maxLines=2)}
        }
    }
}
private fun weatherCodeLabel(code:Int)=when(code){0->"Clear";1,2,3->"Cloudy";45,48->"Fog";51,53,55,56,57->"Drizzle";61,63,65,66,67,80,81,82->"Rain";71,73,75,77,85,86->"Snow";95,96,99->"Storm";else->"Weather"}
@Composable private fun HubTile(title:String,subtitle:String,icon:ImageVector,ink:Color,onClick:()->Unit){Surface(onClick=onClick,color=MaterialTheme.colorScheme.surface.copy(alpha=.92f),shape=RoundedCornerShape(26.dp),shadowElevation=4.dp,modifier=Modifier.fillMaxWidth().height(142.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.SpaceBetween){Surface(shape=RoundedCornerShape(16.dp),color=L1voGreen.copy(alpha=.12f),modifier=Modifier.size(48.dp)){Box(contentAlignment=Alignment.Center){Icon(icon,title,tint=L1voDeepGreen)}};Column{Text(title,color=MaterialTheme.colorScheme.onSurface,fontWeight=FontWeight.SemiBold);Text(subtitle,color=MaterialTheme.colorScheme.onSurface.copy(alpha=.58f),style=MaterialTheme.typography.bodySmall)}}}}
@Composable private fun LeauButton(onClick:()->Unit,animations:Boolean){val t=rememberInfiniteTransition(label="leau");val p by t.animateFloat(.94f,1.06f,infiniteRepeatable(tween(1700),RepeatMode.Reverse),label="pulse");Surface(onClick=onClick,modifier=Modifier.size(66.dp).scale(if(animations)p else 1f),shape=CircleShape,color=L1voInk,shadowElevation=8.dp){Box(contentAlignment=Alignment.Center){Text("⌒  ⌒",color=Color.White,fontWeight=FontWeight.Bold)}}}
@Composable private fun ActionCircle(icon:ImageVector,label:String,onClick:()->Unit){Surface(onClick=onClick,modifier=Modifier.size(48.dp),shape=CircleShape,color=MaterialTheme.colorScheme.surface.copy(alpha=.94f),shadowElevation=3.dp){Box(contentAlignment=Alignment.Center){Icon(icon,label,tint=L1voDeepGreen)}}}
@Composable fun SlotPicker(apps:List<LaunchableApp>,onDismiss:()->Unit,onSelect:(LaunchableApp)->Unit){AlertDialog(onDismissRequest=onDismiss,title={Text("Choose an app")},text={Column(Modifier.height(420.dp).verticalScroll(rememberScrollState())){apps.forEach{a->Surface(onClick={onSelect(a)},color=Color.Transparent,modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically){Image(a.icon.asImageBitmap(),null,Modifier.size(42.dp).padding(6.dp));Spacer(Modifier.width(12.dp));Text(a.label)}}}}},confirmButton={TextButton(onClick=onDismiss){Text("Cancel")}})}
fun loadSlots(c:Context):List<QuickSlot>{val p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);fun x(id:String,l:String,k:SlotKind):QuickSlot{val pkg=p.getString("slot_$id",null);return if(pkg==null)QuickSlot(id,l,null,k)else QuickSlot(id,appLabel(c,pkg),pkg,SlotKind.APP)};return listOf(QuickSlot("home","Home",null,SlotKind.HOME),x("settings","Settings",SlotKind.SETTINGS),x("gallery","Gallery",SlotKind.GALLERY),x("calls","Calls",SlotKind.CALLS))}
fun saveSlot(c:Context,id:String,a:LaunchableApp){c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString("slot_$id",a.packageName).apply()}
fun openSlot(c:Context,s:QuickSlot){when{s.packageName!=null->c.packageManager.getLaunchIntentForPackage(s.packageName)?.let{launch(c,it)};s.kind==SlotKind.GALLERY->launch(c,Intent(Intent.ACTION_VIEW).apply{type="image/*"});s.kind==SlotKind.CALLS->launch(c,Intent(Intent.ACTION_DIAL))}}
fun launchLeau(c:Context){val pm=c.packageManager;runCatching{c.startActivity(Intent("com.liv.ol1viapa.OPEN_ASSISTANT").setPackage(LEAU_PACKAGE))}.onFailure{pm.getLaunchIntentForPackage(LEAU_PACKAGE)?.let{c.startActivity(it)}?:android.widget.Toast.makeText(c,"Leau Assistant is not installed yet",0).show()}}
fun launch(c:Context,i:Intent){runCatching{if(c is android.app.Activity)c.startActivity(i)else c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}.onFailure{android.widget.Toast.makeText(c,"Unable to open app",0).show()}}
fun loadApps(c:Context):List<LaunchableApp>{val pm=c.packageManager;val q=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);return pm.queryIntentActivities(q,PackageManager.MATCH_ALL).mapNotNull{info->val l=info.loadLabel(pm)?.toString()?.takeIf{it.isNotBlank()}?:return@mapNotNull null;val icon=info.loadIcon(pm)?.let{drawableToBitmap(it,64)}?:return@mapNotNull null;LaunchableApp(l,info.activityInfo.packageName,Intent(q).setClassName(info.activityInfo.packageName,info.activityInfo.name),icon)}.distinctBy{it.packageName}.sortedBy{it.label.lowercase(Locale.getDefault())}}
fun appLabel(c:Context,pkg:String)=runCatching{c.packageManager.getApplicationLabel(c.packageManager.getApplicationInfo(pkg,0)).toString()}.getOrDefault("App")
fun drawableToBitmap(d:Drawable,size:Int=64)=d.toBitmap(size,size,Bitmap.Config.ARGB_8888)
