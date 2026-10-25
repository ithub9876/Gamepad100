package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.GamepadLayout

class LayoutPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("gamepad_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAYOUT_JSON = "key_layout_json"
        private const val KEY_SERVER_IP = "key_server_ip"
        private const val KEY_SERVER_PORT = "key_server_port"
        private const val KEY_HAPTIC_ENABLED = "key_haptic_enabled"
        private const val KEY_RECENT_IPS = "key_recent_ips"
    }

    fun getSavedLayout(): GamepadLayout {
        val json = prefs.getString(KEY_LAYOUT_JSON, null)
        return GamepadLayout.fromJson(json)
    }

    fun saveLayout(layout: GamepadLayout) {
        prefs.edit().putString(KEY_LAYOUT_JSON, layout.toJson()).apply()
    }

    fun resetLayout(): GamepadLayout {
        val default = GamepadLayout.defaultLayout()
        prefs.edit().putString(KEY_LAYOUT_JSON, default.toJson()).apply()
        return default
    }

    var serverIp: String
        get() = prefs.getString(KEY_SERVER_IP, "192.168.1.100") ?: "192.168.1.100"
        set(value) = prefs.edit().putString(KEY_SERVER_IP, value).apply()

    var serverPort: Int
        get() = prefs.getInt(KEY_SERVER_PORT, 8080)
        set(value) = prefs.edit().putInt(KEY_SERVER_PORT, value).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    fun getRecentIps(): List<String> {
        val raw = prefs.getString(KEY_RECENT_IPS, "") ?: ""
        return if (raw.isBlank()) listOf("192.168.1.100", "192.168.0.50", "127.0.0.1")
        else raw.split(",").filter { it.isNotBlank() }
    }

    fun addRecentIp(ip: String) {
        if (ip.isBlank()) return
        val current = getRecentIps().toMutableList()
        current.remove(ip)
        current.add(0, ip)
        val trimmed = current.take(5)
        prefs.edit().putString(KEY_RECENT_IPS, trimmed.joinToString(",")).apply()
    }
}
