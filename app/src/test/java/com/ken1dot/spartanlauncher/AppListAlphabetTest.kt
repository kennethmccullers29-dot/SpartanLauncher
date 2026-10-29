package com.ken1dot.spartanlauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppListAlphabetTest {
    @Test
    fun sectionHandlesAccentsWhitespaceSymbolsAndNonLatinNames() {
        assertEquals("E", appListSection("  École"))
        assertEquals("A", appListSection("Ångström"))
        assertEquals("#", appListSection("123 Notes"))
        assertEquals("#", appListSection("💬 Chat"))
        assertEquals("#", appListSection("   "))
        assertEquals("東", appListSection("東京"))
    }

    @Test
    fun everyNonLatinSectionHasAReachableJumpButton() {
        val letters = appListJumpLetters(setOf("A", "Z", "東", "Ω"))
        assertEquals(1, letters.count { it == "A" })
        assertEquals(1, letters.count { it == "#" })
        assertTrue("東" in letters)
        assertTrue("Ω" in letters)
        assertEquals(listOf("#", "A", "Z"), orderedAppSections(listOf("Z", "A", "#")))
    }

    @Test
    fun jumpOffsetsIncludeHeadersAndUpdateAfterFiltering() {
        assertEquals(
            mapOf("A" to 0, "C" to 3, "東" to 5),
            appListSectionStarts(listOf("A" to 2, "C" to 1, "東" to 1))
        )
        assertEquals(
            mapOf("C" to 0, "東" to 2),
            appListSectionStarts(listOf("C" to 1, "東" to 1))
        )
    }

    @Test
    fun searchIgnoresAccentsCaseAndOuterWhitespace() {
        assertTrue(matchesAppSearch("Café Notes", " CAFE "))
        assertTrue(matchesAppSearch("École", "éCO"))
        assertTrue(matchesAppSearch("Discord", "   "))
        assertFalse(matchesAppSearch("Discord", "Reddit"))
    }
}
