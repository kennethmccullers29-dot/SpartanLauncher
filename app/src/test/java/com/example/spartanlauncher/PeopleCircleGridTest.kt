package com.example.spartanlauncher

import org.junit.Assert.assertEquals
import org.junit.Test

class PeopleCircleGridTest {
    @Test
    fun smallTileIsOneCircle() {
        assertEquals(1 to 1, peopleCircleGrid(52f, 52f))
    }

    @Test
    fun mediumTileIsThreeByThree() {
        assertEquals(3 to 3, peopleCircleGrid(114f, 114f))
    }

    @Test
    fun wideTileIsTwoRowsOfFour() {
        assertEquals(4 to 2, peopleCircleGrid(240f, 114f))
    }

    @Test
    fun largeTileIsFourByFour() {
        assertEquals(4 to 4, peopleCircleGrid(240f, 240f))
    }
}
