package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.Song

@Composable
fun MusicStatsDialog(
    songs: List<Song>,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    val totalTracks = songs.size
    val totalDurationMs = remember(songs) { songs.sumOf { it.duration } }
    val totalHours = totalDurationMs / (1000 * 60 * 60)
    val totalMinutes = (totalDurationMs / (1000 * 60)) % 60

    val totalSizeMb = remember(songs) {
        val totalBytes = songs.sumOf { it.fileSize }
        totalBytes / (1024.0 * 1024.0)
    }

    val favoritesCount = remember(songs) { songs.count { it.favorite } }

    val topArtist = remember(songs) {
        songs.groupBy { it.artist }
            .filter { it.key.isNotBlank() && it.key != "<unknown>" }
            .maxByOrNull { it.value.size }?.key ?: "N/A"
    }

    val topGenre = remember(songs) {
        songs.groupBy { it.genre }
            .filter { it.key.isNotBlank() && it.key != "Unknown" }
            .maxByOrNull { it.value.size }?.key ?: "N/A"
    }

    val mostPlayedSong = remember(songs) {
        songs.maxByOrNull { it.playCount }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Music Insights & Stats", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // High level stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatMetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.MusicNote,
                        title = "Tracks",
                        value = totalTracks.toString()
                    )
                    StatMetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Schedule,
                        title = "Playtime",
                        value = "${totalHours}h ${totalMinutes}m"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatMetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.SdCard,
                        title = "Storage",
                        value = "%.1f MB".format(totalSizeMb)
                    )
                    StatMetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Favorite,
                        title = "Favorites",
                        value = "$favoritesCount"
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "TOP HIGHLIGHTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                HighlightRow(
                    label = "Top Artist",
                    value = topArtist,
                    icon = Icons.Default.Person
                )

                HighlightRow(
                    label = "Top Genre",
                    value = topGenre,
                    icon = Icons.Default.Category
                )

                if (mostPlayedSong != null && mostPlayedSong.playCount > 0) {
                    HighlightRow(
                        label = "Most Played Track",
                        value = "${mostPlayedSong.title} (${mostPlayedSong.playCount} plays)",
                        icon = Icons.Default.Whatshot
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("stats_dialog_close")
            ) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun StatMetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String
) {
    GlassCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HighlightRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
