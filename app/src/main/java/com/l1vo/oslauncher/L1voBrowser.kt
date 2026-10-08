package com.l1vo.oslauncher

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URLEncoder

private data class BrowserTab(val id:Int,var url:String)

@Composable
fun L1voBrowser(ink:Color,onClose:()->Unit){
    val context=androidx.compose.ui.platform.LocalContext.current
    var address by rememberSaveable{mutableStateOf("https://www.google.com")}
    var pageTitle by rememberSaveable{mutableStateOf("Leacher")}
    var canGoBack by remember{mutableStateOf(false)}
    var canGoForward by remember{mutableStateOf(false)}
    var desktopSite by rememberSaveable{mutableStateOf(false)}
    var showTabs by remember{mutableStateOf(false)}
    var showMenu by remember{mutableStateOf(false)}
    var findMode by remember{mutableStateOf(false)}
    var findText by remember{mutableStateOf("")}
    val tabs=remember{mutableStateListOf(BrowserTab(1,address))}
    var selectedTab by remember{mutableIntStateOf(1)}
    val webView=remember{
        WebView(context).apply{
            settings.javaScriptEnabled=true
            settings.domStorageEnabled=true
            settings.loadsImagesAutomatically=true
            settings.javaScriptCanOpenWindowsAutomatically=true
            settings.setSupportMultipleWindows(false)
            settings.userAgentString=settings.userAgentString+" L1VO-Leacher"
            CookieManager.getInstance().setAcceptCookie(true)
            webViewClient=object:WebViewClient(){
                override fun shouldOverrideUrlLoading(view:WebView,request:WebResourceRequest):Boolean{
                    val u=request.url.toString()
                    return if(u.startsWith("http://")||u.startsWith("https://")) false else {runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,request.url))};true}
                }
                override fun onPageFinished(view:WebView,url:String){
                    address=url
                    tabs.firstOrNull{it.id==selectedTab}?.url=url
                    canGoBack=view.canGoBack()
                    canGoForward=view.canGoForward()
                }
            }
            webChromeClient=object:WebChromeClient(){
                override fun onReceivedTitle(view:WebView,title:String?){pageTitle=title?.takeIf{it.isNotBlank()}?:"Leacher"}
            }
            setDownloadListener(DownloadListener{url,userAgent,contentDisposition,mimeType,contentLength->
                runCatching{
                    val request=DownloadManager.Request(Uri.parse(url)).apply{
                        setMimeType(mimeType)
                        addRequestHeader("User-Agent",userAgent)
                        setTitle("Leacher download")
                        setDescription("Downloaded by Leacher")
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"leacher_"+System.currentTimeMillis())
                    }
                    (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
                }
            })
        }
    }
    DisposableEffect(Unit){onDispose{webView.stopLoading();webView.destroy()}}
    LaunchedEffect(Unit){webView.loadUrl(address)}
    BackHandler(enabled=canGoBack){webView.goBack()}
    BackHandler(enabled=!canGoBack && !showTabs && !showMenu && !findMode){onClose()}
    Column(Modifier.fillMaxSize()){
        Surface(color=MaterialTheme.colorScheme.surface.copy(alpha=.98f),shadowElevation=4.dp){
            Column{
                Row(Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically){
                    IconButton(onClick={if(webView.canGoBack())webView.goBack()else onClose()}){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Back",tint=ink)}
                    IconButton(onClick={if(webView.canGoForward())webView.goForward()},enabled=webView.canGoForward()){Icon(Icons.AutoMirrored.Outlined.ArrowForward,"Forward",tint=ink)}
                    OutlinedTextField(value=address,onValueChange={address=it},singleLine=true,modifier=Modifier.weight(1f).height(52.dp),placeholder={Text("Search or enter website")},trailingIcon={IconButton(onClick={webView.loadUrl(normalizeBrowserInput(address))}){Icon(Icons.AutoMirrored.Outlined.ArrowForward,"Go")}})
                    IconButton(onClick={webView::reload}){Icon(Icons.Outlined.Refresh,"Reload",tint=ink)}
                    IconButton(onClick={onClose}){Icon(Icons.Outlined.Home,"L1VO",tint=ink)}
                }
                Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){
                    Text(pageTitle,maxLines=1,modifier=Modifier.weight(1f),color=ink.copy(alpha=.65f),style=MaterialTheme.typography.labelSmall)
                    TextButton(onClick={showTabs=!showTabs}){Text("TABS "+tabs.size)}
                    IconButton(onClick={showMenu=!showMenu}){Icon(Icons.Outlined.MoreVert,"Menu",tint=ink)}
                }
                if(showTabs){
                    LazyRow(Modifier.fillMaxWidth().padding(horizontal=10.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                        items(tabs,key={it.id}){tab->FilterChip(selected=tab.id==selectedTab,onClick={selectedTab=tab.id;address=tab.url;webView.loadUrl(tab.url);showTabs=false},label={Text(tab.url.removePrefix("https://").removePrefix("http://").take(22))})}
                        item{AssistChip(onClick={val id=(tabs.maxOfOrNull{it.id}?:0)+1;tabs.add(BrowserTab(id,"https://www.google.com"));selectedTab=id;address="https://www.google.com";webView.loadUrl(address);showTabs=false},label={Text("+ New tab")})}
                    }
                }
                if(showMenu){
                    Row(Modifier.fillMaxWidth().padding(horizontal=10.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                        AssistChip(onClick={desktopSite=!desktopSite;webView.settings.userAgentString=if(desktopSite)"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/120 Safari/537.36" else "L1VO-Leacher";webView.reload()},label={Text(if(desktopSite)"Mobile site" else "Desktop site")})
                        AssistChip(onClick={context.getSharedPreferences(PREFS,0).edit().putString("leacher_bookmark",address).apply();showMenu=false},label={Text("Bookmark")})
                        AssistChip(onClick={findMode=true;showMenu=false},label={Text("Find")})
                        AssistChip(onClick={runCatching{context.startActivity(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,address)})};showMenu=false},label={Text("Share")})
                    }
                }
                if(findMode){
                    Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically){
                        OutlinedTextField(value=findText,onValueChange={findText=it;webView.findAllAsync(it)},singleLine=true,modifier=Modifier.weight(1f),placeholder={Text("Find in page")})
                        TextButton(onClick={findMode=false;webView.clearMatches()}){Text("Done")}
                    }
                }
            }
        }
        AndroidView(factory={webView},modifier=Modifier.fillMaxSize())
    }
}

private fun normalizeBrowserInput(value:String):String{
    val q=value.trim()
    if(q.isBlank()) return "https://www.google.com"
    return if(q.startsWith("http://")||q.startsWith("https://")) q
    else if(q.contains(".")&&!q.contains(" ")) "https://$q"
    else "https://www.google.com/search?q="+URLEncoder.encode(q,"UTF-8")
}
