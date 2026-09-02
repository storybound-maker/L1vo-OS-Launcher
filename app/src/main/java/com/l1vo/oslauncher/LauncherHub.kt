package com.l1vo.oslauncher

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BasicTextField
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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppHub(
    apps: List<LaunchableApp>, ink: Color, onBack: () -> Unit, onLeau: () -> Unit,
    onWallpaper: () -> Unit, onOpen: (LaunchableApp) -> Unit, onL1vo: () -> Unit,
    onLeacher: () -> Unit
) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var favoriteApps by remember(apps) { mutableStateOf(loadFavoriteApps(context, apps)) }
    val filteredApps = remember(apps, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) apps else apps.filter { it.label.lowercase().contains(q) }
    }

    fun openApp(app: LaunchableApp) {
        rememberAppUse(context, app.packageName)
        favoriteApps = loadFavoriteApps(context, apps)
        onOpen(app)
    }

    fun openL1vo(label: String) {
        when (label) {
            "LEAU" -> onLeau()
            "LEACHER" -> onLeacher()
            "GALLERY" -> launch(context, Intent(Intent.ACTION_VIEW).apply { type = "image/*" })
            "PHONE" -> launch(context, Intent(Intent.ACTION_DIAL))
            else -> {
                val match = apps.firstOrNull { it.label.equals(label, ignoreCase = true) }
                if (match != null) openApp(match)
                else Toast.makeText(context, "$label is not installed yet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4), modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.offset(y = 6.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ink)
                }
                Column(Modifier.weight(1f)) {
                    Text("APP HUB", color = ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                    Text("L1vo application space", color = L1voGreen, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                IconButton(onClick = onWallpaper) { Icon(Icons.Outlined.Wallpaper, "Wallpaper", tint = L1voGreen) }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) { SearchHub(query, { query = it }, ink) }

        if (query.isBlank()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    L1voAppsPanel(Modifier.weight(1f), ink) { openL1vo(it) }
                    SystemAppsPanel(Modifier.weight(1f), ink, apps, onWallpaper)
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) { SectionHeading("FAVORITES", "Learns from the apps you use most", ink) }
            items(8) { index ->
                val app = favoriteApps.getOrNull(index)
                FavoriteSlot(app, ink) { if (app != null) openApp(app) }
            }
            item(span = { GridItemSpan(maxLineSpan) }) { SectionHeading("ALL APPS", "Every launchable app on this device", ink) }
        } else {
            item(span = { GridItemSpan(maxLineSpan) }) { SectionHeading("SEARCH RESULTS", "Matching installed apps", ink) }
        }
        items(filteredApps, key = { it.packageName }) { app -> AppIcon(app, ::openApp, ink) }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Surface(onClick = onLeau, shape = RoundedCornerShape(50), color = L1voInk, shadowElevation = 6.dp) {
                    Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Eco, "Leau", tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp)); Text("LEAU", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHub(query: String, onQueryChange: (String) -> Unit, ink: Color) {
    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = .92f), shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().height(58.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Search, "Search Hub", tint = L1voDeepGreen, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(10.dp))
            BasicTextField(value = query, onValueChange = onQueryChange, singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = ink, fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f), decorationBox = { inner ->
                    if (query.isEmpty()) Text("SEARCH HUB", color = ink.copy(alpha = .82f), fontWeight = FontWeight.SemiBold)
                    inner()
                })
            if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Outlined.Close, "Clear search", tint = L1voDeepGreen) }
        }
    }
}

@Composable
private fun L1voAppsPanel(modifier: Modifier, ink: Color, onOpen: (String) -> Unit) {
    val entries = listOf(
        Triple("STEM", Icons.Outlined.Spa, "STEM"), Triple("LEAU", Icons.Outlined.Eco, "LEAU"),
        Triple("LIBRARY", Icons.Outlined.MenuBook, "LIBRARY"), Triple("GALLERY", Icons.Outlined.Collections, "GALLERY"),
        Triple("PHONE", Icons.Outlined.Call, "PHONE"), Triple("LEACHER", Icons.Outlined.Search, "LEACHER"),
        Triple("BLOOM STORE", Icons.Outlined.LocalFlorist, "BLOOM STORE")
    )
    CategoryPanel(modifier, "L1VO APPS", ink, Icons.Outlined.Eco) {
        entries.forEach { (label, icon, key) -> MiniApp(label, icon, ink) { onOpen(key) } }
    }
}

@Composable
private fun SystemAppsPanel(modifier: Modifier, ink: Color, apps: List<LaunchableApp>, onWallpaper: () -> Unit) {
    val context = LocalContext.current
    val systemApps = remember(apps) {
        apps.filter { it.packageName.startsWith("com.android.") || it.packageName.startsWith("com.google.android.") }
            .filterNot { it.packageName == context.packageName }.take(5)
    }
    CategoryPanel(modifier, "SYSTEM", ink, Icons.Outlined.Settings) {
        MiniApp("SETTINGS", Icons.Outlined.Settings, ink) { launch(context, Intent(Settings.ACTION_SETTINGS)) }
        MiniApp("PHONE", Icons.Outlined.Call, ink) { launch(context, Intent(Intent.ACTION_DIAL)) }
        MiniApp("MESSAGES", Icons.Outlined.Message, ink) { launchMessages(context) }
        MiniApp("CONTACTS", Icons.Outlined.Contacts, ink) {
            launch(context, Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI))
        }
        systemApps.forEach { app -> MiniApp(app.label, Icons.Outlined.Android, ink) { launch(context, app.intent); rememberAppUse(context, app.packageName) } }
        MiniApp("WALLPAPER", Icons.Outlined.Wallpaper, ink, onWallpaper)
    }
}

