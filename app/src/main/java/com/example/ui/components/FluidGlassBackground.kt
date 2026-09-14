package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated dynamic fluid mesh gradient background featuring floating iridescent liquid orbs
 * and a frosted glass overlay with music-reactive pulse.
 */
@Composable
fun FluidGlassBackground(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    tertiaryColor: Color = MaterialTheme.colorScheme.tertiary,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluid_orbs")

    val t1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t1"
    )

    val t2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t2"
    )

    val beatPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beat_pulse"
    )

    val pulseScale = if (isPlaying) beatPulse else 1.0f

    Box(modifier = modifier.fillMaxSize()) {
        // Fluid Orbs Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Deep dark backdrop base
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B14),
                        Color(0xFF0B1220),
                        Color(0xFF030712)
                    )
                )
            )

            // Orb 1: Primary Chromatic Bloom (Top-Left quadrant floating)
            val o1x = w * 0.35f + cos(t1.toDouble()).toFloat() * (w * 0.2f)
            val o1y = h * 0.28f + sin(t1.toDouble()).toFloat() * (h * 0.15f)
            val r1 = (w * 0.6f) * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.38f),
                        primaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(o1x, o1y),
                    radius = r1
                ),
                center = Offset(o1x, o1y),
                radius = r1
            )

            // Orb 2: Secondary Chromatic Bloom (Bottom-Right quadrant floating)
            val o2x = w * 0.72f + sin(t2.toDouble()).toFloat() * (w * 0.18f)
            val o2y = h * 0.68f + cos(t2.toDouble()).toFloat() * (h * 0.2f)
            val r2 = (w * 0.65f) * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = 0.32f),
                        secondaryColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(o2x, o2y),
                    radius = r2
                ),
                center = Offset(o2x, o2y),
                radius = r2
            )

            // Orb 3: Tertiary Luminescent Accent (Center-Left shifting)
            val o3x = w * 0.2f + cos((t2 * 1.3f).toDouble()).toFloat() * (w * 0.15f)
            val o3y = h * 0.82f + sin((t1 * 1.1f).toDouble()).toFloat() * (h * 0.12f)
            val r3 = (w * 0.5f) * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tertiaryColor.copy(alpha = 0.28f),
                        tertiaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(o3x, o3y),
                    radius = r3
                ),
                center = Offset(o3x, o3y),
                radius = r3
            )
        }

        // Frosted Glass Translucent Veil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.70f)
                        )
                    )
                )
        )

        // Screen Content Layer
        content()
    }
}

/**
 * Interactive Waveform Bar Scrubber showing audio density peaks and fluid glowing position cursor.
 */
@Composable
fun LiquidWaveformScrubber(
    currentPosition: Long,
    duration: Long,
    waveform: FloatArray?,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    barCount: Int = 42,
    height: Dp = 48.dp
) {
    val totalMs = duration.coerceAtLeast(1L)
    val progress = (currentPosition.toFloat() / totalMs).coerceIn(0f, 1f)

    var draggingProgress by remember { mutableStateOf<Float?>(null) }
    val effectiveProgress = draggingProgress ?: progress

    // Synthetic or sampled waveform energy peaks
    val barHeights = remember(waveform, barCount) {
        val list = FloatArray(barCount)
        val wf = waveform ?: FloatArray(0)
        for (i in 0 until barCount) {
            if (wf.isNotEmpty()) {
                val idx = ((i.toFloat() / barCount) * wf.size).toInt().coerceIn(0, wf.size - 1)
                list[i] = (kotlin.math.abs(wf[idx]) * 0.85f + 0.15f).coerceIn(0.12f, 1f)
            } else {
                // Harmonic organic profile
                val angle = (i.toFloat() / barCount) * Math.PI.toFloat() * 2f
                val h = (sin(angle * 3.5f) * 0.35f + cos(angle * 1.5f) * 0.25f + 0.5f).coerceIn(0.15f, 0.95f)
                list[i] = h
            }
        }
        list
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(12.dp))
                .glassmorphic(
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = Color.White.copy(alpha = 0.04f),
                    borderColor = Color.White.copy(alpha = 0.12f),
                    borderWidth = 1.dp
                )
                .pointerInput(totalMs) {
                    detectTapGestures { offset ->
                        val p = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((p * totalMs).toLong())
                    }
                }
                .pointerInput(totalMs) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val p = (offset.x / size.width).coerceIn(0f, 1f)
                            draggingProgress = p
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val p = (change.position.x / size.width).coerceIn(0f, 1f)
                            draggingProgress = p
                        },
                        onDragEnd = {
                            draggingProgress?.let { p ->
                                onSeek((p * totalMs).toLong())
                            }
                            draggingProgress = null
                        },
                        onDragCancel = {
                            draggingProgress = null
                        }
                    )
                }
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("liquid_waveform_scrubber"),
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                val barSpacing = 2.5.dp.toPx()
                val totalSpacing = barSpacing * (barCount - 1)
                val barW = (canvasW - totalSpacing) / barCount

                val activeBoundaryX = canvasW * effectiveProgress

                for (i in 0 until barCount) {
                    val x = i * (barW + barSpacing)
                    val barH = canvasH * barHeights[i]
                    val y = (canvasH - barH) / 2f

                    val isBarActive = (x + barW / 2) <= activeBoundaryX
                    val color = if (isBarActive) activeColor else inactiveColor

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barW, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2f, barW / 2f)
                    )
                }

                // Glowing seeker cursor needle
                drawCircle(
                    color = activeColor,
                    radius = 6.dp.toPx(),
                    center = Offset(activeBoundaryX, canvasH / 2f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(activeBoundaryX, canvasH / 2f)
                )
            }
        }

        // Time Indicator Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val currMs = if (draggingProgress != null) (draggingProgress!! * totalMs).toLong() else currentPosition
            Text(
                text = formatDuration(currMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatDuration(totalMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
