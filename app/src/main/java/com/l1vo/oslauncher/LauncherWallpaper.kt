package com.l1vo.oslauncher

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@Composable fun WallpaperBackground(value:String?,dark:Boolean){
    val c=LocalContext.current
    val systemFallback=remember{runCatching{android.app.WallpaperManager.getInstance(c).drawable?.let{drawableToWallpaperBitmap(it)}}.getOrNull()}
    var bitmap by remember(value){mutableStateOf(systemFallback)}
    LaunchedEffect(value){
        if(value==null){bitmap=systemFallback}
        else bitmap=runCatching{c.contentResolver.openInputStream(Uri.parse(value))?.use{android.graphics.BitmapFactory.decodeStream(it)}}.getOrNull()?:systemFallback
    }
    Box(Modifier.fillMaxSize().background(if(dark)L1voDark else Color(0xFFE9E8D9))){
        bitmap?.let{Image(it.asImageBitmap(),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)}
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=if(dark).12f else .04f)))
    }
}

private fun drawableToWallpaperBitmap(d:Drawable):Bitmap=d.toBitmap(1080,1920,Bitmap.Config.ARGB_8888)

@Composable fun WallpaperStudio(ink:Color,onBack:()->Unit,onSave:(String)->Unit){val c=LocalContext.current;val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){u->if(u!=null){runCatching{c.contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)};onSave(u.toString())}};Column(Modifier.fillMaxSize().padding(20.dp)){Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack,modifier=Modifier.offset(y=6.dp)){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)};Column(Modifier.weight(1f)){Text("Wallpaper",color=ink,style=MaterialTheme.typography.headlineMedium);Text("Wallpaper Studio",color=L1voGreen)}};Spacer(Modifier.height(20.dp));Card(colors=CardDefaults.cardColors(L1voPanel.copy(alpha=.95f)),shape=RoundedCornerShape(28.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Outlined.Collections,"Photos",tint=L1voDeepGreen,modifier=Modifier.size(48.dp));Spacer(Modifier.height(14.dp));Text("Choose your wallpaper",color=ink,style=MaterialTheme.typography.titleLarge);Text("Pick an image from your device. L1vo keeps its aspect ratio.",color=ink.copy(alpha=.65f));Spacer(Modifier.height(18.dp));Button(onClick={picker.launch(arrayOf("image/*"))},colors=ButtonDefaults.buttonColors(containerColor=L1voGreen)){Text("Choose photo")}}}}}
