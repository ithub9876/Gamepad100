package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LayoutPreferences
import com.example.haptics.HapticsManager
import com.example.model.ControlElement
import com.example.model.GamepadEvent
import com.example.model.GamepadLayout
import com.example.network.ConnectionState
import com.example.network.ConnectionStats
import com.example.network.GamepadWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class GamepadViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = LayoutPreferences(application)
    val haptics = HapticsManager(application).apply {
        isEnabled = preferences.isHapticEnabled
    }
    val webSocketClient = GamepadWebSocketClient(viewModelScope)

    val connectionState: StateFlow<ConnectionState> = webSocketClient.connectionState
    val stats: StateFlow<ConnectionStats> = webSocketClient.stats

    // Pressed buttons state
    private val _pressedButtons = MutableStateFlow<Set<String>>(emptySet())
    val pressedButtons: StateFlow<Set<String>> = _pressedButtons.asStateFlow()

    // Sticks state
    private val _leftStick = MutableStateFlow(Pair(0f, 0f))
    val leftStick: StateFlow<Pair<Float, Float>> = _leftStick.asStateFlow()

    private val _rightStick = MutableStateFlow(Pair(0f, 0f))
    val rightStick: StateFlow<Pair<Float, Float>> = _rightStick.asStateFlow()

    // Triggers state
    private val _leftTrigger = MutableStateFlow(0f)
    val leftTrigger: StateFlow<Float> = _leftTrigger.asStateFlow()

    private val _rightTrigger = MutableStateFlow(0f)
    val rightTrigger: StateFlow<Float> = _rightTrigger.asStateFlow()

    // Layout configuration
    private val _layout = MutableStateFlow(preferences.getSavedLayout())
    val layout: StateFlow<GamepadLayout> = _layout.asStateFlow()

    // Arrange Mode
    private val _isArrangeMode = MutableStateFlow(false)
    val isArrangeMode: StateFlow<Boolean> = _isArrangeMode.asStateFlow()

    private val _selectedElementId = MutableStateFlow<String?>(null)
    val selectedElementId: StateFlow<String?> = _selectedElementId.asStateFlow()

    // Snapshot of layout before entering arrange mode
    private var arrangeSnapshot: GamepadLayout = _layout.value

    // Lock mode (prevents touches from accidentally opening dialogs or modifying anything)
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    // Setup dialog
    private val _showSetupDialog = MutableStateFlow(false)
    val showSetupDialog: StateFlow<Boolean> = _showSetupDialog.asStateFlow()

    private val _serverIp = MutableStateFlow(preferences.serverIp)
    val serverIp: StateFlow<String> = _serverIp.asStateFlow()

    private val _serverPort = MutableStateFlow(preferences.serverPort)
    val serverPort: StateFlow<Int> = _serverPort.asStateFlow()

    private val _recentIps = MutableStateFlow(preferences.getRecentIps())
    val recentIps: StateFlow<List<String>> = _recentIps.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(preferences.isHapticEnabled)
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    // --- CONTROLLER INPUT ACTIONS ---

    fun onButtonDown(buttonName: String) {
        if (_isArrangeMode.value) return
        val current = _pressedButtons.value
        if (!current.contains(buttonName)) {
            _pressedButtons.value = current + buttonName
            haptics.click()
            webSocketClient.sendEvent(GamepadEvent.ButtonEvent(buttonName, true))
        }
    }

    fun onButtonUp(buttonName: String) {
        val current = _pressedButtons.value
        if (current.contains(buttonName)) {
            _pressedButtons.value = current - buttonName
            webSocketClient.sendEvent(GamepadEvent.ButtonEvent(buttonName, false))
        }
    }

    fun onStickMove(isLeft: Boolean, rawX: Float, rawY: Float) {
        if (_isArrangeMode.value) return
        val deadzone = 0.08f
        val x = if (abs(rawX) < deadzone) 0f else rawX
        val y = if (abs(rawY) < deadzone) 0f else rawY

        val stickName = if (isLeft) "left" else "right"
        if (isLeft) {
            _leftStick.value = Pair(x, y)
        } else {
            _rightStick.value = Pair(x, y)
        }
        webSocketClient.sendEvent(GamepadEvent.StickEvent(stickName, x, y))
    }

    fun onStickClick(isLeft: Boolean) {
        val btn = if (isLeft) "L3" else "R3"
        haptics.heavyClick()
        val current = _pressedButtons.value
        val isPressed = !current.contains(btn)
        if (isPressed) {
            _pressedButtons.value = current + btn
            webSocketClient.sendEvent(GamepadEvent.ButtonEvent(btn, true))
        } else {
            _pressedButtons.value = current - btn
            webSocketClient.sendEvent(GamepadEvent.ButtonEvent(btn, false))
        }
    }

    fun onTriggerChange(isLeft: Boolean, value: Float) {
        if (_isArrangeMode.value) return
        val clamped = value.coerceIn(0f, 1f)
        val name = if (isLeft) "LT" else "RT"
        if (isLeft) {
            _leftTrigger.value = clamped
        } else {
            _rightTrigger.value = clamped
        }
        webSocketClient.sendEvent(GamepadEvent.TriggerEvent(name, clamped))
        if (clamped >= 0.95f) {
            haptics.tick()
        }
    }

    // --- ARRANGE MODE ACTIONS ---

    fun enterArrangeMode() {
        if (_isLocked.value) return
        arrangeSnapshot = _layout.value
        _isArrangeMode.value = true
        _selectedElementId.value = null
        haptics.heavyClick()
    }

    fun selectElement(id: String?) {
        _selectedElementId.value = id
        if (id != null) haptics.tick()
    }

    fun updateElementPosition(id: String, xPercent: Float, yPercent: Float) {
        val currentElem = _layout.value.getElement(id)
        val updated = currentElem.copy(
            xPercent = xPercent.coerceIn(0.06f, 0.94f),
            yPercent = yPercent.coerceIn(0.06f, 0.94f)
        )
        _layout.value = _layout.value.withElement(updated)
    }

    fun updateElementScale(id: String, scale: Float) {
        val currentElem = _layout.value.getElement(id)
        val updated = currentElem.copy(scale = scale.coerceIn(0.6f, 1.8f))
        _layout.value = _layout.value.withElement(updated)
    }

    fun updateOverallScale(scale: Float) {
        val clamped = scale.coerceIn(0.7f, 1.4f)
        _layout.value = _layout.value.copy(overallScale = clamped)
    }

    fun saveArrangeChanges() {
        preferences.saveLayout(_layout.value)
        _isArrangeMode.value = false
        _selectedElementId.value = null
        haptics.heavyClick()
    }

    fun cancelArrangeChanges() {
        _layout.value = arrangeSnapshot
        _isArrangeMode.value = false
        _selectedElementId.value = null
        haptics.click()
    }

    fun resetToDefaultLayout() {
        val default = preferences.resetLayout()
        _layout.value = default
        _selectedElementId.value = null
        haptics.heavyClick()
    }

    fun toggleLock() {
        val newLocked = !_isLocked.value
        _isLocked.value = newLocked
        haptics.click()
    }

    // --- SETUP / CONNECTION ACTIONS ---

    fun openSetupDialog() {
        _showSetupDialog.value = true
        haptics.click()
    }

    fun closeSetupDialog() {
        _showSetupDialog.value = false
    }

    fun updateServerIp(ip: String) {
        _serverIp.value = ip
    }

    fun updateServerPort(port: Int) {
        _serverPort.value = port
    }

    fun connectToServer() {
        val ip = _serverIp.value.trim()
        val port = _serverPort.value
        preferences.serverIp = ip
        preferences.serverPort = port
        preferences.addRecentIp(ip)
        _recentIps.value = preferences.getRecentIps()
        webSocketClient.connect(ip, port)
        haptics.heavyClick()
    }

    fun disconnectFromServer() {
        webSocketClient.disconnect()
        haptics.click()
    }

    fun toggleHaptic() {
        val newEnabled = !_isHapticEnabled.value
        _isHapticEnabled.value = newEnabled
        preferences.isHapticEnabled = newEnabled
        haptics.isEnabled = newEnabled
        if (newEnabled) haptics.heavyClick()
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient.disconnect()
    }
}
