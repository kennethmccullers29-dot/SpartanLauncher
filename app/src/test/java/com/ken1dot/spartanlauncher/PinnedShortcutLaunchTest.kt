package com.ken1dot.spartanlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PinnedShortcutLaunchTest {
    @Test fun contactKeySplitsIntoPackageAndShortcutId() {
        assertEquals(
            "com.example.spartanpeople" to "contact-device:17",
            pinnedShortcutSource("pinned.shortcut/com.example.spartanpeople/contact-device:17"),
        )
    }

    @Test fun regularAppKeysHaveNoShortcutSource() {
        assertNull(pinnedShortcutSource("com.example.spartanpeople/com.example.spartanpeople.MainActivity"))
        assertNull(pinnedShortcutSource("pinned.shortcut/onlypackage"))
    }
}
