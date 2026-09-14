package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchActionBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onBatchFavorite: () -> Unit,
    onBatchAddToPlaylist: () -> Unit,
    onBatchSetGenre: () -> Unit,
    onBatchDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClearSelection,
                modifier = Modifier.size(48.dp).testTag("batch_close_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Clear Selection")
            }

            Text(
                text = "$selectedCount selected",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )

            IconButton(
                onClick = onBatchFavorite,
                modifier = Modifier.size(48.dp).testTag("batch_favorite_button")
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "Favorite All")
            }

            IconButton(
                onClick = onBatchAddToPlaylist,
                modifier = Modifier.size(48.dp).testTag("batch_playlist_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to Playlist")
            }

            IconButton(
                onClick = onBatchSetGenre,
                modifier = Modifier.size(48.dp).testTag("batch_genre_button")
            ) {
                Icon(Icons.Default.Category, contentDescription = "Set Genre")
            }

            IconButton(
                onClick = onBatchDelete,
                modifier = Modifier.size(48.dp).testTag("batch_delete_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
