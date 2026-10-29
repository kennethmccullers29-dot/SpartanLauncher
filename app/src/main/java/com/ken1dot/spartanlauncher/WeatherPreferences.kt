package com.ken1dot.spartanlauncher

import android.content.SharedPreferences
import org.json.JSONObject

internal enum class WeatherUnits(val label: String, val apiValue: String, val windUnit: String) {
    Celsius("Celsius (°C)", "celsius", "km/h"),
    Fahrenheit("Fahrenheit (°F)", "fahrenheit", "mph")
}

internal data class WeatherPlace(val name: String, val latitude: Double, val longitude: Double)

internal data class WeatherPreferences(
    val place: WeatherPlace? = null,
    val units: WeatherUnits = WeatherUnits.Celsius,
    val showBackground: Boolean = true
)

internal fun readWeatherPreferences(prefs: SharedPreferences): WeatherPreferences {
    val place = prefs.getString("weather_place", null)?.let { saved ->
        runCatching {
            val json = JSONObject(saved)
            val latitude = json.getDouble("latitude")
            val longitude = json.getDouble("longitude")
            require(latitude.isFinite() && latitude in -90.0..90.0)
            require(longitude.isFinite() && longitude in -180.0..180.0)
            WeatherPlace(json.getString("name"), latitude, longitude)
        }.getOrNull()
    }
    return WeatherPreferences(place,
        WeatherUnits.entries.firstOrNull { it.name == prefs.getString("weather_units", null) } ?: WeatherUnits.Celsius,
        prefs.getBoolean("weather_background", true))
}

internal fun saveWeatherPreferences(prefs: SharedPreferences, options: WeatherPreferences) {
    val place = options.place?.let {
        JSONObject().put("name", it.name).put("latitude", it.latitude).put("longitude", it.longitude).toString()
    }
    prefs.edit().putString("weather_place", place)
        .putString("weather_units", options.units.name)
        .putBoolean("weather_background", options.showBackground).apply()
}
