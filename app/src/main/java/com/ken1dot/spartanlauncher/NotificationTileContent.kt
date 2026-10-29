package com.ken1dot.spartanlauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun NotificationTileContent(
    preview: TileNotificationPreview,
    label: String,
    color: Color,
    count: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier.background(color)) {
        val wide = maxWidth > maxHeight * 1.4f
        val large = maxHeight >= 220.dp
        val fontScale = LocalDensity.current.fontScale
        val tileHeight = maxHeight.value
        preview.image?.let {
            // On see-through tiles the picture is a tint, not a solid slab over the wallpaper.
            val see = color.alpha < 1f
            Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = if (see) .45f else 1f)
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = if (see) .3f else .6f)))
        }
        Column(Modifier.fillMaxSize().padding(if (wide || large) 12.dp else 8.dp)) {
            val title = preview.title?.takeIf { it.isNotBlank() }
            val titleSize = if (wide || large) 17 else 14
            val bodySize = if (large) 20 else if (wide) 16 else 13
            val footerSize = 11
            val reserved = (if (title != null) titleSize * 1.25f else 0f) + footerSize * 1.25f
            val bodyHeight = tileHeight - (if (wide || large) 24 else 16) - 8 - reserved * fontScale
            val lines = (bodyHeight / (bodySize * 1.25f * fontScale)).toInt().coerceIn(1, if (large) 6 else 3)
            if (title != null) {
                Text(title, color = Color.White, fontFamily = MetroRegular, fontSize = titleSize.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
            }
            // Weight constrains the preview to the space above the permanently visible footer.
            Box(Modifier.weight(1f)) {
                if (preview.text != title) Text(preview.text, color = Color.White.copy(alpha = .9f),
                    fontFamily = MetroRegular, fontSize = bodySize.sp, maxLines = lines,
                    overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(label, color = Color.White, fontFamily = MetroRegular, fontSize = footerSize.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (count > 0) Text(if (count > 99) "99+" else count.toString(), color = Color.White,
                    fontFamily = MetroRegular, fontSize = footerSize.sp, maxLines = 1)
            }
        }
    }
}

@Preview(widthDp = 112, heightDp = 112)
@Composable
private fun NotificationTilePreview() = NotificationTileContent(
    TileNotificationPreview("Alex", "Are we still meeting for lunch tomorrow?", null),
    "Messages", Color(0xFF0078D7), 3, Modifier.fillMaxSize()
)

@Preview(widthDp = 240, heightDp = 112)
@Composable
private fun WideNotificationTilePreview() = NotificationTilePreview()
