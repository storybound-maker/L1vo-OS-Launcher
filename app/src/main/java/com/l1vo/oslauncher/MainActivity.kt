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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import java.util.Locale

private val L1voGreen = Color(0xFF3E6B34)
private val L1voInk = Color(0xFF1E2B27)
private val L1voPanel = Color(0xFFF4F4E9)
private val L1voDark = Color(0xFF101612)
private const val PREFS = "l1vo_launcher"
private const val WALLPAPER = "wallpaper"
private const val LEAU_PACKAGE = "com.liv.ol1viapa"
private const val ANIMATIONS = "animations"
private const val DARK_THEME = "dark_theme"
private const val FONT = "font"
private const val NOTIFICATIONS = "notifications"
private const val PILL_APP = "pill_app"
private const val LEAU_VOICE = "leau_voice"
private const val LEAU_AUTO_LISTEN = "leau_auto_listen"
private const val ACCOUNT_NAME = "account_name"

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
    var darkTheme by remember { mutableStateOf(prefs.getBoolean(DARK_THEME, false)) }
    val fontName = prefs.getString(FONT, "Sans") ?: "Sans"
    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        prefs.edit().putString(WALLPAPER, uri.toString()).apply()
        wallpaper = uri.toString()
    }
    val apps = remember(refresh) { loadApps(context) }
    val slots = remember(refresh) { loadQuickSlots(context) }
    val background = if (darkTheme) L1voDark else L1voPanel
    val ink = if (darkTheme) Color(0xFFE9F0E9) else L1voInk
    val fontFamily = when (fontName) { "Serif" -> FontFamily.Serif; "Mono" -> FontFamily.Monospace; else -> FontFamily.SansSerif }
    MaterialTheme(typography = MaterialTheme.typography.copy(
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = fontFamily),
        titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = fontFamily),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = fontFamily)
    )) {
        Surface(Modifier.fillMaxSize(), color = background) {
            WallpaperBackground(wallpaper, darkTheme)
            when (page) {
                "hub" -> AppHub(apps, ink, { page = "home" }, { launchLeau(context) }, { wallpaperPicker.launch("image/*") }, { launch(context, it.intent) })
                "settings" -> L1voSettings(
                    prefs = prefs,
                    darkTheme = darkTheme,
                    onThemeChanged = { value -> darkTheme = value; prefs.edit().putBoolean(DARK_THEME, value).apply() },
                    onFontChanged = { prefs.edit().putString(FONT, it).apply(); refresh++ },
                    onBack = { page = "home" },
                    onWallpaper = { wallpaperPicker.launch("image/*") },
                    onCube = { editingSlot = it },
                    onAndroidSettings = { launch(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    onDefaultLauncher = { launch(context, Intent(Settings.ACTION_HOME_SETTINGS)) }
                )
                else -> HomeScreen(slots, ink, { page = "hub" }, { page = "settings" }, { launchLeau(context) }, { wallpaperPicker.launch("image/*") }, { editingSlot = it })
            }
            editingSlot?.let { id -> SlotPicker(apps, { editingSlot = null }) { app -> saveSlot(context, id, app); editingSlot = null; refresh++ } }
        }
    }
}

@Composable
private fun WallpaperBackground(value: String?, darkTheme: Boolean) {
    val context = LocalContext.current
    var bitmap by remember(value) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(value) { bitmap = value?.let { runCatching { context.contentResolver.openInputStream(Uri.parse(it))?.use { s -> android.graphics.BitmapFactory.decodeStream(s) } }.getOrNull() } }
    Box(Modifier.fillMaxSize().background(if (darkTheme) L1voDark else Color(0xFFE9E8D9))) {
        bitmap?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = if (bitmap == null) .04f else .10f)))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreen(slots: List<QuickSlot>, ink: Color, onHub: () -> Unit, onSettings: () -> Unit, onLeau: () -> Unit, onWallpaper: () -> Unit, onEdit: (String) -> Unit) {
    val context = LocalContext.current
    val time = remember { java.text.SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).windowInsetsPadding(WindowInsets.navigationBars)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp), horizontalArrangement = Arrangement.End) { Text(time.format(java.util.Date()), color = ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium) }
            Spacer(Modifier.weight(1f))
            Card(colors = CardDefaults.cardColors(containerColor = L1voPanel.copy(alpha = .90f)), shape = RoundedCornerShape(34.dp), elevation = CardDefaults.cardElevation(10.dp), modifier = Modifier.size(250.dp).padding(4.dp)) {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.weight(1f).fillMaxWidth()) { HomeTile(slots[0], Modifier.weight(1f), onEdit, context, onSettings, ink); HomeTile(slots[1], Modifier.weight(1f), onEdit, context, onSettings, ink) }
                    Row(Modifier.weight(1f).fillMaxWidth()) { HomeTile(slots[2], Modifier.weight(1f), onEdit, context, onSettings, ink); HomeTile(slots[3], Modifier.weight(1f), onEdit, context, onSettings, ink) }
                }
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 26.dp)) { ActionCircle(Icons.Outlined.Apps, "App Hub", onHub, ink); LeauButton(onLeau); ActionCircle(Icons.Outlined.Wallpaper, "Wallpaper", onWallpaper, ink) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeTile(slot: QuickSlot, modifier: Modifier, onEdit: (String) -> Unit, context: Context, onSettings: () -> Unit, ink: Color) {
    val icon = when (slot.kind) { SlotKind.HOME -> Icons.Outlined.Home; SlotKind.SETTINGS -> Icons.Outlined.Settings; SlotKind.GALLERY -> Icons.Outlined.Collections; SlotKind.CALLS -> Icons.Outlined.Call; SlotKind.APP -> Icons.Outlined.Apps }
    Box(modifier.combinedClickable(onClick = { if (slot.kind == SlotKind.SETTINGS && slot.packageName == null) onSettings() else openSlot(context, slot) }, onLongClick = { if (slot.kind != SlotKind.HOME) onEdit(slot.id) }), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = .32f), modifier = Modifier.size(58.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, slot.label, tint = L1voGreen, modifier = Modifier.size(30.dp)) } }
            Spacer(Modifier.height(7.dp)); Text(slot.label, color = ink, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
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
private fun ActionCircle(icon: ImageVector, label: String, onClick: () -> Unit, ink: Color) { Surface(onClick = onClick, modifier = Modifier.size(48.dp), shape = CircleShape, color = L1voPanel.copy(alpha = .92f), shadowElevation = 3.dp) { Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = ink, modifier = Modifier.size(22.dp)) } } }

