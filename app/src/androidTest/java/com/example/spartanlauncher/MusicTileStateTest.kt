package com.example.spartanlauncher

import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class MusicTileStateTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val sessions = mutableListOf<MediaSession>()

    private fun session(title: String, state: Int, artwork: Bitmap? = null): MediaSession =
        MediaSession(instrumentation.targetContext, "music-tile-test").also {
            sessions += it
            it.setMetadata(MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "Test artist")
                .apply { artwork?.let { image -> putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, image) } }
                .build())
            it.setPlaybackState(PlaybackState.Builder().setState(state, 0, 1f).build())
        }

    @After
    fun cleanUp() = instrumentation.runOnMainSync {
        sessions.forEach { it.release() }
        sessions.clear()
        LauncherMediaPlayback.clear()
    }

    @Test
    fun publishesArtworkAndTrackOnlyForTheOwningApp() = instrumentation.runOnMainSync {
        val art = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        val playing = session("First track", PlaybackState.STATE_PLAYING, art)
        LauncherMediaPlayback.update(listOf(playing.controller))
        val result = LauncherMediaPlayback.sessions.value
        assertEquals(setOf(instrumentation.targetContext.packageName), result.keys)
        assertNull(result["another.music.app"])
        val track = result.values.single()
        assertEquals("First track", track.title)
        assertEquals("Test artist", track.artist)
        assertNotNull(track.artwork)
        assertTrue(track.playing)
    }

    @Test
    fun prefersPlayingSessionAndKeepsPausedMetadata() = instrumentation.runOnMainSync {
        val paused = session("Old track", PlaybackState.STATE_PAUSED)
        val playing = session("Current track", PlaybackState.STATE_PLAYING)
        LauncherMediaPlayback.update(listOf(paused.controller, playing.controller))
        assertEquals("Current track", LauncherMediaPlayback.sessions.value.values.single().title)
        playing.setPlaybackState(PlaybackState.Builder().setState(PlaybackState.STATE_PAUSED, 0, 0f).build())
        LauncherMediaPlayback.update(listOf(playing.controller))
        val track = LauncherMediaPlayback.sessions.value.values.single()
        assertEquals("Current track", track.title)
        assertFalse(track.playing)
    }

    @Test
    fun repeatedArtworkDoesNotRestartTheLiveFace() = instrumentation.runOnMainSync {
        val art = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        val playing = session("Current track", PlaybackState.STATE_PLAYING, art)
        LauncherMediaPlayback.update(listOf(playing.controller))
        val first = LauncherMediaPlayback.sessions.value.values.single()
        val copy = art.copy(Bitmap.Config.ARGB_8888, false)
        playing.setMetadata(MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, "Current track")
            .putString(MediaMetadata.METADATA_KEY_ARTIST, "Test artist")
            .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, copy).build())
        LauncherMediaPlayback.update(listOf(playing.controller))
        assertSame(first.artwork, LauncherMediaPlayback.sessions.value.values.single().artwork)
        assertEquals(first, LauncherMediaPlayback.sessions.value.values.single())

        val changed = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        changed.eraseColor(android.graphics.Color.RED)
        playing.setMetadata(MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, "Current track")
            .putString(MediaMetadata.METADATA_KEY_ARTIST, "Test artist")
            .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, changed).build())
        LauncherMediaPlayback.update(listOf(playing.controller))
        assertNotSame(first.artwork, LauncherMediaPlayback.sessions.value.values.single().artwork)
    }

    @Test
    fun stoppedAndRemovedSessionsClearTheLiveFace() = instrumentation.runOnMainSync {
        val playing = session("Current track", PlaybackState.STATE_PLAYING)
        LauncherMediaPlayback.update(listOf(playing.controller))
        playing.setPlaybackState(PlaybackState.Builder().setState(PlaybackState.STATE_STOPPED, 0, 0f).build())
        LauncherMediaPlayback.update(listOf(playing.controller))
        assertTrue(LauncherMediaPlayback.sessions.value.isEmpty())
        LauncherMediaPlayback.update(listOf(session("Next track", PlaybackState.STATE_PLAYING).controller))
        LauncherMediaPlayback.update(emptyList())
        assertTrue(LauncherMediaPlayback.sessions.value.isEmpty())
    }
}
