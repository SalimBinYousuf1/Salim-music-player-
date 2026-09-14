package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.SalimMusicPlayerTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun song_item_screenshot() {
    val sampleSong = Song(
      id = 1L,
      title = "Symphony No. 5",
      artist = "Ludwig van Beethoven",
      album = "Masterpieces",
      duration = 420000L,
      fileUri = "content://media/external/audio/media/1"
    )

    composeTestRule.setContent {
      SalimMusicPlayerTheme {
        SongListItem(
          song = sampleSong,
          isPlaying = false,
          isCurrentSong = true,
          isSelected = false,
          isBatchMode = false,
          onSongClick = {},
          onSongLongClick = {},
          onToggleSelect = {},
          onToggleFavorite = {},
          onPlayNext = {},
          onAddToPlaylist = {},
          onEditSong = {},
          onDeleteSong = {},
          onViewLyrics = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/song_item.png")
  }
}