@Composable
private fun L1voSettings(
    prefs: android.content.SharedPreferences,
    darkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onFontChanged: (String) -> Unit,
    onBack: () -> Unit,
    onWallpaper: () -> Unit,
    onCube: (String) -> Unit,
    onAndroidSettings: () -> Unit,
    onDefaultLauncher: () -> Unit
) {
    val ink = if (darkTheme) Color(0xFFE9F0E9) else L1voInk
    var animations by remember { mutableStateOf(prefs.getBoolean(ANIMATIONS, true)) }
    var notifications by remember { mutableStateOf(prefs.getBoolean(NOTIFICATIONS, true)) }
    var pillApp by remember { mutableStateOf(prefs.getBoolean(PILL_APP, true)) }
    var leauVoice by remember { mutableStateOf(prefs.getBoolean(LEAU_VOICE, true)) }
    var autoListen by remember { mutableStateOf(prefs.getBoolean(LEAU_AUTO_LISTEN, false)) }
    var accountDialog by remember { mutableStateOf(false) }
    var fontDialog by remember { mutableStateOf(false) }
    var aboutDialog by remember { mutableStateOf(false) }
    val account = prefs.getString(ACCOUNT_NAME, null)
    val font = prefs.getString(FONT, "Sans") ?: "Sans"
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).windowInsetsPadding(WindowInsets.navigationBars).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ink) }
            Column(Modifier.weight(1f)) { Text("L1vo", color = ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Light); Text("Settings", color = L1voGreen, style = MaterialTheme.typography.titleMedium) }
            Icon(Icons.Outlined.Settings, "Settings", tint = L1voGreen, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(18.dp))

        SettingsSection("Account", Icons.Outlined.Person, ink) {
            SettingsRow("L1vo account", account ?: "Not signed in — tap to connect", Icons.Outlined.Person, ink) { accountDialog = true }
            SettingsRow("Notifications", if (notifications) "Launcher notifications are enabled" else "Launcher notifications are disabled", Icons.Outlined.Notifications, ink) { notifications = !notifications; prefs.edit().putBoolean(NOTIFICATIONS, notifications).apply() }
        }
        SettingsSection("Appearance", Icons.Outlined.Palette, ink) {
            SettingsToggleRow("Dark theme", if (darkTheme) "On" else "Off", Icons.Outlined.Palette, darkTheme, onThemeChanged, ink)
            SettingsRow("Wallpaper", "Choose the background used across L1vo", Icons.Outlined.Wallpaper, ink, onWallpaper)
            SettingsRow("App font", font, Icons.Outlined.TextFields, ink) { fontDialog = true }
        }
        SettingsSection("Home & Cube", Icons.Outlined.Tune, ink) {
            SettingsRow("Customize Settings slot", "Choose what the upper-right cube section opens", Icons.Outlined.Settings, ink) { onCube("settings") }
            SettingsRow("Customize Gallery slot", "Choose what the lower-left cube section opens", Icons.Outlined.Collections, ink) { onCube("gallery") }
            SettingsRow("Customize Calls slot", "Choose what the lower-right cube section opens", Icons.Outlined.Call, ink) { onCube("calls") }
        }
        SettingsSection("Leau", Icons.Outlined.Eco, ink) {
            SettingsToggleRow("Voice assistant", if (leauVoice) "Enabled" else "Disabled", Icons.Outlined.VolumeUp, leauVoice, { leauVoice = it; prefs.edit().putBoolean(LEAU_VOICE, it).apply() }, ink)
            SettingsToggleRow("Tap-to-listen", if (autoListen) "Enabled" else "Disabled", Icons.Outlined.Eco, autoListen, { autoListen = it; prefs.edit().putBoolean(LEAU_AUTO_LISTEN, it).apply() }, ink)
        }
        SettingsSection("Launcher", Icons.Outlined.Home, ink) {
            SettingsRow("Default launcher", "Choose L1vo as the device home app", Icons.Outlined.Home, ink, onDefaultLauncher)
            SettingsRow("App Hub", "Manage the launcher application hub", Icons.Outlined.Apps, ink) { }
        }
        SettingsSection("Accessibility", Icons.Outlined.Visibility, ink) {
            SettingsToggleRow("Animations", if (animations) "Enabled" else "Reduced", Icons.Outlined.Visibility, animations, { animations = it; prefs.edit().putBoolean(ANIMATIONS, it).apply() }, ink)
            SettingsToggleRow("Pill app", if (pillApp) "Enabled" else "Disabled", Icons.Outlined.Apps, pillApp, { pillApp = it; prefs.edit().putBoolean(PILL_APP, it).apply() }, ink)
            SettingsRow("Android accessibility", "Open system accessibility controls", Icons.Outlined.Visibility, ink, onAndroidSettings)
        }
        SettingsSection("About", Icons.Outlined.Info, ink) {
            SettingsRow("About L1vo", "Launcher version 0.1.0", Icons.Outlined.Info, ink) { aboutDialog = true }
        }
        Spacer(Modifier.height(22.dp))
    }

    if (accountDialog) AlertDialog(onDismissRequest = { accountDialog = false }, title = { Text("L1vo account") }, text = { Text(if (account == null) "You are using L1vo as a guest. Google sign-in and a normal L1vo account can be connected here when the account service is enabled." else "Signed in as $account.") }, confirmButton = { TextButton(onClick = { accountDialog = false }) { Text("Done") } }, dismissButton = { if (account == null) TextButton(onClick = { prefs.edit().putString(ACCOUNT_NAME, "L1vo User").apply(); accountDialog = false }) { Text("Create local account") } } })
    if (fontDialog) AlertDialog(onDismissRequest = { fontDialog = false }, title = { Text("App font") }, text = { Column { listOf("Sans", "Serif", "Mono").forEach { option -> TextButton(onClick = { onFontChanged(option); fontDialog = false }, modifier = Modifier.fillMaxWidth()) { Text(option) } } } }, confirmButton = { TextButton(onClick = { fontDialog = false }) { Text("Cancel") } })
    if (aboutDialog) AlertDialog(onDismissRequest = { aboutDialog = false }, title = { Text("L1vo") }, text = { Text("L1vo OS Launcher\nVersion 0.1.0\n\nA living launcher experience built around L1vo, Leau, the Home Cube and App Hub.") }, confirmButton = { TextButton(onClick = { aboutDialog = false }) { Text("Done") } })
}

