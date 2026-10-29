package com.ken1dot.spartanlauncher

import org.junit.Assert.*
import org.junit.Test

class StartFoldersTest {
    @Test fun unavailableMembersDoNotHideRemainingApp() {
        val folder = StartFolder("one", "Social", listOf("a", "b"))
        assertTrue(visibleStartFolders(listOf(folder), listOf("a")).isEmpty())
        assertEquals(listOf("a", "b"), folder.members)
    }

    @Test fun eachPinnedAppAppearsInAtMostOneFolder() {
        val folders = listOf(StartFolder("one", "First", listOf("a", "b", "a")),
            StartFolder("two", "Second", listOf("b", "c", "d")))
        val visible = visibleStartFolders(folders, listOf("a", "b", "c", "d"))
        assertEquals(listOf("a", "b"), visible[0].members)
        assertEquals(listOf("c", "d"), visible[1].members)
    }

    @Test fun unpinningOneMemberKeepsFolderWithTwoRemainingMembers() {
        val folder = StartFolder("one", "Social", listOf("a", "b", "c"))
        assertEquals(listOf("b", "c"), visibleStartFolders(listOf(folder), listOf("b", "c")).single().members)
    }

    @Test fun expansionSkipsLargeTilesCrossingTheFolderRow() {
        assertEquals(4, folderExpansionRow(2, listOf(0 to 2, 0 to 4, 4 to 2)))
    }

    @Test fun expansionHandlesAChainOfOverlappingRows() {
        assertEquals(6, folderExpansionRow(2, listOf(0 to 4, 3 to 3, 6 to 2)))
    }

    @Test fun droppingTwoAppsCreatesFolderAtTarget() {
        val result = mergeStartFolders(emptyList(), "a", "b", "new").single()
        assertEquals(listOf("b", "a"), result.members)
        assertEquals("Medium", result.size)
    }

    @Test fun droppingAnAppIntoFolderKeepsNameAndSize() {
        val folder = StartFolder("existing", "Social", listOf("b", "c"), "Wide")
        val result = mergeStartFolders(listOf(folder), "a", "b", "new").single()
        assertEquals("existing", result.id)
        assertEquals("Social", result.name)
        assertEquals("Wide", result.size)
        assertEquals(listOf("b", "c", "a"), result.members)
    }

    @Test fun mergingFoldersFlattensMembersWithoutDuplicates() {
        val first = StartFolder("first", "First", listOf("a", "b"))
        val second = StartFolder("second", "Second", listOf("c", "d"))
        val result = mergeStartFolders(listOf(first, second), "a", "c", "new").single()
        assertEquals("second", result.id)
        assertEquals(listOf("c", "d", "a", "b"), result.members)
        assertEquals(listOf(result), mergeStartFolders(listOf(result), "a", "b", "unused"))
    }

    @Test fun mixedFolderGridKeepsWideAndSmallTileSpansWithoutOverlap() {
        val cells = folderGridCells(listOf(2 to 2, 4 to 2, 2 to 2, 2 to 2, 1 to 1, 1 to 1))
        val occupied = mutableSetOf<Pair<Int, Int>>()
        cells.forEach { cell ->
            assertTrue(cell.column + cell.columns <= 6)
            for (row in cell.row until cell.row + cell.rows)
                for (column in cell.column until cell.column + cell.columns)
                    assertTrue(occupied.add(column to row))
        }
        assertEquals(4, cells[1].columns)
        assertEquals(1, cells.last().columns)
    }

    @Test fun expansionKeepsFreeRowImmediatelyBelowFolder() {
        assertEquals(2, folderExpansionRow(2, listOf(0 to 2, 2 to 4)))
    }
}
