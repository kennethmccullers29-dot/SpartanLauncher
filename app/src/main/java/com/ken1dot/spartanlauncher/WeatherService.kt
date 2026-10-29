package com.ken1dot.spartanlauncher

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.Geocoder
import android.os.Build
import java.util.Locale
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.math.roundToInt
import java.net.URLEncoder

internal data class WeatherTileState(
    val temperature: Int? = null,
    val high: Int? = null,
    val low: Int? = null,
    val condition: String? = null,
    val weatherCode: Int = 0,
    val isDay: Boolean = true,
    val locationName: String = "Current location",
    val feelsLike: Int? = null,
    val humidity: Int? = null,
    val wind: Int? = null,
    val units: WeatherUnits = WeatherUnits.Celsius,
    val showBackground: Boolean = true,
    val updatedAt: Long? = null,
    val loading: Boolean = false,
    val error: String? = null
)

internal const val WEATHER_REFRESH_INTERVAL_MILLIS = 30 * 60 * 1000L

internal fun weatherNeedsRefresh(
    updatedAt: Long?,
    now: Long,
    unitsMatch: Boolean,
    hasReading: Boolean,
    force: Boolean,
    maxAgeMillis: Long = WEATHER_REFRESH_INTERVAL_MILLIS
): Boolean {
    if (force || !hasReading || !unitsMatch || updatedAt == null) return true
    return now - updatedAt >= maxAgeMillis
}

internal suspend fun loadCurrentWeather(
    context: Context,
    options: WeatherPreferences = WeatherPreferences()
): WeatherTileState {
    val place = options.place ?: withContext(Dispatchers.Main.immediate) {
        val location = findCurrentLocation(context)
        WeatherPlace("Current location", location.latitude, location.longitude)
    }
    return withContext(Dispatchers.IO) {
        val namedPlace = if (options.place == null) place.copy(name = currentWeatherCity(context, place)) else place
        val url = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=${place.latitude}&longitude=${place.longitude}" +
            "&current=temperature_2m,weather_code,is_day,apparent_temperature,relative_humidity_2m,wind_speed_10m" +
            "&daily=temperature_2m_max,temperature_2m_min&forecast_days=1&timezone=auto" +
            "&temperature_unit=${options.units.apiValue}" +
            "&wind_speed_unit=${if (options.units == WeatherUnits.Fahrenheit) "mph" else "kmh"}"
        parseWeatherResponse(readWeatherJson(url), namedPlace, options)
    }
}

internal suspend fun searchWeatherPlaces(query: String): List<WeatherPlace> = withContext(Dispatchers.IO) {
    if (query.trim().length < 2) return@withContext emptyList()
    val root = readWeatherJson("https://geocoding-api.open-meteo.com/v1/search?name=" +
        URLEncoder.encode(query.trim(), "UTF-8") + "&count=8&language=en&format=json")
    val results = root.optJSONArray("results") ?: return@withContext emptyList()
    List(results.length()) { index ->
        val place = results.getJSONObject(index)
        val name = listOf(place.getString("name"), place.optString("admin1"), place.optString("country"))
            .filter { it.isNotBlank() }.distinct().joinToString(", ")
        WeatherPlace(name, place.getDouble("latitude"), place.getDouble("longitude"))
    }
}

internal fun weatherCityLabel(name: String): String =
    name.substringBefore(',').trim().ifEmpty { "Current location" }

@Suppress("DEPRECATION")
private suspend fun currentWeatherCity(context: Context, place: WeatherPlace): String {
    if (!Geocoder.isPresent()) return "Current location"
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = if (Build.VERSION.SDK_INT >= 33) {
            withTimeoutOrNull(5_000L) {
                suspendCancellableCoroutine<List<android.location.Address>> { continuation ->
                    geocoder.getFromLocation(place.latitude, place.longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<android.location.Address>) {
                            if (continuation.isActive) continuation.resume(addresses)
                        }
                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) continuation.resume(emptyList())
                        }
                    })
                }
            }.orEmpty()
        } else geocoder.getFromLocation(place.latitude, place.longitude, 1).orEmpty()
        val address = addresses.firstOrNull()
        weatherCityLabel(address?.locality?.takeIf { it.isNotBlank() }
            ?: address?.subAdminArea?.takeIf { it.isNotBlank() } ?: "Current location")
    } catch (_: IOException) {
        "Current location"
    } catch (_: IllegalArgumentException) {
        "Current location"
    }
}

