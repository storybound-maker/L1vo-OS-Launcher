package com.l1vo.oslauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun appKey(pkg: String) = pkg.replace(Regex("[^A-Za-z0-9_.-]"), "_")
private fun appPref(pkg: String, key: String) = "app_${key}_${appKey(pkg)}"

fun appDisplayName(c: Context, a: LaunchableApp): String =
    c.getSharedPreferences(PREFS, 0)
        .getString(appPref(a.packageName, "name"), null)
        ?.takeIf { it.isNotBlank() } ?: a.label

fun appFont(c: Context, pkg: String): String =
    c.getSharedPreferences(PREFS, 0).getString(appPref(pkg, "font"), "Sans") ?: "Sans"

fun appIconUri(c: Context, pkg: String): String? =
    c.getSharedPreferences(PREFS, 0).getString(appPref(pkg, "icon"), null)

fun appNotificationSound(c: Context, pkg: String): String? =
    c.getSharedPreferences(PREFS, 0).getString(appPref(pkg, "sound"), null)

fun isAppLocked(c: Context, pkg: String): Boolean =
    c.getSharedPreferences(PREFS, 0)
        .getString(appPref(pkg, "pin"), null)
        ?.isNotBlank() == true

fun setAppPin(c: Context, pkg: String, pin: String?) {
    c.getSharedPreferences(PREFS, 0).edit().apply {
        if (pin.isNullOrBlank()) {
            remove(appPref(pkg, "pin"))
        } else {
            putString(appPref(pkg, "pin"), pin)
        }
    }.apply()
}

fun clearAppCustomization(c: Context, pkg: String) {
    c.getSharedPreferences(PREFS, 0).edit()
        .remove(appPref(pkg, "name"))
        .remove(appPref(pkg, "font"))
        .remove(appPref(pkg, "color"))
        .remove(appPref(pkg, "icon"))
        .remove(appPref(pkg, "sound"))
        .apply()
}

@Composable
fun AppActionMenu(
    app: LaunchableApp,
    ink: Color,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    var showInfo by remember { mutableStateOf(false) }
    var showLock by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(appDisplayName(context, app)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                TextButton(
                    onClick = {
                        if (isAppLocked(context, app.packageName)) {
                            showLock = true
                        } else {
                            launch(context, app.intent)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open", color = ink)
                }

                TextButton(
                    onClick = { showInfo = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Info", color = ink)
                }

                TextButton(
                    onClick = { showLock = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isAppLocked(context, app.packageName)) "Unlock" else "Lock", color = ink)
                }

                TextButton(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_DELETE,
                                    Uri.parse("package:${app.packageName}")
                                )
                            )
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Uninstall", color = ink)
                }

                TextButton(
                    onClick = {
                        onDismiss()
                        onEdit()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit", color = ink)
                }
            }
        },
        confirmButton = {}
    )

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("App info") },
            text = {
                Column {
                    Text("Name: ${appDisplayName(context, app)}")
                    Text("Package: ${app.packageName}", style = MaterialTheme.typography.bodySmall)
                    Text("Installed application", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showLock) {
        AlertDialog(
            onDismissRequest = {
                showLock = false
                pin = ""
                error = false
            },
            title = {
                Text(if (isAppLocked(context, app.packageName)) "Enter PIN" else "Set app PIN")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it.filter(Char::isDigit).take(12) },
                        label = { Text("PIN") },
                        singleLine = true
                    )
                    if (error) {
                        Text("Incorrect PIN", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val existing = context.getSharedPreferences(PREFS, 0)
                            .getString(appPref(app.packageName, "pin"), null)

                        if (existing == null) {
                            if (pin.length >= 4) {
                                setAppPin(context, app.packageName, pin)
                                showLock = false
                                pin = ""
                            }
                        } else if (pin == existing) {
                            showLock = false
                            pin = ""
                        } else {
                            error = true
                        }
                    }
                ) {
                    Text(if (isAppLocked(context, app.packageName)) "Unlock" else "Set")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLock = false
                        pin = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AppEditorScreen(
    app: LaunchableApp,
    ink: Color,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(PREFS, 0)

    var name by remember { mutableStateOf(appDisplayName(context, app)) }
    var font by remember { mutableStateOf(appFont(context, app.packageName)) }
    var color by remember {
        mutableStateOf(
            prefs.getString(appPref(app.packageName, "color"), "auto") ?: "auto"
        )
    }
    var iconUri by remember { mutableStateOf(appIconUri(context, app.packageName)) }
    var soundUri by remember { mutableStateOf(appNotificationSound(context, app.packageName)) }

    val iconPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            iconUri = uri.toString()
        }
    }

    val soundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            soundUri = uri.toString()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(
                top = 20.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = 24.dp
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ink)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "Edit app",
                    color = ink,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(appDisplayName(context, app), color = L1voGreen)
            }
            Icon(Icons.Outlined.Edit, "Edit", tint = L1voGreen)
        }

        Spacer(Modifier.height(18.dp))

        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .96f)
            )
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Presentation",
                    color = ink,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("App name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Font", color = ink, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Sans", "Serif", "Mono", "Cursive").forEach { value ->
                        FilterChip(
                            selected = font == value,
                            onClick = { font = value },
                            label = { Text(value) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text("Name color", color = ink, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("auto", "green", "white", "warm").forEach { value ->
                        FilterChip(
                            selected = color == value,
                            onClick = { color = value },
                            label = { Text(value) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedButton(
                    onClick = { iconPicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Image, "Icon")
                    Spacer(Modifier.width(8.dp))
                    Text(if (iconUri == null) "Choose custom icon" else "Change custom icon")
                }

                OutlinedButton(
                    onClick = { soundPicker.launch(arrayOf("audio/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Notifications, "Sound")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (soundUri == null) {
                            "Notification sound: default"
                        } else {
                            "Notification sound selected"
                        }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            clearAppCustomization(context, app.packageName)
                            onSaved()
                            onBack()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset")
                    }

                    Button(
                        onClick = {
                            prefs.edit()
                                .putString(appPref(app.packageName, "name"), name)
                                .putString(appPref(app.packageName, "font"), font)
                                .putString(appPref(app.packageName, "color"), color)
                                .apply()

                            if (iconUri == null) {
                                prefs.edit().remove(appPref(app.packageName, "icon")).apply()
                            } else {
                                prefs.edit().putString(
                                    appPref(app.packageName, "icon"),
                                    iconUri
                                ).apply()
                            }

                            if (soundUri == null) {
                                prefs.edit().remove(appPref(app.packageName, "sound")).apply()
                            } else {
                                prefs.edit().putString(
                                    appPref(app.packageName, "sound"),
                                    soundUri
                                ).apply()
                            }

                            onSaved()
                            onBack()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun CustomAppIcon(app: LaunchableApp, modifier: Modifier) {
    val context = LocalContext.current
    val uri = appIconUri(context, app.packageName)
    var bitmap by remember(uri) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(uri) {
        bitmap = uri?.let {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver
                        .openInputStream(Uri.parse(it))
                        ?.use { stream -> android.graphics.BitmapFactory.decodeStream(stream) }
                }.getOrNull()
            }
        }
    }

    if (bitmap != null) {
        Image(bitmap!!.asImageBitmap(), app.label, modifier)
    } else {
        Image(app.icon.asImageBitmap(), app.label, modifier)
    }
}
