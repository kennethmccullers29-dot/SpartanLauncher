package com.ken1dot.spartanlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

internal enum class MetroSocialBrand(val color: Color) {
    Discord(Color(0xFF5865F2)),
    Reddit(Color(0xFFFF4500)),
    Facebook(Color(0xFF1877F2)),
    Instagram(Color(0xFFC13584)),
    YouTube(Color(0xFFFF0000)),
    Teams(Color(0xFF4B53BC));

    val icon: ImageVector
        get() = when (this) {
            Discord -> MetroDiscord
            Reddit -> MetroReddit
            Facebook -> MetroFacebook
            Instagram -> MetroInstagram
            YouTube -> MetroYouTube
            Teams -> MetroTeams
        }
}

internal fun metroSocialBrandFor(packageName: String, label: String): MetroSocialBrand? {
    // Match application identities, avoiding substring matches such as Facebook Messenger.
    val packageId = packageName.lowercase(Locale.ROOT)
    return when (packageId) {
        "com.discord" -> MetroSocialBrand.Discord
        "com.reddit.frontpage" -> MetroSocialBrand.Reddit
        "com.facebook.katana", "com.facebook.lite" -> MetroSocialBrand.Facebook
        "com.instagram.android", "com.instagram.lite" -> MetroSocialBrand.Instagram
        "com.google.android.youtube" -> MetroSocialBrand.YouTube
        "com.microsoft.teams", "com.microsoft.teams2", "com.microsoft.skype.teams.ipphone" -> MetroSocialBrand.Teams
        else -> when (label.trim().lowercase(Locale.ROOT)) {
            "discord" -> MetroSocialBrand.Discord
            "reddit" -> MetroSocialBrand.Reddit
            "facebook", "facebook lite" -> MetroSocialBrand.Facebook
            "instagram", "instagram lite" -> MetroSocialBrand.Instagram
            "youtube" -> MetroSocialBrand.YouTube
            "teams", "microsoft teams" -> MetroSocialBrand.Teams
            else -> null
        }
    }
}

@Composable
internal fun MetroSocialIcon(
    brand: MetroSocialBrand,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Icon(brand.icon, contentDescription, modifier, tint = Color.White)
}

// Original Metro interpretations: one white glyph, transparent counters, no icon backplates.
private val MetroDiscord by lazy {
    ImageVector.Builder("MetroDiscord", 32.dp, 32.dp, 32f, 32f).apply {
        path(fill = SolidColor(Color.White), pathFillType = PathFillType.EvenOdd) {
            moveTo(10f, 6.5f)
            curveTo(7.6f, 6.9f, 5.7f, 7.7f, 4.3f, 8.5f)
            curveTo(2.3f, 13f, 1.4f, 18f, 2f, 22.6f)
            curveTo(4.4f, 24.4f, 6.5f, 25.4f, 9f, 26f)
            lineTo(10.5f, 23.5f)
            lineTo(8.7f, 22.5f)
            curveTo(13.4f, 24.3f, 18.6f, 24.3f, 23.3f, 22.5f)
            lineTo(21.5f, 23.5f)
            lineTo(23f, 26f)
            curveTo(25.5f, 25.4f, 27.6f, 24.4f, 30f, 22.6f)
            curveTo(30.6f, 18f, 29.7f, 13f, 27.7f, 8.5f)
            curveTo(26.3f, 7.7f, 24.4f, 6.9f, 22f, 6.5f)
            lineTo(21f, 8.5f)
            curveTo(17.7f, 7.9f, 14.3f, 7.9f, 11f, 8.5f)
            close()
            // Eyes are transparent, so the tile color shows through.
            moveTo(12.3f, 17f)
            curveTo(12.3f, 15.6f, 11.4f, 14.5f, 10.2f, 14.5f)
            curveTo(9f, 14.5f, 8.1f, 15.6f, 8.1f, 17f)
            curveTo(8.1f, 18.4f, 9f, 19.5f, 10.2f, 19.5f)
            curveTo(11.4f, 19.5f, 12.3f, 18.4f, 12.3f, 17f)
            close()
            moveTo(23.9f, 17f)
            curveTo(23.9f, 15.6f, 23f, 14.5f, 21.8f, 14.5f)
            curveTo(20.6f, 14.5f, 19.7f, 15.6f, 19.7f, 17f)
            curveTo(19.7f, 18.4f, 20.6f, 19.5f, 21.8f, 19.5f)
            curveTo(23f, 19.5f, 23.9f, 18.4f, 23.9f, 17f)
            close()
        }
    }.build()
}

