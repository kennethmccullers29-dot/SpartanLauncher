package com.ken1dot.spartanlauncher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TileEditControlsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun cornerControlsRemainTouchableOnSmallTilesAndAfterResize() {
        val tileSize = mutableStateOf(52.dp)
        var unpins = 0
        var resizes = 0
        var tileClicks = 0
        compose.setContent {
            Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(tileSize.value).clickable { tileClicks++ }
                ) {
                    TileEditControls(
                        label = "Test tile",
                        resizeLabel = "Resize Test tile",
                        expanding = tileSize.value == 52.dp,
                        onUnpin = { unpins++ },
                        onResize = {
                            resizes++
                            tileSize.value = if (tileSize.value == 52.dp) 160.dp else 52.dp
                        }
                    )
                }
            }
        }
        compose.onNodeWithContentDescription("Unpin Test tile").performTouchInput { click(center) }
        compose.onNodeWithContentDescription("Resize Test tile").performTouchInput { click(center) }
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Resize Test tile").performTouchInput { click(center) }
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Unpin Test tile").performTouchInput { click(center) }
        compose.runOnIdle {
            assertEquals(2, unpins)
            assertEquals(2, resizes)
            assertEquals(0, tileClicks)
            assertEquals(52.dp, tileSize.value)
        }
    }
}
