package com.l1vo.oslauncher

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.GenericShape
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
fun AppHub(apps: List<LaunchableApp>, ink: Color, onBack: () -> Unit, onLeau: () -> Unit, onWallpaper: () -> Unit, onOpen: (LaunchableApp) -> Unit, onL1vo: () -> Unit, onLeacher: () -> Unit, onStem: () -> Unit, onEdit: (LaunchableApp) -> Unit, columns: Int = 4, navigation: String = "scroll", hspace: Float = 10f, vspace: Float = 14f, appSize: Float = 1f, highlightShape: String = "round", highlightSize: Float = 1f) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var favoriteApps by remember(apps) { mutableStateOf(loadFavoriteApps(context, apps)) }
    var appPage by rememberSaveable { mutableIntStateOf(0) }
    var dragTotal by remember { mutableFloatStateOf(0f) }
    val filteredApps = remember(apps, query) { val q = query.trim().lowercase(); if (q.isEmpty()) apps else apps.filter { it.label.lowercase().contains(q) } }
    fun openApp(app: LaunchableApp) { rememberAppUse(context, app.packageName); favoriteApps = loadFavoriteApps(context, apps); onOpen(app) }
    fun openL1vo(label: String) {
        when (label) {
            "STEM" -> onStem()
            "LEAU" -> onLeau(); "LEACHER" -> onLeacher()
            "GALLERY" -> launch(context, Intent(Intent.ACTION_VIEW).apply { type = "image/*" })
            "PHONE" -> launch(context, Intent(Intent.ACTION_DIAL))
            else -> apps.firstOrNull { it.label.equals(label, true) }?.let(::openApp) ?: Toast.makeText(context, "$label is not installed yet", Toast.LENGTH_SHORT).show()
        }
    }
    LazyVerticalGrid(columns = GridCells.Fixed(columns.coerceIn(3,6)), modifier = Modifier.fillMaxSize().then(if(navigation=="swipe") Modifier.pointerInput(filteredApps.size) { detectHorizontalDragGestures(onHorizontalDrag = { _, amount -> dragTotal += amount }, onDragEnd = { if (dragTotal < -70f) appPage = (appPage + 1).coerceAtMost(maxOf(0,(filteredApps.size-1)/20)); else if (dragTotal > 70f) appPage = (appPage - 1).coerceAtLeast(0); dragTotal = 0f }) } else Modifier), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(vspace.dp), horizontalArrangement = Arrangement.spacedBy(hspace.dp)) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.offset(y = 8.dp).size(56.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ink) }
                Column(Modifier.weight(1f)) { Text("APP HUB", color = ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold); Text("L1vo application space", color = L1voGreen, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) }
                IconButton(onClick = onWallpaper) { Icon(Icons.Outlined.Wallpaper, "Wallpaper", tint = L1voGreen) }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) { SearchHub(query, { query = it; appPage = 0 }, ink) }
        if (query.isBlank()) {
            item(span = { GridItemSpan(maxLineSpan) }) { L1voAppsPanel(Modifier.fillMaxWidth()) { openL1vo(it) } }
            item(span = { GridItemSpan(maxLineSpan) }) { SystemAppsPanel(Modifier.fillMaxWidth(), apps, onWallpaper) }
            item(span = { GridItemSpan(maxLineSpan) }) { SectionHeading("FAVORITES", "Learns from the apps you use most", ink) }
            items(8) { index -> val app = favoriteApps.getOrNull(index); FavoriteSlot(app, ink, appSize, highlightShape, highlightSize) { if (app != null) openApp(app) } }
            item(span = { GridItemSpan(maxLineSpan) }) { SectionHeading("ALL APPS", "Every launchable app on this device", ink) }
        } else item(span = { GridItemSpan(maxLineSpan) }) { SectionHeading("SEARCH RESULTS", "Matching installed apps", ink) }
        if(navigation=="swipe"){ item(span = { GridItemSpan(maxLineSpan) }) { Text("PAGE ${appPage + 1}/${maxOf(1,(filteredApps.size+19)/20)} • SWIPE LEFT / RIGHT", color = ink.copy(alpha=.62f), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top=4.dp)) }; val visibleApps = filteredApps.chunked(20).getOrNull(appPage) ?: emptyList(); items(visibleApps, key = { it.packageName }, contentType = { "app" }) { app -> AppIcon(app, ::openApp, ink, appSize, highlightShape, highlightSize, onEdit) } } else { items(filteredApps, key = { it.packageName }, contentType = { "app" }) { app -> AppIcon(app, ::openApp, ink, appSize, highlightShape, highlightSize, onEdit) } }
        
    }
}