// Teams: the "T" plate with a person behind it, one white glyph, the T cut out.
private val MetroTeams by lazy {
    // Classic (2017) Teams mark traced from the reference, 1024-unit grid:
    // door panel with the T knocked out, a larger person, a smaller person.
    ImageVector.Builder("MetroTeams", 32.dp, 32.dp, 1024f, 1024f).apply {
        path(fill = SolidColor(Color.White), pathFillType = PathFillType.EvenOdd) {
            moveTo(85f, 170f); lineTo(577f, 85f); lineTo(577f, 938f); lineTo(85f, 853f); close()
            moveTo(213f, 360f); lineTo(448f, 347f); lineTo(448f, 405f); lineTo(362f, 408f)
            lineTo(362f, 662f); lineTo(299f, 660f); lineTo(299f, 412f); lineTo(213f, 415f); close()
        }
        path(fill = SolidColor(Color.White)) {
            // Large person: head clipped flat on the left, body with rounded bottom-right.
            moveTo(620f, 255f)
            arcTo(85f, 85f, 0f, true, true, 620f, 341f); close()
            moveTo(620f, 427f); lineTo(768f, 427f); lineTo(768f, 715f)
            arcTo(75f, 75f, 0f, false, true, 693f, 790f); lineTo(620f, 790f); close()
            // Small person.
            moveTo(960f, 352f)
            arcTo(75f, 75f, 0f, true, true, 810f, 352f)
            arcTo(75f, 75f, 0f, true, true, 960f, 352f); close()
            moveTo(810f, 490f); lineTo(960f, 490f); lineTo(960f, 649f)
            arcTo(75f, 75f, 0f, false, true, 885f, 724f); lineTo(810f, 724f); close()
        }
    }.build()
}
private val MetroReddit by lazy {
    ImageVector.Builder("MetroReddit", 32.dp, 32.dp, 32f, 32f).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(16f, 12f)
            lineTo(18f, 4f)
            lineTo(24.5f, 5.5f)
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(29f, 5.5f)
            arcTo(2.5f, 2.5f, 0f, true, true, 24f, 5.5f)
            arcTo(2.5f, 2.5f, 0f, true, true, 29f, 5.5f)
            close()
            moveTo(8f, 16f)
            arcTo(3.5f, 3.5f, 0f, true, true, 1f, 16f)
            arcTo(3.5f, 3.5f, 0f, true, true, 8f, 16f)
            close()
            moveTo(31f, 16f)
            arcTo(3.5f, 3.5f, 0f, true, true, 24f, 16f)
            arcTo(3.5f, 3.5f, 0f, true, true, 31f, 16f)
            close()
        }
        path(fill = SolidColor(Color.White), pathFillType = PathFillType.EvenOdd) {
            moveTo(29f, 19f)
            curveTo(29f, 13.8f, 23.2f, 10.5f, 16f, 10.5f)
            curveTo(8.8f, 10.5f, 3f, 13.8f, 3f, 19f)
            curveTo(3f, 24.2f, 8.8f, 27.5f, 16f, 27.5f)
            curveTo(23.2f, 27.5f, 29f, 24.2f, 29f, 19f)
            close()
            moveTo(12.4f, 17.5f)
            arcTo(1.9f, 1.9f, 0f, true, true, 8.6f, 17.5f)
            arcTo(1.9f, 1.9f, 0f, true, true, 12.4f, 17.5f)
            close()
            moveTo(23.4f, 17.5f)
            arcTo(1.9f, 1.9f, 0f, true, true, 19.6f, 17.5f)
            arcTo(1.9f, 1.9f, 0f, true, true, 23.4f, 17.5f)
            close()
            moveTo(10f, 21f)
            curveTo(13f, 23.3f, 19f, 23.3f, 22f, 21f)
            lineTo(23f, 22.4f)
            curveTo(19.4f, 25.2f, 12.6f, 25.2f, 9f, 22.4f)
            close()
        }
    }.build()
}

