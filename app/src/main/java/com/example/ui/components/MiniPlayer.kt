package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.PlayerUiState

@Composable
fun MiniPlayer(
    uiState: PlayerUiState,
    onMiniPlayerClick: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong = uiState.currentSong ?: return

    val progress = if (uiState.duration > 0) {
        (uiState.currentPosition.toFloat() / uiState.duration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("mini_player")
    ) {
        GlassCard(
            shape = RoundedCornerShape(20.dp),
            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            borderEndColor = Color.White.copy(alpha = 0.12f),
            onClick = onMiniPlayerClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Sleek Luminous Progress Line on Top Edge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album Art Placeholder / Mini Visualizer Disc
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (uiState.isPlaying) {
                                GlassBeatIndicator(
                                    isPlaying = true,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(4.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Track Title and Artist
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentSong.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentSong.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Glass Favorite Button
                    GlassIconButton(
                        onClick = onToggleFavorite,
                        icon = if (currentSong.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        size = 40.dp,
                        iconSize = 20.dp,
                        tint = if (currentSong.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("mini_player_favorite")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Glass Play / Pause Button with prominent glow
                    GlassIconButton(
                        onClick = onTogglePlayPause,
                        icon = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        size = 44.dp,
                        iconSize = 24.dp,
                        tint = Color.Black,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        borderColor = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.testTag("mini_player_play_pause")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Glass Skip Next Button
                    GlassIconButton(
                        onClick = onPlayNext,
                        icon = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        size = 40.dp,
                        iconSize = 22.dp,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("mini_player_next")
                    )
                }
            }
        }
    }
}
