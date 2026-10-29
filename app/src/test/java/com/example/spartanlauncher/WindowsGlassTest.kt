package com.example.spartanlauncher

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowsGlassTest {
    @Test fun glassOnlyOnWindows10MobileTilesOverWallpaper() {
        assertTrue(useGlassTiles(windows10Mobile = true, hasWallpaper = true))
        assertFalse(useGlassTiles(windows10Mobile = false, hasWallpaper = true))
        assertFalse(useGlassTiles(windows10Mobile = true, hasWallpaper = false))
        assertFalse(useGlassTiles(windows10Mobile = true, hasWallpaper = true, coversOwnFace = true))
        assertFalse(useGlassTiles(windows10Mobile = true, hasWallpaper = true, enabled = false))
    }
}
