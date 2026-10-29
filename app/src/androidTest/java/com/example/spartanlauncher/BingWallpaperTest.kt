package com.example.spartanlauncher

import android.content.Context
import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.IOException

class BingWallpaperTest {
    @Test fun parsesCreditAndRelativeImageAddress() {
        val value = parseBingWallpaper(JSONObject("""{"images":[{"url":"/th?id=photo.jpg","copyright":"Photo credit","wp":true}]}"""))
        assertEquals("https://www.bing.com/th?id=photo.jpg", value.url)
        assertEquals("Photo credit", value.credit)
    }

    @Test(expected = IOException::class)
    fun rejectsExternalImageAddress() {
        parseBingWallpaper(JSONObject("""{"images":[{"url":"https://example.com/photo.jpg"}]}"""))
    }

    @Test(expected = IOException::class)
    fun respectsUnavailableWallpaperFlag() {
        parseBingWallpaper(JSONObject("""{"images":[{"url":"/photo.jpg","wp":false}]}"""))
    }

    @Test fun downloadsAndRestoresDailyWallpaperFromCache() = runBlocking {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(base.cacheDir, "bing-wallpaper-test").apply { mkdirs() }
        val testContext = object : ContextWrapper(base) {
            override fun getFilesDir() = directory
            override fun getSharedPreferences(name: String, mode: Int) =
                base.getSharedPreferences("bing-wallpaper-test", Context.MODE_PRIVATE)
        }
        val prefs = testContext.getSharedPreferences("launcher", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        try {
            val (image, credit) = loadBingWallpaper(testContext, true)
            assertTrue(image.width > 0 && image.height > 0)
            assertTrue(maxOf(image.width, image.height) <= 3840)
            assertTrue(credit.isNotBlank())
            val cached = cachedBingWallpaper(testContext)
            assertNotNull(cached)
            assertEquals(image.width, cached!!.width)
            assertEquals(credit, loadBingWallpaper(testContext, false).second)
        } finally {
            prefs.edit().clear().commit()
            directory.deleteRecursively()
        }
    }
}
