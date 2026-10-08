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
    val pulse=rememberInfiniteTransition(label="vibee")
    val scale by pulse.animateFloat(.96f,1.05f,infiniteRepeatable(tween(1200),RepeatMode.Reverse),label="breathing")
    val recognizer=remember{SpeechRecognizer.createSpeechRecognizer(context)}
    var pendingCommand by remember{mutableStateOf<String?>(null)}
    val contactLauncher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ok->
        pendingCommand?.let{cmd->if(ok) handleVibeeCommand(context,apps,cmd) else {message="Contacts permission is needed";vibeeTone(ToneGenerator.TONE_PROP_NACK)}};pendingCommand=null
    }
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ok->
        if(ok) startVibeeListening(context,recognizer,{text->{message=text;handleVibeeCommand(context,apps,text){pendingCommand=it;contactLauncher.launch(Manifest.permission.READ_CONTACTS)};listening=false}}, {message="I didn't hear that";listening=false;vibeeTone(ToneGenerator.TONE_PROP_NACK)})
        else {message="Microphone permission is needed";listening=false}
    }
    DisposableEffect(Unit){onDispose{recognizer.destroy()}}
    Surface(color=Color.Black.copy(alpha=.42f),modifier=Modifier.fillMaxSize()){
        Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
            Surface(shape=RoundedCornerShape(32.dp),color=MaterialTheme.colorScheme.surface.copy(alpha=.98f),shadowElevation=18.dp,modifier=Modifier.fillMaxWidth(.88f).padding(18.dp)){
                Column(Modifier.padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    Box(Modifier.size(132.dp).scale(scale).background(L1voGreen.copy(alpha=.13f),RoundedCornerShape(66.dp)),contentAlignment=Alignment.Center){
                        Text("⌒  ⌒",color=L1voGreen,style=MaterialTheme.typography.headlineLarge)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("VIBEE",color=ink,style=MaterialTheme.typography.headlineSmall)
                    Text(if(listening)"Listening…" else message,color=ink.copy(alpha=.65f))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick={
                        vibeeTone(ToneGenerator.TONE_PROP_BEEP)
                        listening=true
                        message="Listening…"
                        if(androidx.core.content.ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO)==android.content.pm.PackageManager.PERMISSION_GRANTED)
                            startVibeeListening(context,recognizer,{text->{message=text;handleVibeeCommand(context,apps,text){pendingCommand=it;contactLauncher.launch(Manifest.permission.READ_CONTACTS)};listening=false}},{message="I didn't hear that";listening=false;vibeeTone(ToneGenerator.TONE_PROP_NACK)})
                        else launcher.launch(Manifest.permission.RECORD_AUDIO)
                    }){Text(if(listening)"LISTENING" else "TAP TO SPEAK")}
                    TextButton(onClick={vibeeTone(ToneGenerator.TONE_PROP_ACK);onDismiss()}){Text("Close")}
                }
            }
        }
    }
}

private fun startVibeeListening(context:Context,recognizer:SpeechRecognizer,onText:(String)->Unit,onFail:()->Unit){
    runCatching { recognizer.cancel() }
    recognizer.setRecognitionListener(object:android.speech.RecognitionListener{
        override fun onReadyForSpeech(p0:android.os.Bundle?){}
        override fun onBeginningOfSpeech(){}
        override fun onRmsChanged(p0:Float){}
        override fun onBufferReceived(p0:ByteArray?){}
        override fun onEndOfSpeech(){}
        override fun onError(p0:Int){runCatching{recognizer.cancel()};onFail()}
        override fun onResults(b:android.os.Bundle?){val result=b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim().orEmpty();runCatching{recognizer.cancel()};if(result.isBlank())onFail()else onText(result)}
        override fun onPartialResults(p0:android.os.Bundle?){}
        override fun onEvent(p0:Int,p1:android.os.Bundle?){}
    })
    recognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true)
    })
}

private fun handleVibeeCommand(context:Context,apps:List<LaunchableApp>,raw:String,onNeedContacts:(String)->Unit={}){
    val text=raw.trim().lowercase(Locale.getDefault())
    val open=text.removePrefix("open ").trim()
    val app=apps.firstOrNull{
        val label=it.label.lowercase(Locale.getDefault())
        label==open || label.contains(open) || open.contains(label)
    }
    when{
        text.startsWith("open ")&&app!=null->{vibeeTone(ToneGenerator.TONE_PROP_ACK);launch(context,app.intent)}
        text.startsWith("call ")->{
            val target=raw.substringAfter("call ").trim()
            val direct=target.filter{it.isDigit()||it=='+'||it=='*'||it=='#'}
            val hasContacts=androidx.core.content.ContextCompat.checkSelfPermission(context,Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED
            if(!hasContacts){onNeedContacts(target);return}
            val number=if(direct.length>=5)direct else findContactNumber(context,target)
            if(number!=null){
                launch(context,Intent(Intent.ACTION_DIAL,android.net.Uri.parse("tel:"+android.net.Uri.encode(number))))
                vibeeTone(ToneGenerator.TONE_PROP_ACK)
            }else vibeeTone(ToneGenerator.TONE_PROP_NACK)
        }
        text=="open settings"||text=="settings"->{launch(context,Intent(android.provider.Settings.ACTION_SETTINGS));vibeeTone(ToneGenerator.TONE_PROP_ACK)}
        text=="record"||text=="start recording"||text=="record audio"->{runCatching{launch(context,Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION));vibeeTone(ToneGenerator.TONE_PROP_ACK)}.onFailure{vibeeTone(ToneGenerator.TONE_PROP_NACK)}}
        else->vibeeTone(ToneGenerator.TONE_PROP_NACK)
    }
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
