package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.ConnectionState
import com.example.network.ConnectionStats
import com.example.ui.theme.GamepadBorder
import com.example.ui.theme.GamepadBorderActive
import com.example.ui.theme.GamepadCyan
import com.example.ui.theme.GamepadGreen
import com.example.ui.theme.GamepadRed
import com.example.ui.theme.GamepadSurface
import com.example.ui.theme.GamepadSurfaceVariant
import com.example.ui.theme.GamepadYellow

@Composable
fun TopCenterHUD(
    connectionState: ConnectionState,
    stats: ConnectionStats,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionState is ConnectionState.Connected

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0D111A).copy(alpha = 0.9f))
            .border(1.dp, GamepadBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("top_status_hud"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
            contentDescription = "Wi-Fi link status",
            tint = if (isConnected) GamepadGreen else Color(0xFF64748B),
            modifier = Modifier.size(13.dp)
        )

        Text(
            text = if (isConnected) "${stats.pingMs} ms" else "OFFLINE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isConnected) GamepadCyan else Color(0xFF94A3B8)
        )

        Box(
            modifier = Modifier
                .size(width = 1.dp, height = 10.dp)
                .background(Color(0xFF283244))
        )

        Text(
            text = "${stats.packetsSent} pkts",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
        )
    }
}

@Composable
fun CenterSystemButtons(
    pressedButtons: Set<String>,
    onButtonDown: (String) -> Unit,
    onButtonUp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag("center_system_buttons"),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Create button
        SystemButtonPill(
            label = "CREATE",
            buttonName = "CREATE",
            isPressed = pressedButtons.contains("CREATE"),
            icon = Icons.Default.Share,
            onButtonDown = onButtonDown,
            onButtonUp = onButtonUp
        )

        // Home button
        HomeCircleButton(
            isPressed = pressedButtons.contains("HOME"),
            onButtonDown = onButtonDown,
            onButtonUp = onButtonUp
        )

        // Options button
        SystemButtonPill(
            label = "OPTIONS",
            buttonName = "OPTIONS",
            isPressed = pressedButtons.contains("OPTIONS"),
            icon = Icons.Default.Menu,
            onButtonDown = onButtonDown,
            onButtonUp = onButtonUp
        )
    }
}

@Composable
private fun SystemButtonPill(
    label: String,
    buttonName: String,
    isPressed: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onButtonDown: (String) -> Unit,
    onButtonUp: (String) -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val bg = if (isPressed) {
        Brush.horizontalGradient(listOf(GamepadCyan.copy(alpha = 0.5f), Color(0xFF132235)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF1B2130), Color(0xFF131722)))
    }

    Row(
        modifier = Modifier
            .size(width = 82.dp, height = 34.dp)
            .clip(shape)
            .background(bg)
            .border(1.dp, if (isPressed) GamepadBorderActive else GamepadBorder, shape)
            .pointerInput(buttonName) {
                detectTapGestures(
                    onPress = {
                        onButtonDown(buttonName)
                        tryAwaitRelease()
                        onButtonUp(buttonName)
                    }
                )
            }
            .padding(horizontal = 6.dp)
            .testTag("btn_${buttonName.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isPressed) Color.White else Color(0xFF8896AB),
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressed) Color.White else Color(0xFF8896AB)
        )
    }
}

@Composable
private fun HomeCircleButton(
    isPressed: Boolean,
    onButtonDown: (String) -> Unit,
    onButtonUp: (String) -> Unit
) {
    val bg = if (isPressed) {
        Brush.radialGradient(listOf(GamepadCyan, Color(0xFF0F3255)))
    } else {
        Brush.radialGradient(listOf(Color(0xFF263347), Color(0xFF151B27)))
    }

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(bg)
            .border(2.dp, if (isPressed) GamepadCyan else GamepadBorder, CircleShape)
            .pointerInput("HOME") {
                detectTapGestures(
                    onPress = {
                        onButtonDown("HOME")
                        tryAwaitRelease()
                        onButtonUp("HOME")
                    }
                )
            }
            .testTag("btn_home"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Home Guide Button",
            tint = if (isPressed) Color.White else GamepadCyan,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun CenterStatusAndLock(
    connectionState: ConnectionState,
    isLocked: Boolean,
    onStatusClick: () -> Unit,
    onLockToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.testTag("center_status_and_lock"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status Pill
        val (dotColor, statusText) = when (connectionState) {
            is ConnectionState.Connected -> Pair(GamepadGreen, connectionState.url.removePrefix("ws://"))
            is ConnectionState.Connecting -> Pair(GamepadYellow, "CONNECTING...")
            is ConnectionState.Error -> Pair(GamepadRed, "FAILED • TAP TO RETRY")
            is ConnectionState.Disconnected -> Pair(Color(0xFF64748B), "DISCONNECTED")
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF111622))
                .border(1.dp, GamepadBorder, RoundedCornerShape(20.dp))
                .clickable { onStatusClick() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("status_pill"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = statusText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFCBD5E1)
            )
        }

        // Lock Control
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (isLocked) Color(0xFF1F2937) else Color(0xFF141924))
                .border(
                    1.dp,
                    if (isLocked) GamepadYellow.copy(alpha = 0.6f) else GamepadBorder,
                    RoundedCornerShape(14.dp)
                )
                .clickable { onLockToggle() }
                .padding(horizontal = 12.dp, vertical = 5.dp)
                .testTag("lock_control"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = if (isLocked) "Layout Locked" else "Layout Unlocked",
                tint = if (isLocked) GamepadYellow else Color(0xFF8896AB),
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = if (isLocked) "LOCKED" else "UNLOCK",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLocked) GamepadYellow else Color(0xFF8896AB)
            )
        }
    }
}

@Composable
fun BottomControls(
    isArrangeEnabled: Boolean,
    onArrangeClick: () -> Unit,
    onSetupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag("bottom_controls"),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Arrange button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (isArrangeEnabled) GamepadCyan.copy(alpha = 0.2f) else Color(0xFF161B28))
                .border(
                    1.dp,
                    if (isArrangeEnabled) GamepadCyan else GamepadBorder,
                    RoundedCornerShape(14.dp)
                )
                .clickable { onArrangeClick() }
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("arrange_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Dashboard,
                contentDescription = "Arrange mode",
                tint = if (isArrangeEnabled) GamepadCyan else Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "ARRANGE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isArrangeEnabled) GamepadCyan else Color(0xFF94A3B8)
            )
        }

        // Setup button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF161B28))
                .border(1.dp, GamepadBorder, RoundedCornerShape(14.dp))
                .clickable { onSetupClick() }
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("setup_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Setup connection",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "SETUP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
