package com.l1vo.oslauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import java.util.Locale

private val L1voGreen = Color(0xFF3E6B34)
private val L1voInk = Color(0xFF1E2B27)
private val L1voPanel = Color(0xFFF4F4E9)
private const val PREFS = "l1vo_launcher"
private const val WALLPAPER = "wallpaper"
private const val DARK_THEME = "dark_theme"
private const val DYNAMIC_LIGHTING = "dynamic_lighting"
private const val LEAU_PACKAGE = "com.liv.ol1viapa"

data class LaunchableApp(val label: String, val packageName: String, val intent: Intent, val icon: Bitmap)
data class QuickSlot(val id: String, val label: String, val packageName: String?, val kind: SlotKind)
enum class SlotKind { HOME, SETTINGS, GALLERY, CALLS, APP }
data class WallpaperState(val uri: String?, val bitmap: Bitmap?, val tone: Color)
enum class Page { HOME, HUB, SEARCH, STEM, LEAU, LIBRARY, GALLERY, PHONE, LEACHER, BLOOM }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT)
        window.setNavigationBarColor(android.graphics.Color.TRANSPARENT)
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.rgb(233, 232, 217)))
        setContent { L1voLauncherApp() }
    }
}

@Composable
private fun L1voLauncherApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var page by remember { mutableStateOf(Page.HOME) }
    var editingSlot by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }
    var darkTheme by remember { mutableStateOf(prefs.getBoolean(DARK_THEME, false)) }
    var dynamicLighting by remember { mutableStateOf(prefs.getBoolean(DYNAMIC_LIGHTING, true)) }
    var wallpaper by remember { mutableStateOf(prefs.getString(WALLPAPER, null)) }

    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        val value = uri.toString()
        prefs.edit().putString(WALLPAPER, value).apply()
        wallpaper = value
    }

    val apps = remember(refresh) { loadApps(context) }
    val slots = remember(refresh) { loadQuickSlots(context) }
    val wallpaperState = rememberWallpaper(context, wallpaper)
    val base = if (darkTheme) darkColorScheme() else lightColorScheme()
    val colors = if (dynamicLighting) base.copy(primary = wallpaperState.tone, secondary = wallpaperState.tone, tertiary = wallpaperState.tone) else base

    MaterialTheme(colorScheme = colors) {
        Box(Modifier.fillMaxSize()) {
            WallpaperBackground(wallpaperState)
            when (page) {
                Page.HOME -> HomeScreen(slots, { page = Page.HUB }, { launchLeau(context) }, { page = Page.STEM }, { wallpaperPicker.launch(arrayOf("image/*")) }, { editingSlot = it })
                Page.HUB -> AppHub(apps, { page = Page.HOME }, { page = Page.SEARCH }, { launchLeau(context) }, { openLauncherApp(context, it) }, { page = it }, { wallpaperPicker.launch(arrayOf("image/*")) })
                Page.SEARCH -> SearchHub(apps, { page = Page.HUB }, { openLauncherApp(context, it) })
                Page.STEM -> StemSettings(darkTheme, dynamicLighting, { page = Page.HUB }, { darkTheme = it; prefs.edit().putBoolean(DARK_THEME, it).apply() }, { dynamicLighting = it; prefs.edit().putBoolean(DYNAMIC_LIGHTING, it).apply() }, { wallpaperPicker.launch(arrayOf("image/*")) })
                Page.LEAU -> InternalPlaceholder("LEAU", "Your L1vo companion", Icons.Outlined.Eco) { page = Page.HUB }
                Page.LIBRARY -> InternalPlaceholder("L1VO LIBRARY", "Your saved L1vo collection", Icons.Outlined.Collections) { page = Page.HUB }
                Page.GALLERY -> InternalPlaceholder("L1VO GALLERY", "Your visual space", Icons.Outlined.Collections) { page = Page.HUB }
                Page.PHONE -> PhoneHub(context) { page = Page.HUB }
                Page.LEACHER -> Leacher(context) { page = Page.HUB }
                Page.BLOOM -> BloomStore { page = Page.HUB }
            }
            if (editingSlot != null) {
                SlotPicker(apps, { editingSlot = null }) { app ->
                    saveSlot(context, editingSlot!!, app)
                    editingSlot = null
                    refresh++
                }
            }
        }
    }
}

