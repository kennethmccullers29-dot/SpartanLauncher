package com.example.spartanlauncher

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class WeatherSettingsTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs get() = context.getSharedPreferences("weather-settings-tests", Context.MODE_PRIVATE)

    @After
    fun clear() { prefs.edit().clear().commit() }

    @Test
    fun cityUnitsAndBackgroundSurviveReload() {
        val options = WeatherPreferences(WeatherPlace("London, UK", 51.5, -0.12),
            WeatherUnits.Fahrenheit, false)
        saveWeatherPreferences(prefs, options)
        assertEquals(options, readWeatherPreferences(prefs))
        saveWeatherPreferences(prefs, options.copy(place = null))
        assertNull(readWeatherPreferences(prefs).place)
        assertEquals(WeatherUnits.Fahrenheit, readWeatherPreferences(prefs).units)
    }

    @Test
    fun invalidSavedLocationFallsBackToCurrentLocation() {
        prefs.edit().putString("weather_place",
            """{"name":"Invalid","latitude":200,"longitude":0}""").commit()
        assertNull(readWeatherPreferences(prefs).place)
    }

    @Test
    fun forecastKeepsLocationUnitsAndConditionData() {
        val root = JSONObject("""{
            "current":{"temperature_2m":-1.6,"weather_code":71,"is_day":0,
            "apparent_temperature":-5.2,"relative_humidity_2m":83,"wind_speed_10m":14.6},
            "daily":{"temperature_2m_max":[2.4],"temperature_2m_min":[-4.6]}
        }""")
        val options = WeatherPreferences(WeatherPlace("Test city", 10.0, 20.0), WeatherUnits.Fahrenheit, false)
        val state = parseWeatherResponse(root, options.place!!, options)
        assertEquals(-2, state.temperature)
        assertEquals(2, state.high)
        assertEquals(-5, state.low)
        assertEquals("Snow", state.condition)
        assertEquals(71, state.weatherCode)
        assertFalse(state.isDay)
        assertEquals("Test city", state.locationName)
        assertEquals(-5, state.feelsLike)
        assertEquals(83, state.humidity)
        assertEquals(15, state.wind)
        assertEquals(WeatherUnits.Fahrenheit, state.units)
        assertFalse(state.showBackground)
    }
}
