package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.EqualizerPreset
import com.example.ui.components.ResponsiveChipGroup
import com.example.ui.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerUiState by viewModel.playerUiState.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val audioEffects = playerUiState.audioEffects

    val scrollState = rememberScrollState()
    var showSavePresetDialog by remember { mutableStateOf(false) }

    val bandLabels = listOf("60 Hz", "250 Hz", "1 kHz", "4 kHz", "12 kHz")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equalizer & Audio FX") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp).testTag("eq_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Switch(
                        checked = audioEffects.equalizerEnabled,
                        onCheckedChange = { viewModel.setEqualizerEnabled(it) },
                        modifier = Modifier.padding(end = 12.dp).testTag("eq_master_switch")
                    )
                }
            )
        },
        modifier = modifier.fillMaxSize().testTag("equalizer_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Presets Horizontal Row
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Presets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(
                        onClick = { showSavePresetDialog = true },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("save_preset_button")
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save As Preset")
                    }
                }

                ResponsiveChipGroup(
                    items = presets,
                    selectedItem = presets.firstOrNull { it.name == audioEffects.currentPresetName },
                    onItemSelected = { preset -> viewModel.applyPreset(preset) },
                    labelProvider = { it.name }
                )
            }

            // 5 Band Equalizer Sliders
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "5-Band Graphic Equalizer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    bandLabels.forEachIndexed { index, label ->
                        val currentLevel = audioEffects.bandLevels.getOrElse(index) { 0 }
                        val db = currentLevel / 100

                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (db > 0) "+${db} dB" else "${db} dB",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = currentLevel.toFloat(),
                                onValueChange = { viewModel.setBandLevel(index, it.toInt()) },
                                valueRange = -1500f..1500f,
                                steps = 30,
                                enabled = audioEffects.equalizerEnabled,
                                modifier = Modifier.fillMaxWidth().testTag("eq_band_$index")
                            )
                        }
                    }
                }
            }

            // Bass Boost & Virtualizer
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Bass Boost
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bass Boost", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${audioEffects.bassBoostStrength / 10}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = audioEffects.bassBoostStrength.toFloat(),
                            onValueChange = { viewModel.setBassBoost(it > 0, it.toInt()) },
                            valueRange = 0f..1000f,
                            modifier = Modifier.fillMaxWidth().testTag("bass_boost_slider")
                        )
                    }

                    // Virtualizer 3D
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Virtualizer (3D Surround)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${audioEffects.virtualizerStrength / 10}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = audioEffects.virtualizerStrength.toFloat(),
                            onValueChange = { viewModel.setVirtualizer(it > 0, it.toInt()) },
                            valueRange = 0f..1000f,
                            modifier = Modifier.fillMaxWidth().testTag("virtualizer_slider")
                        )
                    }
                }
            }

            // Stereo Balance & Mono Audio
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Soundstage & Panning", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Left", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = when {
                                audioEffects.stereoBalance < -0.05f -> "Left ${(-audioEffects.stereoBalance * 100).toInt()}%"
                                audioEffects.stereoBalance > 0.05f -> "Right ${(audioEffects.stereoBalance * 100).toInt()}%"
                                else -> "Center"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Right", style = MaterialTheme.typography.labelSmall)
                    }

                    Slider(
                        value = audioEffects.stereoBalance,
                        onValueChange = { viewModel.setStereoBalance(it) },
                        valueRange = -1f..1f,
                        modifier = Modifier.fillMaxWidth().testTag("stereo_balance_slider")
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Mono Audio", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(
                                "Combine stereo channels into identical mono",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = audioEffects.monoAudio,
                            onCheckedChange = { viewModel.setMonoAudio(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Save Custom Preset Dialog
    if (showSavePresetDialog) {
        var presetName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save Custom Preset") },
            text = {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("Preset Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetName.isNotBlank()) {
                            viewModel.saveCustomPreset(presetName.trim())
                            showSavePresetDialog = false
                        }
                    },
                    enabled = presetName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
