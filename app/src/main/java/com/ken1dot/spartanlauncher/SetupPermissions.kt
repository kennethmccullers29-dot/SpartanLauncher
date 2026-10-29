package com.ken1dot.spartanlauncher

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

internal fun permissionSetupText(tag: String): List<String> = when (tag) {
    "es" -> listOf("Permisos", "Activa los mosaicos dinámicos. Todo es opcional y puedes cambiarlo después en Ajustes.",
        "Ubicación", "El tiempo en tu ciudad", "Contactos", "Fotos en el mosaico Personas", "Fotos", "Tus imágenes en Inicio",
        "Calendario", "Tu próxima cita", "Notificaciones", "Contadores, mensajes y música", "permitir", "permitido", "abrir ajustes")
    "fr" -> listOf("Autorisations", "Activez les tuiles dynamiques. Tout est facultatif et modifiable dans les paramètres.",
        "Position", "La météo de votre ville", "Contacts", "Photos sur la tuile Contacts", "Photos", "Vos images sur l’accueil",
        "Calendrier", "Votre prochain rendez-vous", "Notifications", "Compteurs, messages et musique", "autoriser", "autorisé", "paramètres")
    "de" -> listOf("Berechtigungen", "Aktiviere Live-Kacheln. Alles ist optional und später in den Einstellungen änderbar.",
        "Standort", "Wetter in deiner Stadt", "Kontakte", "Bilder auf der Kontakte-Kachel", "Fotos", "Deine Bilder auf Start",
        "Kalender", "Dein nächster Termin", "Benachrichtigungen", "Zähler, Nachrichten und Musik", "erlauben", "erlaubt", "Einstellungen")
    "pt" -> listOf("Permissões", "Ative os blocos dinâmicos. Tudo é opcional e pode ser alterado nas configurações.",
        "Localização", "Clima da sua cidade", "Contatos", "Fotos no bloco Pessoas", "Fotos", "Suas imagens na tela inicial",
        "Calendário", "Seu próximo compromisso", "Notificações", "Contadores, mensagens e música", "permitir", "permitido", "configurações")
    "zh" -> listOf("权限", "让动态磁贴动起来。这些都是可选的，你可以稍后在设置中更改。",
        "位置", "你所在城市的天气", "联系人", "人脉磁贴上的联系人照片", "照片", "开始屏幕上的图片",
        "日历", "你的下一个约会", "通知", "磁贴计数、消息预览和音乐", "允许", "已允许", "打开设置")
    else -> listOf("Permissions", "Bring your live tiles to life. These are optional; you can change them later in Settings.",
        "Location", "Weather for your current city", "Contacts", "Contact photos on the People tile", "Photos", "Your pictures on Start",
        "Calendar", "Your next appointment", "Notifications", "Tile counts, message previews and music", "allow", "allowed", "open settings")
}

private fun Context.setupActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.setupActivity()
    else -> null
}

@Composable
internal fun SetupPermissions(language: String) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var revision by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        busy = false
        revision++
        CalendarAccess.revision++
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) revision++
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val text = permissionSetupText(language)
    val photoPermissions = when {
        Build.VERSION.SDK_INT >= 34 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val groups = listOf(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
        arrayOf(Manifest.permission.READ_CONTACTS), photoPermissions, arrayOf(Manifest.permission.READ_CALENDAR))
    // Re-evaluate on resume, including returns from Android's special-access settings.
    val granted = remember(revision) { groups.map { group ->
        group.any { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    } + (context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context)) }
    val preferences = remember { context.getSharedPreferences("launcher", Context.MODE_PRIVATE) }
    fun appSettings() {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
    }
    repeat(5) { index ->
        val allowed = granted[index]
        Column(Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
            Text(text[2 + index * 2], color = Color.White, fontFamily = MetroRegular, fontSize = 22.sp)
            Text(text[3 + index * 2], color = Color.White.copy(alpha = .85f), fontFamily = MetroRegular, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            val requested = preferences.getBoolean("setup_permission_$index", false)
            val blocked = index < 4 && requested && !allowed &&
                groups[index].all { context.setupActivity()?.shouldShowRequestPermissionRationale(it) == false }
            Box(Modifier.fillMaxWidth().border(2.dp, Color.White.copy(alpha = if (allowed) .45f else 1f))
                .clickable(enabled = !allowed && !busy, role = Role.Button) {
                    if (index == 4) {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    } else if (blocked) appSettings() else {
                        preferences.edit().putBoolean("setup_permission_$index", true).apply()
                        if (index == 3) preferences.edit().putBoolean("calendar_permission_requested", true).apply()
                        busy = true
                        launcher.launch(groups[index])
                    }
                }.padding(12.dp)) {
                Text(if (allowed) "✓  " + text[13] else if (index == 4 || blocked) text[14] else text[12],
                    color = Color.White, fontFamily = MetroRegular, fontSize = 16.sp)
            }
        }
    }
}
