package com.ken1dot.spartanlauncher

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import java.util.Locale

/**
 * UI text lives in res/values/ui_strings.xml (translated in values-zh-rCN).
 * Call sites keep their English text as the key; [L] looks up the localized
 * resource, so navigation paths that use English titles keep working.
 */
private val uiStringIds: Map<String, Int> = mapOf(
    "About your phone" to R.string.ui_about_your_phone,
    "Accent color" to R.string.ui_accent_color,
    "Accessibility" to R.string.ui_accessibility,
    "Accessibility settings" to R.string.ui_accessibility_settings,
    "Accounts" to R.string.ui_accounts,
    "Acrylic blur behind Start tiles" to R.string.ui_acrylic_blur_behind_start_tiles,
    "Airplane mode & cellular" to R.string.ui_airplane_mode_cellular,
    "All apps" to R.string.ui_all_apps,
    "All day" to R.string.ui_all_day,
    "Allow calendar access in App permissions to show appointments" to R.string.ui_allow_calendar_access_in_app_permissions,
    "Allow contacts to see people here" to R.string.ui_allow_contacts_to_see_people_here,
    "Allow location access or choose a city in Settings → Weather" to R.string.ui_allow_location_access_or_choose_a_city_i,
    "Allow photo access to show recent pictures" to R.string.ui_allow_photo_access_to_show_recent_pictur,
    "Android’s status bar stays visible until SpartanBar is running." to R.string.ui_android_s_status_bar_stays_visible_until,
    "App permissions" to R.string.ui_app_permissions,
    "Asked when you swipe up. Fingerprint or face unlock appears as Windows Hello." to R.string.ui_asked_when_you_swipe_up_fingerprint_or_f,
    "Available networks and connections" to R.string.ui_available_networks_and_connections,
    "Background" to R.string.ui_background,
    "Background, People tile" to R.string.ui_background_people_tile,
    "Background, clock, Glance screen, Windows Hello, PIN" to R.string.ui_background_clock_glance_screen_windows_h,
    "Battery" to R.string.ui_battery,
    "Battery use and power saving" to R.string.ui_battery_use_and_power_saving,
    "Bing daily wallpaper" to R.string.ui_bing_daily_wallpaper,
    "Bing image of the day" to R.string.ui_bing_image_of_the_day,
    "Bing is temporarily unavailable" to R.string.ui_bing_is_temporarily_unavailable,
    "Bing is unavailable right now" to R.string.ui_bing_is_unavailable_right_now,
    "Bluetooth" to R.string.ui_bluetooth,
    "Bluetooth, camera" to R.string.ui_bluetooth_camera,
    "Brightness, screen timeout, text size" to R.string.ui_brightness_screen_timeout_text_size,
    "Browse" to R.string.ui_browse,
    "Calendar live tile" to R.string.ui_calendar_live_tile,
    "Camera permissions" to R.string.ui_camera_permissions,
    "Celsius (°C)" to R.string.ui_celsius_c,
    "Check the current PIN and use 4 or more digits" to R.string.ui_check_the_current_pin_and_use_4_or_more_,
    "Choose a photo" to R.string.ui_choose_a_photo,
    "Choose apps" to R.string.ui_choose_apps,
    "Choose at least two apps" to R.string.ui_choose_at_least_two_apps,
    "Choose the icons used on Start and in the app list. Messaging keeps its own icon." to R.string.ui_choose_the_icons_used_on_start_and_in_th,
    "Choose which apps can use your camera" to R.string.ui_choose_which_apps_can_use_your_camera,
    "City search is unavailable. Try again shortly." to R.string.ui_city_search_is_unavailable_try_again_sho,
    "Classic" to R.string.ui_classic,
    "Clear search" to R.string.ui_clear_search,
    "Clock style" to R.string.ui_clock_style,
    "Colors" to R.string.ui_colors,
    "Connect and manage devices" to R.string.ui_connect_and_manage_devices,
    "Connect to SpartanBar" to R.string.ui_connect_to_spartanbar,
    "Connected · transparent on Start" to R.string.ui_connected_transparent_on_start,
    "Could not load that photo" to R.string.ui_could_not_load_that_photo,
    "Could not read that photo" to R.string.ui_could_not_read_that_photo,
    "Couldn't open that photo" to R.string.ui_couldn_t_open_that_photo,
    "Couldn't save the photo" to R.string.ui_couldn_t_save_the_photo,
    "Couldn't determine your location. Check that location is turned on." to R.string.ui_couldn_t_determine_your_location_check_t,
    "Couldn't search for cities. Check your connection and try again." to R.string.ui_couldn_t_search_for_cities_check_your_co,
    "Couldn't refresh · tap to retry" to R.string.ui_couldn_t_refresh_tap_to_retry,
    "Current PIN" to R.string.ui_current_pin,
    "Current location" to R.string.ui_current_location,
    "Custom color" to R.string.ui_custom_color,
    "Date & time" to R.string.ui_date_time,
    "Device information and software updates" to R.string.ui_device_information_and_software_updates,
    "Device security and protection" to R.string.ui_device_security_and_protection,
    "Devices" to R.string.ui_devices,
    "Display" to R.string.ui_display,
    "Display, notifications, battery" to R.string.ui_display_notifications_battery,
    "Dissolve folder" to R.string.ui_dissolve_folder,
    "Ease of Access" to R.string.ui_ease_of_access,
    "Enable notification counts and previews for pinned apps" to R.string.ui_enable_notification_counts_and_previews_,
    "Extras" to R.string.ui_extras,
    "Fahrenheit (°F)" to R.string.ui_fahrenheit_f,
    "Find a city" to R.string.ui_find_a_city,
    "Find a setting" to R.string.ui_find_a_setting,
    "Glance screen" to R.string.ui_glance_screen,
    "Glass tiles" to R.string.ui_glass_tiles,
    "Glassy tiles with your background showing through" to R.string.ui_glassy_tiles_with_your_background_showin,
    "Greeting name" to R.string.ui_greeting_name,
    "Icon pack" to R.string.ui_icon_pack,
    "Input methods and typing" to R.string.ui_input_methods_and_typing,
    "Install or update SpartanLock to change these settings." to R.string.ui_install_or_update_spartanlock_to_change_,
    "Jump to other scripts" to R.string.ui_jump_to_other_scripts,
    "Keyboard" to R.string.ui_keyboard,
    "Language" to R.string.ui_language,
    "Live tile notifications" to R.string.ui_live_tile_notifications,
    "Live tile notifications are on" to R.string.ui_live_tile_notifications_are_on,
    "Loading apps" to R.string.ui_loading_apps,
    "Loading people..." to R.string.ui_loading_people,
    "Loading photos..." to R.string.ui_loading_photos,
    "Loading wallpaper…" to R.string.ui_loading_wallpaper,
    "Location" to R.string.ui_location,
    "Location access and services" to R.string.ui_location_access_and_services,
    "Location permission is required" to R.string.ui_location_permission_is_required,
    "Location services are unavailable" to R.string.ui_location_services_are_unavailable,
    "Location, camera, permissions" to R.string.ui_location_camera_permissions,
    "Location, units, live tile background" to R.string.ui_location_units_live_tile_background,
    "Lock screen" to R.string.ui_lock_screen,
    "Long-press an app in All apps and choose Pin to Start." to R.string.ui_long_press_an_app_in_all_apps_and_choose,
    "Manage access to your personal information" to R.string.ui_manage_access_to_your_personal_informati,
    "Manage accounts and sync" to R.string.ui_manage_accounts_and_sync,
    "Modern" to R.string.ui_modern,
    "Moving artist and song text in SpartanLock while music plays" to R.string.ui_moving_artist_and_song_text_in_spartanlo,
    "Network & wireless" to R.string.ui_network_wireless,
    "New PIN (4+ digits)" to R.string.ui_new_pin_4_digits,
    "New notification" to R.string.ui_new_notification,
    "No apps found" to R.string.ui_no_apps_found,
    "No cities found. Try a nearby city or add a country." to R.string.ui_no_cities_found_try_a_nearby_city_or_add,
    "No icon packs installed." to R.string.ui_no_icon_packs_installed,
    "No phone app installed" to R.string.ui_no_phone_app_installed,
    "No recent photos" to R.string.ui_no_recent_photos,
    "No settings found" to R.string.ui_no_settings_found,
    "No upcoming events" to R.string.ui_no_upcoming_events,
    "Notification access is enabled" to R.string.ui_notification_access_is_enabled,
    "Notifications & actions" to R.string.ui_notifications_actions,
    "Open SpartanBar" to R.string.ui_open_spartanbar,
    "PIN (not set)" to R.string.ui_pin_not_set,
    "PIN removed" to R.string.ui_pin_removed,
    "PIN saved" to R.string.ui_pin_saved,
    "People tile" to R.string.ui_people_tile,
    "Personalization" to R.string.ui_personalization,
    "Photos mixed with accent blocks" to R.string.ui_photos_mixed_with_accent_blocks,
    "Pin to Start" to R.string.ui_pin_to_start,
    "Privacy" to R.string.ui_privacy,
    "Refresh wallpaper" to R.string.ui_refresh_wallpaper,
    "Refresh weather" to R.string.ui_refresh_weather,
    "Region and display language" to R.string.ui_region_and_display_language,
    "Remove wallpaper" to R.string.ui_remove_wallpaper,
    "Rename folder" to R.string.ui_rename_folder,
    "Return to the plain background" to R.string.ui_return_to_the_plain_background,
    "Ringtones and notification sounds" to R.string.ui_ringtones_and_notification_sounds,
    "Rows of circles that roll" to R.string.ui_rows_of_circles_that_roll,
    "Same as Start" to R.string.ui_same_as_start,
    "Search" to R.string.ui_search,
    "Search apps" to R.string.ui_search_apps,
    "Security" to R.string.ui_security,
    "Set PIN" to R.string.ui_set_pin,
    "Settings" to R.string.ui_settings,
    "Show the time and notifications on a dim black screen after the screen turns off" to R.string.ui_show_the_time_and_notifications_on_a_dim,
    "Solid-color tiles" to R.string.ui_solid_color_tiles,
    "Sounds" to R.string.ui_sounds,
    "SpartanLock needs its accessibility service on to show over the lock screen." to R.string.ui_spartanlock_needs_its_accessibility_serv,
    "Speech, region, keyboard" to R.string.ui_speech_region_keyboard,
    "Start" to R.string.ui_start,
    "Start is empty" to R.string.ui_start_is_empty,
    "Start layout style, glass tiles" to R.string.ui_start_layout_style_glass_tiles,
    "Start style" to R.string.ui_start_style,
    "Start, colors, sounds, lock screen, themes" to R.string.ui_start_colors_sounds_lock_screen_themes,
    "Status bar look and items" to R.string.ui_status_bar_look_and_items,
    "System" to R.string.ui_system,
    "Tap to allow access to contacts" to R.string.ui_tap_to_allow_access_to_contacts,
    "Tap to allow location for weather" to R.string.ui_tap_to_allow_location_for_weather,
    "Tap to allow photo access" to R.string.ui_tap_to_allow_photo_access,
    "Tap to set up weather" to R.string.ui_tap_to_set_up_weather,
    "Temperature" to R.string.ui_temperature,
    "Text size, narrator, high contrast" to R.string.ui_text_size_narrator_high_contrast,
    "Text size, screen reader, and interaction" to R.string.ui_text_size_screen_reader_and_interaction,
    "That PIN is incorrect" to R.string.ui_that_pin_is_incorrect,
    "Themes" to R.string.ui_themes,
    "Tile counts, message previews and music" to R.string.ui_tile_counts_message_previews_and_music,
    "Time & language" to R.string.ui_time_language,
    "Time zone and automatic time" to R.string.ui_time_zone_and_automatic_time,
    "Turn on location services to get local weather." to R.string.ui_turn_on_location_services_to_get_local_w,
    "Unpin from Start" to R.string.ui_unpin_from_start,
    "Update & security" to R.string.ui_update_security,
    "Updates, security, backup" to R.string.ui_updates_security_backup,
    "Updating weather" to R.string.ui_updating_weather,
    "Updating weather…" to R.string.ui_updating_weather_2,
    "Use an image behind your Start tiles" to R.string.ui_use_an_image_behind_your_start_tiles,
    "Use my current location" to R.string.ui_use_my_current_location,
    "Uses your phone's location permission" to R.string.ui_uses_your_phone_s_location_permission,
    "Weather" to R.string.ui_weather,
    "Weather backgrounds" to R.string.ui_weather_backgrounds,
    "Weather data unavailable" to R.string.ui_weather_data_unavailable,
    "Weather for your current city" to R.string.ui_weather_for_your_current_city,
    "Weather service is unavailable. Try again shortly." to R.string.ui_weather_service_is_unavailable_try_again,
    "Weather unavailable" to R.string.ui_weather_unavailable,
    "Weather, calendar live tile" to R.string.ui_weather_calendar_live_tile,
    "Wi-Fi" to R.string.ui_wi_fi,
    "Wi-Fi, airplane mode, cellular" to R.string.ui_wi_fi_airplane_mode_cellular,
    "Windows Hello" to R.string.ui_windows_hello,
    "Wireless and mobile network settings" to R.string.ui_wireless_and_mobile_network_settings,
    "Your accounts" to R.string.ui_your_accounts,
    "Your accounts, email, sync" to R.string.ui_your_accounts_email_sync,
    "Zune animated text" to R.string.ui_zune_animated_text,
    "App language" to R.string.ui_app_language,
    "Display language for Spartan Launcher" to R.string.ui_display_language_for_spartan_launcher,
    "Use system language" to R.string.ui_use_system_language,
    "Choose the language Spartan Launcher uses. This doesn't change your phone's language." to R.string.ui_choose_the_language_spartan_launcher_use
)

