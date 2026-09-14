package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.domain.model.VisualizerMode
import kotlin.math.*
import kotlin.random.Random

@Composable
fun VisualizerCanvas(
    mode: VisualizerMode,
    waveform: FloatArray,
    fft: FloatArray,
    isPlaying: Boolean,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    sensitivity: Float = 1.0f,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    accentColor: Color = Color(0xFF00F0FF)
) {
    // Dynamic continuous rotation & phase animation
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val slowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pre-allocated particle seed positions for CYBER_PARTICLES
    val particleSeeds = remember {
        List(48) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = 0.3f + Random.nextFloat() * 0.7f
            val baseRadius = 0.1f + Random.nextFloat() * 0.85f
            val size = 2f + Random.nextFloat() * 4f
            Triple(angle, speed, Pair(baseRadius, size))
        }
    }

    // Peak tracking for Frequency Bars
    var peakHeights by remember { mutableStateOf(FloatArray(32) { 0f }) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(12.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val centerX = width / 2f

        // Calculate aggregate beat energy (bass dominance)
        var bassEnergy = 0f
        val bassRange = minOf(6, fft.size)
        for (i in 0 until bassRange) {
            bassEnergy += fft[i]
        }
        bassEnergy = if (bassRange > 0) (bassEnergy / bassRange) * sensitivity else 0.05f
        if (!isPlaying || reducedMotion) bassEnergy = 0.08f

        when (mode) {
            VisualizerMode.FREQUENCY_BARS -> {
                val numBars = minOf(fft.size.coerceAtLeast(1), 32)
                val barSpacing = 4.dp.toPx()
                val totalSpacing = barSpacing * (numBars - 1)
                val barWidth = ((width - totalSpacing) / numBars).coerceAtLeast(3f)

                if (peakHeights.size != numBars) {
                    peakHeights = FloatArray(numBars) { 0f }
                }

                for (i in 0 until numBars) {
                    val rawMag = if (isPlaying && !reducedMotion) fft[i] * sensitivity else 0.06f
                    val magnitude = rawMag.coerceIn(0.04f, 1.0f)
                    val barHeight = (height * 0.88f * magnitude).coerceIn(4.dp.toPx(), height * 0.9f)
                    val x = i * (barWidth + barSpacing)
                    val y = height - barHeight

                    // Update falling peak caps
                    if (barHeight > peakHeights[i]) {
                        peakHeights[i] = barHeight
                    } else {
                        peakHeights[i] = (peakHeights[i] - 1.5f).coerceAtLeast(0f)
                    }

                    // Bar Gradient
                    val barBrush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor,
                            primaryColor,
                            secondaryColor.copy(alpha = 0.4f)
                        ),
                        startY = y,
                        endY = height
                    )

                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Peak cap dot
                    val peakY = (height - peakHeights[i] - 3.dp.toPx()).coerceAtLeast(0f)
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = Offset(x, peakY),
                        size = Size(barWidth, 3.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }

            VisualizerMode.OSCILLOSCOPE -> {
                val numPoints = waveform.size.coerceAtLeast(2)
                val stepX = width / (numPoints - 1)
                val path = Path()
                val glowPath = Path()

                for (i in 0 until numPoints) {
                    val rawSample = if (isPlaying && !reducedMotion) waveform[i] * sensitivity else 0f
                    val sample = rawSample.coerceIn(-1f, 1f)
                    val x = i * stepX
                    val y = centerY + (sample * (centerY * 0.85f))

                    if (i == 0) {
                        path.moveTo(x, y)
                        glowPath.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        glowPath.lineTo(x, y)
                    }
                }

                // Ambient gradient fill under wave
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        startY = centerY * 0.5f,
                        endY = height
                    )
                )

                // Neon Outer Glow Stroke
                drawPath(
                    path = glowPath,
                    color = accentColor.copy(alpha = 0.35f),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Crisp Center Neon Line
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(secondaryColor, primaryColor, accentColor)
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Central Horizon Baseline
                drawLine(
                    color = primaryColor.copy(alpha = 0.2f),
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            VisualizerMode.RADIAL_PULSE -> {
                val maxRadius = minOf(centerX, centerY) * 0.9f
                val minRadius = maxRadius * 0.32f
                val numPoints = minOf(fft.size, 48).coerceAtLeast(16)
                val angleStep = (2.0 * PI / numPoints).toFloat()

                // Center Pulsing Core with Bass Energy
                val corePulse = minRadius * (0.8f + bassEnergy * 0.4f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = corePulse * 1.5f
                    ),
                    radius = corePulse * 1.5f,
                    center = Offset(centerX, centerY)
                )

                drawCircle(
                    color = primaryColor.copy(alpha = 0.2f),
                    radius = minRadius,
                    center = Offset(centerX, centerY)
                )

                val path = Path()
                for (i in 0 until numPoints) {
                    val rawMag = if (isPlaying && !reducedMotion) fft[i % fft.size] * sensitivity else 0.08f
                    val mag = rawMag.coerceIn(0.04f, 1.2f)
                    val r = minRadius + (maxRadius - minRadius) * mag
                    val angle = i * angleStep + (if (!reducedMotion) phase * 0.2f else 0f)
                    val x = centerX + r * cos(angle)
                    val y = centerY + r * sin(angle)

                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()

                // Outer Glowing Polygon
                drawPath(
                    path = path,
                    color = accentColor.copy(alpha = 0.3f),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = path,
                    brush = Brush.sweepGradient(
                        colors = listOf(primaryColor, accentColor, secondaryColor, primaryColor),
                        center = Offset(centerX, centerY)
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            VisualizerMode.BEAT_RINGS -> {
                val maxRadius = minOf(centerX, centerY) * 0.95f
                val ringCount = 5

                // Central beat thump sphere
                val centerPulse = (24.dp.toPx() + bassEnergy * 40.dp.toPx()).coerceAtMost(maxRadius * 0.4f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            primaryColor.copy(alpha = 0.7f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = centerPulse
                    ),
                    radius = centerPulse,
                    center = Offset(centerX, centerY)
                )

                // Expanding concentric beat rings
                for (i in 0 until ringCount) {
                    val progress = ((phase / (2 * PI).toFloat()) + (i.toFloat() / ringCount)) % 1f
                    val ringRadius = 20.dp.toPx() + (maxRadius - 20.dp.toPx()) * progress
                    val alpha = ((1f - progress) * (0.15f + bassEnergy * 0.75f)).coerceIn(0f, 1f)
                    val strokeWidth = (4.dp.toPx() * (1f - progress * 0.6f)).coerceAtLeast(1.dp.toPx())

                    val ringColor = if (i % 2 == 0) primaryColor else accentColor

                    drawCircle(
                        color = ringColor.copy(alpha = alpha),
                        radius = ringRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = strokeWidth)
                    )
                }
            }

            VisualizerMode.CYBER_PARTICLES -> {
                val maxDist = minOf(centerX, centerY) * 0.95f

                // Central energy generator
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = 48.dp.toPx() * (1f + bassEnergy)
                    ),
                    radius = 48.dp.toPx() * (1f + bassEnergy),
                    center = Offset(centerX, centerY)
                )

                // Draw floating & bursting audio particles
                particleSeeds.forEachIndexed { index, seed ->
                    val (baseAngle, speedMultiplier, radiusAndSize) = seed
                    val (baseRadiusFraction, dotSize) = radiusAndSize

                    val dynamicAngle = baseAngle + (slowRotation * (PI / 180f).toFloat() * speedMultiplier)
                    val fftFactor = if (index < fft.size) fft[index] * sensitivity else 0.1f
                    val distance = maxDist * (baseRadiusFraction + fftFactor * 0.5f + (bassEnergy * 0.25f)).coerceIn(0.1f, 1f)

                    val px = centerX + distance * cos(dynamicAngle)
                    val py = centerY + distance * sin(dynamicAngle)

                    val particleColor = when (index % 3) {
                        0 -> primaryColor
                        1 -> accentColor
                        else -> secondaryColor
                    }

                    val particleAlpha = (0.35f + (fftFactor * 0.65f)).coerceIn(0.2f, 1f)

                    drawCircle(
                        color = particleColor.copy(alpha = particleAlpha),
                        radius = (dotSize * (1f + fftFactor * 1.5f)).coerceAtLeast(1.5f),
                        center = Offset(px, py)
                    )
                }
            }

            VisualizerMode.CIRCULAR_BARS -> {
                val outerRadius = minOf(centerX, centerY) * 0.92f
                val innerRadius = outerRadius * 0.45f
                val numBars = 48
                val angleStep = (2 * PI / numBars).toFloat()

                // Center Vinyl/Disc Core
                drawCircle(
                    color = Color.Black.copy(alpha = 0.5f),
                    radius = innerRadius - 4.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.4f),
                    radius = innerRadius - 4.dp.toPx(),
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = accentColor,
                    radius = 6.dp.toPx() * (1f + bassEnergy * 0.5f),
                    center = Offset(centerX, centerY)
                )

                for (i in 0 until numBars) {
                    val fftIndex = (i * fft.size / numBars).coerceIn(0, fft.size - 1)
                    val mag = if (isPlaying && !reducedMotion) (fft[fftIndex] * sensitivity).coerceIn(0.04f, 1f) else 0.06f
                    val barLen = (outerRadius - innerRadius) * mag

                    val angle = i * angleStep + (slowRotation * (PI / 180f).toFloat())
                    val startX = centerX + innerRadius * cos(angle)
                    val startY = centerY + innerRadius * sin(angle)
                    val endX = centerX + (innerRadius + barLen) * cos(angle)
                    val endY = centerY + (innerRadius + barLen) * sin(angle)

                    val barColor = if (i % 2 == 0) primaryColor else accentColor

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(primaryColor, accentColor),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY)
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            VisualizerMode.WAVE_HORIZON -> {
                val layers = 3
                val pointsPerWave = 24
                val stepX = width / (pointsPerWave - 1)

                for (layer in 0 until layers) {
                    val wavePath = Path()
                    val layerY = centerY + (layer - 1) * 35.dp.toPx()
                    val layerColor = when (layer) {
                        0 -> secondaryColor.copy(alpha = 0.35f)
                        1 -> primaryColor.copy(alpha = 0.65f)
                        else -> accentColor
                    }

                    for (i in 0 until pointsPerWave) {
                        val fftIndex = (i * fft.size / pointsPerWave).coerceIn(0, fft.size - 1)
                        val mag = if (isPlaying && !reducedMotion) fft[fftIndex] * sensitivity else 0.08f
                        val wavePhase = phase + layer * 1.2f + i * 0.3f
                        val waveHeight = sin(wavePhase) * (mag * 50.dp.toPx() + 8.dp.toPx())

                        val x = i * stepX
                        val y = layerY - waveHeight

                        if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                    }

                    // Stroke with gradient
                    drawPath(
                        path = wavePath,
                        color = layerColor,
                        style = Stroke(width = (4 - layer).dp.toPx().coerceAtLeast(1.5.dp.toPx()), cap = StrokeCap.Round)
                    )
                }
            }

            VisualizerMode.NEON_HEARTBEAT -> {
                // Electrocardiogram / rhythmic beat spike pulse
                val path = Path()
                val glowPath = Path()
                val numPoints = 64
                val stepX = width / (numPoints - 1)

                for (i in 0 until numPoints) {
                    val x = i * stepX
                    val normX = i.toFloat() / numPoints
                    // Spike occurs around normalized 0.3, 0.5, 0.7
                    val spike1 = exp(-((normX - 0.45f) * (normX - 0.45f)) / 0.003f) * (bassEnergy * 1.8f)
                    val spike2 = -exp(-((normX - 0.52f) * (normX - 0.52f)) / 0.002f) * (bassEnergy * 1.2f)
                    val flutter = if (isPlaying) sin(normX * 24f + phase * 2f) * 6.dp.toPx() else 0f

                    val y = centerY - (spike1 + spike2) * (centerY * 0.8f) + flutter

                    if (i == 0) {
                        path.moveTo(x, y)
                        glowPath.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        glowPath.lineTo(x, y)
                    }
                }

                // Outer glow
                drawPath(
                    path = glowPath,
                    color = accentColor.copy(alpha = 0.3f),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                // Crisp core
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}
