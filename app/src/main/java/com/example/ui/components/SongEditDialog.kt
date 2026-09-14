package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.Song

@Composable
fun SongEditDialog(
    song: Song,
    onDismiss: () -> Unit,
    onSave: (Song) -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }
    var albumArtist by remember { mutableStateOf(song.albumArtist) }
    var genre by remember { mutableStateOf(song.genre) }
    var yearStr by remember { mutableStateOf(if (song.year > 0) song.year.toString() else "") }
    var trackNumberStr by remember { mutableStateOf(if (song.trackNumber > 0) song.trackNumber.toString() else "") }
    var discNumberStr by remember { mutableStateOf(if (song.discNumber > 0) song.discNumber.toString() else "") }
    var rating by remember { mutableFloatStateOf(song.rating) }
    var lyrics by remember { mutableStateOf(song.lyrics) }
    var favorite by remember { mutableStateOf(song.favorite) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Metadata") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_title_input")
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_artist_input")
                )

                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_album_input")
                )

                OutlinedTextField(
                    value = albumArtist,
                    onValueChange = { albumArtist = it },
                    label = { Text("Album Artist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = yearStr,
                        onValueChange = { yearStr = it },
                        label = { Text("Year") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = trackNumberStr,
                        onValueChange = { trackNumberStr = it },
                        label = { Text("Track #") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discNumberStr,
                        onValueChange = { discNumberStr = it },
                        label = { Text("Disc #") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Rating Stars
                Text("Rating", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i.toFloat() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (rating >= i) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Rating $i",
                                tint = if (rating >= i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Favorite Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = favorite,
                        onCheckedChange = { favorite = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Favorite")
                }

                // Lyrics
                OutlinedTextField(
                    value = lyrics,
                    onValueChange = { lyrics = it },
                    label = { Text("Lyrics (Plain or LRC [mm:ss.xx])") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = song.copy(
                        title = title.ifBlank { "Unknown" },
                        artist = artist.ifBlank { "Unknown Artist" },
                        album = album.ifBlank { "Unknown Album" },
                        albumArtist = albumArtist.ifBlank { artist },
                        genre = genre.ifBlank { "All" },
                        year = yearStr.toIntOrNull() ?: 0,
                        trackNumber = trackNumberStr.toIntOrNull() ?: 0,
                        discNumber = discNumberStr.toIntOrNull() ?: 1,
                        rating = rating,
                        lyrics = lyrics,
                        favorite = favorite,
                        lastModifiedTimestamp = System.currentTimeMillis()
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("save_metadata_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
