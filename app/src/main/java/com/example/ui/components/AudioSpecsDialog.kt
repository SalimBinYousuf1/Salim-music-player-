package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.Song
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AudioSpecsDialog(
    song: Song,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val formattedSize = remember(song.fileSize) {
        if (song.fileSize > 0) {
            val mb = song.fileSize / (1024f * 1024f)
            "%.2f MB (%d bytes)".format(mb, song.fileSize)
        } else {
            "Unknown"
        }
    }

    val formattedDate = remember(song.dateAdded) {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(song.dateAdded))
    }

    val fileExt = remember(song.fileUri, song.mimeType) {
        when {
            song.fileUri.endsWith(".flac", ignoreCase = true) || song.mimeType.contains("flac") -> "FLAC (Lossless Audio)"
            song.fileUri.endsWith(".wav", ignoreCase = true) || song.mimeType.contains("wav") -> "WAV (PCM Linear)"
            song.fileUri.endsWith(".m4a", ignoreCase = true) || song.mimeType.contains("mp4") -> "AAC / M4A (MPEG-4)"
            song.fileUri.endsWith(".ogg", ignoreCase = true) || song.mimeType.contains("ogg") -> "OGG Vorbis"
            else -> "MP3 (MPEG-1 Audio Layer III)"
        }
    }

    val bitRateEstimate = remember(song.duration, song.fileSize) {
        if (song.duration > 0 && song.fileSize > 0) {
            val kbps = (song.fileSize * 8) / song.duration
            "~$kbps kbps"
        } else {
            "320 kbps (Standard)"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Audio Tech Specs", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpecItem(label = "Title", value = song.title)
                SpecItem(label = "Artist", value = song.artist)
                SpecItem(label = "Album", value = song.album)
                SpecItem(label = "Genre", value = song.genre)
                SpecItem(label = "Format & Codec", value = fileExt)
                SpecItem(label = "Estimated Bitrate", value = bitRateEstimate)
                SpecItem(label = "Duration", value = formatDuration(song.duration))
                SpecItem(label = "File Size", value = formattedSize)
                SpecItem(label = "Date Imported", value = formattedDate)
                SpecItem(label = "Play Count", value = "${song.playCount} times")
                SpecItem(label = "File Path", value = song.fileUri)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Song Path", song.fileUri))
                            Toast.makeText(context, "File path copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Path")
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, song.title)
                                putExtra(Intent.EXTRA_TEXT, "Now Listening to \"${song.title}\" by ${song.artist} on Salim Music Player!")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Track Info"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("specs_dialog_close")
            ) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