@Composable private fun SearchHub(query: String, onQueryChange: (String) -> Unit, ink: Color) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface.copy(alpha = .94f), shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth().height(58.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Search, "Search Hub", tint = L1voDeepGreen, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(10.dp)); BasicTextField(value = query, onValueChange = onQueryChange, singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f), decorationBox = { inner -> if (query.isEmpty()) Text("SEARCH HUB", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .82f), fontWeight = FontWeight.SemiBold); inner() }); if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Outlined.Close, "Clear search", tint = L1voDeepGreen) } }
    }
}
@Composable private fun L1voAppsPanel(modifier: Modifier, onOpen: (String) -> Unit) {
    val entries = listOf(
        Triple("STEM", Icons.Outlined.Spa, "STEM"),
        Triple("LEAU", Icons.Outlined.Eco, "LEAU"),
        Triple("LIBRARY", Icons.Outlined.MenuBook, "LIBRARY"),
        Triple("GALLERY", Icons.Outlined.Collections, "GALLERY"),
        Triple("PHONE", Icons.Outlined.Call, "PHONE"),
        Triple("LEACHER", Icons.Outlined.Search, "LEACHER"),
        Triple("BLOOM STORE", Icons.Outlined.LocalFlorist, "BLOOM STORE")
    )

    CategoryPanel(modifier, "L1VO APPS", Icons.Outlined.Eco) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 2.dp)
        ) {
            items(entries, key = { it.third }) { (label, icon, key) ->
                MiniApp(label, icon, Modifier.width(104.dp), onClick = { onOpen(key) })
            }
        }
    }
}

@Composable private fun SystemAppsPanel(modifier: Modifier, apps: List<LaunchableApp>, onWallpaper: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val systemApps = remember(apps) { apps.filter { it.packageName.startsWith("com.android.") || it.packageName.startsWith("com.google.android.") }.filterNot { it.packageName == context.packageName }.filterNot { it.label.equals("Settings",true)||it.label.equals("Phone",true)||it.label.equals("Messages",true)||it.label.equals("Contacts",true) }.sortedBy { it.label.lowercase() } }
    val builtIns=listOf(
        "SETTINGS" to Intent(Settings.ACTION_SETTINGS),
        "PHONE" to Intent(Intent.ACTION_DIAL),
        "MESSAGES" to Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING),
        "CONTACTS" to Intent(Intent.ACTION_VIEW,ContactsContract.Contacts.CONTENT_URI)
    )
    CategoryPanel(modifier,"SYSTEM",Icons.Outlined.Settings){
        LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(end=2.dp)){
            items(builtIns,key={it.first}){(label,intent)->MiniApp(label,Icons.Outlined.Android,Modifier.width(104.dp),{launch(context,intent)},resolveIntentIcon(context,intent))}
            items(systemApps.take(8),key={it.packageName}){app->MiniApp(app.label,Icons.Outlined.Android,Modifier.width(104.dp),{rememberAppUse(context,app.packageName);launch(context,app.intent)},app.icon)}
            item{MiniApp("WALLPAPER",Icons.Outlined.Wallpaper,Modifier.width(104.dp),onClick = onWallpaper)}
        }
    }
}
private fun resolveIntentIcon(context:Context,intent:Intent):Bitmap?=runCatching{context.packageManager.resolveActivity(intent,0)?.activityInfo?.loadIcon(context.packageManager)?.let{drawableToBitmap(it,64)}}.getOrNull()

@Composable private fun CategoryPanel(modifier: Modifier, title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = .96f),
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 5.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = L1voGreen.copy(alpha = .13f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, title, tint = L1voDeepGreen, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(9.dp))
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(Modifier.height(10.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
    }
}

