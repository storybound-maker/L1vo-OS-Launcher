package com.l1vo.oslauncher

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Movie
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
import kotlinx.coroutines.delay

@Composable
fun WallpaperBackground(value: String?, dark: Boolean, playlist: List<String> = emptyList(), intervalSeconds: Long = 3600L, useSystemWallpaper: Boolean = false) {
    val context = LocalContext.current
    val systemFallback = remember { runCatching { WallpaperManager.getInstance(context).drawable?.let { drawableToWallpaperBitmap(it) } }.getOrNull() }
    var activeUri by remember(value, playlist) { mutableStateOf(playlist.firstOrNull() ?: value) }
    LaunchedEffect(playlist, value, intervalSeconds) {
        val items = if (playlist.isNotEmpty()) playlist else listOfNotNull(value)
        if (items.isEmpty()) { activeUri = null; return@LaunchedEffect }
        var index = items.indexOfFirst { it == activeUri }.takeIf { it >= 0 } ?: 0
        activeUri = items[index]
        while (items.size > 1) {
            delay(intervalSeconds.coerceAtLeast(10L) * 1000L)
            index = (index + 1) % items.size
            activeUri = items[index]
        }
    }
    Box(Modifier.fillMaxSize().background(if (useSystemWallpaper) Color.Transparent else if (dark) L1voDark else Color(0xFFE9E8D9))) {
        if (!useSystemWallpaper) {
            val bitmap = activeUri?.let { uri ->
                remember(uri) { runCatching { context.contentResolver.openInputStream(Uri.parse(uri))?.use { android.graphics.BitmapFactory.decodeStream(it) } }.getOrNull() }
            } ?: systemFallback
            bitmap?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = if (dark) .12f else .04f)))
    }
}

private fun drawableToWallpaperBitmap(d: Drawable): Bitmap = d.toBitmap(1080, 1920, Bitmap.Config.ARGB_8888)

@Composable
fun rememberHomeAdaptiveInk(value: String?, darkFallback: Boolean): Color {
    val context = LocalContext.current
    val fallback = if (darkFallback) Color(0xFFE9F0E9) else L1voInk
    var ink by remember(value, darkFallback) { mutableStateOf(fallback) }
    LaunchedEffect(value, darkFallback) {
        val bitmap = runCatching {
            if (value == null) WallpaperManager.getInstance(context).drawable?.let { drawableToWallpaperBitmap(it) }
            else context.contentResolver.openInputStream(Uri.parse(value))?.use { android.graphics.BitmapFactory.decodeStream(it) }
        }.getOrNull()
        ink = bitmap?.let {
            val left = (it.width * 0.22f).toInt().coerceAtLeast(0); val right = (it.width * 0.78f).toInt().coerceAtMost(it.width); val bottom = (it.height * 0.42f).toInt().coerceAtMost(it.height)
            val stepX = ((right - left) / 18).coerceAtLeast(1); val stepY = (bottom / 12).coerceAtLeast(1)
            var total = 0L; var count = 0; var y = 0
            while (y < bottom) { var x = left; while (x < right) { val p = it.getPixel(x, y); val r = (p shr 16) and 0xFF; val g = (p shr 8) and 0xFF; val b = p and 0xFF; total += (0.2126 * r + 0.7152 * g + 0.0722 * b).toLong(); count++; x += stepX }; y += stepY }
            if (count > 0 && total.toDouble() / count > 150.0) L1voDeepGreen else Color(0xFFF2F7F2)
        } ?: fallback
    }
    return ink
}

@Composable
fun rememberAdaptiveInk(value: String?, darkFallback: Boolean): Color {
    val context = LocalContext.current
    val fallback = if (darkFallback) Color(0xFFE9F0E9) else L1voInk
    var ink by remember(value, darkFallback) { mutableStateOf(fallback) }
    LaunchedEffect(value, darkFallback) {
        val bitmap = runCatching { WallpaperManager.getInstance(context).drawable?.let { drawableToWallpaperBitmap(it) } }.getOrNull()
        ink = bitmap?.let {
            val w = it.width.coerceAtLeast(1); val h = it.height.coerceAtLeast(1); var total = 0L; var count = 0
            val stepX = (w / 24).coerceAtLeast(1); val stepY = (h / 24).coerceAtLeast(1); var y = 0
            while (y < h) { var x = 0; while (x < w) { val p = it.getPixel(x, y); val r = (p shr 16) and 0xFF; val g = (p shr 8) and 0xFF; val b = p and 0xFF; total += (0.2126 * r + 0.7152 * g + 0.0722 * b).toLong(); count++; x += stepX }; y += stepY }
            if (count > 0 && total.toDouble() / count > 150.0) L1voDeepGreen else Color(0xFFF2F7F2)
        } ?: fallback
    }
    return ink
}

@Composable
fun WallpaperStudio(ink: Color, onBack: () -> Unit, onSave: (String) -> Unit, onPlaylist: (List<String>) -> Unit, onLiveWallpaper: () -> Unit, playlist: List<String>, intervalSeconds: Long) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) { runCatching { context.contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; onSave(u.toString()) }
    }
    val multiPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            val saved = uris.mapNotNull { uri -> runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; uri.toString() }
            if (saved.isNotEmpty()) onPlaylist(saved)
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.offset(y = 8.dp).size(56.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ink) }
            Column(Modifier.weight(1f)) { Text("Wallpaper", color = ink, style = MaterialTheme.typography.headlineMedium); Text("Wallpaper Studio", color = L1voGreen) }
        }
        Spacer(Modifier.height(20.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .95f)), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Collections, "Photos", tint = L1voDeepGreen, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(14.dp)); Text("Choose your wallpaper", color = ink, style = MaterialTheme.typography.titleLarge)
                Text("Use one image, a rotating playlist, or an Android live wallpaper.", color = ink.copy(alpha = .65f))
                Spacer(Modifier.height(18.dp))
                Button(onClick = { picker.launch(arrayOf("image/*")) }) { Text("Choose photo") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { multiPicker.launch(arrayOf("image/*")) }) { Icon(Icons.Outlined.Collections, "Playlist", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Choose wallpaper playlist") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onLiveWallpaper) { Icon(Icons.Outlined.Movie, "Live wallpaper", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Choose live wallpaper") }
                Spacer(Modifier.height(14.dp))
                Text(if (playlist.isEmpty()) "No rotating playlist selected." else "${playlist.size} wallpapers in the playlist • changes every ${formatInterval(intervalSeconds)}", color = ink.copy(alpha = .7f), style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}
private fun formatInterval(seconds: Long): String = when { seconds < 60 -> "${seconds}s"; seconds % 3600L == 0L -> "${seconds / 3600L}h"; else -> "${seconds / 60L}m" }
