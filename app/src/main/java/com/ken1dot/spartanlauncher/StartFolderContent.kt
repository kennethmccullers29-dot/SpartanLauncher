package com.ken1dot.spartanlauncher

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

internal data class FolderAppChoice(val key: String, val label: String)

@Composable
internal fun StartFolderEditor(
    folder: StartFolder?, choices: List<FolderAppChoice>, initialKey: String?,
    accent: Color, onDismiss: () -> Unit, onSave: (String, List<String>) -> Unit,
    onDissolve: () -> Unit
) {
    var name by rememberSaveable(folder?.id) { mutableStateOf(folder?.name ?: "Folder") }
    var selected by rememberSaveable(folder?.id) { mutableStateOf((folder?.members ?: listOfNotNull(initialKey)).filter { key -> choices.any { it.key == key } }) }
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().heightIn(max = 600.dp).imePadding().background(Color.Black)
            .border(1.dp, Color.Gray).padding(20.dp)) {
            Text(if (folder == null) "new folder" else "edit folder", color = Color.White,
                fontFamily = MetroRegular, fontSize = 28.sp)
            Spacer(Modifier.height(16.dp))
            BasicTextField(name, onValueChange = { name = it.take(40) },
                textStyle = TextStyle(color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp),
                singleLine = true, modifier = Modifier.fillMaxWidth().border(2.dp, accent).padding(12.dp))
            Spacer(Modifier.height(12.dp))
            Text("Choose at least two apps", color = Color.Gray, fontFamily = MetroRegular, fontSize = 14.sp)
            LazyColumn(Modifier.weight(1f)) {
                items(choices, key = { it.key }) { app ->
                    val checked = app.key in selected
                    Row(Modifier.fillMaxWidth().clickable(role = Role.Checkbox) {
                        selected = if (checked) selected - app.key else selected + app.key
                    }.padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (checked) "☑" else "☐", color = if (checked) accent else Color.White, fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(app.label, color = Color.White, fontFamily = MetroRegular, fontSize = 18.sp)
                    }
                }
            }
            if (folder != null) TextButton(onClick = onDissolve) { Text("Dissolve folder", color = accent) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("cancel", color = Color.White) }
                TextButton(enabled = name.isNotBlank() && selected.size >= 2,
                    onClick = { onSave(name.trim(), selected) }) { Text("save", color = accent) }
            }
        }
    }
}

@Composable
internal fun StartFolderMosaic(
    members: List<String>, modifier: Modifier = Modifier,
    icon: @Composable (String, Modifier) -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val footer = if (maxHeight > 80.dp) 20.dp else 0.dp
        val shown = members.take(9)
        FolderTileGrid(
            List(shown.size) { 1 to 1 },
            miniature = true,
            columns = 3,
            modifier = Modifier.fillMaxSize().padding(4.dp).padding(bottom = footer)
        ) { index, cell ->
            icon(shown[index], cell)
        }
    }
}