@Composable private fun MiniApp(label:String,icon:ImageVector,modifier:Modifier=Modifier,onClick:()->Unit,bitmap:Bitmap?=null){
    Surface(onClick=onClick,color=MaterialTheme.colorScheme.surface.copy(alpha=.92f),shape=RoundedCornerShape(18.dp),shadowElevation=2.dp,modifier=modifier.height(78.dp)){
        Column(Modifier.fillMaxSize().padding(7.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
            if(bitmap!=null)Image(bitmap.asImageBitmap(),label,Modifier.size(27.dp),contentScale=ContentScale.Fit) else Icon(icon,label,tint=L1voDeepGreen,modifier=Modifier.size(27.dp))
            Spacer(Modifier.height(5.dp));Text(label,color=MaterialTheme.colorScheme.onSurface,fontWeight=FontWeight.SemiBold,style=MaterialTheme.typography.labelSmall,maxLines=2,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}


@Composable private fun SectionHeading(title: String, subtitle: String, ink: Color) { Column(Modifier.fillMaxWidth().padding(top = 2.dp)) { Text(title, color = ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); Text(subtitle, color = ink.copy(alpha = .80f), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall) } }

@Composable private fun FavoriteSlot(app: LaunchableApp?, ink: Color, appSize: Float, highlightShape: String, highlightSize: Float, onClick: () -> Unit) { val icon = remember(app?.packageName) { app?.icon?.asImageBitmap() }; Surface(onClick = onClick, enabled = app != null, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = if (app != null) .92f else .52f), shadowElevation = if (app != null) 2.dp else 0.dp, modifier = Modifier.fillMaxWidth().height(86.dp)) { Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { if (app != null) { icon?.let { Image(it, app.label, Modifier.size(38.dp), contentScale = ContentScale.Fit) }; Spacer(Modifier.height(5.dp)); Text(app.label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall, maxLines = 1) } else { Icon(Icons.Outlined.Add, "Empty favorite", tint = ink.copy(alpha = .45f), modifier = Modifier.size(24.dp)); Spacer(Modifier.height(4.dp)); Text("EMPTY", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall) } } } }

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable private fun AppIcon(a: LaunchableApp, onOpen: (LaunchableApp) -> Unit, ink: Color, appSize: Float, highlightShape: String, highlightSize: Float, onEdit:(LaunchableApp)->Unit) {
    val context=LocalContext.current
    var menu by remember{mutableStateOf(false)}
    val label=appDisplayName(context,a)
    val customColor=when(context.getSharedPreferences(PREFS,0).getString("app_color_${a.packageName.replace(Regex("[^A-Za-z0-9_.-]"),"_")}","auto")){"white"->Color.White;"green"->L1voGreen;"warm"->Color(0xFFFFF4D6);else->MaterialTheme.colorScheme.onSurface}
    val font=when(appFont(context,a.packageName)){"Serif"->androidx.compose.ui.text.font.FontFamily.Serif;"Mono"->androidx.compose.ui.text.font.FontFamily.Monospace;"Cursive"->androidx.compose.ui.text.font.FontFamily.Cursive;else->androidx.compose.ui.text.font.FontFamily.SansSerif}
    Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.fillMaxWidth()){
        Box(Modifier.size((62f*highlightSize).dp).then(if(highlightShape=="none")Modifier else Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha=.92f),highlightShapeValue(highlightShape))).combinedClickable(onClick={onOpen(a)},onLongClick={menu=true}),contentAlignment=Alignment.Center){
            CustomAppIcon(a,Modifier.size((38f*appSize).dp).padding((7f*appSize).dp))
        }
        Spacer(Modifier.height(5.dp));Text(label,color=customColor,fontFamily=font,fontWeight=FontWeight.Medium,style=MaterialTheme.typography.labelMedium,maxLines=1)
    }
    if(menu)AppActionMenu(a,ink,{menu=false}){onEdit(a)}
}