@Composable
internal fun L(english: String): String = uiStringIds[english]?.let { stringResource(it) } ?: english

internal fun Context.l(english: String): String = uiStringIds[english]?.let(::getString) ?: english

/** Package-wide Text: same as Material 3 Text, but localizes known UI strings. */
@Composable
internal fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = androidx.compose.material3.LocalTextStyle.current
) {
    androidx.compose.material3.Text(text = L(text), modifier = modifier, color = color, fontSize = fontSize,
        fontStyle = fontStyle, fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
        textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight, overflow = overflow,
        softWrap = softWrap, maxLines = maxLines, minLines = minLines, onTextLayout = onTextLayout, style = style)
}

@Composable
internal fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = androidx.compose.material3.LocalTextStyle.current
) {
    androidx.compose.material3.Text(text = text, modifier = modifier, color = color, fontSize = fontSize,
        fontStyle = fontStyle, fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
        textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight, overflow = overflow,
        softWrap = softWrap, maxLines = maxLines, minLines = minLines, inlineContent = inlineContent,
        onTextLayout = onTextLayout, style = style)
}

/** In-app language override: "" follows the system, otherwise a BCP-47 tag. */
internal object AppLanguage {
    private const val KEY = "app_language"
    val options = listOf("" to "Use system language", "en" to "English", "zh-CN" to "简体中文")

