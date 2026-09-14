package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import com.example.data.backup.BackupRestoreManager
import com.example.domain.model.SalimTheme
import com.example.domain.model.ThemeMode
import com.example.ui.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: MusicPlayerViewModel,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appTheme by viewModel.appTheme.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColors by viewModel.dynamicColors.collectAsState()
    val highContrast by viewModel.highContrast.collectAsState()
    val compactMode by viewModel.compactMode.collectAsState()
    val reducedMotion by viewModel.reducedMotion.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showThemeModeDialog by remember { mutableStateOf(false) }
    var importPreview by remember { mutableStateOf<Pair<Uri, BackupRestoreManager.ImportPreview>?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // SAF File Pickers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = viewModel.backupManager.exportBackupToJson(uri)
                snackbarMessage = if (success) "Backup exported successfully" else "Backup export failed"
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val preview = viewModel.backupManager.parsePreview(uri)
                if (preview != null) {
                    importPreview = Pair(uri, preview)
                } else {
                    snackbarMessage = "Invalid or corrupted backup JSON"
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            snackbarMessage?.let { msg ->
                Snackbar(
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) { Text("OK") }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(msg)
                }
            }
        },
        modifier = modifier.fillMaxSize().testTag("settings_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Appearance & Themes Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Appearance & Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    // Theme Palette
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showThemeDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Theme Palette", style = MaterialTheme.typography.bodyLarge)
                            Text(appTheme.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    HorizontalDivider()

                    // Theme Mode (System, Light, Dark)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showThemeModeDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Theme Mode", style = MaterialTheme.typography.bodyLarge)
                            Text(themeMode.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Default.Brightness4, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    HorizontalDivider()

                    // High Contrast
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("High Contrast Mode", style = MaterialTheme.typography.bodyLarge)
                            Text("Enhances borders and text readability", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = highContrast, onCheckedChange = { viewModel.setHighContrast(it) })
                    }

                    HorizontalDivider()

                    // Reduced Motion
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reduced Motion", style = MaterialTheme.typography.bodyLarge)
                            Text("Minimizes canvas and transition animations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = reducedMotion, onCheckedChange = { viewModel.setReducedMotion(it) })
                    }
                }
            }

            // Library & Scanning Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Library & Storage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Scan Device MediaStore", style = MaterialTheme.typography.bodyLarge)
                            Text("Discovers newly added audio files on storage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { viewModel.scanDeviceMedia() },
                            enabled = !isScanning,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("settings_scan_button")
                        ) {
                            Text(if (isScanning) "Scanning..." else "Scan Now")
                        }
                    }

                    HorizontalDivider()

                    // Backup & Restore
                    Text("Backup & Restore (SAF JSON)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { exportLauncher.launch("salim_backup_${System.currentTimeMillis()}.json") },
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp).testTag("export_backup_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export")
                        }

                        Button(
                            onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp).testTag("import_backup_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import")
                        }
                    }
                }
            }

            // Data & Reset Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Maintenance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    TextButton(
                        onClick = {
                            viewModel.clearHistory()
                            snackbarMessage = "Play history cleared"
                        },
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Clear Play History", color = MaterialTheme.colorScheme.error)
                    }

                    TextButton(
                        onClick = onNavigateToAbout,
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("About Salim Music Player")
                    }
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    // Theme Picker Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Color Palette") },
            text = {
                Column {
                    SalimTheme.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = appTheme == theme,
                                onClick = {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(theme.displayName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }

    // Theme Mode Dialog
    if (showThemeModeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeModeDialog = false },
            title = { Text("Select Theme Mode") },
            text = {
                Column {
                    ThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeModeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeModeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(mode.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeModeDialog = false }) { Text("Close") }
            }
        )
    }

    // Import Preview Dialog
    importPreview?.let { (uri, preview) ->
        AlertDialog(
            onDismissRequest = { importPreview = null },
            title = { Text("Restore Backup Preview") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Songs: ${preview.songCount}")
                    Text("• Playlists: ${preview.playlistCount}")
                    Text("• Play History: ${preview.historyCount}")
                    Text("• Lyrics: ${preview.lyricsCount}")
                    Text("• Equalizer Presets: ${preview.presetCount}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Choose conflict resolution:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = viewModel.backupManager.importBackupFromJson(uri, replaceExisting = true)
                            snackbarMessage = res.message
                            importPreview = null
                        }
                    }
                ) {
                    Text("Replace All")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val res = viewModel.backupManager.importBackupFromJson(uri, replaceExisting = false)
                            snackbarMessage = res.message
                            importPreview = null
                        }
                    }
                ) {
                    Text("Merge")
                }
            }
        )
    }
}
