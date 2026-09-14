package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.SalimTheme
import com.example.domain.model.ThemeMode
import com.example.ui.viewmodel.MusicPlayerViewModel

data class ThemeColorMeta(
    val theme: SalimTheme,
    val primary: Color,
    val secondary: Color,
    val surface: Color
)

private val THEME_META = listOf(
    ThemeColorMeta(SalimTheme.AURORA_GLASS, Color(0xFF67E8F9), Color(0xFFA855F7), Color(0xFF0F172A)),
    ThemeColorMeta(SalimTheme.CYBER_NEON, Color(0xFF00FFCC), Color(0xFFFF007F), Color(0xFF0A0A14)),
    ThemeColorMeta(SalimTheme.ROSE_QUARTZ, Color(0xFFFB7185), Color(0xFFF472B6), Color(0xFF1F1218)),
    ThemeColorMeta(SalimTheme.LIQUID_EMERALD, Color(0xFF34D399), Color(0xFF059669), Color(0xFF06231A)),
    ThemeColorMeta(SalimTheme.SUNSET_HORIZON, Color(0xFFF97316), Color(0xFFEF4444), Color(0xFF1C100B)),
    ThemeColorMeta(SalimTheme.MIDNIGHT_VIOLET, Color(0xFFA78BFA), Color(0xFF8B5CF6), Color(0xFF0A0518)),
    ThemeColorMeta(SalimTheme.DARK_SLATE, Color(0xFF10B981), Color(0xFF64748B), Color(0xFF0F172A)),
    ThemeColorMeta(SalimTheme.OLED_PITCH_BLACK, Color(0xFF10B981), Color(0xFF888888), Color(0xFF000000)),
    ThemeColorMeta(SalimTheme.SUNSET_AMBER, Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFF140E0A)),
    ThemeColorMeta(SalimTheme.FOREST_GREEN, Color(0xFF22C55E), Color(0xFF15803D), Color(0xFF07140B)),
    ThemeColorMeta(SalimTheme.OCEAN_BLUE, Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0B192C)),
    ThemeColorMeta(SalimTheme.LIGHT_SLATE, Color(0xFF059669), Color(0xFF475569), Color(0xFFF8FAFC))
)

@Composable
fun FluidThemeCreatorDialog(
    viewModel: MusicPlayerViewModel,
    onDismiss: () -> Unit
) {
    val currentTheme by viewModel.appTheme.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    var selectedTheme by remember(currentTheme) { mutableStateOf(currentTheme) }

    val activeMeta = THEME_META.find { it.theme == selectedTheme } ?: THEME_META.first()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .testTag("fluid_theme_creator_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fluid Theme Studio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Translucent Glass & Hue Presets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Interactive Theme Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = activeMeta.surface),
                    border = BorderStroke(1.5.dp, activeMeta.primary.copy(alpha = 0.6f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        activeMeta.primary.copy(alpha = 0.25f),
                                        activeMeta.secondary.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = activeMeta.primary.copy(alpha = 0.35f),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = activeMeta.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeMeta.theme.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Fluid Morphism Ambient Engine",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = activeMeta.primary,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme Mode Selector (System, Dark, Light)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.values().forEach { mode ->
                        val isSelected = themeMode == mode
                        GlassButton(
                            onClick = { viewModel.setThemeMode(mode) },
                            text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Color Palettes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Grid of Theme Swatches
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(THEME_META) { meta ->
                        val isCurrent = selectedTheme == meta.theme
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedTheme = meta.theme
                                    viewModel.setTheme(meta.theme)
                                }
                                .testTag("theme_swatch_${meta.theme.name}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) {
                                    meta.primary.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                }
                            ),
                            border = BorderStroke(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isCurrent) meta.primary else Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Double color pill
                                Row(modifier = Modifier.clip(CircleShape)) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp, 24.dp)
                                            .background(meta.primary)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp, 24.dp)
                                            .background(meta.secondary)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = meta.theme.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Done Button
                Button(
                    onClick = {
                        viewModel.setTheme(selectedTheme)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("apply_theme_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply Theme")
                }
            }
        }
    }
}
