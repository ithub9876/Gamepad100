package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GamepadBorder
import com.example.ui.theme.GamepadBorderActive
import com.example.ui.theme.GamepadCyan
import com.example.ui.theme.GamepadSurface
import com.example.ui.theme.GamepadSurfaceVariant

@Composable
fun DPadControl(
    size: Dp = 150.dp,
    pressedButtons: Set<String>,
    onButtonDown: (String) -> Unit,
    onButtonUp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val upPressed = pressedButtons.contains("DPAD_UP")
    val downPressed = pressedButtons.contains("DPAD_DOWN")
    val leftPressed = pressedButtons.contains("DPAD_LEFT")
    val rightPressed = pressedButtons.contains("DPAD_RIGHT")

    val armWidth = size * 0.36f
    val armLength = size

    Box(
        modifier = modifier
            .size(size)
            .testTag("dpad_control"),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow base
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GamepadSurfaceVariant.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )

        // Vertical arm
        Box(
            modifier = Modifier
                .size(width = armWidth, height = armLength)
                .clip(RoundedCornerShape(12.dp))
                .background(GamepadSurface)
                .border(1.5.dp, GamepadBorder, RoundedCornerShape(12.dp))
        )

        // Horizontal arm
        Box(
            modifier = Modifier
                .size(width = armLength, height = armWidth)
                .clip(RoundedCornerShape(12.dp))
                .background(GamepadSurface)
                .border(1.5.dp, GamepadBorder, RoundedCornerShape(12.dp))
        )

        // Center hub
        Box(
            modifier = Modifier
                .size(armWidth * 0.95f)
                .clip(RoundedCornerShape(8.dp))
                .background(GamepadSurfaceVariant)
        )

        // UP direction pad
        DirectionalPad(
            buttonName = "DPAD_UP",
            isPressed = upPressed,
            width = armWidth,
            height = (armLength - armWidth) / 2 + 8.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 0.dp),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            onButtonDown = onButtonDown,
            onButtonUp = onButtonUp
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "D-Pad Up",
                tint = if (upPressed) GamepadCyan else Color(0xFF94A3B8),
                modifier = Modifier.size(24.dp)
            )
        }

        // DOWN direction pad
        DirectionalPad(
            buttonName = "DPAD_DOWN",
            isPressed = downPressed,
            width = armWidth,
            height = (armLength - armWidth) / 2 + 8.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 0.dp),
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            onButtonDown = onButtonDown,
            onButtonUp = onButtonUp
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "D-Pad Down",
                tint = if (downPressed) GamepadCyan else Color(0xFF94A3B8),
                modifier = Modifier.size(24.dp)
            )
        }

        // LEFT direction pad
        DirectionalPad(
            buttonName = "DPAD_LEFT",
            isPressed = leftPressed,
            width = (armLength - armWidth) / 2 + 8.dp,
            height = armWidth,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 0.dp),
            shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
            onButtonDown = onButtonDown,
            onButtonUp = onButtonUp
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "D-Pad Left",
                tint = if (leftPressed) GamepadCyan else Color(0xFF94A3B8),
                modifier = Modifier.size(24.dp)
            )
        }

        // RIGHT direction pad
        DirectionalPad(
            buttonName = "DPAD_RIGHT",
            isPressed = rightPressed,
            width = (armLength - armWidth) / 2 + 8.dp,
            height = armWidth,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 0.dp),
            shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
            onButtonDown = onButtonDown,
            onButtonUp = onButtonUp
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "D-Pad Right",
                tint = if (rightPressed) GamepadCyan else Color(0xFF94A3B8),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DirectionalPad(
    buttonName: String,
    isPressed: Boolean,
    width: Dp,
    height: Dp,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    onButtonDown: (String) -> Unit,
    onButtonUp: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val bg = if (isPressed) {
        Brush.verticalGradient(listOf(Color(0xFF0284C7).copy(alpha = 0.5f), Color(0xFF1E293B)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF1E2430), Color(0xFF141822)))
    }

    val borderCol = if (isPressed) GamepadBorderActive else GamepadBorder

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(shape)
            .background(bg)
            .border(1.dp, borderCol, shape)
            .pointerInput(buttonName) {
                detectTapGestures(
                    onPress = {
                        onButtonDown(buttonName)
                        tryAwaitRelease()
                        onButtonUp(buttonName)
                    }
                )
            }
            .testTag(buttonName.lowercase()),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
