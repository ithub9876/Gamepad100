package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.network.ConnectionState
import com.example.ui.theme.GamepadBorder
import com.example.ui.theme.GamepadCyan
import com.example.ui.theme.GamepadGreen
import com.example.ui.theme.GamepadRed
import com.example.ui.theme.GamepadSurface
import com.example.ui.theme.GamepadSurfaceVariant
import com.example.ui.theme.GamepadYellow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupDialog(
    currentIp: String,
    currentPort: Int,
    recentIps: List<String>,
    connectionState: ConnectionState,
    isHapticEnabled: Boolean,
    onIpChange: (String) -> Unit,
    onPortChange: (Int) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onToggleHaptic: () -> Unit,
    onDismiss: () -> Unit
) {
    var ipText by remember { mutableStateOf(currentIp) }
    var portText by remember { mutableStateOf(currentPort.toString()) }
    var showHelp by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(GamepadSurface)
                .border(1.5.dp, GamepadBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("setup_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "PC Gamepad Setup",
                            tint = GamepadCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Windows PC Connection",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_setup_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                // Connection Status Banner
                val (bannerBg, statusTitle, statusSubtitle) = when (connectionState) {
                    is ConnectionState.Connected -> Triple(
                        GamepadGreen.copy(alpha = 0.15f),
                        "CONNECTED",
                        "Streaming inputs to ${connectionState.url}"
                    )
                    is ConnectionState.Connecting -> Triple(
                        GamepadYellow.copy(alpha = 0.15f),
                        "CONNECTING...",
                        "Establishing WebSocket link to ${connectionState.url}"
                    )
                    is ConnectionState.Error -> Triple(
                        GamepadRed.copy(alpha = 0.15f),
                        "CONNECTION FAILED",
                        connectionState.message
                    )
                    is ConnectionState.Disconnected -> Triple(
                        Color(0xFF1E2536),
                        "DISCONNECTED",
                        "Ensure your PC and phone are on the same Wi-Fi network"
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bannerBg)
                        .border(1.dp, GamepadBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (connectionState) {
                                        is ConnectionState.Connected -> GamepadGreen
                                        is ConnectionState.Connecting -> GamepadYellow
                                        is ConnectionState.Error -> GamepadRed
                                        is ConnectionState.Disconnected -> Color(0xFF64748B)
                                    }
                                )
                        )
                        Column {
                            Text(
                                text = statusTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = statusSubtitle,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }

                // IP and Port Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // IP field
                    OutlinedTextField(
                        value = ipText,
                        onValueChange = {
                            ipText = it
                            onIpChange(it)
                        },
                        label = { Text("PC IP Address", fontSize = 11.sp) },
                        placeholder = { Text("e.g. 192.168.1.100", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GamepadCyan,
                            unfocusedBorderColor = GamepadBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .weight(2.5f)
                            .testTag("ip_input_field")
                    )

                    // Port field
                    OutlinedTextField(
                        value = portText,
                        onValueChange = {
                            portText = it
                            it.toIntOrNull()?.let { p -> onPortChange(p) }
                        },
                        label = { Text("Port", fontSize = 11.sp) },
                        placeholder = { Text("8080", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GamepadCyan,
                            unfocusedBorderColor = GamepadBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("port_input_field")
                    )
                }

                // Recent IPs Quick Selection
                if (recentIps.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Recent Host IPs:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            recentIps.forEach { ip ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GamepadSurfaceVariant)
                                        .border(1.dp, GamepadBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            ipText = ip
                                            onIpChange(ip)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("recent_ip_$ip")
                                ) {
                                    Text(
                                        text = ip,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = GamepadCyan
                                    )
                                }
                            }
                        }
                    }
                }

                // Connect / Disconnect Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (connectionState is ConnectionState.Connected) {
                        Button(
                            onClick = onDisconnect,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GamepadRed,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("disconnect_button")
                        ) {
                            Text("DISCONNECT", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onConnect,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GamepadCyan,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("connect_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text("CONNECT TO PC", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                // Settings: Haptic Vibration Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141824))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Haptic feedback",
                            tint = GamepadCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Haptic Vibration",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Tactile touch responses on buttons and stick clicks",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Switch(
                        checked = isHapticEnabled,
                        onCheckedChange = { onToggleHaptic() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GamepadCyan,
                            checkedTrackColor = Color(0xFF0F3255),
                            uncheckedThumbColor = Color(0xFF64748B),
                            uncheckedTrackColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("haptic_switch")
                    )
                }

                // Companion Help toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHelp = !showHelp }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Help",
                        tint = GamepadCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (showHelp) "Hide Windows companion setup instructions"
                        else "How to connect with Windows companion app?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GamepadCyan
                    )
                }

                if (showHelp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0D1017))
                            .border(1.dp, GamepadBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "1. Run the Python companion server on your PC:",
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "python companion_server.py --port 8080",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = GamepadGreen
                        )
                        Text(
                            text = "2. Enter your PC's local IP address (find using 'ipconfig' in cmd).",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "3. Tap CONNECT. The status pill will turn green.",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}
