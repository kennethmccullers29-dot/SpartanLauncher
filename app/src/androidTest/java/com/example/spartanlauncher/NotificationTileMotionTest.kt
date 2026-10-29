package com.example.spartanlauncher

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NotificationTileMotionTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun contentUpdatesStayVisibleAndMotionResumesAfterPausing() {
        val preview = mutableStateOf<TileNotificationPreview?>(TileNotificationPreview("First", "Message", null))
        val active = mutableStateOf(true)
        lateinit var progress: Animatable<Float, AnimationVector1D>
        compose.mainClock.autoAdvance = false
        compose.setContent {
            progress = rememberNotificationTileFlip("test/tile", preview.value, active.value)
        }
        compose.mainClock.advanceTimeBy(1_700L)
        compose.runOnIdle { assertEquals(1f, progress.value, 0.001f) }

        compose.runOnIdle { preview.value = TileNotificationPreview("Second", "New message", null) }
        compose.mainClock.advanceTimeByFrame()
        compose.runOnIdle { assertEquals(1f, progress.value, 0.001f) }
        compose.mainClock.advanceTimeBy(1_700L)
        compose.runOnIdle { assertEquals(1f, progress.value, 0.001f) }

        compose.runOnIdle { active.value = false }
        compose.mainClock.advanceTimeBy(10_000L)
        compose.runOnIdle { assertEquals(0f, progress.value, 0.001f) }
        compose.runOnIdle { active.value = true }
        compose.mainClock.advanceTimeBy(1_700L)
        compose.runOnIdle { assertEquals(1f, progress.value, 0.001f) }

        compose.runOnIdle { preview.value = null }
        compose.mainClock.advanceTimeByFrame()
        compose.runOnIdle { assertEquals(0f, progress.value, 0.001f) }
    }
}
