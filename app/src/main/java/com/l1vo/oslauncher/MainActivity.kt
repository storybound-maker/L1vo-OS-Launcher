package com.l1vo.oslauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import java.util.Locale

private val L1voGreen = Color(0xFF3E6B34)
private val L1voInk = Color(0xFF1E2B27)
private val L1voPanel = Color(0xFFF4F4E9)
private const val PREFS = "l1vo_launcher"
private const val WALLPAPER = "wallpaper"
private const val LEAU_PACKAGE = "com.liv.ol1viapa"
private const val ANIMATIONS = "animations"

data class LaunchableApp(val label: String, val packageName: String, val intent: Intent, val icon: Bitmap)
data class QuickSlot(val id: String, val label: String, val packageName: String?, val kind: SlotKind)
enum class SlotKind { HOME, SETTINGS, GALLERY, CALLS, APP }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT)
        setContent { L1voLauncherApp() }
    }
}

@Composable
private fun L1voLauncherApp() {
    val context = LocalContext.current
    var page by remember { mutableStateOf("home") }
    var editingSlot by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var wallpaper by remember { mutableStateOf(prefs.getString(WALLPAPER, null)) }
    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        prefs.edit().putString(WALLPAPER, uri.toString()).apply()
        wallpaper = uri.toString()
    }
    val apps = remember(refresh) { loadApps(context) }
    val slots = remember(refresh) { loadQuickSlots(context) }
    Surface(Modifier.fillMaxSize(), color = L1voPanel) {
        WallpaperBackground(wallpaper)
        when (page) {
            "hub" -> AppHub(apps, onBack = { page = "home" }, onLeau = { launchLeau(context) }, onWallpaper = { wallpaperPicker.launch("image/*") }, onOpen = { launch(context, it.intent) })
            "settings" -> L1voSettings(prefs, { page = "home" }, { wallpaperPicker.launch("image/*") }, { editingSlot = it }, { launch(context, Intent(Settings.ACTION_SETTINGS)) }, { launch(context, Intent(Settings.ACTION_HOME_SETTINGS)) })
            else -> HomeScreen(slots, { page = "hub" }, { page = "settings" }, { launchLeau(context) }, { wallpaperPicker.launch("image/*") }, { editingSlot = it })
        }
        editingSlot?.let { id -> SlotPicker(apps, { editingSlot = null }) { app -> saveSlot(context, id, app); editingSlot = null; refresh++ } }
    }
}