private suspend fun findCurrentLocation(context: Context): Location {
    val hasFinePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarsePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!hasFinePermission && !hasCoarsePermission) {
        throw SecurityException("Location permission is required")
    }

    val locationManager = context.getSystemService(LocationManager::class.java)
        ?: throw IOException("Location services are unavailable")
    val providers = locationManager.getProviders(true)
        .filter { hasFinePermission || it != LocationManager.GPS_PROVIDER }
    if (providers.isEmpty()) {
        throw IOException("Turn on location services to get local weather.")
    }
    providers.asSequence()
        .sortedBy { if (it == LocationManager.NETWORK_PROVIDER) 0 else 1 }
        .mapNotNull { provider ->
            try {
                locationManager.getLastKnownLocation(provider)
            } catch (_: SecurityException) {
                null
            }
        }
        .filter { System.currentTimeMillis() - it.time < LOCATION_MAX_AGE_MS }
        .maxByOrNull { it.time }
        ?.let { return it }

    for (provider in providers.sortedBy { if (it == LocationManager.NETWORK_PROVIDER) 0 else 1 }) {
        val result = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(location)
                    }
                }
                try {
                    locationManager.requestLocationUpdates(
                        provider,
                        0L,
                        0f,
                        listener,
                        Looper.getMainLooper()
                    )
                } catch (exception: SecurityException) {
                    continuation.cancel(exception)
                }
                continuation.invokeOnCancellation {
                    locationManager.removeUpdates(listener)
                }
            }
        }
        if (result != null) return result
    }
    throw IOException("Couldn't determine your location. Check that location is turned on.")
}

private fun readWeatherJson(url: String): JSONObject {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = NETWORK_TIMEOUT_MS
        readTimeout = NETWORK_TIMEOUT_MS
        requestMethod = "GET"
    }
    try {
        if (connection.responseCode !in 200..299) {
            throw IOException("Weather service is unavailable. Try again shortly.")
        }
        return connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
    } finally {
        connection.disconnect()
    }
}

internal fun parseWeatherResponse(
    root: JSONObject,
    place: WeatherPlace,
    options: WeatherPreferences
): WeatherTileState {
    val current = root.getJSONObject("current")
    val daily = root.getJSONObject("daily")
    return WeatherTileState(
        temperature = current.getDouble("temperature_2m").roundToInt(),
        high = daily.getJSONArray("temperature_2m_max").getDouble(0).roundToInt(),
        low = daily.getJSONArray("temperature_2m_min").getDouble(0).roundToInt(),
        condition = weatherCondition(current.getInt("weather_code")),
        weatherCode = current.getInt("weather_code"),
        isDay = current.optInt("is_day", 1) == 1,
        locationName = weatherCityLabel(place.name),
        feelsLike = current.optDouble("apparent_temperature").takeIf { it.isFinite() }?.roundToInt(),
        humidity = current.optInt("relative_humidity_2m", -1).takeIf { it >= 0 },
        wind = current.optDouble("wind_speed_10m").takeIf { it.isFinite() }?.roundToInt(),
        units = options.units,
        showBackground = options.showBackground,
        updatedAt = System.currentTimeMillis()
    )
}

private fun weatherCondition(code: Int): String = when (code) {
    0 -> "Clear"
    1, 2 -> "Partly cloudy"
    3 -> "Cloudy"
    45, 48 -> "Fog"
    51, 53, 55, 56, 57 -> "Drizzle"
    61, 63, 65, 66, 67 -> "Rain"
    71, 73, 75, 77 -> "Snow"
    80, 81, 82 -> "Showers"
    85, 86 -> "Snow showers"
    95, 96, 99 -> "Thunderstorms"
    else -> "Weather"
}

private const val LOCATION_MAX_AGE_MS = 30 * 60 * 1000L
private const val LOCATION_TIMEOUT_MS = 15_000L
private const val NETWORK_TIMEOUT_MS = 10_000