@Composable
private fun rememberWallpaper(context: Context, value: String?): WallpaperState {
    var bitmap by remember(value) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(value) {
        bitmap = value?.let { uriString ->
            runCatching { context.contentResolver.openInputStream(Uri.parse(uriString))?.use { android.graphics.BitmapFactory.decodeStream(it) } }.getOrNull()
        }
    }
    val tone = remember(bitmap) {
        bitmap?.let {
            runCatching {
                val tiny = Bitmap.createScaledBitmap(it, 1, 1, true)
                val px = tiny.getPixel(0, 0)
                Color(android.graphics.Color.red(px), android.graphics.Color.green(px), android.graphics.Color.blue(px))
            }.getOrDefault(L1voGreen)
        } ?: L1voGreen
    }
    return WallpaperState(value, bitmap, tone)
}

@Composable
private fun WallpaperBackground(state: WallpaperState) {
    Box(Modifier.fillMaxSize().background(if (state.bitmap == null) Color(0xFFE9E8D9) else state.tone.copy(alpha = .10f))) {
        state.bitmap?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = if (state.bitmap == null) .02f else .16f)))
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit, trailing: @Composable (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 12.dp, top = 10.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(onClick = onBack, modifier = Modifier.size(52.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = .88f), shadowElevation = 2.dp) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(25.dp)) }
        }
        Text(title, modifier = Modifier.weight(1f).padding(start = 12.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        trailing?.invoke()
    }
}

@Composable
private fun HomeScreen(slots: List<QuickSlot>, onHub: () -> Unit, onLeau: () -> Unit, onStem: () -> Unit, onWallpaper: () -> Unit, onEdit: (String) -> Unit) {
    val context = LocalContext.current
    val time = remember { java.text.SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp), horizontalArrangement = Arrangement.End) {
                Text(time.format(java.util.Date()), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .90f)), shape = RoundedCornerShape(34.dp), elevation = CardDefaults.cardElevation(10.dp), modifier = Modifier.size(250.dp).padding(4.dp)) {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.weight(1f).fillMaxWidth()) { HomeTile(slots[0], Modifier.weight(1f), context); HomeTile(slots[1], Modifier.weight(1f), context) }
                    Row(Modifier.weight(1f).fillMaxWidth()) { HomeTile(slots[2], Modifier.weight(1f), context); HomeTile(slots[3], Modifier.weight(1f), context) }
                }
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 26.dp)) {
                ActionCircle(Icons.Outlined.Apps, "App Hub", onHub)
                LeauButton(onLeau)
                ActionCircle(Icons.Outlined.Wallpaper, "Wallpaper", onWallpaper)
                ActionCircle(Icons.Outlined.Tune, "STEM", onStem)
            }
        }
    }
}

@Composable
private fun HomeTile(slot: QuickSlot, modifier: Modifier, context: Context) {
    val icon = when (slot.kind) {
        SlotKind.HOME -> Icons.Outlined.Home
        SlotKind.SETTINGS -> Icons.Outlined.Settings
        SlotKind.GALLERY -> Icons.Outlined.Collections
        SlotKind.CALLS -> Icons.Outlined.Call
        SlotKind.APP -> Icons.Outlined.Apps
    }
    Box(modifier.clickable { openSlot(context, slot) }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .10f), modifier = Modifier.size(58.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, slot.label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp)) }
            }
            Spacer(Modifier.height(7.dp))
            Text(slot.label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LeauButton(onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.size(66.dp), shape = CircleShape, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .96f), shadowElevation = 8.dp) {
        Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Eco, "Leau", tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(31.dp)) }
    }
}

@Composable
private fun ActionCircle(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = .92f), shadowElevation = 3.dp) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp)) }
    }
}

