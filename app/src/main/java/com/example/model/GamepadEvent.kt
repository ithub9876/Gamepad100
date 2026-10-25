package com.example.model

import java.util.Locale

sealed interface GamepadEvent {
    fun toJson(): String

    data class ButtonEvent(
        val button: String,
        val pressed: Boolean
    ) : GamepadEvent {
        override fun toJson(): String {
            return """{"type":"button","button":"$button","pressed":$pressed}"""
        }
    }

    data class StickEvent(
        val stick: String, // "left" or "right"
        val x: Float,
        val y: Float
    ) : GamepadEvent {
        override fun toJson(): String {
            val clampedX = String.format(Locale.US, "%.3f", x.coerceIn(-1.0f, 1.0f))
            val clampedY = String.format(Locale.US, "%.3f", y.coerceIn(-1.0f, 1.0f))
            return """{"type":"stick","stick":"$stick","x":$clampedX,"y":$clampedY}"""
        }
    }

    data class TriggerEvent(
        val trigger: String, // "LT" / "RT" or "L2" / "R2"
        val value: Float
    ) : GamepadEvent {
        override fun toJson(): String {
            val clampedVal = String.format(Locale.US, "%.3f", value.coerceIn(0.0f, 1.0f))
            return """{"type":"trigger","trigger":"$trigger","value":$clampedVal}"""
        }
    }
}
