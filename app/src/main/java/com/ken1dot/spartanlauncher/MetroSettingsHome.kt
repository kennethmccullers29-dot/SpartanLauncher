package com.ken1dot.spartanlauncher

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val SettingsBackground = Color.Black
internal val SettingsSecondary = Color(0xFF9A9A9A)

internal enum class MetroSettingsCategory(val title: String, val subtitle: String) {
    System("System", "Display, notifications, battery"),
    Devices("Devices", "Bluetooth, camera"),
    Network("Network & wireless", "Wi-Fi, airplane mode, cellular"),
    Personalization("Personalization", "Start, colors, sounds, lock screen, themes"),
    Weather("Extras", "Weather, calendar live tile"),
    Accounts("Accounts", "Your accounts, email, sync"),
    Language("Time & language", "Speech, region, keyboard"),
    Accessibility("Ease of Access", "Text size, narrator, high contrast"),
    Privacy("Privacy", "Location, camera, permissions"),
    Security("Update & security", "Updates, security, backup")
}

@Composable
internal fun MetroSettingsHome(
    query: String,
    onQueryChange: (String) -> Unit,
    onCategory: (MetroSettingsCategory) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val categories = MetroSettingsCategory.entries.filter {
        ("${it.title} ${it.subtitle}" + if (it == MetroSettingsCategory.Personalization) " background wallpaper accent color glance hello truesight pin clock" else if (it == MetroSettingsCategory.Weather) " weather calendar" else if (it == MetroSettingsCategory.System) " spartan bar status" else "")
            .contains(query.trim(), ignoreCase = true)
    }
    Column(modifier.fillMaxSize().background(SettingsBackground).imePadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsGlyph(null, Modifier.size(24.dp), accentColor)
            Spacer(Modifier.width(12.dp))
            Text("Settings", color = Color.White, fontFamily = MetroRegular,
                fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
        Row(
            Modifier.padding(horizontal = 14.dp).fillMaxWidth().height(36.dp)
                .background(Color.White).border(2.dp, if (query.isEmpty()) Color(0xFFCCCCCC) else accentColor)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                query, onQueryChange, singleLine = true,
                textStyle = TextStyle(color = Color.Black, fontFamily = MetroRegular, fontSize = 16.sp),
                cursorBrush = SolidColor(accentColor),
                modifier = Modifier.weight(1f),
                decorationBox = { field ->
                    Box {
                        if (query.isEmpty()) Text("Find a setting", color = Color(0xFF767676),
                            fontFamily = MetroRegular, fontSize = 16.sp)
                        field()
                    }
                }
            )
            Icon(painterResource(R.drawable.ic_mdl2_search), contentDescription = null,
                modifier = Modifier.size(18.dp), tint = Color.Black)
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.weight(1f)) {
            LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 20.dp)) {
                items(categories, key = { it.name }) { category ->
                    Row(
                        Modifier.fillMaxWidth().metroTilt { onCategory(category) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsGlyph(category, Modifier.size(34.dp), accentColor)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(category.title, color = Color.White, fontFamily = MetroRegular, fontSize = 17.sp)
                            Text(category.subtitle, color = SettingsSecondary, fontFamily = MetroRegular,
                                fontSize = 13.sp, lineHeight = 17.sp)
                        }
                    }
                }
                if (categories.isEmpty()) item {
                    Text("No settings found", color = SettingsSecondary, fontFamily = MetroRegular,
                        fontSize = 18.sp, modifier = Modifier.padding(24.dp))
                }
            }
            // A quiet Windows-style scroll indicator, derived from the visible rows.
            Canvas(Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(3.dp)) {
                val info = listState.layoutInfo
                if (info.totalItemsCount > info.visibleItemsInfo.size && info.visibleItemsInfo.isNotEmpty()) {
                    val fraction = info.visibleItemsInfo.size.toFloat() / info.totalItemsCount
                    val first = info.visibleItemsInfo.first()
                    val position = (first.index + (-first.offset).coerceAtLeast(0).toFloat() / first.size.coerceAtLeast(1)) / info.totalItemsCount
                    drawRect(Color(0xFF555555), Offset(0f, size.height * position),
                        Size(size.width, size.height * fraction))
                }
            }
        }
    }
}

/** Fluent MDL2 icon for a settings category; [category] null is the Settings gear. */
private fun mdl2SettingsIcon(category: MetroSettingsCategory?): Int = when (category) {
    null -> R.drawable.ic_mdl2_settings
    MetroSettingsCategory.System -> R.drawable.ic_mdl2_cell_phone
    MetroSettingsCategory.Devices -> R.drawable.ic_mdl2_devices_2
    MetroSettingsCategory.Network -> R.drawable.ic_mdl2_globe
    MetroSettingsCategory.Personalization -> R.drawable.ic_mdl2_personalize
    MetroSettingsCategory.Weather -> R.drawable.ic_mdl2_sunny
    MetroSettingsCategory.Accounts -> R.drawable.ic_mdl2_contact
    MetroSettingsCategory.Language -> R.drawable.ic_mdl2_locale_language
    MetroSettingsCategory.Accessibility -> R.drawable.ic_mdl2_wheelchair
    MetroSettingsCategory.Privacy -> R.drawable.ic_mdl2_lock
    MetroSettingsCategory.Security -> R.drawable.ic_mdl2_sync
}

@Composable
internal fun SettingsGlyph(category: MetroSettingsCategory?, modifier: Modifier = Modifier, color: Color = Color(0xFF0078D7)) {
    Icon(painterResource(mdl2SettingsIcon(category)), contentDescription = null,
        modifier = modifier, tint = color)
}

@Composable
internal fun MetroSettingsPage(
    category: String,
    accentColor: Color,
    onBack: () -> Unit,
    iconCategory: String = category,
    parentTitle: String = "Settings",
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(SettingsBackground)) {
        // W10M page header: small uppercase app/parent title, big page title.
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsGlyph(MetroSettingsCategory.entries.firstOrNull { it.title == iconCategory },
                Modifier.size(18.dp), accentColor)
            Spacer(Modifier.width(10.dp))
            Text(L(parentTitle).uppercase(), color = Color.White, fontFamily = MetroRegular,
                fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 0.5.sp)
        }
        Text(category, Modifier.padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 10.dp),
            color = Color.White, fontFamily = MetroRegular, fontWeight = FontWeight.Light, fontSize = 34.sp, lineHeight = 40.sp)
        Column(
            Modifier.weight(1f).fillMaxWidth()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Preview(widthDp = 400, heightDp = 680, showBackground = true)
@Composable
private fun MetroSettingsPagePreview() {
    val accent = Color(0xFF8764B8)
    MetroSettingsPage("System", accent, {}) {
        listOf("Display" to "Brightness, screen timeout, text size",
            "Battery" to "Battery use and power saving",
            "Live tile notifications" to "Notification access is enabled").forEach { (title, subtitle) ->
            Column(Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, color = accent, fontFamily = MetroRegular, fontSize = 20.sp)
                Text(subtitle, color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 16.sp)
            }
        }
    }
}

@Preview(widthDp = 400, heightDp = 680, showBackground = true)
@Composable
private fun MetroSettingsHomePreview() {
    MetroSettingsHome("", {}, {}, Color(0xFF0078D7))
}

