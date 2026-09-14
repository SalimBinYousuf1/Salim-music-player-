package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.LyricsLine
import com.example.domain.model.Song
import com.example.ui.screens.parseLrcToLines
import com.example.ui.viewmodel.MusicPlayerViewModel

@Composable
fun LyricsSyncEditorDialog(
    song: Song,
    viewModel: MusicPlayerViewModel,
    onDismiss: () -> Unit
) {
    val playerUiState by viewModel.playerUiState.collectAsState()
    val currentPos = playerUiState.currentPosition

    val linesFlow = remember(song.id) { viewModel.getLyricsForSong(song.id) }
    val initialLines by linesFlow.collectAsState(initial = emptyList())

    var isRawMode by remember { mutableStateOf(false) }
    var rawLrcText by remember { mutableStateOf(song.lyrics) }

    // Editable in-memory lines list
    var editableLines by remember(initialLines) {
        mutableStateOf(
            if (initialLines.isNotEmpty()) {
                initialLines.toMutableList()
            } else if (song.lyrics.isNotBlank()) {
                val parsed = parseLrcToLines(song.id, song.lyrics)
                if (parsed.isNotEmpty()) parsed.toMutableList()
                else {
                    // Convert plain text lines into initial untimed lines
                    song.lyrics.lines().filter { it.isNotBlank() }.mapIndexed { idx, lineText ->
                        LyricsLine(
                            songId = song.id,
                            timestamp = (idx * 4000L).coerceAtMost(song.duration),
                            text = lineText.trim(),
                            position = idx
                        )
                    }.toMutableList()
                }
            } else {
                mutableListOf(
                    LyricsLine(songId = song.id, timestamp = 0L, text = "Verse 1", position = 0)
                )
            }
        )
    }

    var newLineText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("lyrics_sync_editor_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lyrics Sync Studio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { isRawMode = !isRawMode },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isRawMode) Icons.Default.FormatListBulleted else Icons.Default.Code,
                                contentDescription = "Toggle Mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mini Audio Player Bar inside the editor
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (playerUiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = formatTimestamp(currentPos),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { viewModel.seekBackward10s() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Replay10, contentDescription = "-10s", modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = { viewModel.seekForward10s() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Forward10, contentDescription = "+10s", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isRawMode) {
                    // Raw LRC mode
                    OutlinedTextField(
                        value = rawLrcText,
                        onValueChange = { rawLrcText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        label = { Text("Raw LRC Lyrics") },
                        placeholder = { Text("[00:12.50] Lyric line goes here...") },
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    // Interactive Line Sync List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(editableLines, key = { idx, _ -> idx }) { index, line ->
                            val isNearCurrent = kotlin.math.abs(line.timestamp - currentPos) < 2000L

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = if (isNearCurrent) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                },
                                borderColor = if (isNearCurrent) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.White.copy(alpha = 0.12f)
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Timestamp button (tapping seeks playback here)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        modifier = Modifier
                                            .clickable { viewModel.seekTo(line.timestamp) }
                                            .padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = formatTimestamp(line.timestamp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    // Line text
                                    Text(
                                        text = line.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isNearCurrent) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Sync Button: sets this line's timestamp to current playback position!
                                    IconButton(
                                        onClick = {
                                            val updated = editableLines.toMutableList()
                                            updated[index] = line.copy(timestamp = currentPos)
                                            updated.sortBy { it.timestamp }
                                            editableLines = updated
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("sync_line_$index")
                                    ) {
                                        Icon(
                                            Icons.Default.PinDrop,
                                            contentDescription = "Sync Current Time",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Micro adjust +/- 0.5s
                                    IconButton(
                                        onClick = {
                                            val updated = editableLines.toMutableList()
                                            updated[index] = line.copy(timestamp = (line.timestamp - 500L).coerceAtLeast(0L))
                                            editableLines = updated
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "-0.5s", modifier = Modifier.size(14.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            val updated = editableLines.toMutableList()
                                            updated[index] = line.copy(timestamp = line.timestamp + 500L)
                                            editableLines = updated
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "+0.5s", modifier = Modifier.size(14.dp))
                                    }

                                    // Delete line
                                    IconButton(
                                        onClick = {
                                            val updated = editableLines.toMutableList()
                                            updated.removeAt(index)
                                            editableLines = updated
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Line",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add new line row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newLineText,
                            onValueChange = { newLineText = it },
                            placeholder = { Text("Add lyric line at current time...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                if (newLineText.isNotBlank()) {
                                    val updated = editableLines.toMutableList()
                                    updated.add(
                                        LyricsLine(
                                            songId = song.id,
                                            timestamp = currentPos,
                                            text = newLineText.trim(),
                                            position = updated.size
                                        )
                                    )
                                    updated.sortBy { it.timestamp }
                                    editableLines = updated
                                    newLineText = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom actions: Cancel and Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (isRawMode) {
                                val parsed = parseLrcToLines(song.id, rawLrcText)
                                viewModel.saveLyrics(song.id, rawLrcText, parsed)
                            } else {
                                val lrcBuilder = StringBuilder()
                                editableLines.sortedBy { it.timestamp }.forEach { line ->
                                    lrcBuilder.append("[${formatTimestamp(line.timestamp)}] ${line.text}\n")
                                }
                                val formattedLrc = lrcBuilder.toString()
                                viewModel.saveLyrics(song.id, formattedLrc, editableLines.sortedBy { it.timestamp })
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("save_lyrics_sync_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Apply")
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hundredths = (ms % 1000) / 10
    return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
}