@Composable
private fun FolderTileGrid(
    sizes: List<Pair<Int, Int>>, miniature: Boolean, modifier: Modifier,
    columns: Int = 6,
    tile: @Composable (Int, Modifier) -> Unit
) {
    val cells = remember(sizes, columns) { folderGridCells(sizes, columns) }
    androidx.compose.ui.layout.Layout(modifier = modifier, content = {
        sizes.indices.forEach { index -> tile(index, Modifier) }
    }) { measurables, constraints ->
        val gap = (if (miniature) 2.dp else 8.dp).roundToPx()
        val rows = cells.maxOfOrNull { it.row + it.rows } ?: 0
        val byWidth = ((constraints.maxWidth - gap * (columns - 1)) / columns.toFloat()).coerceAtLeast(1f)
        val cell = if (miniature && rows > 0) minOf(byWidth,
            ((constraints.maxHeight - gap * (rows - 1)) / rows.toFloat()).coerceAtLeast(1f)) else byWidth
        val places = measurables.mapIndexed { index, measurable ->
            val placement = cells[index]
            measurable.measure(androidx.compose.ui.unit.Constraints.fixed(
                (placement.columns * cell + (placement.columns - 1) * gap).toInt().coerceAtLeast(1),
                (placement.rows * cell + (placement.rows - 1) * gap).toInt().coerceAtLeast(1)))
        }
        val height = (rows * cell + (rows - 1).coerceAtLeast(0) * gap).toInt()
            .coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(constraints.maxWidth, height) {
            places.forEachIndexed { index, placeable ->
                placeable.place((cells[index].column * (cell + gap)).toInt(),
                    (cells[index].row * (cell + gap)).toInt())
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
internal fun StartFolderPanel(
    name: String, count: Int, onEdit: () -> Unit, onClose: () -> Unit,
    modifier: Modifier = Modifier, accent: Color = Color(0xFF0078D7),
    sizes: List<Pair<Int, Int>> = List(count) { 2 to 2 },
    onRename: (String) -> Unit = {},
    startRenaming: Boolean = false,
    onRenamingStarted: () -> Unit = {},
    tile: @Composable (Int, Modifier) -> Unit
) {
    val preview = LocalInspectionMode.current
    val reveal = remember(name) { Animatable(if (preview) 1f else 0f) }
    LaunchedEffect(name) { reveal.animateTo(1f, tween(240)) }
    // W10M: the folder name edits in place. Tap it, type, Enter or tap away saves; back cancels.
    var renaming by remember { mutableStateOf(false) }
    var field by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(name)) }
    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var hadFocus by remember { mutableStateOf(false) }
    fun begin() {
        field = androidx.compose.ui.text.input.TextFieldValue(name, androidx.compose.ui.text.TextRange(0, name.length))
        hadFocus = false
        renaming = true
    }
    fun commit() {
        if (!renaming) return
        renaming = false
        val value = field.text.trim().take(40)
        if (value.isNotEmpty() && value != name) onRename(value)
        focusManager.clearFocus()
    }
    LaunchedEffect(startRenaming) { if (startRenaming) { begin(); onRenamingStarted() } }
    androidx.activity.compose.BackHandler(enabled = renaming) {
        renaming = false
        focusManager.clearFocus()
    }
    Column(modifier.graphicsLayer {
        alpha = reveal.value
        translationY = (1f - reveal.value) * 16.dp.toPx()
    }.pointerInput(renaming) {
        if (!renaming) return@pointerInput
        // Tapping anywhere off the name saves it.
        awaitEachGesture {
            val down = awaitFirstDown(pass = androidx.compose.ui.input.pointer.PointerEventPass.Initial)
            if (down.position.y > 52.dp.toPx()) commit()
        }
    }.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().height(36.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (renaming) {
                LaunchedEffect(Unit) { focus.requestFocus() }
                BasicTextField(field, onValueChange = { field = it.copy(text = it.text.take(40)) },
                    textStyle = TextStyle(color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Light),
                    singleLine = true,
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(accent),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { commit() }),
                    modifier = Modifier.weight(1f).focusRequester(focus)
                        .onFocusChanged { if (it.isFocused) hadFocus = true else if (hadFocus) commit() }
                        .drawBehind {
                            val y = size.height - 1.dp.toPx()
                            drawLine(accent, androidx.compose.ui.geometry.Offset(0f, y),
                                androidx.compose.ui.geometry.Offset(size.width, y), 2.dp.toPx())
                        }.padding(bottom = 4.dp))
            } else {
                Text(name, color = Color.White, fontFamily = MetroRegular, fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 200.dp)
                        .combinedClickable(onClickLabel = "Rename folder", onClick = { begin() },
                            onLongClickLabel = "Choose apps", onLongClick = onEdit))
                Box(Modifier.weight(1f).height(8.dp).background(accent))
            }
        }
        FolderTileGrid(sizes, false, Modifier.fillMaxWidth().weight(1f), tile = tile)
        Box(Modifier.fillMaxWidth().height(24.dp).clickable(role = Role.Button, onClick = onClose),
            contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxWidth().height(8.dp).background(accent))
        }
    }
}

@Preview(widthDp = 360, heightDp = 350)
@Composable
private fun StartFolderPanelPreview() {
    val names = listOf("Phone", "Messages", "Browser", "Mail", "Calendar", "Music")
    StartFolderPanel("Productivity", names.size, {}, {}, Modifier.fillMaxSize().background(Color.Black),
        accent = Color(0xFF623B9D), sizes = listOf(2 to 2, 4 to 2, 2 to 2, 2 to 2, 1 to 1, 1 to 1)) { index, modifier ->
        Box(modifier.background(listOf(Color(0xFF16874A), Color(0xFF0078D7), Color(0xFF185ABD),
            Color(0xFFDA4626), Color(0xFF623B9D), Color(0xFF679B20))[index]).padding(8.dp)) {
            Text(names[index].take(1), color = Color.White, fontSize = 32.sp, modifier = Modifier.align(Alignment.Center))
            Text(names[index], color = Color.White, fontFamily = MetroRegular, fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomStart))
        }
    }
}

@Preview(widthDp = 112, heightDp = 112)
@Composable
private fun StartFolderMosaicPreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFF0078D7))) {
        StartFolderMosaic(listOf("P", "M", "B", "C", "S", "F")) { key, modifier ->
            Box(modifier.background(if (key == "M") Color(0xFFDA4626) else Color(0xFF185ABD)),
                contentAlignment = Alignment.Center) { Text(key, color = Color.White, fontSize = 12.sp) }
        }
        Text("Everyday", color = Color.White, fontFamily = MetroRegular, fontSize = 11.sp,
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
    }
}
// V20FOLDER
