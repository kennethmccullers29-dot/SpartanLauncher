package com.ken1dot.spartanlauncher

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import org.json.JSONException

@Composable
internal fun WeatherSettingsContent(
    options: WeatherPreferences,
    weather: WeatherTileState,
    accentColor: Color,
    onChange: (WeatherPreferences) -> Unit,
    onRefresh: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<WeatherPlace>>(emptyList()) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var searching by remember { mutableStateOf(false) }
    var searchVersion by remember { mutableIntStateOf(0) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    Text("Location", color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp)
    Row(Modifier.fillMaxWidth().metroTilt { onChange(options.copy(place = null)) },
        verticalAlignment = Alignment.CenterVertically) {
        MetroRadio(options.place == null, onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = accentColor, unselectedColor = Color.Gray))
        Column(Modifier.weight(1f)) {
            Text("Use my current location", color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp)
            Text("Uses your phone's location permission", color = SettingsSecondary,
                fontFamily = MetroRegular, fontSize = 14.sp)
        }
    }
    options.place?.let {
        Text("Selected city: ${it.name}", color = accentColor, fontFamily = MetroRegular, fontSize = 16.sp)
    }
    BasicTextField(query, onValueChange = {
        searchVersion++
        searchJob?.cancel()
        query = it
        searching = false
        results = emptyList()
        searchError = null
    }, singleLine = true, cursorBrush = SolidColor(accentColor),
        textStyle = TextStyle(color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp),
        modifier = Modifier.fillMaxWidth().border(2.dp, Color.Gray).padding(12.dp),
        decorationBox = { field ->
            Box {
                if (query.isEmpty()) Text("Find a city", color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 18.sp)
                field()
            }
        })
    Text(if (searching) "Searching…" else "Search",
        color = if (query.trim().length >= 2 && !searching) accentColor else Color.Gray,
        fontFamily = MetroRegular, fontSize = 18.sp,
        modifier = Modifier.border(2.dp, Color.Gray)
            .clickable(enabled = query.trim().length >= 2 && !searching) {
                searchJob?.cancel()
                val version = ++searchVersion
                val cityQuery = query
                searchJob = scope.launch {
                    searching = true
                    searchError = null
                    try {
                        results = searchWeatherPlaces(cityQuery)
                        if (results.isEmpty()) searchError = "No cities found. Try a nearby city or add a country."
                    } catch (_: IOException) {
                        searchError = "Couldn't search for cities. Check your connection and try again."
                    } catch (_: JSONException) {
                        searchError = "City search is unavailable. Try again shortly."
                    } finally {
                        if (searchVersion == version) searching = false
                    }
                }
            }.padding(horizontal = 20.dp, vertical = 10.dp))
    searchError?.let { Text(it, color = Color(0xFFFFB4AB), fontFamily = MetroRegular, fontSize = 14.sp) }
    results.forEach { place ->
        Text(place.name, color = accentColor, fontFamily = MetroRegular, fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth().metroTilt {
                onChange(options.copy(place = place))
                results = emptyList()
                query = ""
            }.padding(vertical = 12.dp))
    }
    Text("Temperature", color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp)
    WeatherUnits.entries.forEach { unit ->
        Row(Modifier.fillMaxWidth().metroTilt { onChange(options.copy(units = unit)) },
            verticalAlignment = Alignment.CenterVertically) {
            MetroRadio(options.units == unit, onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = accentColor, unselectedColor = Color.Gray))
            Text(unit.label, color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp)
        }
    }
    Row(Modifier.fillMaxWidth().metroTilt { onChange(options.copy(showBackground = !options.showBackground)) },
        verticalAlignment = Alignment.CenterVertically) {
        Checkbox(options.showBackground, onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = accentColor, uncheckedColor = Color.Gray))
        Text("Weather backgrounds", color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp)
    }
    Text(if (weather.loading) "Updating weather…" else "Refresh weather",
        color = accentColor, fontFamily = MetroRegular, fontSize = 18.sp,
        modifier = Modifier.fillMaxWidth().clickable(enabled = !weather.loading, onClick = onRefresh)
            .padding(vertical = 12.dp))
    weather.error?.let { Text(it, color = Color(0xFFFFB4AB), fontFamily = MetroRegular, fontSize = 14.sp) }
    weather.updatedAt?.let {
        Text("Updated " + java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(java.util.Date(it)),
            color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 14.sp)
    }
    Text("Weather data: Open-Meteo · Location search: GeoNames",
        color = SettingsSecondary, fontFamily = MetroRegular, fontSize = 12.sp)
}

@androidx.compose.ui.tooling.preview.Preview(widthDp = 400, heightDp = 900)
@Composable
private fun WeatherSettingsPreview() {
    val accent = Color(0xFF0078D7)
    MetroSettingsPage("Weather", accent, {}) {
        WeatherSettingsContent(WeatherPreferences(), WeatherTileState(), accent, {}, {})
    }
}