@Composable
private fun SettingsSection(title: String, icon: ImageVector, ink: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)) { Icon(icon, title, tint = L1voGreen, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(9.dp)); Text(title.uppercase(Locale.getDefault()), color = L1voGreen, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold) }
        Card(colors = CardDefaults.cardColors(containerColor = L1voPanel.copy(alpha = .94f)), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth(), content = content) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, icon: ImageVector, ink: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = .55f), modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, title, tint = ink, modifier = Modifier.size(22.dp)) } }
            Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) { Text(title, color = ink, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium); Spacer(Modifier.height(2.dp)); Text(subtitle, color = ink.copy(alpha = .62f), style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun SettingsToggleRow(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit, ink: Color) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = Color.White.copy(alpha = .55f), modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, title, tint = ink, modifier = Modifier.size(22.dp)) } }
        Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) { Text(title, color = ink, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium); Spacer(Modifier.height(2.dp)); Text(subtitle, color = ink.copy(alpha = .62f), style = MaterialTheme.typography.bodySmall) }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AppHub(apps: List<LaunchableApp>, ink: Color, onBack: () -> Unit, onLeau: () -> Unit, onWallpaper: () -> Unit, onOpen: (LaunchableApp) -> Unit) {
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).windowInsetsPadding(WindowInsets.navigationBars).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ink) }; Text("APP HUB", color = ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Light, modifier = Modifier.weight(1f)); IconButton(onClick = onWallpaper) { Icon(Icons.Outlined.Wallpaper, "Wallpaper", tint = L1voGreen) } }
        Spacer(Modifier.height(14.dp)); AppPanel(apps.take(8), onOpen, ink); Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) { CategoryPanel("System", listOf(Icons.Outlined.Settings, Icons.Outlined.Home, Icons.Outlined.Wallpaper, Icons.Outlined.Apps), Modifier.weight(1f), ink); CategoryPanel("L1vo", listOf(Icons.Outlined.Eco, Icons.Outlined.Call, Icons.Outlined.Apps, Icons.Outlined.Collections), Modifier.weight(1f), ink) }
        Spacer(Modifier.height(16.dp)); AppPanel(apps.drop(8), onOpen, ink); Spacer(Modifier.height(18.dp))
        Surface(onClick = onLeau, modifier = Modifier.align(Alignment.CenterHorizontally).size(58.dp), shape = CircleShape, color = L1voInk, shadowElevation = 7.dp) { Box(contentAlignment = Alignment.Center) { Text("⌒  ⌒", color = Color.White, fontWeight = FontWeight.Bold) } }; Spacer(Modifier.height(20.dp))
    }
}