@Composable fun LeacherScreen(apps: List<LaunchableApp>, ink: Color, onBack: () -> Unit) {
    val context=LocalContext.current
    var query by rememberSaveable{mutableStateOf("")}
    val browserCandidates=listOf("Google Chrome","Chrome","Opera","Brave")
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(top=WindowInsets.statusBars.asPaddingValues().calculateTopPadding())){
        Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
            IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)}
            Text("LEACHER",color=ink,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f))
            IconButton(onClick={query=""}){Icon(Icons.Outlined.MoreVert,"Menu",tint=ink)}
        }
        Surface(shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surface,shadowElevation=2.dp,modifier=Modifier.fillMaxWidth().padding(horizontal=14.dp).height(52.dp)){
            Row(Modifier.fillMaxSize().padding(horizontal=14.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Search,"Search",tint=L1voDeepGreen);Spacer(Modifier.width(8.dp));BasicTextField(value=query,onValueChange={query=it},singleLine=true,textStyle=MaterialTheme.typography.bodyLarge.copy(color=ink),modifier=Modifier.weight(1f),decorationBox={inner->if(query.isEmpty())Text("Search or enter website",color=ink.copy(alpha=.55f));inner()});if(query.isNotEmpty())IconButton(onClick={query=""}){Icon(Icons.Outlined.Close,"Clear",tint=ink)}}}
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal=14.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){browserCandidates.forEach{label->val app=apps.firstOrNull{it.label.equals(label,true)||it.label.contains(label,true)};BrowserBridge(label,ink,app,Modifier.weight(1f)){if(app!=null)launch(context,app.intent)else launchBrowser(context)}}}
        Spacer(Modifier.height(20.dp))
        Text("Favorites",color=ink,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(horizontal=18.dp))
        Spacer(Modifier.height(8.dp))
        listOf("google.com","youtube.com","wikipedia.org","github.com").forEach{site->Surface(onClick={launch(context,Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://$site")))},shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface,modifier=Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=4.dp)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Language,site,tint=L1voDeepGreen);Spacer(Modifier.width(12.dp));Text(site,color=ink,modifier=Modifier.weight(1f));Icon(Icons.Outlined.ChevronRight,"Open",tint=ink.copy(alpha=.5f))}}}
        Spacer(Modifier.height(20.dp))
        Text("Start Page",color=ink,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(horizontal=18.dp))
        Spacer(Modifier.height(8.dp))
        Surface(onClick={launchBrowser(context)},shape=RoundedCornerShape(18.dp),color=L1voDeepGreen,modifier=Modifier.fillMaxWidth().padding(horizontal=14.dp).height(64.dp)){Row(Modifier.padding(horizontal=18.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Public,"Web",tint=Color.White);Spacer(Modifier.width(12.dp));Text("Open the web",color=Color.White,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Icon(Icons.Outlined.ArrowForward,"Go",tint=Color.White)}}
    }
}

@Composable private fun BrowserBridge(label:String,ink:Color,app:LaunchableApp?,modifier:Modifier,onClick:()->Unit){Surface(onClick=onClick,shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface,shadowElevation=2.dp,modifier=modifier.height(86.dp)){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center,modifier=Modifier.fillMaxSize()){Icon(if(label.contains("Chrome"))Icons.Outlined.Language else Icons.Outlined.Public,label,tint=L1voDeepGreen,modifier=Modifier.size(28.dp));Spacer(Modifier.height(5.dp));Text(label,color=ink,fontWeight=FontWeight.Medium,style=MaterialTheme.typography.labelSmall,maxLines=1)}}}
private fun launchBrowser(context:Context){launch(context,Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://www.google.com")))}

@Composable fun L1voHub(ink: Color, onBack: () -> Unit, onSettings: () -> Unit, onLeau: () -> Unit, onWallpaper: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack, modifier = Modifier.offset(y = 8.dp).size(56.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ink) }; Column(Modifier.weight(1f)) { Text("L1VO", style = MaterialTheme.typography.headlineMedium, color = ink, fontWeight = FontWeight.SemiBold); Text("L1vo apps & features", color = L1voGreen, fontWeight = FontWeight.Medium) }; IconButton(onClick = onWallpaper) { Icon(Icons.Outlined.Wallpaper, "Wallpaper", tint = L1voGreen) } }
        Spacer(Modifier.height(18.dp)); Feature("L1vo Settings", "Native launcher settings", Icons.Outlined.Settings, ink, onSettings); Feature("Leau", "L1vo assistant", Icons.Outlined.Eco, ink, onLeau); Feature("L1vo Gallery", "Media space", Icons.Outlined.Collections, ink) { launch(context, Intent(Intent.ACTION_VIEW).apply { type = "image/*" }) }; Feature("L1vo Phone & Contacts", "Calls and contacts", Icons.Outlined.Call, ink) { launch(context, Intent(Intent.ACTION_DIAL)) }
    }
}

@Composable private fun Feature(t: String, s: String, i: ImageVector, ink: Color, onClick: () -> Unit) { Surface(onClick = onClick, color = MaterialTheme.colorScheme.surface.copy(alpha = .96f), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(i, t, tint = L1voDeepGreen, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(15.dp)); Column { Text(t, color = ink, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge); Text(s, color = ink.copy(alpha = .78f), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall) } } } }

@Composable private fun highlightShapeValue(value:String):androidx.compose.ui.graphics.Shape=when(value){"square"->RoundedCornerShape(2.dp);"diamond"->GenericShape{size, _ ->moveTo(size.width/2f,0f);lineTo(size.width,size.height/2f);lineTo(size.width/2f,size.height);lineTo(0f,size.height/2f);close()};"none"->RoundedCornerShape(0.dp);else->RoundedCornerShape(18.dp)}