    fun current(context: Context): String =
        context.getSharedPreferences("launcher", Context.MODE_PRIVATE).getString(KEY, "") ?: ""

    fun save(context: Context, tag: String) {
        context.getSharedPreferences("launcher", Context.MODE_PRIVATE).edit().putString(KEY, tag).commit()
    }

    /** Wraps a base context with the chosen locale (works on API 26+). */
    fun wrap(base: Context): Context {
        val tag = current(base)
        if (tag.isEmpty()) {
            // Follow the system again: undo any process-wide default left by a previous choice,
            // so date/time formatters and Locale.getDefault() callers stop using the old language.
            val system = android.content.res.Resources.getSystem().configuration.locales
            Locale.setDefault(system[0])
            android.os.LocaleList.setDefault(system)
            return base
        }
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        android.os.LocaleList.setDefault(android.os.LocaleList(locale))
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }

    fun apply(activity: Activity, tag: String) {
        if (tag == current(activity)) return
        save(activity, tag)
        // Keep the first-start screen in step with the app language if setup is revisited.
        activity.getSharedPreferences("launcher", Context.MODE_PRIVATE).edit()
            .putString(SETUP_LANGUAGE_KEY, tag.ifEmpty {
                android.content.res.Resources.getSystem().configuration.locales[0].toLanguageTag() }).commit()
        activity.recreate()
    }
}

/** Leaves Settings / All apps and lands on Start (used by the system Home gesture). */
internal object SettingsHome { var handler: (() -> Unit)? = null }