@Composable private fun AppPanel(apps: List<LaunchableApp>, onOpen: (LaunchableApp) -> Unit, ink: Color) { Card(colors = CardDefaults.cardColors(containerColor = L1voPanel.copy(alpha = .94f)), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) { LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(((apps.size + 3) / 4 * 94).coerceAtMost(470).dp), contentPadding = PaddingValues(16.dp), userScrollEnabled = false) { items(apps, key = { it.packageName }) { AppIcon(it, onOpen, ink) } } } }
@Composable private fun AppIcon(app: LaunchableApp, onOpen: (LaunchableApp) -> Unit, ink: Color) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(5.dp)) { Surface(onClick = { onOpen(app) }, shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.size(58.dp)) { Image(app.icon.asImageBitmap(), app.label, Modifier.padding(8.dp), contentScale = ContentScale.Fit) }; Spacer(Modifier.height(5.dp)); Text(app.label, color = ink, style = MaterialTheme.typography.labelSmall, maxLines = 1) } }
@Composable private fun CategoryPanel(title: String, icons: List<ImageVector>, modifier: Modifier, ink: Color) { Card(colors = CardDefaults.cardColors(containerColor = L1voPanel.copy(alpha = .94f)), shape = RoundedCornerShape(24.dp), modifier = modifier.height(190.dp)) { Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(title, color = ink, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { icons.take(2).forEach { Icon(it, null, tint = ink, modifier = Modifier.size(34.dp)) } }; Spacer(Modifier.height(20.dp)); Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { icons.drop(2).take(2).forEach { Icon(it, null, tint = L1voGreen, modifier = Modifier.size(34.dp)) } } } } }

