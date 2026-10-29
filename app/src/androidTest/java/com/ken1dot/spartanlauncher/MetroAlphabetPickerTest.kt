package com.ken1dot.spartanlauncher

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MetroAlphabetPickerTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun unavailableLettersDoNotSelectOrDismissAndAvailableLettersSelect() {
        var selected: String? = null
        var dismissals = 0
        compose.setContent {
            MetroAlphabetPicker(
                availableSections = setOf("A", "R"),
                accentColor = Color.Blue,
                onSelect = { selected = it },
                onDismiss = { dismissals++ }
            )
        }
        compose.onNodeWithContentDescription("Jump to B")
            .assertIsNotEnabled().performTouchInput { click(center) }
        compose.runOnIdle {
            assertEquals(null, selected)
            assertEquals(0, dismissals)
        }
        compose.onNodeWithContentDescription("Jump to R").performTouchInput { click(center) }
        compose.runOnIdle {
            assertEquals("R", selected)
            assertEquals(0, dismissals)
        }
    }
}
