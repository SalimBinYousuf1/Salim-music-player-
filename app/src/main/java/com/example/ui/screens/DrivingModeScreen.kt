package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.viewmodel.MusicPlayerViewModel

@Composable
fun DrivingModeScreen(
    viewModel: MusicPlayerViewModel,
    onExitDrivingMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerUiState by viewModel.playerUiState.collectAsState()
    val currentSong = playerUiState.currentSong

    FluidGlassBackground(
        isPlaying = playerUiState.isPlaying,
        primaryColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF818CF8)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Driving Badge & Exit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassPill(
                    text = "DRIVE MODE ACTIVE",
                    selected = true,
                    icon = Icons.Default.DirectionsCar,
                    onClick = {
                        Toast.makeText(context, "Safe Driving Interface Active", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("driving_mode_badge")
                )

                GlassIconButton(
                    onClick = onExitDrivingMode,
                    icon = Icons.Default.Close,
                    contentDescription = "Exit Driving Mode",
                    size = 54.dp,
                    iconSize = 28.dp,
                    modifier = Modifier.testTag("exit_driving_mode_button")
                )
            }

            // Central Giant Song Info Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(110.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (playerUiState.isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = currentSong?.title ?: "No Song Playing",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentSong?.artist ?: "Select a song to start",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Giant Primary Playback Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Transport Row: Prev - Play/Pause - Next
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Giant Previous Button
                    GlassIconButton(
                        onClick = { viewModel.playPrevious() },
                        icon = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        size = 76.dp,
                        iconSize = 40.dp,
                        modifier = Modifier.testTag("driving_prev_button")
                    )

                    // Mega Center Play/Pause Button
                    GlassIconButton(
                        onClick = { viewModel.togglePlayPause() },
                        icon = if (playerUiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerUiState.isPlaying) "Pause" else "Play",
                        size = 96.dp,
                        iconSize = 52.dp,
                        tint = Color.Black,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        borderColor = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.testTag("driving_play_pause_button")
                    )

                    // Giant Next Button
                    GlassIconButton(
                        onClick = { viewModel.playNext() },
                        icon = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        size = 76.dp,
                        iconSize = 40.dp,
                        modifier = Modifier.testTag("driving_next_button")
                    )
                }

                // Secondary Driving Actions: -15s, Voice Announce, Favorite, +15s
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconButton(
                        onClick = { viewModel.seekBackward10s() },
                        icon = Icons.Default.Replay10,
                        contentDescription = "Rewind",
                        size = 58.dp,
                        iconSize = 28.dp
                    )

                    GlassIconButton(
                        onClick = {
                            if (currentSong != null) {
                                viewModel.toggleFavorite(currentSong)
                            }
                        },
                        icon = if (currentSong?.favorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        size = 58.dp,
                        iconSize = 28.dp,
                        isActive = currentSong?.favorite == true,
                        activeColor = Color(0xFFFB7185)
                    )

                    GlassIconButton(
                        onClick = {
                            val msg = currentSong?.let { "Playing: ${it.title} by ${it.artist}" } ?: "Nothing playing"
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        },
                        icon = Icons.Default.VolumeUp,
                        contentDescription = "Announce Track",
                        size = 58.dp,
                        iconSize = 28.dp
                    )

                    GlassIconButton(
                        onClick = { viewModel.seekForward10s() },
                        icon = Icons.Default.Forward10,
                        contentDescription = "Forward",
                        size = 58.dp,
                        iconSize = 28.dp
                    )
                }
            }
        }
    }
}
