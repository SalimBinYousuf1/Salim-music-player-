package com.example

import com.example.domain.model.AudioEffectSettings
import com.example.domain.model.PlayerUiState
import com.example.domain.model.SalimTheme
import com.example.domain.model.SortOption
import com.example.domain.model.VisualizerMode
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun playerUiState_defaultValues_areCorrect() {
    val state = PlayerUiState()
    assertNull(state.currentSong)
    assertFalse(state.isPlaying)
    assertEquals(0L, state.currentPosition)
    assertEquals(0L, state.duration)
    assertEquals(1.0f, state.playbackSpeed, 0.001f)
    assertTrue(state.queue.isEmpty())
  }

  @Test
  fun audioEffectSettings_defaults_areValid() {
    val effects = AudioEffectSettings()
    assertTrue(effects.equalizerEnabled)
    assertEquals("Flat", effects.currentPresetName)
    assertEquals(5, effects.bandLevels.size)
    assertFalse(effects.bassBoostEnabled)
    assertEquals(0, effects.bassBoostStrength)
  }

  @Test
  fun sortOption_displayNames_areNotEmpty() {
    for (option in SortOption.values()) {
      assertTrue(option.displayName.isNotEmpty())
    }
  }

  @Test
  fun salimTheme_allThemesDefined() {
    assertEquals(9, SalimTheme.values().size)
    assertTrue(SalimTheme.values().any { it == SalimTheme.AURORA_GLASS })
    assertTrue(SalimTheme.values().any { it == SalimTheme.CYBER_NEON })
  }

  @Test
  fun visualizerMode_allBeatModesDefined() {
    assertEquals(8, VisualizerMode.values().size)
    for (mode in VisualizerMode.values()) {
      assertTrue(mode.displayName.isNotEmpty())
    }
  }
}

