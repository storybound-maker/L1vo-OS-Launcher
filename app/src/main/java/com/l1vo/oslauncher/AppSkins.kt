package com.l1vo.oslauncher

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.LinearGradient
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

data class AppSkin(
    val id: String,
    val name: String,
    val premium: Boolean,
    val start: Int,
    val end: Int,
    val accent: Int
)

private val commonSkins = listOf(
    AppSkin("gold","Gold",true,Color.rgb(255,225,120),Color.rgb(139,87,0),Color.rgb(255,215,80)),
    AppSkin("black_diamond","Black Diamond",true,Color.rgb(70,70,78),Color.rgb(5,5,8),Color.rgb(205,205,220)),
    AppSkin("platinum","Platinum",true,Color.rgb(245,245,250),Color.rgb(105,110,120),Color.rgb(220,225,235)),
    AppSkin("rose_gold","Rose Gold",true,Color.rgb(255,210,220),Color.rgb(125,55,75),Color.rgb(245,145,170)),
    AppSkin("emerald","Emerald",true,Color.rgb(100,235,155),Color.rgb(5,75,45),Color.rgb(80,255,160)),
    AppSkin("sapphire","Sapphire",true,Color.rgb(100,185,255),Color.rgb(8,35,105),Color.rgb(90,170,255)),
    AppSkin("ruby","Ruby",true,Color.rgb(255,105,115),Color.rgb(105,5,18),Color.rgb(255,75,90)),
    AppSkin("obsidian","Obsidian",true,Color.rgb(70,75,85),Color.rgb(8,10,16),Color.rgb(150,160,180)),
    AppSkin("aurora","Aurora",true,Color.rgb(100,255,205),Color.rgb(75,35,155),Color.rgb(120,220,255)),
    AppSkin("carbon","Carbon",true,Color.rgb(85,90,95),Color.rgb(18,20,22),Color.rgb(170,175,180)),
    AppSkin("pearl","Pearl",true,Color.rgb(255,255,255),Color.rgb(185,190,205),Color.rgb(245,245,255)),
    AppSkin("neon_chrome","Neon Chrome",true,Color.rgb(60,255,240),Color.rgb(35,45,120),Color.rgb(255,70,220))
)

fun skinsForPackage(pkg:String):List<AppSkin>{
    val p=pkg.lowercase()
    return when {
        p.contains("snapchat") -> listOf(commonSkins[0],commonSkins[1],commonSkins[7],commonSkins[11])
        p.contains("whatsapp") -> listOf(commonSkins[4],commonSkins[0],commonSkins[9],commonSkins[1])
        p.contains("youtube") -> listOf(commonSkins[6],commonSkins[1],commonSkins[2],commonSkins[11])
        p.contains("instagram") -> listOf(commonSkins[3],commonSkins[10],commonSkins[8],commonSkins[1])
        p.contains("tiktok") -> listOf(commonSkins[11],commonSkins[1],commonSkins[9],commonSkins[2])
        p.endsWith(".x") || p.contains(".twitter") -> listOf(commonSkins[7],commonSkins[9],commonSkins[2],commonSkins[1])
        p.contains("google") -> listOf(commonSkins[2],commonSkins[5],commonSkins[0],commonSkins[8])
        p.contains("chrome") -> listOf(commonSkins[9],commonSkins[5],commonSkins[1],commonSkins[8])
        p.contains("gmail") -> listOf(commonSkins[6],commonSkins[10],commonSkins[2],commonSkins[0])
        p.contains("spotify") -> listOf(commonSkins[4],commonSkins[1],commonSkins[9],commonSkins[11])
        p.contains("discord") -> listOf(AppSkin("amethyst","Amethyst",true,Color.rgb(205,130,255),Color.rgb(55,15,100),Color.rgb(180,100,255)),commonSkins[1],commonSkins[5],commonSkins[11])
        p.contains("facebook") -> listOf(commonSkins[5],commonSkins[2],commonSkins[9],commonSkins[0])
        else -> commonSkins.take(6)
    }
}

fun appSkin(c:android.content.Context,pkg:String):AppSkin?{
    val id=c.getSharedPreferences(PREFS,0).getString(appPref(pkg,"skin"),null) ?: return null
    return skinsForPackage(pkg).firstOrNull{it.id==id}
}

fun setAppSkin(c:android.content.Context,pkg:String,id:String?){
    c.getSharedPreferences(PREFS,0).edit().apply{
        if(id.isNullOrBlank()) remove(appPref(pkg,"skin")) else putString(appPref(pkg,"skin"),id)
    }.apply()
}

fun skinBitmap(source:Bitmap,skin:AppSkin,size:Int):Bitmap{
    val out=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888)
    val canvas=Canvas(out)
    val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    paint.shader=LinearGradient(0f,0f,size.toFloat(),size.toFloat(),skin.start,skin.end,Shader.TileMode.CLAMP)
    canvas.drawRect(0f,0f,size.toFloat(),size.toFloat(),paint)
    val icon=Bitmap.createScaledBitmap(source,(size*.68f).toInt(),(size*.68f).toInt(),true)
    val left=(size-icon.width)/2f
    val top=(size-icon.height)/2f
    val iconPaint=Paint(Paint.ANTI_ALIAS_FLAG)
    val matrix=ColorMatrix().apply{setSaturation(.35f)}
    iconPaint.colorFilter=ColorMatrixColorFilter(matrix)
    canvas.drawBitmap(icon,left,top,iconPaint)
    val ring=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeWidth=size*.035f;color=skin.accent}
    canvas.drawCircle(size/2f,size/2f,size*.43f,ring)
    return out
}

@Composable
fun PremiumSkinPreview(app:LaunchableApp,skin:AppSkin,modifier:Modifier=Modifier.size(58.dp)){
    val context=LocalContext.current
    val bitmap=remember(app.packageName,skin.id){
        skinBitmap(app.icon,skin,128)
    }
    Image(bitmap.asImageBitmap(),contentDescription=skin.name,modifier=modifier,contentScale=ContentScale.Fit)
}
