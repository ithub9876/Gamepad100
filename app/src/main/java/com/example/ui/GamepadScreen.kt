package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.ControlElement
import com.example.model.GamepadLayout
import com.example.ui.components.ActionButtonsCluster
import com.example.ui.components.ArrangeGridBackground
import com.example.ui.components.ArrangeTopBar
import com.example.ui.components.BottomControls
import com.example.ui.components.CenterStatusAndLock
import com.example.ui.components.CenterSystemButtons
import com.example.ui.components.DPadControl
import com.example.ui.components.SetupDialog
import com.example.ui.components.ThumbStickControl
import com.example.ui.components.TopCenterHUD
import com.example.ui.components.TriggersCluster
import com.example.ui.theme.GamepadBackground
import com.example.ui.theme.GamepadCyan
import com.example.viewmodel.GamepadViewModel
import kotlin.math.roundToInt

@Composable
fun GamepadScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val layout by viewModel.layout.collectAsState()
    val isArrangeMode by viewModel.isArrangeMode.collectAsState()
    val selectedElementId by viewModel.selectedElementId.collectAsState()
    val pressedButtons by viewModel.pressedButtons.collectAsState()
    val leftTrigger by viewModel.leftTrigger.collectAsState()
    val rightTrigger by viewModel.rightTrigger.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val showSetupDialog by viewModel.showSetupDialog.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val serverIp by viewModel.serverIp.collectAsState()
    val serverPort by viewModel.serverPort.collectAsState()
    val recentIps by viewModel.recentIps.collectAsState()
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState()

    val selectedElement = selectedElementId?.let { layout.getElement(it) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GamepadBackground)
            .testTag("gamepad_main_screen")
    ) {
        // Arrange mode grid background
        if (isArrangeMode) {
            ArrangeGridBackground()
        }

        // Top Status HUD (always centered at the top)
        TopCenterHUD(
            connectionState = connectionState,
            stats = stats,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
        )

        // Main Responsive Gamepad Canvas
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            val density = LocalDensity.current

            // 1. L1 & L2 Triggers (Top Left)
            ArrangedElementWrapper(
                element = layout.getElement(GamepadLayout.ID_L1L2),
                overallScale = layout.overallScale,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isArrangeMode = isArrangeMode,
                isSelected = selectedElementId == GamepadLayout.ID_L1L2,
                onSelect = { viewModel.selectElement(GamepadLayout.ID_L1L2) },
                onPositionChange = { x, y -> viewModel.updateElementPosition(GamepadLayout.ID_L1L2, x, y) }
            ) {
                TriggersCluster(
                    isLeft = true,
                    triggerValue = leftTrigger,
                    isBumperPressed = pressedButtons.contains("L1"),
                    onBumperDown = { viewModel.onButtonDown(it) },
                    onBumperUp = { viewModel.onButtonUp(it) },
                    onTriggerChange = { isLeft, val_ -> viewModel.onTriggerChange(isLeft, val_) }
                )
            }

            // 2. R1 & R2 Triggers (Top Right)
            ArrangedElementWrapper(
                element = layout.getElement(GamepadLayout.ID_R1R2),
                overallScale = layout.overallScale,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isArrangeMode = isArrangeMode,
                isSelected = selectedElementId == GamepadLayout.ID_R1R2,
                onSelect = { viewModel.selectElement(GamepadLayout.ID_R1R2) },
                onPositionChange = { x, y -> viewModel.updateElementPosition(GamepadLayout.ID_R1R2, x, y) }
            ) {
                TriggersCluster(
                    isLeft = false,
                    triggerValue = rightTrigger,
                    isBumperPressed = pressedButtons.contains("R1"),
                    onBumperDown = { viewModel.onButtonDown(it) },
                    onBumperUp = { viewModel.onButtonUp(it) },
                    onTriggerChange = { isLeft, val_ -> viewModel.onTriggerChange(isLeft, val_) }
                )
            }

            // 3. D-Pad (Left side)
            ArrangedElementWrapper(
                element = layout.getElement(GamepadLayout.ID_DPAD),
                overallScale = layout.overallScale,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isArrangeMode = isArrangeMode,
                isSelected = selectedElementId == GamepadLayout.ID_DPAD,
                onSelect = { viewModel.selectElement(GamepadLayout.ID_DPAD) },
                onPositionChange = { x, y -> viewModel.updateElementPosition(GamepadLayout.ID_DPAD, x, y) }
            ) {
                DPadControl(
                    size = 140.dp,
                    pressedButtons = pressedButtons,
                    onButtonDown = { viewModel.onButtonDown(it) },
                    onButtonUp = { viewModel.onButtonUp(it) }
                )
            }

            // 4. Left Analog Thumbstick (Bottom Left)
            ArrangedElementWrapper(
                element = layout.getElement(GamepadLayout.ID_LEFT_STICK),
                overallScale = layout.overallScale,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isArrangeMode = isArrangeMode,
                isSelected = selectedElementId == GamepadLayout.ID_LEFT_STICK,
                onSelect = { viewModel.selectElement(GamepadLayout.ID_LEFT_STICK) },
                onPositionChange = { x, y -> viewModel.updateElementPosition(GamepadLayout.ID_LEFT_STICK, x, y) }
            ) {
                ThumbStickControl(
                    size = 136.dp,
                    label = "L3",
                    isPressed = pressedButtons.contains("L3"),
                    onMove = { x, y -> viewModel.onStickMove(true, x, y) },
                    onClick = { viewModel.onStickClick(true) }
                )
            }

            // 5. Action Buttons ABXY (Right side)
            ArrangedElementWrapper(
                element = layout.getElement(GamepadLayout.ID_ACTION_BUTTONS),
                overallScale = layout.overallScale,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isArrangeMode = isArrangeMode,
                isSelected = selectedElementId == GamepadLayout.ID_ACTION_BUTTONS,
                onSelect = { viewModel.selectElement(GamepadLayout.ID_ACTION_BUTTONS) },
                onPositionChange = { x, y -> viewModel.updateElementPosition(GamepadLayout.ID_ACTION_BUTTONS, x, y) }
            ) {
                ActionButtonsCluster(
                    size = 140.dp,
                    pressedButtons = pressedButtons,
                    onButtonDown = { viewModel.onButtonDown(it) },
                    onButtonUp = { viewModel.onButtonUp(it) }
                )
            }

            // 6. Right Analog Thumbstick (Bottom Right)
            ArrangedElementWrapper(
                element = layout.getElement(GamepadLayout.ID_RIGHT_STICK),
                overallScale = layout.overallScale,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isArrangeMode = isArrangeMode,
                isSelected = selectedElementId == GamepadLayout.ID_RIGHT_STICK,
                onSelect = { viewModel.selectElement(GamepadLayout.ID_RIGHT_STICK) },
                onPositionChange = { x, y -> viewModel.updateElementPosition(GamepadLayout.ID_RIGHT_STICK, x, y) }
            ) {
                ThumbStickControl(
                    size = 136.dp,
                    label = "R3",
                    isPressed = pressedButtons.contains("R3"),
                    onMove = { x, y -> viewModel.onStickMove(false, x, y) },
                    onClick = { viewModel.onStickClick(false) }
                )
            }

            // 7. Center System Buttons: Create, Home, Options
            ArrangedElementWrapper(
                element = layout.getElement(GamepadLayout.ID_CENTER_BUTTONS),
                overallScale = layout.overallScale,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isArrangeMode = isArrangeMode,
                isSelected = selectedElementId == GamepadLayout.ID_CENTER_BUTTONS,
                onSelect = { viewModel.selectElement(GamepadLayout.ID_CENTER_BUTTONS) },
                onPositionChange = { x, y -> viewModel.updateElementPosition(GamepadLayout.ID_CENTER_BUTTONS, x, y) }
            ) {
                CenterSystemButtons(
                    pressedButtons = pressedButtons,
                    onButtonDown = { viewModel.onButtonDown(it) },
                    onButtonUp = { viewModel.onButtonUp(it) }
                )
            }

            // 8. Center Status Pill & Lock Control
            ArrangedElementWrapper(
                element = layout.getElement(GamepadLayout.ID_STATUS_PILL),
                overallScale = layout.overallScale,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isArrangeMode = isArrangeMode,
                isSelected = selectedElementId == GamepadLayout.ID_STATUS_PILL,
                onSelect = { viewModel.selectElement(GamepadLayout.ID_STATUS_PILL) },
                onPositionChange = { x, y -> viewModel.updateElementPosition(GamepadLayout.ID_STATUS_PILL, x, y) }
            ) {
                CenterStatusAndLock(
                    connectionState = connectionState,
                    isLocked = isLocked,
                    onStatusClick = { viewModel.openSetupDialog() },
                    onLockToggle = { viewModel.toggleLock() }
                )
            }

            // 9. Bottom Controls: Arrange & Setup
            ArrangedElementWrapper(
                element = layout.getElement(GamepadLayout.ID_BOTTOM_CONTROLS),
                overallScale = layout.overallScale,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                isArrangeMode = isArrangeMode,
                isSelected = selectedElementId == GamepadLayout.ID_BOTTOM_CONTROLS,
                onSelect = { viewModel.selectElement(GamepadLayout.ID_BOTTOM_CONTROLS) },
                onPositionChange = { x, y -> viewModel.updateElementPosition(GamepadLayout.ID_BOTTOM_CONTROLS, x, y) }
            ) {
                BottomControls(
                    isArrangeEnabled = isArrangeMode,
                    onArrangeClick = {
                        if (isArrangeMode) viewModel.saveArrangeChanges()
                        else viewModel.enterArrangeMode()
                    },
                    onSetupClick = { viewModel.openSetupDialog() }
                )
            }
        }

        // Arrange Top Bar (Overlay when active)
        if (isArrangeMode) {
            ArrangeTopBar(
                selectedElement = selectedElement,
                overallScale = layout.overallScale,
                onElementScaleChange = { selectedElement?.let { elem -> viewModel.updateElementScale(elem.id, it) } },
                onOverallScaleChange = { viewModel.updateOverallScale(it) },
                onSave = { viewModel.saveArrangeChanges() },
                onCancel = { viewModel.cancelArrangeChanges() },
                onReset = { viewModel.resetToDefaultLayout() },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // Setup Connection Dialog
        if (showSetupDialog) {
            SetupDialog(
                currentIp = serverIp,
                currentPort = serverPort,
                recentIps = recentIps,
                connectionState = connectionState,
                isHapticEnabled = isHapticEnabled,
                onIpChange = { viewModel.updateServerIp(it) },
                onPortChange = { viewModel.updateServerPort(it) },
                onConnect = { viewModel.connectToServer() },
                onDisconnect = { viewModel.disconnectFromServer() },
                onToggleHaptic = { viewModel.toggleHaptic() },
                onDismiss = { viewModel.closeSetupDialog() }
            )
        }
    }
}

@Composable
private fun ArrangedElementWrapper(
    element: ControlElement,
    overallScale: Float,
    screenWidth: Dp,
    screenHeight: Dp,
    isArrangeMode: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPositionChange: (Float, Float) -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val combinedScale = element.scale * overallScale

    val screenWidthPx = with(density) { screenWidth.toPx() }
    val screenHeightPx = with(density) { screenHeight.toPx() }

    var itemModifier = Modifier
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val posX = (screenWidthPx * element.xPercent) - (placeable.width / 2f)
            val posY = (screenHeightPx * element.yPercent) - (placeable.height / 2f)
            layout(placeable.width, placeable.height) {
                placeable.placeRelative(posX.roundToInt(), posY.roundToInt())
            }
        }
        .scale(combinedScale)

    if (isArrangeMode) {
        itemModifier = itemModifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) GamepadCyan else Color(0x6638BDF8),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .pointerInput(element.id) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = element.xPercent + (dragAmount.x / screenWidthPx)
                        val newY = element.yPercent + (dragAmount.y / screenHeightPx)
                        onPositionChange(newX, newY)
                    }
                )
            }
    }

    Box(
        modifier = itemModifier,
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
