package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ControlElement
import com.example.ui.theme.GamepadBorder
import com.example.ui.theme.GamepadCyan
import com.example.ui.theme.GamepadGreen
import com.example.ui.theme.GamepadRed
import com.example.ui.theme.GamepadSurface

@Composable
fun ArrangeGridBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val step = 40.dp.toPx()
        val gridColor = Color(0x1A38BDF8)
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += step
        }
    }
}

@Composable
fun ArrangeTopBar(
    selectedElement: ControlElement?,
    overallScale: Float,
    onElementScaleChange: (Float) -> Unit,
    onOverallScaleChange: (Float) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xE60F1420))
            .border(1.dp, GamepadCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("arrange_top_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selected item status & instructions
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ARRANGE MODE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = GamepadCyan
                )
                Text(
                    text = if (selectedElement != null) "Selected: ${selectedElement.name} (drag to reposition)"
                    else "Tap any control to select, drag or resize",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Sliders for scaling
            Row(
                modifier = Modifier
                    .weight(1.8f)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Individual element scale
                if (selectedElement != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Size: ${String.format("%.1f", selectedElement.scale)}x",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCBD5E1)
                        )
                        Slider(
                            value = selectedElement.scale,
                            onValueChange = onElementScaleChange,
                            valueRange = 0.6f..1.8f,
                            colors = SliderDefaults.colors(
                                thumbColor = GamepadCyan,
                                activeTrackColor = GamepadCyan,
                                inactiveTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.testTag("element_scale_slider")
                        )
                    }
                }

                // Overall scale
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Global: ${String.format("%.1f", overallScale)}x",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1)
                    )
                    Slider(
                        value = overallScale,
                        onValueChange = onOverallScaleChange,
                        valueRange = 0.7f..1.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF38BDF8),
                            inactiveTrackColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("overall_scale_slider")
                    )
                }
            }

            // Action Buttons (Reset, Cancel, Save)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset
                OutlinedButton(
                    onClick = onReset,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("arrange_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Layout",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Cancel
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GamepadRed
                    ),
                    modifier = Modifier.testTag("arrange_cancel_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Arrange",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Save
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamepadGreen,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.testTag("arrange_save_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save Layout",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
