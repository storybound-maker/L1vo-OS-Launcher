package com.l1vo.oslauncher

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.provider.MediaStore
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale

private fun vibeeTone(type:Int){
    runCatching{ToneGenerator(AudioManager.STREAM_NOTIFICATION,70).also{it.startTone(type,110);it.release()}}
}

@Composable
fun VibeeOverlay(apps:List<LaunchableApp>,ink:Color,onDismiss:()->Unit){
    val context=LocalContext.current
    var listening by remember{mutableStateOf(false)}
    var message by remember{mutableStateOf("Tap to speak")}
    var pendingCommand by remember{mutableStateOf<String?>(null)}
    val contactLauncher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ok->
        pendingCommand?.let{cmd->if(ok) handleVibeeCommand(context,apps,cmd) else {message="Contacts permission is needed";vibeeTone(ToneGenerator.TONE_PROP_NACK)}}
        pendingCommand=null
    }
    val speechLauncher=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        listening=false
        val spoken=result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim().orEmpty()
        if(result.resultCode==android.app.Activity.RESULT_OK && spoken.isNotBlank()){
            message=spoken
            handleVibeeCommand(context,apps,spoken){pendingCommand=it;contactLauncher.launch(Manifest.permission.READ_CONTACTS)}
        }else{message="I didn't hear that";vibeeTone(ToneGenerator.TONE_PROP_NACK)}
    }
    val micLauncher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ok->
        if(ok){message="Listening…";listening=true;startVibeeActivity(context,speechLauncher::launch)}
        else{message="Microphone permission is needed";listening=false}
    }
    val pulse=rememberInfiniteTransition(label="vibee")
    val scale by pulse.animateFloat(.96f,1.05f,infiniteRepeatable(tween(1200),RepeatMode.Reverse),label="breathing")
    Surface(color=Color.Black.copy(alpha=.42f),modifier=Modifier.fillMaxSize()){
        Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
            Surface(shape=RoundedCornerShape(32.dp),color=MaterialTheme.colorScheme.surface.copy(alpha=.98f),shadowElevation=18.dp,modifier=Modifier.fillMaxWidth(.88f).padding(18.dp)){
                Column(Modifier.padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    Box(Modifier.size(132.dp).scale(scale).background(L1voGreen.copy(alpha=.13f),RoundedCornerShape(66.dp)),contentAlignment=Alignment.Center){Text("⌒  ⌒",color=L1voGreen,style=MaterialTheme.typography.headlineLarge)}
                    Spacer(Modifier.height(14.dp))
                    Text("VIBEE",color=ink,style=MaterialTheme.typography.headlineSmall)
                    Text(if(listening)"Listening…" else message,color=ink.copy(alpha=.65f))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick={
                        if(listening)return@Button
                        vibeeTone(ToneGenerator.TONE_PROP_BEEP)
                        if(androidx.core.content.ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){
                            message="Listening…";listening=true;startVibeeActivity(context,speechLauncher::launch)
                        }else micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }){Text(if(listening)"LISTENING" else "TAP TO SPEAK")}
                    TextButton(onClick={vibeeTone(ToneGenerator.TONE_PROP_ACK);onDismiss()}){Text("Close")}
                }
            }
        }
    }
}

private fun startVibeeActivity(context:Context,launch:(Intent)->Unit){
    if(!SpeechRecognizer.isRecognitionAvailable(context)){
        android.widget.Toast.makeText(context,"No speech recognition service is available",android.widget.Toast.LENGTH_LONG).show()
        return
    }
    launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PROMPT,"Say a command")
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true)
    })
}

private fun findContactNumber(context:Context,name:String):String?{
    if(androidx.core.content.ContextCompat.checkSelfPermission(context,Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED)return null
    val uri=ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    val projection=arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
    context.contentResolver.query(uri,projection,null,null,null)?.use{cursor->
        val nameIndex=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        while(cursor.moveToNext()){
            val contact=cursor.getString(nameIndex)?.lowercase(Locale.getDefault())?:""
            if(contact==name.lowercase(Locale.getDefault())||contact.contains(name.lowercase(Locale.getDefault()))){
                return cursor.getString(numberIndex)
            }
        }
    }
    return null
}

private fun resolveVibeeApp(context:Context,wanted:String,fallback:List<LaunchableApp>):LaunchableApp?{
    val normalized=wanted.lowercase(Locale.getDefault()).replace(Regex("[^a-z0-9]+"),"")
    val fresh=loadApps(context)
    return (fresh+fallback).distinctBy{it.packageName}.firstOrNull{app->
        val label=app.label.lowercase(Locale.getDefault()).replace(Regex("[^a-z0-9]+"),"")
        label==normalized || label.contains(normalized) || normalized.contains(label)
    }
}

private fun handleVibeeCommand(context:Context,apps:List<LaunchableApp>,raw:String,requestContacts:((String)->Unit)?=null){
    val command=raw.trim()
    val lower=command.lowercase(Locale.getDefault())
    when{
        lower=="open settings" || lower=="settings" -> launch(context,Intent(android.provider.Settings.ACTION_SETTINGS))
        lower.startsWith("open ") || lower.startsWith("launch ")->{
            val wanted=command.substringAfter(" ").trim().removePrefix("the ").trim()
            val app=resolveVibeeApp(context,wanted,apps)
            if(app!=null) launch(context,app.intent) else android.widget.Toast.makeText(context,"I couldn't find $wanted",android.widget.Toast.LENGTH_SHORT).show()
        }
        lower.startsWith("call ")->{
            val who=command.substringAfter("call ", "").trim()
            if(who.matches(Regex("[+0-9 ()-]{3,}"))){
                launch(context,Intent(Intent.ACTION_DIAL,android.net.Uri.parse("tel:"+who.replace(" ",""))))
            }else if(requestContacts!=null){
                requestContacts(who)
            }else{
                val number=findContactNumber(context,who)
                if(number!=null)launch(context,Intent(Intent.ACTION_DIAL,android.net.Uri.parse("tel:"+number))) else android.widget.Toast.makeText(context,"Contact not found",android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        lower=="record" || lower=="start recording" || lower=="record audio"->{
            runCatching{launch(context,Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION))}.onFailure{android.widget.Toast.makeText(context,"No audio recorder is available",android.widget.Toast.LENGTH_SHORT).show()}
        }
        else->android.widget.Toast.makeText(context,"Try: open YouTube, call Mom, settings, or record audio",android.widget.Toast.LENGTH_LONG).show()
    }
}