@Composable
private fun AppHub(apps: List<LaunchableApp>, onBack: () -> Unit, onSearch: () -> Unit, onLeau: () -> Unit, onOpen: (LaunchableApp) -> Unit, onInternal: (Page) -> Unit, onWallpaper: () -> Unit) {
    val favorites = apps.take(8)
    val system = systemApps(apps)
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 4.dp)) {
        TopBar("APP HUB", onBack) { IconButton(onClick = onWallpaper) { Icon(Icons.Outlined.Wallpaper, "Wallpaper") } }
        Surface(onClick = onSearch, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .94f), shadowElevation = 2.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 18.dp)) { Icon(Icons.Outlined.Search, "Search Hub", modifier = Modifier.size(22.dp)); Text("Search Hub", modifier = Modifier.padding(start = 10.dp), fontWeight = FontWeight.SemiBold) }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            L1voAppsCube(Modifier.weight(1f), onInternal, onLeau)
            SystemCube(system, Modifier.weight(1f), onOpen)
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle("FAVORITES")
        AppGrid(favorites, onOpen, 196.dp)
        Spacer(Modifier.height(18.dp))
        SectionTitle("ALL APPS")
        AppGrid(apps, onOpen, (((apps.size + 3) / 4).coerceAtLeast(1) * 92).coerceAtMost(900).dp)
        Spacer(Modifier.height(18.dp))
        Surface(onClick = onLeau, modifier = Modifier.align(Alignment.CenterHorizontally).size(58.dp), shape = CircleShape, color = MaterialTheme.colorScheme.onSurface, shadowElevation = 7.dp) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Eco, "Leau", tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(28.dp)) }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable private fun SectionTitle(title: String) { Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) }

@Composable
private fun L1voAppsCube(modifier: Modifier, onInternal: (Page) -> Unit, onLeau: () -> Unit) {
    val entries = listOf(
        Triple("STEM", Icons.Outlined.Eco, Page.STEM), Triple("LEAU", Icons.Outlined.Eco, Page.LEAU), Triple("LIBRARY", Icons.Outlined.Collections, Page.LIBRARY),
        Triple("GALLERY", Icons.Outlined.Collections, Page.GALLERY), Triple("PHONE", Icons.Outlined.Call, Page.PHONE), Triple("LEACHER", Icons.Outlined.Search, Page.LEACHER), Triple("BLOOM", Icons.Outlined.Storefront, Page.BLOOM)
    )
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .94f)), shape = RoundedCornerShape(28.dp), modifier = modifier.height(250.dp)) {
        Column(Modifier.fillMaxSize().padding(13.dp)) {
            Text("L1vo Apps", fontWeight = FontWeight.Bold)
            LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxSize(), userScrollEnabled = false, contentPadding = PaddingValues(top = 8.dp)) {
                items(entries) { entry ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable { if (entry.third == Page.LEAU) onLeau() else onInternal(entry.third) }.padding(5.dp)) {
                        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .12f), modifier = Modifier.size(46.dp)) { Box(contentAlignment = Alignment.Center) { Icon(entry.second, entry.first, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) } }
                        Text(entry.first, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemCube(apps: List<LaunchableApp>, modifier: Modifier, onOpen: (LaunchableApp) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .94f)), shape = RoundedCornerShape(28.dp), modifier = modifier.height(250.dp)) {
        Column(Modifier.fillMaxSize().padding(13.dp)) { Text("System", fontWeight = FontWeight.Bold); AppGrid(apps.take(6), onOpen, 206.dp, 3, 4.dp) }
    }
}

@Composable
private fun AppGrid(apps: List<LaunchableApp>, onOpen: (LaunchableApp) -> Unit, height: Dp, columns: Int = 4, padding: Dp = 8.dp) {
    LazyVerticalGrid(columns = GridCells.Fixed(columns), modifier = Modifier.fillMaxWidth().height(height), userScrollEnabled = false, contentPadding = PaddingValues(padding)) { items(apps, key = { it.packageName }) { AppIcon(it, onOpen) } }
}

@Composable
private fun AppIcon(app: LaunchableApp, onOpen: (LaunchableApp) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(5.dp)) {
        Surface(onClick = { onOpen(app) }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp, modifier = Modifier.size(58.dp)) { Image(app.icon.asImageBitmap(), app.label, modifier = Modifier.padding(8.dp), contentScale = ContentScale.Fit) }
        Spacer(Modifier.height(5.dp))
        Text(app.label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun SearchHub(apps: List<LaunchableApp>, onBack: () -> Unit, onOpen: (LaunchableApp) -> Unit) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val filtered = apps.filter { it.label.contains(query.text, true) }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 4.dp)) {
        TopBar("SEARCH HUB", onBack)
        OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text("Search apps") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp))
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) { items(filtered, key = { it.packageName }) { AppIcon(it, onOpen) } }
    }
}

