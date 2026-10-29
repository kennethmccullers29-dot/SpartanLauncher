package com.example.spartanlauncher

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

internal const val SETUP_COMPLETE_KEY = "setup_complete_v1"
internal const val SETUP_LANGUAGE_KEY = "setup_language"

internal data class SetupLanguage(
    val tag: String, val name: String, val welcome: String, val prompt: String,
    val next: String, val ready: String, val description: String, val start: String, val back: String
)

internal val setupLanguages = listOf(
    SetupLanguage("en", "English (United States)", "Welcome", "Let's get started. Choose your language.",
        "next", "You're all set", "Your Start screen is ready. Make it yours by resizing and moving your tiles.",
        "let's go", "back"),
    SetupLanguage("es", "Español", "Bienvenido", "Comencemos. Elige tu idioma.",
        "siguiente", "Todo listo", "Tu pantalla de Inicio está lista. Personalízala cambiando el tamaño y la posición de tus mosaicos.",
        "comenzar", "atrás"),
    SetupLanguage("fr", "Français", "Bienvenue", "Commençons. Choisissez votre langue.",
        "suivant", "Tout est prêt", "Votre écran d’accueil est prêt. Personnalisez la taille et la position de vos tuiles.",
        "c'est parti", "retour"),
    SetupLanguage("de", "Deutsch", "Willkommen", "Los geht’s. Wähle deine Sprache.",
        "weiter", "Alles bereit", "Deine Startseite ist bereit. Passe die Größe und Position deiner Kacheln an.",
        "los geht’s", "zurück"),
    SetupLanguage("pt", "Português (Brasil)", "Bem-vindo", "Vamos começar. Escolha seu idioma.",
        "próximo", "Tudo pronto", "Sua tela inicial está pronta. Personalize o tamanho e a posição dos seus blocos.",
        "começar", "voltar")
)

internal val setupLanguageOptions = buildList {
    val german = setupLanguages.first { it.tag == "de" }
    listOf("de" to "Deutsch (Deutschland)", "de-LI" to "Deutsch (Liechtenstein)",
        "de-LU" to "Deutsch (Luxemburg)", "de-AT" to "Deutsch (Österreich)",
        "de-CH" to "Deutsch (Schweiz)").forEach { (tag, name) -> add(german.copy(tag = tag, name = name)) }
    val english = setupLanguages.first { it.tag == "en" }
    listOf("en-AU" to "English (Australia)", "en-BZ" to "English (Belize)",
        "en-CA" to "English (Canada)", "en-HK" to "English (Hong Kong SAR)",
        "en-GB" to "English (United Kingdom)", "en" to "English (United States)")
        .forEach { (tag, name) -> add(english.copy(tag = tag, name = name)) }
    addAll(setupLanguages.filter { it.tag !in listOf("en", "de") })
}

@Composable
internal fun LauncherSetup(initialLanguage: String, loading: Boolean, onFinish: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var languageTag by rememberSaveable { mutableStateOf(initialLanguage) }
    var page by rememberSaveable { mutableIntStateOf(0) }
    val language = setupLanguageOptions.firstOrNull { it.tag == languageTag }
        ?: setupLanguages.firstOrNull { it.tag == languageTag.substringBefore("-") } ?: setupLanguages.first()
    BackHandler { if (page > 0) page-- }
    Column(Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding()) {
        AnimatedContent(page, modifier = Modifier.weight(1f), transitionSpec = {
            val direction = if (targetState > initialState) 1 else -1
            (slideInHorizontally(tween(320)) { it / 4 * direction } + fadeIn(tween(240))) togetherWith
                (slideOutHorizontally(tween(240)) { -it / 4 * direction } + fadeOut(tween(180)))
        }, label = "Setup page") { step ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(12.dp))
                Text(when (step) { 0 -> language.welcome; 1 -> permissionSetupText(language.tag.substringBefore("-"))[0]; else -> language.ready }, color = Color.White,
                    fontFamily = MetroRegular, fontWeight = FontWeight.Light, fontSize = 28.sp)
                Spacer(Modifier.height(10.dp))
                if (step != 0) {
                    Text(if (step == 1) permissionSetupText(language.tag.substringBefore("-"))[1] else language.description,
                        color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp)
                    Spacer(Modifier.height(24.dp))
                }
                if (step == 1) SetupPermissions(language.tag.substringBefore("-"))
                if (step == 0) setupLanguageOptions.forEach { option ->
                    val selected = option.tag == language.tag
                    Row(Modifier.fillMaxWidth()
                        .selectable(selected, role = Role.RadioButton, onClick = { languageTag = option.tag })
                        .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(option.name, color = if (selected) Color(0xFF5570F0) else Color.White,
                            fontFamily = MetroRegular, fontSize = 22.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (page > 0) SetupButton(language.back, Modifier.weight(1f)) { page-- }
            else SetupButton(when (language.tag.substringBefore("-")) {
                "es" -> "Teléfono"; "fr" -> "Téléphone"; "de" -> "Telefon"; "pt" -> "Telefone"
                else -> "Phone"
            }, Modifier.weight(1f)) {
                try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_DIAL)) }
                catch (_: android.content.ActivityNotFoundException) {
                    android.widget.Toast.makeText(context, "No phone app installed", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            SetupButton(if (page < 2) language.next else language.start,
                Modifier.weight(1f), enabled = !loading) {
                if (page < 2) page++ else onFinish(language.tag)
            }
        }
    }
}

@Composable
private fun SetupButton(label: String, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(modifier.heightIn(min = 48.dp).background(Color(0xFF202020))
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(12.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text(label, color = Color.White.copy(alpha = if (enabled) 1f else .4f),
            fontFamily = MetroRegular, fontSize = 18.sp)
    }
}

@Preview(widthDp = 360, heightDp = 720)
@Composable
private fun LauncherSetupPreview() = LauncherSetup("en-AU", false) {}
