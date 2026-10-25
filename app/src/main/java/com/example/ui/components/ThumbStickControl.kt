package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GamepadBorder
import com.example.ui.theme.GamepadBorderActive
import com.example.ui.theme.GamepadCyan
import com.example.ui.theme.GamepadSurface
import com.example.ui.theme.GamepadSurfaceVariant
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ThumbStickControl(
    size: Dp = 140.dp,
    label: String, // "L3" or "R3"
    isPressed: Boolean = false,
    onMove: (Float, Float) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    val capSize = size * 0.46f
    val capSizePx = with(density) { capSize.toPx() }
    val maxRadiusPx = (sizePx - capSizePx) / 2f

    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .size(size)
            .testTag(if (label == "L3") "left_stick" else "right_stick"),
        contentAlignment = Alignment.Center
    ) {
        // Base plate background
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GamepadSurfaceVariant, GamepadSurface, Color(0xFF0C0E14))
                    )
                )
                .border(1.5.dp, GamepadBorder, CircleShape)
        )

        // Concentric guidelines & crosshairs
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val r1 = size.toPx() * 0.35f
            val r2 = size.toPx() * 0.22f

            drawCircle(
                color = Color(0xFF1E2433),
                radius = r1,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF191F2C),
                radius = r2,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )

            // Crosshair markers
            drawLine(
                color = Color(0xFF222B3D),
                start = Offset(center.x - 12.dp.toPx(), center.y),
                end = Offset(center.x + 12.dp.toPx(), center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0xFF222B3D),
                start = Offset(center.x, center.y - 12.dp.toPx()),
                end = Offset(center.x, center.y + 12.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draggable Thumb Cap
        val currentOffsetX = offsetXAnim.value
        val currentOffsetY = offsetYAnim.value

        Box(
            modifier = Modifier
                .offset { IntOffset(currentOffsetX.roundToInt(), currentOffsetY.roundToInt()) }
                .size(capSize)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isPressed) {
                            listOf(GamepadCyan.copy(alpha = 0.8f), Color(0xFF0F2B48), Color(0xFF0A1929))
                        } else {
                            listOf(Color(0xFF2E384D), Color(0xFF1E2535), Color(0xFF121620))
                        }
                    )
                )
                .border(
                    width = 2.dp,
                    color = if (isPressed) GamepadBorderActive else Color(0xFF333F54),
                    shape = CircleShape
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { onClick() },
                        onTap = { onClick() }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            coroutineScope.launch {
                                offsetXAnim.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            coroutineScope.launch {
                                offsetYAnim.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            onMove(0f, 0f)
                        },
                        onDragCancel = {
                            coroutineScope.launch { offsetXAnim.animateTo(0f) }
                            coroutineScope.launch { offsetYAnim.animateTo(0f) }
                            onMove(0f, 0f)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val nextX = offsetXAnim.value + dragAmount.x
                            val nextY = offsetYAnim.value + dragAmount.y
                            val dist = sqrt(nextX * nextX + nextY * nextY)

                            val (clampedX, clampedY) = if (dist > maxRadiusPx && dist > 0f) {
                                val angle = atan2(nextY, nextX)
                                Pair(cos(angle) * maxRadiusPx, sin(angle) * maxRadiusPx)
                            } else {
                                Pair(nextX, nextY)
                            }

                            coroutineScope.launch {
                                offsetXAnim.snapTo(clampedX)
                                offsetYAnim.snapTo(clampedY)
                            }

                            // Normalized coordinates: -1.0 to 1.0 (inverted Y for standard gamepad stick: up is negative or positive)
                            // Usually: stick up is negative Y or positive Y depending on convention.
                            // In JSON protocol: y: 0.8 is positive or negative. We send normalized: x = clampedX / maxRadius, y = clampedY / maxRadius
                            val normX = (clampedX / maxRadiusPx).coerceIn(-1f, 1f)
                            val normY = (clampedY / maxRadiusPx).coerceIn(-1f, 1f)
                            onMove(normX, normY)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Thumb cap textured grip ring
            Box(
                modifier = Modifier
                    .size(capSize * 0.7f)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF263246), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPressed) GamepadCyan else Color(0xFF64748B)
                )
            }
        }
    }
}