@Composable
private fun WallpaperBackground(value: String?) {
    val context = LocalContext.current
    var bitmap by remember(value) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(value) { bitmap = value?.let { runCatching { context.contentResolver.openInputStream(Uri.parse(it))?.use { s -> android.graphics.BitmapFactory.decodeStream(s) } }.getOrNull() } }
    Box(Modifier.fillMaxSize().background(Color(0xFFE9E8D9))) {
        bitmap?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = if (bitmap == null) .04f else .10f)))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreen(slots: List<QuickSlot>, onHub: () -> Unit, onSettings: () -> Unit, onLeau: () -> Unit, onWallpaper: () -> Unit, onEdit: (String) -> Unit) {
    val context = LocalContext.current
    val time = remember { java.text.SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).windowInsetsPadding(WindowInsets.navigationBars)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp), horizontalArrangement = Arrangement.End) { Text(time.format(java.util.Date()), color = L1voInk, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium) }
            Spacer(Modifier.weight(1f))
            Card(colors = CardDefaults.cardColors(containerColor = L1voPanel.copy(alpha = .90f)), shape = RoundedCornerShape(34.dp), elevation = CardDefaults.cardElevation(10.dp), modifier = Modifier.size(250.dp).padding(4.dp)) {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.weight(1f).fillMaxWidth()) { HomeTile(slots[0], Modifier.weight(1f), onEdit, context, onSettings); HomeTile(slots[1], Modifier.weight(1f), onEdit, context, onSettings) }
                    Row(Modifier.weight(1f).fillMaxWidth()) { HomeTile(slots[2], Modifier.weight(1f), onEdit, context, onSettings); HomeTile(slots[3], Modifier.weight(1f), onEdit, context, onSettings) }
                }
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 26.dp)) { ActionCircle(Icons.Outlined.Apps, "App Hub", onHub); LeauButton(onLeau); ActionCircle(Icons.Outlined.Wallpaper, "Wallpaper", onWallpaper) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeTile(slot: QuickSlot, modifier: Modifier, onEdit: (String) -> Unit, context: Context, onSettings: () -> Unit) {
    val icon = when (slot.kind) { SlotKind.HOME -> Icons.Outlined.Home; SlotKind.SETTINGS -> Icons.Outlined.Settings; SlotKind.GALLERY -> Icons.Outlined.Collections; SlotKind.CALLS -> Icons.Outlined.Call; SlotKind.APP -> Icons.Outlined.Apps }
    Box(modifier.combinedClickable(onClick = { if (slot.kind == SlotKind.SETTINGS && slot.packageName == null) onSettings() else openSlot(context, slot) }, onLongClick = { if (slot.kind != SlotKind.HOME) onEdit(slot.id) }), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = .32f), modifier = Modifier.size(58.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, slot.label, tint = L1voGreen, modifier = Modifier.size(30.dp)) } }
            Spacer(Modifier.height(7.dp)); Text(slot.label, color = L1voInk, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun LeauButton(onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "leau-button")
    val pulse by transition.animateFloat(.94f, 1.06f, infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    Surface(onClick = onClick, modifier = Modifier.size(66.dp).scale(pulse), shape = CircleShape, color = L1voInk.copy(alpha = .96f), shadowElevation = 8.dp) { Box(contentAlignment = Alignment.Center) { Text("⌒  ⌒", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) } }
}

@Composable
private fun ActionCircle(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) { Surface(onClick = onClick, modifier = Modifier.size(48.dp), shape = CircleShape, color = L1voPanel.copy(alpha = .92f), shadowElevation = 3.dp) { Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = L1voInk, modifier = Modifier.size(22.dp)) } } }

