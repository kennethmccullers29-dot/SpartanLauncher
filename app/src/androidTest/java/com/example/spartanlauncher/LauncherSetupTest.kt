package com.example.spartanlauncher

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class LauncherSetupTest {
    @get:Rule val compose = createComposeRule()

    @Test fun languageSelectionAndBackKeepChosenLanguage() {
        var result: String? = null
        compose.setContent { LauncherSetup("en", false) { result = it } }
        compose.onNodeWithText("Español").performScrollTo().performClick()
        compose.onNodeWithText("Bienvenido").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("siguiente").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Permisos").assertIsDisplayed()
        compose.onNodeWithText("atrás").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Bienvenido").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("siguiente").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Permisos").assertIsDisplayed()
        compose.onNodeWithText("siguiente").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Todo listo").assertIsDisplayed()
        compose.onNodeWithText("comenzar").performClick()
        compose.runOnIdle { assertEquals("es", result) }
    }

    @Test fun cannotFinishWhileAppsAreLoading() {
        compose.setContent { LauncherSetup("en", true) {} }
        compose.onNodeWithText("next").assertIsNotEnabled()
    }
}
