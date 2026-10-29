package com.example.spartanlauncher

/** Wide tiles get two rows of four. Large squares get four by four. The medium square gets a 3 by 3 the tile clips. Small tiles get one circle. */
internal fun peopleCircleGrid(widthDp: Float, heightDp: Float): Pair<Int, Int> = when {
    widthDp < 90f && heightDp < 90f -> 1 to 1
    widthDp > heightDp * 1.4f -> 6 to 3
    widthDp > 160f && heightDp > 160f -> 6 to 6
    else -> 3 to 3
}
