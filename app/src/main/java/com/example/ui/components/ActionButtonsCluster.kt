package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ButtonAColor
import com.example.ui.theme.ButtonBColor
import com.example.ui.theme.ButtonXColor
import com.example.ui.theme.ButtonYColor
import com.example.ui.theme.GamepadBorder
import com.example.ui.theme.GamepadSurface
import com.example.ui.theme.GamepadSurfaceVariant

@Composable
fun ActionButtonsCluster(
    size: Dp = 150.dp,
    pressedButtons: Set<String>,
    onButtonDown: (String) -> Unit,
    onButtonUp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val btnSize = size * 0.32f
    val radius = size * 0.34f

    Box(
        modifier = modifier
            .size(size)
            .testTag("action_buttons_cluster"),
        contentAlignment = Alignment.Center
    ) {
        // Base plate background
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GamepadSurfaceVariant.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )

        // Y Button (Top)
        ActionButton(
            label = "Y",
            accentColor = ButtonYColor,
            isPressed = pressedButtons.contains("Y"),
            size = btnSize,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -radius),
            onButtonDown = onButtonDown,
            onButtonUp = onButtonUp
        )

        // X Button (Left)
        ActionButton(
            label = "X",
            accentColor = ButtonXColor,
            isPressed = pressedButtons.contains("X"),
            size = btnSize,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = -radius),
            onButtonDown = onButtonDown,
            onButtonUp = onButtonUp
        )

        // B Button (Right)
        ActionButton(
            label = "B",
            accentColor = ButtonBColor,
            isPressed = pressedButtons.contains("B"),
            size = btnSize,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = radius),
            onButtonDown = onButtonDown,
            onButtonUp = onButtonUp
        )

        // A Button (Bottom)
        ActionButton(
            label = "A",
            accentColor = ButtonAColor,
            isPressed = pressedButtons.contains("A"),
            size = btnSize,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = radius),
            onButtonDown = onButtonDown,
            onButtonUp = onButtonUp
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    accentColor: Color,
    isPressed: Boolean,
    size: Dp,
    modifier: Modifier = Modifier,
    onButtonDown: (String) -> Unit,
    onButtonUp: (String) -> Unit
) {
    val bg = if (isPressed) {
        Brush.radialGradient(
            colors = listOf(accentColor.copy(alpha = 0.85f), accentColor.copy(alpha = 0.4f), Color(0xFF131722))
        )
    } else {
        Brush.radialGradient(
            colors = listOf(Color(0xFF262F40), Color(0xFF171B26), Color(0xFF0F121A))
        )
    }

    val borderCol = if (isPressed) accentColor else GamepadBorder

    Box(
        modifier = modifier
            .size(size)
            .shadow(if (isPressed) 8.dp else 4.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(bg)
            .border(1.5.dp, borderCol, CircleShape)
            .pointerInput(label) {
                detectTapGestures(
                    onPress = {
                        onButtonDown(label)
                        tryAwaitRelease()
                        onButtonUp(label)
                    }
                )
            }
            .testTag("btn_${label.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = if (isPressed) Color.White else accentColor
        )
    }
}