@Composable
private fun L1voSettings(prefs: android.content.SharedPreferences, onBack: () -> Unit, onWallpaper: () -> Unit, onCube: (String) -> Unit, onAndroidSettings: () -> Unit, onDefaultLauncher: () -> Unit) {
    var animations by remember { mutableStateOf(prefs.getBoolean(ANIMATIONS, true)) }
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).windowInsetsPadding(WindowInsets.navigationBars).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = L1voInk) }
            Column(Modifier.weight(1f)) { Text("L1vo", color = L1voInk, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Light); Text("Settings", color = L1voGreen, style = MaterialTheme.typography.titleMedium) }
            Icon(Icons.Outlined.Settings, "Settings", tint = L1voGreen, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(18.dp))
        SettingsSection("Appearance", Icons.Outlined.Palette) {
            SettingsRow("Wallpaper", "Choose the background used across L1vo", Icons.Outlined.Wallpaper, onWallpaper)
            SettingsRow("Launcher appearance", "L1vo visual style and surface treatment", Icons.Outlined.Visibility) { }
        }
        SettingsSection("Home & Cube", Icons.Outlined.Tune) {
            SettingsRow("Customize Settings slot", "Choose what the upper-right cube section opens", Icons.Outlined.Settings) { onCube("settings") }
            SettingsRow("Customize Gallery slot", "Choose what the lower-left cube section opens", Icons.Outlined.Collections) { onCube("gallery") }
            SettingsRow("Customize Calls slot", "Choose what the lower-right cube section opens", Icons.Outlined.Call) { onCube("calls") }
        }
        SettingsSection("Leau", Icons.Outlined.Eco) {
            SettingsRow("Leau integration", "Open Leau Assistant from the launcher", Icons.Outlined.Eco) { }
            SettingsRow("Leau controls", "Voice and assistant behavior will live here", Icons.Outlined.Tune) { }
        }
        SettingsSection("Launcher", Icons.Outlined.Home) {
            SettingsRow("Default launcher", "Choose L1vo as the device home app", Icons.Outlined.Home, onDefaultLauncher)
            SettingsRow("App Hub", "Manage the launcher application hub", Icons.Outlined.Apps) { }
        }
        SettingsSection("Accessibility", Icons.Outlined.Visibility) {
            SettingsRow("Animations", if (animations) "Enabled" else "Reduced", Icons.Outlined.Visibility) { animations = !animations; prefs.edit().putBoolean(ANIMATIONS, animations).apply() }
            SettingsRow("Android accessibility", "Open system accessibility controls", Icons.Outlined.Visibility, onAndroidSettings)
        }
        SettingsSection("About", Icons.Outlined.Info) { SettingsRow("About L1vo", "Launcher version and project information", Icons.Outlined.Info) { } }
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun SettingsSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)) { Icon(icon, title, tint = L1voGreen, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(9.dp)); Text(title.uppercase(Locale.getDefault()), color = L1voGreen, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold) }
        Card(colors = CardDefaults.cardColors(containerColor = L1voPanel.copy(alpha = .94f)), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth(), content = content) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = .55f), modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, title, tint = L1voInk, modifier = Modifier.size(22.dp)) } }
            Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) { Text(title, color = L1voInk, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium); Spacer(Modifier.height(2.dp)); Text(subtitle, color = L1voInk.copy(alpha = .62f), style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun AppHub(apps: List<LaunchableApp>, onBack: () -> Unit, onLeau: () -> Unit, onWallpaper: () -> Unit, onOpen: (LaunchableApp) -> Unit) {
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).windowInsetsPadding(WindowInsets.navigationBars).verticalScroll(scroll).padding(horizontal = 22.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = L1voInk) }; Text("APP HUB", color = L1voInk, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Light, modifier = Modifier.weight(1f)); IconButton(onClick = onWallpaper) { Icon(Icons.Outlined.Wallpaper, "Wallpaper", tint = L1voGreen) } }
        Spacer(Modifier.height(14.dp)); AppPanel(apps.take(8), onOpen); Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) { CategoryPanel("System", listOf(Icons.Outlined.Settings, Icons.Outlined.Home, Icons.Outlined.Wallpaper, Icons.Outlined.Apps), Modifier.weight(1f)); CategoryPanel("L1vo", listOf(Icons.Outlined.Eco, Icons.Outlined.Call, Icons.Outlined.Apps, Icons.Outlined.Collections), Modifier.weight(1f)) }
        Spacer(Modifier.height(16.dp)); AppPanel(apps.drop(8), onOpen); Spacer(Modifier.height(18.dp))
        Surface(onClick = onLeau, modifier = Modifier.align(Alignment.CenterHorizontally).size(58.dp), shape = CircleShape, color = L1voInk, shadowElevation = 7.dp) { Box(contentAlignment = Alignment.Center) { Text("⌒  ⌒", color = Color.White, fontWeight = FontWeight.Bold) } }; Spacer(Modifier.height(20.dp))
    }
}

@Composable private fun AppPanel(apps: List<LaunchableApp>, onOpen: (LaunchableApp) -> Unit) { Card(colors = CardDefaults.cardColors(containerColor = L1voPanel.copy(alpha = .94f)), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) { LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(((apps.size + 3) / 4 * 94).coerceAtMost(470).dp), contentPadding = PaddingValues(16.dp), userScrollEnabled = false) { items(apps, key = { it.packageName }) { AppIcon(it, onOpen) } } } }
@Composable private fun AppIcon(app: LaunchableApp, onOpen: (LaunchableApp) -> Unit) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(5.dp)) { Surface(onClick = { onOpen(app) }, shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.size(58.dp)) { Image(app.icon.asImageBitmap(), app.label, Modifier.padding(8.dp), contentScale = ContentScale.Fit) }; Spacer(Modifier.height(5.dp)); Text(app.label, color = L1voInk, style = MaterialTheme.typography.labelSmall, maxLines = 1) } }
@Composable private fun CategoryPanel(title: String, icons: List<androidx.compose.ui.graphics.vector.ImageVector>, modifier: Modifier) { Card(colors = CardDefaults.cardColors(containerColor = L1voPanel.copy(alpha = .94f)), shape = RoundedCornerShape(24.dp), modifier = modifier.height(190.dp)) { Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(title, color = L1voInk, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { icons.take(2).forEach { Icon(it, null, tint = L1voInk, modifier = Modifier.size(34.dp)) } }; Spacer(Modifier.height(20.dp)); Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { icons.drop(2).take(2).forEach { Icon(it, null, tint = L1voGreen, modifier = Modifier.size(34.dp)) } } } } }

