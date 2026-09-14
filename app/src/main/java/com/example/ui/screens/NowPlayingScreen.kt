package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.VisualizerMode
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicPlayerViewModel
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToLyrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerUiState by viewModel.playerUiState.collectAsState()
    val waveform by viewModel.waveformFlow.collectAsState()
    val fft by viewModel.fftFlow.collectAsState()
    val reducedMotion by viewModel.reducedMotion.collectAsState()

    val currentSong = playerUiState.currentSong
    val scrollState = rememberScrollState()

    var isVisualizerEmbedded by remember { mutableStateOf(false) }
    var embeddedVisualizerMode by remember { mutableStateOf(VisualizerMode.FREQUENCY_BARS) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showQuickBassDialog by remember { mutableStateOf(false) }
    var showAudioSpecsDialog by remember { mutableStateOf(false) }
    var useWaveformScrubber by remember { mutableStateOf(false) }

    var userDraggingSlider by remember { mutableStateOf(false) }
    var sliderTempPosition by remember { mutableFloatStateOf(0f) }

    // Audio beat reactive pulsation
    val infiniteTransition = rememberInfiniteTransition(label = "now_playing_ambient")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_pulse"
    )

    if (currentSong == null) {
        Box(
            modifier = modifier.fillMaxSize().testTag("now_playing_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No track currently playing", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                GlassButton(onClick = onBack, text = "Back to Library")
            }
        }
        return
    }

    val duration = playerUiState.duration.coerceAtLeast(1L)
    val displayPosition = if (userDraggingSlider) sliderTempPosition.toLong() else playerUiState.currentPosition

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentSong.album,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    GlassIconButton(
                        onClick = onBack,
                        icon = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Close player",
                        size = 44.dp,
                        iconSize = 26.dp,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .testTag("now_playing_back_button")
                    )
                },
                actions = {
                    GlassIconButton(
                        onClick = { showAudioSpecsDialog = true },
                        icon = Icons.Default.Info,
                        contentDescription = "Technical Specs",
                        size = 44.dp,
                        iconSize = 22.dp,
                        modifier = Modifier.padding(end = 4.dp).testTag("now_playing_specs_button")
                    )
                    GlassIconButton(
                        onClick = { showQuickBassDialog = true },
                        icon = Icons.Default.GraphicEq,
                        contentDescription = "Quick Audio Boost",
                        size = 44.dp,
                        iconSize = 22.dp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    GlassIconButton(
                        onClick = onNavigateToEqualizer,
                        icon = Icons.Default.Tune,
                        contentDescription = "Equalizer",
                        size = 44.dp,
                        iconSize = 22.dp,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("now_playing_eq_button")
                    )
                    GlassIconButton(
                        onClick = onNavigateToQueue,
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        size = 44.dp,
                        iconSize = 22.dp,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("now_playing_queue_button")
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        modifier = modifier.fillMaxSize().testTag("now_playing_screen")
    ) { paddingValues ->
        FluidGlassBackground(
            isPlaying = playerUiState.isPlaying,
            primaryColor = MaterialTheme.colorScheme.primary,
            secondaryColor = MaterialTheme.colorScheme.secondary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ambient Glow & Artwork / Embedded Visualizer Hero Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Soft Glowing Backdrop Aura
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .scale(if (playerUiState.isPlaying) ambientPulse else 1f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                                        NeonMagenta.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Glassmorphic Artwork Card
                    GlassCard(
                        shape = RoundedCornerShape(28.dp),
                        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        onClick = { isVisualizerEmbedded = !isVisualizerEmbedded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .testTag("now_playing_artwork_card")
                    ) {
                        if (isVisualizerEmbedded) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                VisualizerCanvas(
                                    mode = embeddedVisualizerMode,
                                    waveform = waveform,
                                    fft = fft,
                                    isPlaying = playerUiState.isPlaying,
                                    reducedMotion = reducedMotion,
                                    primaryColor = MaterialTheme.colorScheme.primary,
                                    secondaryColor = MaterialTheme.colorScheme.secondary,
                                    accentColor = NeonCyan,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Overlay badge showing current mode & tap to switch
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GlassPill(
                                        text = embeddedVisualizerMode.displayName,
                                        selected = true,
                                        icon = Icons.Default.GraphicEq,
                                        onClick = {
                                            val allModes = VisualizerMode.values()
                                            val nextIndex = (allModes.indexOf(embeddedVisualizerMode) + 1) % allModes.size
                                            embeddedVisualizerMode = allModes[nextIndex]
                                        }
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Black.copy(alpha = 0.45f),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 12.dp)
                                ) {
                                    Text(
                                        text = "Tap art to toggle mode",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            // Sleek Vinyl Disc Artwork with Beat Ring
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Pulsing vinyl grooves
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .size(200.dp)
                                        .glassmorphic(
                                            shape = CircleShape,
                                            backgroundColor = Color.Black.copy(alpha = 0.4f),
                                            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                        )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        // Inner Center Disc
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(80.dp)
                                                .shadow(12.dp, CircleShape)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (playerUiState.isPlaying) {
                                                    GlassBeatIndicator(
                                                        isPlaying = true,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.MusicNote,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Visualizer toggle badge at top right
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                ) {
                                    GlassPill(
                                        text = "Visualizer",
                                        selected = false,
                                        icon = Icons.Default.GraphicEq,
                                        onClick = { isVisualizerEmbedded = true }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title, Artist, and Favorite Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentSong.artist,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Sleek Glass Favorite Button
                    GlassIconButton(
                        onClick = { viewModel.toggleFavorite(currentSong) },
                        icon = if (currentSong.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        size = 50.dp,
                        iconSize = 28.dp,
                        tint = if (currentSong.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        isActive = currentSong.favorite,
                        activeColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("now_playing_favorite_button")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Smooth Progress Slider or Liquid Waveform Scrubber
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassPill(
                            text = if (useWaveformScrubber) "Waveform" else "Standard",
                            selected = useWaveformScrubber,
                            icon = if (useWaveformScrubber) Icons.Default.GraphicEq else Icons.Default.LinearScale,
                            onClick = { useWaveformScrubber = !useWaveformScrubber },
                            modifier = Modifier.testTag("waveform_toggle_pill")
                        )
                    }

                    if (useWaveformScrubber) {
                        LiquidWaveformScrubber(
                            currentPosition = displayPosition,
                            duration = duration,
                            waveform = waveform,
                            onSeek = { viewModel.seekTo(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                        )
                    } else {
                        Slider(
                            value = displayPosition.toFloat(),
                            onValueChange = {
                                userDraggingSlider = true
                                sliderTempPosition = it
                            },
                            onValueChangeFinished = {
                                userDraggingSlider = false
                                viewModel.seekTo(sliderTempPosition.toLong())
                            },
                            valueRange = 0f..duration.toFloat(),
                            modifier = Modifier.fillMaxWidth().testTag("now_playing_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(displayPosition),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatDuration(duration),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Playback Controls Row with Smooth Glass Translucent Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Glass Shuffle Button
                    GlassIconButton(
                        onClick = { viewModel.toggleShuffle() },
                        icon = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        size = 46.dp,
                        iconSize = 22.dp,
                        isActive = playerUiState.isShuffleOn,
                        activeColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("now_playing_shuffle_button")
                    )

                    // Glass Rewind 10s
                    GlassIconButton(
                        onClick = { viewModel.seekBackward10s() },
                        icon = Icons.Default.Replay10,
                        contentDescription = "Rewind 10s",
                        size = 46.dp,
                        iconSize = 22.dp,
                        modifier = Modifier.testTag("now_playing_rewind_10s")
                    )

                    // Glass Previous Button
                    GlassIconButton(
                        onClick = { viewModel.playPrevious() },
                        icon = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        size = 52.dp,
                        iconSize = 28.dp,
                        modifier = Modifier.testTag("now_playing_previous_button")
                    )

                    // Grand Center Play/Pause Glass Button with Luminous Glow
                    Box(contentAlignment = Alignment.Center) {
                        // Ambient ring behind play/pause
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .scale(if (playerUiState.isPlaying) ambientPulse else 1f)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        GlassIconButton(
                            onClick = { viewModel.togglePlayPause() },
                            icon = if (playerUiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerUiState.isPlaying) "Pause" else "Play",
                            size = 68.dp,
                            iconSize = 36.dp,
                            tint = Color.Black,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            borderColor = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.testTag("now_playing_play_pause_button")
                        )
                    }

                    // Glass Next Button
                    GlassIconButton(
                        onClick = { viewModel.playNext() },
                        icon = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        size = 52.dp,
                        iconSize = 28.dp,
                        modifier = Modifier.testTag("now_playing_next_button")
                    )

                    // Glass Forward 10s
                    GlassIconButton(
                        onClick = { viewModel.seekForward10s() },
                        icon = Icons.Default.Forward10,
                        contentDescription = "Forward 10s",
                        size = 46.dp,
                        iconSize = 22.dp,
                        modifier = Modifier.testTag("now_playing_forward_10s")
                    )

                    // Glass Repeat Mode Button
                    GlassIconButton(
                        onClick = { viewModel.toggleRepeat() },
                        icon = when (playerUiState.repeatMode) {
                            1 -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        size = 46.dp,
                        iconSize = 22.dp,
                        isActive = playerUiState.repeatMode > 0,
                        activeColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("now_playing_repeat_button")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Secondary Quick Actions: Speed, A-B Loop, Sleep Timer, Lyrics in Glass Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Playback Speed Glass Pill
                    GlassPill(
                        text = "${playerUiState.playbackSpeed}x",
                        selected = playerUiState.playbackSpeed != 1.0f,
                        icon = Icons.Default.Speed,
                        onClick = { showSpeedDialog = true },
                        modifier = Modifier.testTag("speed_chip")
                    )

                    // A-B Looper Quick Glass Pill
                    val abState = playerUiState.abLoop
                    val abText = when {
                        abState.pointA == null -> "Set A"
                        abState.pointB == null -> "A: ${formatDuration(abState.pointA)} -> Set B"
                        else -> "Loop [A-B]"
                    }
                    GlassPill(
                        text = abText,
                        selected = abState.isEnabled,
                        icon = Icons.Default.Loop,
                        onClick = {
                            if (abState.pointA == null) {
                                viewModel.setLoopA(displayPosition)
                            } else if (abState.pointB == null) {
                                viewModel.setLoopB(displayPosition)
                            } else {
                                viewModel.clearLoop()
                            }
                        },
                        modifier = Modifier.testTag("ab_loop_chip")
                    )

                    // Sleep Timer Glass Pill
                    val sleepState = playerUiState.sleepTimer
                    GlassPill(
                        text = if (sleepState.isActive) "${sleepState.remainingSeconds / 60}m" else "Timer",
                        selected = sleepState.isActive,
                        icon = Icons.Default.Timer,
                        onClick = { showSleepTimerDialog = true },
                        modifier = Modifier.testTag("sleep_timer_chip")
                    )

                    // Lyrics Shortcut Glass Icon Button
                    GlassIconButton(
                        onClick = onNavigateToLyrics,
                        icon = Icons.Default.Subtitles,
                        contentDescription = "Lyrics",
                        size = 44.dp,
                        iconSize = 22.dp,
                        modifier = Modifier.testTag("lyrics_shortcut_button")
                    )
                }
            }
        }
    }

    // Quick Bass / Audio Enhancer Modal
    if (showQuickBassDialog) {
        AlertDialog(
            onDismissRequest = { showQuickBassDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Audio Boost & Bass")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val effects = playerUiState.audioEffects

                    // Bass Boost Switch & Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bass Boost", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = effects.bassBoostEnabled,
                            onCheckedChange = { viewModel.setBassBoostEnabled(it) }
                        )
                    }
                    if (effects.bassBoostEnabled) {
                        Slider(
                            value = effects.bassBoostStrength.toFloat(),
                            onValueChange = { viewModel.setBassBoostStrength(it.toInt()) },
                            valueRange = 0f..1000f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Virtualizer Switch & Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("3D Surround Virtualizer", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = effects.virtualizerEnabled,
                            onCheckedChange = { viewModel.setVirtualizerEnabled(it) }
                        )
                    }
                    if (effects.virtualizerEnabled) {
                        Slider(
                            value = effects.virtualizerStrength.toFloat(),
                            onValueChange = { viewModel.setVirtualizerStrength(it.toInt()) },
                            valueRange = 0f..1000f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Loudness Enhancer Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Volume Booster", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = effects.loudnessEnhancerEnabled,
                            onCheckedChange = { viewModel.setLoudnessEnhancerEnabled(it) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuickBassDialog = false }) { Text("Done") }
            }
        )
    }

    // Speed Selector Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Playback Speed") },
            text = {
                Column {
                    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
                    speeds.forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = playerUiState.playbackSpeed == speed,
                                onClick = {
                                    viewModel.setPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("${speed}x", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) { Text("Close") }
            }
        )
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text("Sleep Timer") },
            text = {
                Column {
                    if (playerUiState.sleepTimer.isActive) {
                        Text(
                            text = "Remaining: ${playerUiState.sleepTimer.remainingSeconds / 60}m ${playerUiState.sleepTimer.remainingSeconds % 60}s",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.extendSleepTimer(5) }) { Text("+5 min") }
                            OutlinedButton(onClick = {
                                viewModel.cancelSleepTimer()
                                showSleepTimerDialog = false
                            }) { Text("Turn Off") }
                        }
                    } else {
                        val remainingSongSeconds = ((duration - displayPosition) / 1000).toInt().coerceAtLeast(1)
                        TextButton(
                            onClick = {
                                viewModel.startSleepTimer(remainingSongSeconds / 60 + 1)
                                showSleepTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("End of current song (~${remainingSongSeconds / 60}m)", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        val minutesList = listOf(5, 15, 30, 45, 60, 90)
                        minutesList.forEach { mins ->
                            TextButton(
                                onClick = {
                                    viewModel.startSleepTimer(mins)
                                    showSleepTimerDialog = false
                                },
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("$mins minutes", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) { Text("Close") }
            }
        )
    }

    // Audio Technical Specs Dialog
    if (showAudioSpecsDialog) {
        AudioSpecsDialog(
            song = currentSong,
            onDismiss = { showAudioSpecsDialog = false }
        )
    }
}