@Composable
private fun CategoryPanel(modifier: Modifier, title: String, ink: Color, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, color = L1voPanel.copy(alpha = .96f), shape = RoundedCornerShape(26.dp), shadowElevation = 5.dp) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(14.dp), color = L1voGreen.copy(alpha = .13f), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, title, tint = L1voDeepGreen, modifier = Modifier.size(20.dp)) }
                }
                Spacer(Modifier.width(9.dp)); Text(title, color = ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(10.dp)); Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
        }
    }
}

@Composable
private fun MiniApp(label: String, icon: ImageVector, ink: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.White.copy(alpha = .76f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, label, tint = L1voDeepGreen, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(9.dp))
            Text(label, color = ink, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String, ink: Color) {
    Column(Modifier.fillMaxWidth().padding(top = 2.dp)) {
        Text(title, color = ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, color = ink.copy(alpha = .80f), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FavoriteSlot(app: LaunchableApp?, ink: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, enabled = app != null, shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = if (app != null) .90f else .52f), shadowElevation = if (app != null) 2.dp else 0.dp,
        modifier = Modifier.fillMaxWidth().height(86.dp)) {
        Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (app != null) {
                Image(app.icon.asImageBitmap(), app.label, Modifier.size(38.dp), contentScale = ContentScale.Fit); Spacer(Modifier.height(5.dp))
                Text(app.label, color = ink, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            } else {
                Icon(Icons.Outlined.Add, "Empty favorite", tint = ink.copy(alpha = .45f), modifier = Modifier.size(24.dp)); Spacer(Modifier.height(4.dp))
                Text("EMPTY", color = ink.copy(alpha = .55f), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun AppIcon(a: LaunchableApp, onOpen: (LaunchableApp) -> Unit, ink: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Surface(onClick = { onOpen(a) }, shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = .90f), shadowElevation = 2.dp, modifier = Modifier.size(62.dp)) {
            Image(a.icon.asImageBitmap(), a.label, Modifier.padding(9.dp), contentScale = ContentScale.Fit)
        }
        Spacer(Modifier.height(5.dp)); Text(a.label, color = ink, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
fun LeacherScreen(apps: List<LaunchableApp>, ink: Color, onBack: () -> Unit) {
    val context = LocalContext.current
    val browserCandidates = listOf("Google Chrome", "Chrome", "Opera", "Brave")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.offset(y = 6.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ink) }
            Column(Modifier.weight(1f)) {
                Text("LEACHER", color = ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Browser bridge", color = L1voGreen, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Outlined.Search, "Leacher", tint = L1voGreen)
        }
        Spacer(Modifier.height(22.dp))
        Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = .92f), shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Row(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Search, "Search", tint = L1voDeepGreen, modifier = Modifier.size(23.dp)); Spacer(Modifier.width(10.dp))
                Text("Search...", color = ink.copy(alpha = .68f), fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            browserCandidates.forEach { label ->
                val app = apps.firstOrNull { it.label.equals(label, true) || it.label.contains(label, true) }
                BrowserBridge(label, ink, app) {
                    if (app != null) launch(context, app.intent)
                    else launchBrowser(context)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("🍃", style = MaterialTheme.typography.headlineMedium)
        }
        Text("← Leacher settings", color = ink.copy(alpha = .70f), fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun BrowserBridge(label: String, ink: Color, app: LaunchableApp?, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = .88f), shadowElevation = 2.dp, modifier = Modifier.weight(1f).height(82.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize().padding(6.dp)) {
            Icon(if (label.contains("Chrome")) Icons.Outlined.Language else Icons.Outlined.Public, label, tint = L1voDeepGreen, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(5.dp)); Text(label, color = ink, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

private fun launchBrowser(context: Context) { launch(context, Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com"))) }

private fun launchMessages(context: Context) {
    launch(context, Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_MESSAGING) })
}

private fun rememberAppUse(context: Context, packageName: String) {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE); val key = "app_use_$packageName"
    prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
}

private fun loadFavoriteApps(context: Context, apps: List<LaunchableApp>): List<LaunchableApp> {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    return apps.sortedWith(compareByDescending<LaunchableApp> { prefs.getInt("app_use_${it.packageName}", 0) }.thenBy { it.label.lowercase() }).take(8)
}

@Composable
fun L1voHub(ink: Color, onBack: () -> Unit, onSettings: () -> Unit, onLeau: () -> Unit, onWallpaper: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.offset(y = 6.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ink) }
            Column(Modifier.weight(1f)) { Text("L1VO", style = MaterialTheme.typography.headlineMedium, color = ink, fontWeight = FontWeight.SemiBold); Text("L1vo apps & features", color = L1voGreen, fontWeight = FontWeight.Medium) }
            IconButton(onClick = onWallpaper) { Icon(Icons.Outlined.Wallpaper, "Wallpaper", tint = L1voGreen) }
        }
        Spacer(Modifier.height(18.dp)); Feature("L1vo Settings", "Native launcher settings", Icons.Outlined.Settings, ink, onSettings)
        Feature("Leau", "L1vo assistant", Icons.Outlined.Eco, ink, onLeau)
        Feature("L1vo Gallery", "Media space", Icons.Outlined.Collections, ink) { launch(context, Intent(Intent.ACTION_VIEW).apply { type = "image/*" }) }
        Feature("L1vo Phone & Contacts", "Calls and contacts", Icons.Outlined.Call, ink) { launch(context, Intent(Intent.ACTION_DIAL)) }
    }
}

@Composable
private fun Feature(t: String, s: String, i: ImageVector, ink: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = L1voPanel.copy(alpha = .96f), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(i, t, tint = L1voDeepGreen, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(15.dp)); Column { Text(t, color = ink, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge); Text(s, color = ink.copy(alpha = .78f), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall) } }
    }
}
