package com.example.model

import org.json.JSONArray
import org.json.JSONObject

data class ControlElement(
    val id: String,
    val name: String,
    val xPercent: Float,
    val yPercent: Float,
    val scale: Float = 1.0f
)

data class GamepadLayout(
    val overallScale: Float = 1.0f,
    val elements: Map<String, ControlElement> = emptyMap()
) {
    fun getElement(id: String): ControlElement {
        return elements[id] ?: defaultLayout().elements[id] ?: ControlElement(id, id, 0.5f, 0.5f, 1.0f)
    }

    fun withElement(element: ControlElement): GamepadLayout {
        return copy(elements = elements + (element.id to element))
    }

    fun toJson(): String {
        val root = JSONObject()
        root.put("overallScale", overallScale.toDouble())
        val array = JSONArray()
        for ((_, elem) in elements) {
            val obj = JSONObject()
            obj.put("id", elem.id)
            obj.put("name", elem.name)
            obj.put("xPercent", elem.xPercent.toDouble())
            obj.put("yPercent", elem.yPercent.toDouble())
            obj.put("scale", elem.scale.toDouble())
            array.put(obj)
        }
        root.put("elements", array)
        return root.toString()
    }

    companion object {
        const val ID_L1L2 = "l1l2"
        const val ID_R1R2 = "r1r2"
        const val ID_DPAD = "dpad"
        const val ID_LEFT_STICK = "left_stick"
        const val ID_ACTION_BUTTONS = "action_buttons"
        const val ID_RIGHT_STICK = "right_stick"
        const val ID_CENTER_BUTTONS = "center_buttons"
        const val ID_STATUS_PILL = "status_pill"
        const val ID_BOTTOM_CONTROLS = "bottom_controls"

        fun defaultLayout(): GamepadLayout {
            val list = listOf(
                ControlElement(ID_L1L2, "L1 / L2 Triggers", 0.12f, 0.13f, 1.0f),
                ControlElement(ID_R1R2, "R1 / R2 Triggers", 0.88f, 0.13f, 1.0f),
                ControlElement(ID_DPAD, "D-Pad", 0.15f, 0.43f, 1.0f),
                ControlElement(ID_LEFT_STICK, "Left Analog Stick", 0.22f, 0.77f, 1.0f),
                ControlElement(ID_ACTION_BUTTONS, "ABXY Action Buttons", 0.85f, 0.43f, 1.0f),
                ControlElement(ID_RIGHT_STICK, "Right Analog Stick", 0.78f, 0.77f, 1.0f),
                ControlElement(ID_CENTER_BUTTONS, "Create / Home / Options", 0.50f, 0.18f, 1.0f),
                ControlElement(ID_STATUS_PILL, "Status Pill & Lock", 0.50f, 0.44f, 1.0f),
                ControlElement(ID_BOTTOM_CONTROLS, "Arrange & Setup", 0.50f, 0.84f, 1.0f)
            )
            return GamepadLayout(
                overallScale = 1.0f,
                elements = list.associateBy { it.id }
            )
        }

        fun fromJson(jsonStr: String?): GamepadLayout {
            if (jsonStr.isNullOrEmpty()) return defaultLayout()
            return try {
                val root = JSONObject(jsonStr)
                val overallScale = root.optDouble("overallScale", 1.0).toFloat().coerceIn(0.7f, 1.4f)
                val array = root.optJSONArray("elements") ?: return defaultLayout()
                val map = mutableMapOf<String, ControlElement>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getString("id")
                    val name = obj.optString("name", id)
                    val x = obj.optDouble("xPercent", 0.5).toFloat().coerceIn(0.04f, 0.96f)
                    val y = obj.optDouble("yPercent", 0.5).toFloat().coerceIn(0.04f, 0.96f)
                    val scale = obj.optDouble("scale", 1.0).toFloat().coerceIn(0.6f, 1.8f)
                    map[id] = ControlElement(id, name, x, y, scale)
                }
                GamepadLayout(overallScale, map)
            } catch (e: Exception) {
                defaultLayout()
            }
        }
    }
}
