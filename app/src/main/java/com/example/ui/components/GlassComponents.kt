package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Applies a smooth frosted glassmorphic background and specular highlight border.
 */
fun Modifier.glassmorphic(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = Color(0x331E293B),
    gradientEndColor: Color = Color(0x1A0F172A),
    borderColor: Color = Color(0x33FFFFFF),
    borderEndColor: Color = Color(0x10FFFFFF),
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.linearGradient(
            colors = listOf(backgroundColor, gradientEndColor)
        ),
        shape = shape
    )
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(borderColor, borderEndColor)
        ),
        shape = shape
    )

/**
 * Sleek Translucent Glass Card with soft specular sheen and rounded corners.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
    borderColor: Color = Color.White.copy(alpha = 0.22f),
    borderEndColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "glass_card_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            )
            .glassmorphic(
                shape = shape,
                backgroundColor = backgroundColor,
                gradientEndColor = backgroundColor.copy(alpha = 0.35f),
                borderColor = borderColor,
                borderEndColor = borderEndColor,
                borderWidth = 1.2.dp
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(),
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        content()
    }
}

/**
 * Ultra-smooth Glass Tactile Button with press bounce, specular edge highlight, and glow.
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(24.dp),
    containerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    leadingIcon: ImageVector? = null,
    text: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "glass_button_scale"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        color = Color.Transparent,
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 48.dp)
            .glassmorphic(
                shape = shape,
                backgroundColor = if (isPressed) containerColor.copy(alpha = 0.4f) else containerColor,
                gradientEndColor = containerColor.copy(alpha = 0.12f),
                borderColor = if (isPressed) Color.White.copy(alpha = 0.6f) else borderColor,
                borderEndColor = borderColor.copy(alpha = 0.2f),
                borderWidth = 1.2.dp
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Translucent Glass Circular/Squircle Icon Button with glowing rim.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String?,
    shape: Shape = CircleShape,
    size: Dp = 48.dp,
    iconSize: Dp = 22.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
    borderColor: Color = Color.White.copy(alpha = 0.28f),
    isActive: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "glass_icon_button_scale"
    )

    val actualBg = if (isActive) activeColor.copy(alpha = 0.35f) else backgroundColor
    val actualBorder = if (isActive) activeColor.copy(alpha = 0.7f) else borderColor
    val actualTint = if (isActive) activeColor else tint

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .glassmorphic(
                shape = shape,
                backgroundColor = actualBg,
                gradientEndColor = actualBg.copy(alpha = 0.15f),
                borderColor = actualBorder,
                borderEndColor = actualBorder.copy(alpha = 0.2f),
                borderWidth = 1.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = actualTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Sleek Translucent Glass Pill Badge / Chip.
 */
@Composable
fun GlassPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selectedColor: Color = MaterialTheme.colorScheme.primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        label = "pill_scale"
    )

    val bg = if (selected) selectedColor.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f)
    val border = if (selected) selectedColor.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.18f)
    val textColor = if (selected) selectedColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 36.dp)
            .glassmorphic(
                shape = RoundedCornerShape(18.dp),
                backgroundColor = bg,
                gradientEndColor = bg.copy(alpha = 0.1f),
                borderColor = border,
                borderEndColor = border.copy(alpha = 0.2f),
                borderWidth = if (selected) 1.5.dp else 1.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * Live animated beat equalizer bars for displaying active rhythm in mini player or hero cards.
 */
@Composable
fun GlassBeatIndicator(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 4
) {
    val infiniteTransition = rememberInfiniteTransition(label = "beat_bars")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 410, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h4"
    )

    val heights = listOf(h1, h2, h3, h4)

    Row(
        modifier = modifier
            .height(20.dp)
            .width(22.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val fraction = if (isPlaying) heights[i % heights.size] else 0.25f
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                color,
                                color.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }
    }
}