@Composable
private fun StemSettings(darkTheme: Boolean, dynamicLighting: Boolean, onBack: () -> Unit, onDarkTheme: (Boolean) -> Unit, onDynamicLighting: (Boolean) -> Unit, onWallpaper: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        TopBar("STEM", onBack)
        Text("L1vo settings", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
        SettingRow("Dark theme", "Bright readable text is preserved", darkTheme, onDarkTheme)
        SettingRow("Dynamic lighting", "Let apps react to the current wallpaper", dynamicLighting, onDynamicLighting)
        Surface(onClick = onWallpaper, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .94f)) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Wallpaper, null); Column(Modifier.padding(start = 14.dp)) { Text("Wallpaper", fontWeight = FontWeight.Bold); Text("Choose a new background", style = MaterialTheme.typography.bodySmall) } }
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .94f)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodySmall) }; Switch(checked, onCheckedChange = onChecked) }
    }
}

@Composable
private fun InternalPlaceholder(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp)) {
        TopBar(title, onBack)
        Spacer(Modifier.height(40.dp))
        Surface(shape = RoundedCornerShape(34.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .94f), modifier = Modifier.fillMaxWidth().height(260.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp)); Spacer(Modifier.height(14.dp)); Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp)) }
        }
    }
}

@Composable
private fun PhoneHub(context: Context, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp)) {
        TopBar("L1VO PHONE", onBack)
        Surface(onClick = { launch(context, Intent(Intent.ACTION_DIAL)) }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .94f)) { Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Call, null); Text("Open phone", modifier = Modifier.padding(start = 14.dp), fontWeight = FontWeight.Bold) } }
        Spacer(Modifier.height(12.dp))
        Surface(onClick = { launchContacts(context) }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .94f)) { Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Call, null); Text("Open contacts", modifier = Modifier.padding(start = 14.dp), fontWeight = FontWeight.Bold) } }
    }
}

@Composable
private fun Leacher(context: Context, onBack: () -> Unit) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val links = listOf(
        Triple("Google", Icons.Outlined.Language) { openPackageOrStore(context, listOf("com.google.android.googlequicksearchbox"), "Google") },
        Triple("Chrome", Icons.Outlined.Language) { openPackageOrStore(context, listOf("com.android.chrome"), "Chrome") },
        Triple("Play Store", Icons.Outlined.Storefront) { openPlayStore(context, "") },
        Triple("Opera", Icons.Outlined.Language) { openPackageOrStore(context, listOf("com.opera.browser", "com.opera.mini.native"), "Opera") },
        Triple("Brave", Icons.Outlined.Language) { openPackageOrStore(context, listOf("com.brave.browser"), "Brave") }
    )
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        TopBar("LEACHER", onBack, trailing = { IconButton(onClick = {}) { Icon(Icons.Outlined.Tune, "Leacher settings") } })
        Spacer(Modifier.height(22.dp))
        Surface(shape = RoundedCornerShape(36.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .94f), modifier = Modifier.fillMaxWidth().height(180.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Eco, "Leacher", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(96.dp)) } }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text("Search...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp))
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            links.forEach { (label, icon, action) -> Surface(onClick = action, shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = .92f), modifier = Modifier.size(52.dp), shadowElevation = 2.dp) { Box(contentAlignment = Alignment.Center) { Icon(icon, label, modifier = Modifier.size(24.dp)) } } }
        }
        Spacer(Modifier.height(18.dp))
        Text("Leacher search is coming soon.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun BloomStore(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        TopBar("BLOOM STORE", onBack)
        Surface(shape = RoundedCornerShape(34.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .94f), modifier = Modifier.fillMaxWidth().height(210.dp)) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text("✿", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary); Text("Bloom", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("The L1vo app store", style = MaterialTheme.typography.bodyMedium) } }
        Spacer(Modifier.height(14.dp))
        Text("Featured", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Store functionality will grow here.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun SlotPicker(apps: List<LaunchableApp>, onDismiss: () -> Unit, onSelect: (LaunchableApp) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Choose an app") }, text = {
        Column(Modifier.height(420.dp).verticalScroll(rememberScrollState())) {
            apps.forEach { app -> Row(Modifier.fillMaxWidth().clickable { onSelect(app) }.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) { Image(app.icon.asImageBitmap(), null, Modifier.size(42.dp)); Spacer(Modifier.width(14.dp)); Text(app.label, fontWeight = FontWeight.Medium) } }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

private fun loadQuickSlots(context: Context): List<QuickSlot> {
    val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    fun custom(id: String, fallbackLabel: String, kind: SlotKind): QuickSlot { val pkg = p.getString("slot_$id", null); return if (pkg == null) QuickSlot(id, fallbackLabel, null, kind) else QuickSlot(id, appLabel(context, pkg), pkg, SlotKind.APP) }
    return listOf(QuickSlot("home", "Home", null, SlotKind.HOME), custom("settings", "STEM", SlotKind.SETTINGS), custom("gallery", "Gallery", SlotKind.GALLERY), custom("calls", "Phone", SlotKind.CALLS))
}

private fun saveSlot(context: Context, id: String, app: LaunchableApp) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("slot_$id", app.packageName).apply() }

private fun openSlot(context: Context, slot: QuickSlot) {
    when {
        slot.packageName != null -> context.packageManager.getLaunchIntentForPackage(slot.packageName)?.let { launch(context, it) }
        slot.kind == SlotKind.SETTINGS -> launch(context, Intent(Settings.ACTION_SETTINGS))
        slot.kind == SlotKind.GALLERY -> launch(context, Intent(Intent.ACTION_VIEW).apply { type = "image/*" })
        slot.kind == SlotKind.CALLS -> launch(context, Intent(Intent.ACTION_DIAL))
    }
}

private fun openLauncherApp(context: Context, app: LaunchableApp) { recordUse(context, app.packageName); launch(context, app.intent) }
private fun recordUse(context: Context, packageName: String) { val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE); prefs.edit().putInt("usage_$packageName", prefs.getInt("usage_$packageName", 0) + 1).apply() }

private fun systemApps(apps: List<LaunchableApp>): List<LaunchableApp> {
    val preferred = listOf("com.android.settings", "com.android.dialer", "com.google.android.dialer", "com.android.camera2", "com.android.camera", "com.google.android.apps.messaging", "com.android.mms")
    val result = preferred.mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }.toMutableList()
    if (result.size < 6) result += apps.filterNot { a -> result.any { it.packageName == a.packageName } }.take(6 - result.size)
    return result
}