private val MetroFacebook by lazy {
    ImageVector.Builder("MetroFacebook", 32.dp, 32.dp, 32f, 32f).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(18f, 29f)
            lineTo(12f, 29f)
            lineTo(12f, 18f)
            lineTo(8f, 18f)
            lineTo(8f, 12.5f)
            lineTo(12f, 12.5f)
            lineTo(12f, 9f)
            curveTo(12f, 4.9f, 14.5f, 2.5f, 19f, 2.5f)
            lineTo(24f, 2.5f)
            lineTo(24f, 8f)
            lineTo(20.7f, 8f)
            curveTo(18.8f, 8f, 18f, 8.8f, 18f, 10.5f)
            lineTo(18f, 12.5f)
            lineTo(23.6f, 12.5f)
            lineTo(22.8f, 18f)
            lineTo(18f, 18f)
            close()
        }
    }.build()
}

private val MetroYouTube by lazy {
    ImageVector.Builder("MetroYouTube", 32.dp, 32.dp, 32f, 32f).apply {
        path(fill = SolidColor(Color.White), pathFillType = PathFillType.EvenOdd) {
            moveTo(10f, 7f)
            lineTo(22f, 7f)
            curveTo(26.2f, 7f, 28.5f, 9f, 28.5f, 12f)
            lineTo(28.5f, 20f)
            curveTo(28.5f, 23f, 26.2f, 25f, 22f, 25f)
            lineTo(10f, 25f)
            curveTo(5.8f, 25f, 3.5f, 23f, 3.5f, 20f)
            lineTo(3.5f, 12f)
            curveTo(3.5f, 9f, 5.8f, 7f, 10f, 7f)
            close()
            moveTo(13.2f, 11.4f)
            lineTo(21.2f, 16f)
            lineTo(13.2f, 20.6f)
            close()
        }
    }.build()
}

private val MetroInstagram by lazy {
    ImageVector.Builder("MetroInstagram", 32.dp, 32.dp, 32f, 32f).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(10f, 4f)
            lineTo(22f, 4f)
            curveTo(25.5f, 4f, 28f, 6.5f, 28f, 10f)
            lineTo(28f, 22f)
            curveTo(28f, 25.5f, 25.5f, 28f, 22f, 28f)
            lineTo(10f, 28f)
            curveTo(6.5f, 28f, 4f, 25.5f, 4f, 22f)
            lineTo(4f, 10f)
            curveTo(4f, 6.5f, 6.5f, 4f, 10f, 4f)
            close()
            moveTo(22f, 16f)
            arcTo(6f, 6f, 0f, true, true, 10f, 16f)
            arcTo(6f, 6f, 0f, true, true, 22f, 16f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(25f, 9f)
            arcTo(1.5f, 1.5f, 0f, true, true, 22f, 9f)
            arcTo(1.5f, 1.5f, 0f, true, true, 25f, 9f)
            close()
        }
    }.build()
}

@Preview(showBackground = true, backgroundColor = 0xFF101010, widthDp = 360)
@Composable
internal fun MetroSocialIconsPreview() {
    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetroSocialBrand.entries.forEach { brand ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(70.dp).background(brand.color), contentAlignment = Alignment.Center) {
                    MetroSocialIcon(brand, brand.name, Modifier.size(42.dp))
                }
                Text(brand.name, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
