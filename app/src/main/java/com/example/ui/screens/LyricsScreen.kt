package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LyricsLine
import com.example.ui.components.formatDuration
import com.example.ui.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerUiState by viewModel.playerUiState.collectAsState()
    val currentSong = playerUiState.currentSong

    var showEditLyricsDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    if (currentSong == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No song currently playing")
        }
        return
    }

    val linesFlow = remember(currentSong.id) { viewModel.getLyricsForSong(currentSong.id) }
    val lyricsLines by linesFlow.collectAsState(initial = emptyList())

    // Active line detection
    val currentPos = playerUiState.currentPosition
    val activeIndex = lyricsLines.indexOfLast { it.timestamp <= currentPos }.takeIf { it >= 0 } ?: 0

    // Auto-scroll to active line
    LaunchedEffect(activeIndex) {
        if (lyricsLines.isNotEmpty() && activeIndex in lyricsLines.indices) {
            listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Lyrics", style = MaterialTheme.typography.titleMedium)
                        Text(currentSong.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp).testTag("lyrics_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditLyricsDialog = true },
                        modifier = Modifier.size(48.dp).testTag("edit_lyrics_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Lyrics")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize().testTag("lyrics_screen")
    ) { paddingValues ->
        if (lyricsLines.isEmpty() && currentSong.lyrics.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Subtitles,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No lyrics available",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showEditLyricsDialog = true },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Add Lyrics")
                    }
                }
            }
        } else if (lyricsLines.isNotEmpty()) {
            // Synchronized lyrics
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(vertical = 40.dp)
            ) {
                itemsIndexed(lyricsLines, key = { index, line -> "${line.id}_$index" }) { index, line ->
                    val isActive = index == activeIndex

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.seekTo(line.timestamp) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = line.text,
                            style = if (isActive) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Plain text lyrics
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {
                item {
                    Text(
                        text = currentSong.lyrics,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Edit Lyrics Dialog
    if (showEditLyricsDialog) {
        var rawInput by remember { mutableStateOf(currentSong.lyrics) }

        AlertDialog(
            onDismissRequest = { showEditLyricsDialog = false },
            title = { Text("Edit Lyrics") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Paste plain text or synchronized LRC format (e.g. [01:23.45] lyric line)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rawInput,
                        onValueChange = { rawInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        placeholder = { Text("[00:10.00] In the quiet night\n[00:15.50] Hear the acoustic strings...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedLines = parseLrcToLines(currentSong.id, rawInput)
                        viewModel.saveLyrics(currentSong.id, rawInput, parsedLines)
                        showEditLyricsDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditLyricsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun parseLrcToLines(songId: Long, content: String): List<LyricsLine> {
    val lrcRegex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")
    val lines = mutableListOf<LyricsLine>()
    var position = 0

    content.lines().forEach { rawLine ->
        val match = lrcRegex.matchEntire(rawLine.trim())
        if (match != null) {
            val (minStr, secStr, msStr, text) = match.destructured
            val min = minStr.toLongOrNull() ?: 0L
            val sec = secStr.toLongOrNull() ?: 0L
            val ms = if (msStr.length == 2) (msStr.toLongOrNull() ?: 0L) * 10 else msStr.toLongOrNull() ?: 0L
            val totalMs = (min * 60 + sec) * 1000 + ms
            if (text.isNotBlank()) {
                lines.add(LyricsLine(songId = songId, timestamp = totalMs, text = text.trim(), position = position++))
            }
        }
    }
    return lines
}