private fun loadApps(context: Context): List<LaunchableApp> {
    val pm = context.packageManager
    val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    return pm.queryIntentActivities(query, PackageManager.MATCH_ALL).mapNotNull { info ->
        val pkg = info.activityInfo.packageName
        if (pkg == context.packageName) return@mapNotNull null
        val label = info.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val icon = info.loadIcon(pm)?.let { drawableToBitmap(it) } ?: return@mapNotNull null
        LaunchableApp(label, pkg, Intent(query).setClassName(pkg, info.activityInfo.name), icon)
    }.distinctBy { it.packageName }.sortedWith(compareByDescending<LaunchableApp> { prefs.getInt("usage_${it.packageName}", 0) }.thenBy { it.label.lowercase(Locale.getDefault()) })
}

private fun launchLeau(context: Context) {
    val pm = context.packageManager
    val intent = Intent("com.liv.ol1viapa.OPEN_ASSISTANT").setPackage(LEAU_PACKAGE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }.onFailure {
        pm.getLaunchIntentForPackage(LEAU_PACKAGE)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)?.let { context.startActivity(it) }
            ?: android.widget.Toast.makeText(context, "Leau Assistant is not installed yet", android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun launchContacts(context: Context) { runCatching { launch(context, Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("content://contacts/people") }) }.onFailure { launch(context, Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("content://contacts") }) } }

private fun openPackageOrStore(context: Context, packages: List<String>, query: String) {
    packages.firstNotNullOfOrNull { context.packageManager.getLaunchIntentForPackage(it) }?.let { launch(context, it); return }
    openPlayStore(context, query)
}

private fun openPlayStore(context: Context, query: String) {
    val uri = Uri.parse(if (query.isBlank()) "market://details?id=com.android.vending" else "market://search?q=${Uri.encode(query)}")
    runCatching { launch(context, Intent(Intent.ACTION_VIEW, uri)) }.onFailure { launch(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store"))) }
}

private fun launch(context: Context, intent: Intent) { runCatching { context.startActivity(Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.onFailure { android.widget.Toast.makeText(context, "Unable to open app", android.widget.Toast.LENGTH_SHORT).show() } }
private fun appLabel(context: Context, packageName: String): String = runCatching { context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(packageName, 0)).toString() }.getOrDefault("App")
private fun drawableToBitmap(drawable: Drawable): Bitmap = drawable.toBitmap(96, 96, Bitmap.Config.ARGB_8888)
