package com.l1vo.oslauncher

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay

@Composable
fun WallpaperBackground(value:String?,dark:Boolean,playlist:List<String> = emptyList(),intervalSeconds:Long = 3600L,useSystemWallpaper:Boolean=false){
    val context=LocalContext.current
    val systemFallback=remember{runCatching{WallpaperManager.getInstance(context).drawable?.let{drawableToWallpaperBitmap(it)}}.getOrNull()}
    var activeUri by remember(value,playlist){mutableStateOf(playlist.firstOrNull()?:value)}
    LaunchedEffect(playlist,value,intervalSeconds){val items=if(playlist.isNotEmpty())playlist else listOfNotNull(value);if(items.isEmpty()){activeUri=null;return@LaunchedEffect};var index=items.indexOfFirst{it==activeUri}.takeIf{it>=0}?:0;activeUri=items[index];while(items.size>1){delay(intervalSeconds.coerceIn(10L,86400L)*1000L);index=(index+1)%items.size;activeUri=items[index]}}
    Box(Modifier.fillMaxSize().background(if(useSystemWallpaper)Color.Transparent else if(dark)L1voDark else Color(0xFFE9E8D9))){
        if(!useSystemWallpaper){
            var mime by remember(activeUri){mutableStateOf<String?>(null)}
            var bitmap by remember(activeUri){mutableStateOf<Bitmap?>(null)}
            LaunchedEffect(activeUri){val uri=activeUri?.let(Uri::parse);mime=uri?.let{runCatching{context.contentResolver.getType(it)}.getOrNull()};bitmap=if(mime?.startsWith("video/")!=true)uri?.let{runCatching{context.contentResolver.openInputStream(it)?.use{stream->android.graphics.BitmapFactory.decodeStream(stream)}}.getOrNull()}?:systemFallback else null}
            if(mime?.startsWith("video/")==true&&activeUri!=null){
                AndroidView(factory={VideoView(it)},update={view->view.setVideoURI(Uri.parse(activeUri));view.setOnPreparedListener{mp->mp.isLooping=true;mp.setVolume(0f,0f);view.start()};view.start()},modifier=Modifier.fillMaxSize())
            } else bitmap?.let{Image(it.asImageBitmap(),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)}
        }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=if(dark).10f else .035f)))
    }
}
private fun drawableToWallpaperBitmap(d:Drawable):Bitmap=d.toBitmap(1080,1920,Bitmap.Config.ARGB_8888)
@Composable fun rememberHomeAdaptiveInk(value:String?,darkFallback:Boolean):Color=if(darkFallback)Color(0xFFE9F0E9)else L1voInk
@Composable fun rememberAdaptiveInk(value:String?,darkFallback:Boolean):Color=if(darkFallback)Color(0xFFE9F0E9)else L1voInk