@Composable private fun SlotPicker(apps: List<LaunchableApp>, onDismiss: () -> Unit, onSelect: (LaunchableApp) -> Unit) { AlertDialog(onDismissRequest = onDismiss, title = { Text("Choose an app") }, text = { Column(Modifier.height(420.dp).verticalScroll(rememberScrollState())) { apps.forEach { app -> Surface(onClick = { onSelect(app) }, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) { Image(app.icon.asImageBitmap(), null, Modifier.size(42.dp)); Spacer(Modifier.width(14.dp)); Text(app.label) } } } } }, confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) }

private fun loadQuickSlots(context: Context): List<QuickSlot> { val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE); fun custom(id: String, fallbackLabel: String, kind: SlotKind): QuickSlot { val pkg = p.getString("slot_$id", null); return if (pkg == null) QuickSlot(id, fallbackLabel, null, kind) else QuickSlot(id, appLabel(context, pkg), pkg, SlotKind.APP) }; return listOf(QuickSlot("home", "Home", null, SlotKind.HOME), custom("settings", "Settings", SlotKind.SETTINGS), custom("gallery", "Gallery", SlotKind.GALLERY), custom("calls", "Calls", SlotKind.CALLS)) }
private fun saveSlot(context: Context, id: String, app: LaunchableApp) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("slot_$id", app.packageName).apply() }
private fun openSlot(context: Context, slot: QuickSlot) { when { slot.packageName != null -> context.packageManager.getLaunchIntentForPackage(slot.packageName)?.let { launch(context, it) }; slot.kind == SlotKind.GALLERY -> launch(context, Intent(Intent.ACTION_VIEW).apply { type = "image/*" }); slot.kind == SlotKind.CALLS -> launch(context, Intent(Intent.ACTION_DIAL)) } }
private fun launchLeau(context: Context) { val pm = context.packageManager; val intent = Intent("com.liv.ol1viapa.OPEN_ASSISTANT").setPackage(LEAU_PACKAGE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); runCatching { context.startActivity(intent) }.onFailure { pm.getLaunchIntentForPackage(LEAU_PACKAGE)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)?.let { context.startActivity(it) } ?: android.widget.Toast.makeText(context, "Leau Assistant is not installed yet", android.widget.Toast.LENGTH_SHORT).show() } }
private fun launch(context: Context, intent: Intent) { runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.onFailure { android.widget.Toast.makeText(context, "Unable to open app", android.widget.Toast.LENGTH_SHORT).show() } }
private fun loadApps(context: Context): List<LaunchableApp> { val pm = context.packageManager; val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER); return pm.queryIntentActivities(query, PackageManager.MATCH_ALL).mapNotNull { info -> val label = info.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null; val icon = info.loadIcon(pm)?.let { drawableToBitmap(it) } ?: return@mapNotNull null; LaunchableApp(label, info.activityInfo.packageName, Intent(query).setClassName(info.activityInfo.packageName, info.activityInfo.name), icon) }.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) } }
private fun appLabel(context: Context, packageName: String): String = runCatching { context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(packageName, 0)).toString() }.getOrDefault("App")
private fun drawableToBitmap(drawable: Drawable): Bitmap = drawable.toBitmap(96, 96, android.graphics.Bitmap.Config.ARGB_8888)
