package com.ken1dot.spartanlauncher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class PhotosTileContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun libraryShrinkingDuringCrossfadeKeepsOutgoingPhotoValid() {
        val first = ImageBitmap(32, 32)
        val second = ImageBitmap(32, 32)
        val state = mutableStateOf(PhotosTileState(photos = listOf(first, second)))
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.size(160.dp)) {
                PhotosTileContent(
                    animationsActive = true,
                    state = state.value,
                    tileLabel = "Photos",
                    modifier = Modifier.size(160.dp)
                )
            }
        }
        compose.mainClock.advanceTimeBy(8_200L)
        compose.runOnIdle { state.value = PhotosTileState(photos = listOf(first)) }
        compose.mainClock.advanceTimeBy(1_200L)
        compose.onNodeWithText("Photos").assertIsDisplayed()
        compose.runOnIdle { state.value = PhotosTileState() }
        compose.mainClock.advanceTimeBy(1_200L)
        compose.onNodeWithText("No recent photos").assertIsDisplayed()
    }

    @Test
    fun pausedTileSurvivesPhotoReplacementAndResume() {
        val active = mutableStateOf(false)
        val state = mutableStateOf(PhotosTileState(photos = listOf(ImageBitmap(32, 32))))
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.size(160.dp)) {
                PhotosTileContent(active.value, state.value, "Photos", Modifier.size(160.dp))
            }
        }
        compose.mainClock.advanceTimeBy(10_000L)
        compose.runOnIdle {
            state.value = PhotosTileState(photos = listOf(ImageBitmap(32, 32), ImageBitmap(32, 32)))
            active.value = true
        }
        compose.mainClock.advanceTimeBy(9_500L)
        compose.onNodeWithText("Photos").assertIsDisplayed()
    }
}
