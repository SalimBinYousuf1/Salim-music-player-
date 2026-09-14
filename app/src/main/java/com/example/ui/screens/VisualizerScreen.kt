package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.VisualizerMode
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicPlayerViewModel

enum class VisualizerColorTheme(val displayName: String, val primary: Color, val secondary: Color, val accent: Color) {
    CYBER_NEON("Cyber Neon", NeonCyan, NeonMagenta, NeonPurple),
    AURORA("Aurora Borealis", Color(0xFF2DD4BF), Color(0xFFC084FC), Color(0xFF38BDF8)),
    SUNSET_BLAZE("Sunset Blaze", NeonAmber, NeonCoral, Color(0xFFFF007F)),
    ELECTRIC_BLUE("Electric Blue", Color(0xFF38BDF8), Color(0xFF6366F1), Color(0xFF00F0FF)),
    EMERALD_PULSE("Emerald Pulse", SalimGreen, Color(0xFF34D399), Color(0xFF6EE7B7))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizerScreen(
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerUiState by viewModel.playerUiState.collectAsState()
    val waveform by viewModel.waveformFlow.collectAsState()
    val fft by viewModel.fftFlow.collectAsState()
    val reducedMotion by viewModel.reducedMotion.collectAsState()

    var selectedMode by remember { mutableStateOf(VisualizerMode.FREQUENCY_BARS) }
    var selectedColorTheme by remember { mutableStateOf(VisualizerColorTheme.CYBER_NEON) }
    var beatSensitivity by remember { mutableFloatStateOf(1.2f) }
    var isImmersiveMode by remember { mutableStateOf(false) }

    val currentSong = playerUiState.currentSong

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = !isImmersiveMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Live Beat Visualizer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${selectedMode.displayName} • ${selectedColorTheme.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = selectedColorTheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        GlassIconButton(
                            onClick = onBack,
                            icon = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .testTag("visualizer_back_button")
                        )
                    },
                    actions = {
                        // Toggle Immersive Mode
                        GlassIconButton(
                            onClick = { isImmersiveMode = !isImmersiveMode },
                            icon = Icons.Default.Fullscreen,
                            contentDescription = "Immersive Mode",
                            tint = selectedColorTheme.primary,
                            modifier = Modifier.padding(end = 8.dp).testTag("visualizer_fullscreen_button")
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                    )
                )
            }
        },
        modifier = modifier.fillMaxSize().testTag("visualizer_screen")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            selectedColorTheme.secondary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background,
                            selectedColorTheme.primary.copy(alpha = 0.06f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Controls: Mode Picker & Color Themes
                AnimatedVisibility(
                    visible = !isImmersiveMode,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        // Visualizer Beat Modes Scrollable Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VisualizerMode.values().forEach { mode ->
                                GlassPill(
                                    text = mode.displayName,
                                    selected = selectedMode == mode,
                                    onClick = { selectedMode = mode },
                                    selectedColor = selectedColorTheme.primary,
                                    modifier = Modifier.testTag("mode_pill_${mode.name}")
                                )
                            }
                        }

                        // Secondary Row: Color Schemes & Sensitivity
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "THEME:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            VisualizerColorTheme.values().forEach { theme ->
                                GlassPill(
                                    text = theme.displayName,
                                    selected = selectedColorTheme == theme,
                                    onClick = { selectedColorTheme = theme },
                                    selectedColor = theme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "BEAT SENSITIVITY:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            listOf(0.6f to "0.6x", 1.0f to "1.0x", 1.4f to "1.4x", 2.0f to "2.0x").forEach { (value, label) ->
                                GlassPill(
                                    text = label,
                                    selected = (beatSensitivity == value),
                                    onClick = { beatSensitivity = value },
                                    selectedColor = selectedColorTheme.accent
                                )
                            }
                        }
                    }
                }

                // Interactive Immersive Visualizer Canvas Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = if (isImmersiveMode) 0.dp else 16.dp, vertical = 8.dp)
                        .clickable {
                            // Tap on visualizer toggles immersive mode
                            isImmersiveMode = !isImmersiveMode
                        },
                    contentAlignment = Alignment.Center
                ) {
                    VisualizerCanvas(
                        mode = selectedMode,
                        waveform = waveform,
                        fft = fft,
                        isPlaying = playerUiState.isPlaying,
                        reducedMotion = reducedMotion,
                        sensitivity = beatSensitivity,
                        primaryColor = selectedColorTheme.primary,
                        secondaryColor = selectedColorTheme.secondary,
                        accentColor = selectedColorTheme.accent,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Floating hint when entering immersive
                    if (isImmersiveMode) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                        ) {
                            Text(
                                text = "Tap anywhere to restore controls",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Sleek Glassy Floating Music Controller Bar
                if (currentSong != null) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("visualizer_bottom_controller"),
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                        borderColor = selectedColorTheme.primary.copy(alpha = 0.35f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Mini beat equalizer indicator
                                GlassBeatIndicator(
                                    isPlaying = playerUiState.isPlaying,
                                    color = selectedColorTheme.primary,
                                    modifier = Modifier.padding(end = 12.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentSong.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${currentSong.artist} • ${currentSong.album}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Quick Shuffle Button
                                GlassIconButton(
                                    onClick = { viewModel.toggleShuffle() },
                                    icon = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    size = 40.dp,
                                    iconSize = 18.dp,
                                    isActive = playerUiState.isShuffleOn,
                                    activeColor = selectedColorTheme.primary
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Quick Previous Button
                                GlassIconButton(
                                    onClick = { viewModel.playPrevious() },
                                    icon = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous Track",
                                    size = 42.dp,
                                    iconSize = 22.dp
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Big Smooth Glass Play/Pause
                                GlassIconButton(
                                    onClick = { viewModel.togglePlayPause() },
                                    icon = if (playerUiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playerUiState.isPlaying) "Pause" else "Play",
                                    size = 50.dp,
                                    iconSize = 28.dp,
                                    tint = Color.Black,
                                    backgroundColor = selectedColorTheme.primary,
                                    borderColor = Color.White.copy(alpha = 0.6f)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Quick Next Button
                                GlassIconButton(
                                    onClick = { viewModel.playNext() },
                                    icon = Icons.Default.SkipNext,
                                    contentDescription = "Next Track",
                                    size = 42.dp,
                                    iconSize = 22.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
