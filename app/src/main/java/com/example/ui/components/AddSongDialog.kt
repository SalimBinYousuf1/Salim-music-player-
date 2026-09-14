package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun AddSongDialog(
    onDismiss: () -> Unit,
    onGeneratePracticeTrack: (title: String, durationSec: Int) -> Unit
) {
    var title by remember { mutableStateOf("Salim Practice Harmony") }
    var durationSecStr by remember { mutableStateOf("20") }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Practice Audio Track") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Generate a real native 16-bit 44.1kHz WAV musical chord practice track on local device storage, playable by the audio engine and equalizer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Track Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_track_title_input")
                )

                OutlinedTextField(
                    value = durationSecStr,
                    onValueChange = { durationSecStr = it },
                    label = { Text("Duration (seconds: 5 - 60)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_track_duration_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dur = (durationSecStr.toIntOrNull() ?: 20).coerceIn(5, 60)
                    onGeneratePracticeTrack(title.ifBlank { "Salim Practice" }, dur)
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_generate_track_button")
            ) {
                Text("Generate & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
