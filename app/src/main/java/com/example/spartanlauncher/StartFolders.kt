package com.example.spartanlauncher

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

internal data class StartFolder(val id: String, val name: String, val members: List<String>, val size: String = "Medium")

/** Members remain pinned so dissolving a folder restores the original tiles and sizes. */
internal fun visibleStartFolders(folders: List<StartFolder>, pins: List<String>): List<StartFolder> {
    val claimed = mutableSetOf<String>()
    return folders.mapNotNull { folder ->
        val members = folder.members.distinct().filter { it in pins && it !in claimed }
        if (members.size < 2) null else folder.copy(members = members).also { claimed.addAll(members) }
    }
}

/** Insert below the entire occupied row, including large tiles that cross that row. */
internal fun folderExpansionRow(initialRow: Int, occupiedRows: List<Pair<Int, Int>>): Int {
    var row = initialRow
    while (true) {
        val end = occupiedRows.filter { (start, count) -> start < row && start + count > row }
            .maxOfOrNull { (start, count) -> start + count } ?: return row
        row = end
    }
}

internal fun mergeStartFolders(folders: List<StartFolder>, source: String, target: String, newId: String): List<StartFolder> {
    if (source == target) return folders
    val from = folders.firstOrNull { source in it.members }
    val into = folders.firstOrNull { target in it.members }
    if (from != null && from.id == into?.id) return folders
    val members = ((into?.members ?: listOf(target)) + (from?.members ?: listOf(source))).distinct()
    val merged = StartFolder(into?.id ?: newId, into?.name ?: "Folder", members, into?.size ?: "Medium")
    return folders.filterNot { it.id == from?.id || it.id == into?.id } + merged
}

internal data class FolderGridCell(val column: Int, val row: Int, val columns: Int, val rows: Int)

internal fun folderGridCells(sizes: List<Pair<Int, Int>>, columns: Int = 6): List<FolderGridCell> {
    val occupied = mutableSetOf<Pair<Int, Int>>()
    return sizes.map { (width, height) ->
        val w = width.coerceIn(1, columns)
        val h = height.coerceIn(1, 4)
        var row = 0
        var column: Int
        while (true) {
            column = (0..columns - w).firstOrNull { col ->
                (row until row + h).all { r -> (col until col + w).all { c -> c to r !in occupied } }
            } ?: -1
            if (column >= 0) break
            row++
        }
        for (r in row until row + h) for (c in column until column + w) occupied += c to r
        FolderGridCell(column, row, w, h)
    }
}

internal object StartFolderStore {
    var folders by mutableStateOf<List<StartFolder>>(emptyList())
        private set

    fun load(context: Context) {
        folders = try {
            val array = JSONArray(context.getSharedPreferences("launcher", Context.MODE_PRIVATE)
                .getString("start_folders_v1", "[]"))
            List(array.length()) { index ->
                val value = array.getJSONObject(index)
                val members = value.getJSONArray("members")
                StartFolder(value.getString("id"), value.optString("name", "Folder"),
                    List(members.length()) { members.getString(it) }, value.optString("size", "Medium"))
            }
        } catch (_: org.json.JSONException) { emptyList() }
    }

    fun save(context: Context, values: List<StartFolder>) {
        val array = JSONArray()
        values.forEach {
            array.put(JSONObject().put("id", it.id).put("name", it.name)
                .put("members", JSONArray(it.members.distinct())).put("size", it.size))
        }
        context.getSharedPreferences("launcher", Context.MODE_PRIVATE).edit()
            .putString("start_folders_v1", array.toString()).apply()
        folders = values
    }
}
