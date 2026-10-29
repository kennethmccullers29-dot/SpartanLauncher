package com.ken1dot.spartanlauncher

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumeRefreshTest {
    @Test fun freshWeatherReadingIsKept() {
        val updatedAt = 1_000L
        assertFalse(weatherNeedsRefresh(updatedAt, updatedAt + WEATHER_REFRESH_INTERVAL_MILLIS - 1,
            unitsMatch = true, hasReading = true, force = false))
    }

    @Test fun weatherRefreshesAtTheAgeLimitOnTapOrWhenUnitsChange() {
        val updatedAt = 1_000L
        assertTrue(weatherNeedsRefresh(updatedAt, updatedAt + WEATHER_REFRESH_INTERVAL_MILLIS,
            unitsMatch = true, hasReading = true, force = false))
        assertTrue(weatherNeedsRefresh(updatedAt, updatedAt + 1,
            unitsMatch = true, hasReading = true, force = true))
        assertTrue(weatherNeedsRefresh(updatedAt, updatedAt + 1,
            unitsMatch = false, hasReading = true, force = false))
        assertTrue(weatherNeedsRefresh(null, updatedAt,
            unitsMatch = true, hasReading = false, force = false))
    }

    @Test fun samePhotoIdsSkipDecodeUntilTheLibraryOrLastErrorChanges() {
        assertFalse(shouldReloadPhotos(listOf(1L, 2L), listOf(1L, 2L), hasError = false))
        assertTrue(shouldReloadPhotos(listOf(1L, 2L), listOf(2L, 3L), hasError = false))
        assertTrue(shouldReloadPhotos(listOf(1L), listOf(1L), hasError = true))
        assertTrue(shouldReloadPhotos(emptyList(), null, hasError = false))
    }

    @Test fun iconIsReusedOnlyWhenLabelAndPackageUpdateTimeMatch() {
        assertTrue(canReuseLaunchableIcon("Phone", 10L, "Phone", 10L))
        assertFalse(canReuseLaunchableIcon("Phone", 10L, "Phone", 11L))
        assertFalse(canReuseLaunchableIcon("Phone", 10L, "Dialer", 10L))
        assertFalse(canReuseLaunchableIcon(null, null, "Phone", 10L))
    }

    @Test fun unchangedPackagesSkipTheAppReload() {
        assertTrue(launchableReloadSkipped(emptyList(), hasApps = true))
        assertFalse(launchableReloadSkipped(emptyList(), hasApps = false))
        assertFalse(launchableReloadSkipped(null, hasApps = true))
        assertFalse(launchableReloadSkipped(listOf("com.example.app"), hasApps = true))
    }

    @Test fun cachedIconLivesUntilTheAppUpdates() {
        // Cache written after the app's last update: still good.
        assertTrue(cachedIconValid(cacheWrittenAt = 2_000L, packageUpdatedAt = 1_000L))
        assertTrue(cachedIconValid(cacheWrittenAt = 1_000L, packageUpdatedAt = 1_000L))
        // The app updated after the icon was cached: decode it fresh.
        assertFalse(cachedIconValid(cacheWrittenAt = 1_000L, packageUpdatedAt = 2_000L))
        // No cache file, or an app whose update time is unknown: no cache.
        assertFalse(cachedIconValid(cacheWrittenAt = 0L, packageUpdatedAt = 1_000L))
        assertFalse(cachedIconValid(cacheWrittenAt = 1_000L, packageUpdatedAt = 0L))
    }
}