@Composable private fun SlotPicker(apps: List<LaunchableApp>, onDismiss: () -> Unit, onSelect: (LaunchableApp) -> Unit) { AlertDialog(onDismissRequest = onDismiss, title = { Text("Choose an app") }, text = { Column(Modifier.height(420.dp).verticalScroll(rememberScrollState())) { apps.forEach { app -> Surface(onClick = { onSelect(app) }, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) { Image(app.icon.asImageBitmap(), null, Modifier.size(42.dp)); Spacer(Modifier.width(14.dp)); Text(app.label, color = L1voInk) } } } } }, confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) }

private fun loadQuickSlots(context: Context): List<QuickSlot> { val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE); fun custom(id: String, fallbackLabel: String, kind: SlotKind): QuickSlot { val pkg = p.getString("slot_$id", null); return if (pkg == null) QuickSlot(id, fallbackLabel, null, kind) else QuickSlot(id, appLabel(context, pkg), pkg, SlotKind.APP) }; return listOf(QuickSlot("home", "Home", null, SlotKind.HOME), custom("settings", "Settings", SlotKind.SETTINGS), custom("gallery", "Gallery", SlotKind.GALLERY), custom("calls", "Calls", SlotKind.CALLS)) }
private fun saveSlot(context: Context, id: String, app: LaunchableApp) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("slot_$id", app.packageName).apply() }
private fun openSlot(context: Context, slot: QuickSlot) { when { slot.packageName != null -> context.packageManager.getLaunchIntentForPackage(slot.packageName)?.let { launch(context, it) }; slot.kind == SlotKind.GALLERY -> launch(context, Intent(Intent.ACTION_VIEW).apply { type = "image/*" }); slot.kind == SlotKind.CALLS -> launch(context, Intent(Intent.ACTION_DIAL)) } }
private fun launchLeau(context: Context) { val pm = context.packageManager; val intent = Intent("com.liv.ol1viapa.OPEN_ASSISTANT").setPackage(LEAU_PACKAGE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); runCatching { context.startActivity(intent) }.onFailure { pm.getLaunchIntentForPackage(LEAU_PACKAGE)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)?.let { context.startActivity(it) } ?: android.widget.Toast.makeText(context, "Leau Assistant is not installed yet", android.widget.Toast.LENGTH_SHORT).show() } }
private fun launch(context: Context, intent: Intent) { runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.onFailure { android.widget.Toast.makeText(context, "Unable to open app", android.widget.Toast.LENGTH_SHORT).show() } }
private fun loadApps(context: Context): List<LaunchableApp> { val pm = context.packageManager; val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER); return pm.queryIntentActivities(query, PackageManager.MATCH_ALL).mapNotNull { info -> val label = info.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null; val icon = info.loadIcon(pm)?.let { drawableToBitmap(it) } ?: return@mapNotNull null; LaunchableApp(label, info.activityInfo.packageName, Intent(query).setClassName(info.activityInfo.packageName, info.activityInfo.name), icon) }.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) } }
private fun appLabel(context: Context, packageName: String): String = runCatching { context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(packageName, 0)).toString() }.getOrDefault("App")
private fun drawableToBitmap(drawable: Drawable): Bitmap = drawable.toBitmap(96, 96, Bitmap.Config.ARGB_8888)
