package com.ken1dot.spartanlauncher

import org.junit.Assert.assertEquals
import org.junit.Test

class PinnedShortcutMergeTest {
    @Test fun pinningAContactKeepsTheOtherTiles() {
        val start = ShortcutPinMerge(
            pins = setOf("phone", "people", "contact-a"),
            order = listOf("phone", "people", "contact-a"),
        )
        val merged = mergeShortcutPins(start, listOf("contact-a", "contact-b"))
        assertEquals(listOf("phone", "people", "contact-a", "contact-b"), merged.order)
        assertEquals(setOf("phone", "people", "contact-a", "contact-b"), merged.pins)
    }

    @Test fun shortcutAlreadyOrderedButMissingFromThePinSetIsAdded() {
        val start = ShortcutPinMerge(
            pins = setOf("phone"),
            order = listOf("phone", "contact-a"),
        )
        val merged = mergeShortcutPins(start, listOf("contact-a"))
        assertEquals(listOf("phone", "contact-a"), merged.order)
        assertEquals(setOf("phone", "contact-a"), merged.pins)
    }

    @Test fun noNewShortcutsLeavesStartUntouched() {
        val start = ShortcutPinMerge(setOf("phone"), listOf("phone"))
        assertEquals(start, mergeShortcutPins(start, emptyList()))
    }
}