@Composable
fun WallpaperStudio(
    ink:Color,onBack:()->Unit,onSave:(String,String)->Unit,onPlaylist:(List<String>,String)->Unit,
    onLiveWallpaper:()->Unit,playlist:List<String>,intervalSeconds:Long,playlists:Map<String,List<String>> = emptyMap()
){
    val context=LocalContext.current
    var target by rememberSaveable{mutableStateOf("all")}
    var interval by rememberSaveable{mutableLongStateOf(intervalSeconds)}
    var items by remember(playlist){mutableStateOf(playlist)}
    LaunchedEffect(target,playlists){items=playlists[target]?:playlist}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){u->if(u!=null){runCatching{context.contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)};onSave(target,u.toString())}}
    val multiPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){uris->if(uris.isNotEmpty()){val saved=uris.mapNotNull{u->runCatching{context.contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)};u.toString()};if(saved.isNotEmpty()){items=saved;onPlaylist(saved,target)}}}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack,modifier=Modifier.offset(y=8.dp).size(56.dp)){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)};Column(Modifier.weight(1f)){Text("Wallpaper",color=ink,style=MaterialTheme.typography.headlineMedium);Text("Wallpaper Studio",color=L1voGreen)}}
        Spacer(Modifier.height(18.dp))
        Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface.copy(alpha=.96f)),shape=RoundedCornerShape(26.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp)){
            Text("Where should this wallpaper appear?",color=ink,fontWeight=FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("all" to "All","main" to "Main","home" to "Home Hub","hub" to "App Hub").forEach{(id,label)->FilterChip(selected=target==id,onClick={target=id},label={Text(label)},modifier=Modifier.weight(1f))}}
        }}
        Spacer(Modifier.height(12.dp))
        Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface.copy(alpha=.96f)),shape=RoundedCornerShape(26.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){
            Icon(Icons.Outlined.Collections,"Photos",tint=L1voGreen,modifier=Modifier.size(44.dp));Spacer(Modifier.height(10.dp));Text("Choose wallpaper",color=ink,style=MaterialTheme.typography.titleLarge)
            Text("Images and videos can be used in your rotating playlist.",color=ink.copy(alpha=.62f),textAlign=TextAlign.Center)
            Spacer(Modifier.height(14.dp));Button(onClick={picker.launch(arrayOf("image/*","video/*"))}){Text("Choose one")}
            Spacer(Modifier.height(7.dp));OutlinedButton(onClick={multiPicker.launch(arrayOf("image/*","video/*"))}){Text("Add to playlist")}
            Spacer(Modifier.height(7.dp));OutlinedButton(onClick=onLiveWallpaper){Icon(Icons.Outlined.Movie,"Live wallpaper",modifier=Modifier.size(18.dp));Spacer(Modifier.width(7.dp));Text("Choose phone live wallpaper")}
        }}
        Spacer(Modifier.height(12.dp))
        Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface.copy(alpha=.96f)),shape=RoundedCornerShape(26.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp)){
            Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Playlist file",color=ink,style=MaterialTheme.typography.titleMedium);Text(if(items.isEmpty())"No playlist selected" else "${items.size} items",color=ink.copy(alpha=.62f),style=MaterialTheme.typography.bodySmall)};Icon(Icons.Outlined.FolderOpen,"Playlist",tint=L1voGreen)}
            Spacer(Modifier.height(10.dp))
            items.forEachIndexed{index,uri->Row(Modifier.fillMaxWidth().padding(vertical=4.dp),verticalAlignment=Alignment.CenterVertically){Surface(shape=CircleShape,color=L1voGreen.copy(alpha=.12f),modifier=Modifier.size(30.dp)){Box(contentAlignment=Alignment.Center){Text("${index+1}",color=L1voDeepGreen,style=MaterialTheme.typography.labelSmall)}};Spacer(Modifier.width(9.dp));Text(Uri.parse(uri).lastPathSegment?:"Wallpaper ${index+1}",color=ink,modifier=Modifier.weight(1f),maxLines=1);IconButton(onClick={items=items.toMutableList().also{it.removeAt(index)};onPlaylist(items,target)}){Icon(Icons.Outlined.Close,"Remove",tint=ink.copy(alpha=.65f))}}}
            Spacer(Modifier.height(8.dp));Text("Change every ${formatInterval(interval)}",color=ink,fontWeight=FontWeight.Medium)
            Slider(value=interval.toFloat(),onValueChange={interval=it.toLong().coerceIn(10,86400)},valueRange=10f..86400f,steps=95,onValueChangeFinished={if(items.isNotEmpty())onPlaylist(items,target)})
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(60L to "1m",300L to "5m",1800L to "30m",3600L to "1h",21600L to "6h").forEach{(s,label)->FilterChip(selected=interval==s,onClick={interval=s;if(items.isNotEmpty())onPlaylist(items,target)},label={Text(label)},modifier=Modifier.weight(1f))}}
        }}
    }
}
private fun formatInterval(seconds:Long):String=when{seconds<60->"${seconds}s";seconds%3600L==0L->"${seconds/3600L}h";else->"${seconds/60L}m"}