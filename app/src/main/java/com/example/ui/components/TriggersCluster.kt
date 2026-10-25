package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GamepadBorder
import com.example.ui.theme.GamepadBorderActive
import com.example.ui.theme.GamepadCyan
import com.example.ui.theme.GamepadSurface
import com.example.ui.theme.GamepadSurfaceVariant

@Composable
fun TriggersCluster(
    isLeft: Boolean,
    triggerValue: Float,
    isBumperPressed: Boolean,
    onBumperDown: (String) -> Unit,
    onBumperUp: (String) -> Unit,
    onTriggerChange: (Boolean, Float) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 150.dp,
    height: Dp = 70.dp
) {
    val bumperName = if (isLeft) "L1" else "R1"
    val triggerName = if (isLeft) "L2" else "R2"

    Row(
        modifier = modifier
            .size(width = width, height = height)
            .testTag(if (isLeft) "cluster_l1l2" else "cluster_r1r2"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLeft) {
            // L2 Trigger on outer left
            TriggerButton(
                label = triggerName,
                isLeft = true,
                value = triggerValue,
                onTriggerChange = onTriggerChange,
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
            )
            // L1 Bumper on inner
            BumperButton(
                label = bumperName,
                isPressed = isBumperPressed,
                onButtonDown = onBumperDown,
                onButtonUp = onBumperUp,
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
            )
        } else {
            // R1 Bumper on inner
            BumperButton(
                label = bumperName,
                isPressed = isBumperPressed,
                onButtonDown = onBumperDown,
                onButtonUp = onBumperUp,
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
            )
            // R2 Trigger on outer right
            TriggerButton(
                label = triggerName,
                isLeft = false,
                value = triggerValue,
                onTriggerChange = onTriggerChange,
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun BumperButton(
    label: String,
    isPressed: Boolean,
    onButtonDown: (String) -> Unit,
    onButtonUp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val bg = if (isPressed) {
        Brush.verticalGradient(listOf(GamepadCyan.copy(alpha = 0.6f), Color(0xFF0F243B)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF202636), Color(0xFF141824)))
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(1.5.dp, if (isPressed) GamepadBorderActive else GamepadBorder, shape)
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
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isPressed) Color.White else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun TriggerButton(
    label: String,
    isLeft: Boolean,
    value: Float,
    onTriggerChange: (Boolean, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val isPressed = value > 0.05f

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A2130), Color(0xFF101420))
                )
            )
            .border(1.5.dp, if (isPressed) GamepadBorderActive else GamepadBorder, shape)
            .pointerInput(label) {
                detectTapGestures(
                    onPress = {
                        onTriggerChange(isLeft, 1.0f)
                        tryAwaitRelease()
                        onTriggerChange(isLeft, 0.0f)
                    }
                )
            }
            .testTag("btn_${label.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        // Active pressure fill indicator
        if (value > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(value.coerceIn(0f, 1f))
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(GamepadCyan.copy(alpha = 0.7f), GamepadCyan.copy(alpha = 0.3f))
                        )
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (isPressed) Color.White else Color(0xFF94A3B8)
            )
            if (value > 0.1f) {
                Text(
                    text = "${(value * 100).toInt()}%",
                    fontSize = 9.sp,
                    color = GamepadCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
