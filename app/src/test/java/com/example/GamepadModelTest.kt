package com.example

import com.example.model.ControlElement
import com.example.model.GamepadEvent
import com.example.model.GamepadLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GamepadModelTest {

    @Test
    fun testButtonEventJson() {
        val event = GamepadEvent.ButtonEvent("A", true)
        assertEquals("""{"type":"button","button":"A","pressed":true}""", event.toJson())

        val eventRelease = GamepadEvent.ButtonEvent("B", false)
        assertEquals("""{"type":"button","button":"B","pressed":false}""", eventRelease.toJson())
    }

    @Test
    fun testStickEventJson() {
        val event = GamepadEvent.StickEvent("left", -0.25f, 0.8f)
        val json = event.toJson()
        assertTrue(json.contains(""""type":"stick""""))
        assertTrue(json.contains(""""stick":"left""""))
        assertTrue(json.contains(""""x":-0.250"""))
        assertTrue(json.contains(""""y":0.800"""))
    }

    @Test
    fun testTriggerEventJson() {
        val event = GamepadEvent.TriggerEvent("LT", 0.6f)
        val json = event.toJson()
        assertTrue(json.contains(""""type":"trigger""""))
        assertTrue(json.contains(""""trigger":"LT""""))
        assertTrue(json.contains(""""value":0.600"""))
    }

    @Test
    fun testDefaultLayoutCompleteness() {
        val layout = GamepadLayout.defaultLayout()
        assertNotNull(layout.getElement(GamepadLayout.ID_DPAD))
        assertNotNull(layout.getElement(GamepadLayout.ID_LEFT_STICK))
        assertNotNull(layout.getElement(GamepadLayout.ID_ACTION_BUTTONS))
        assertNotNull(layout.getElement(GamepadLayout.ID_RIGHT_STICK))
        assertNotNull(layout.getElement(GamepadLayout.ID_L1L2))
        assertNotNull(layout.getElement(GamepadLayout.ID_R1R2))
        assertNotNull(layout.getElement(GamepadLayout.ID_CENTER_BUTTONS))
        assertNotNull(layout.getElement(GamepadLayout.ID_STATUS_PILL))
        assertNotNull(layout.getElement(GamepadLayout.ID_BOTTOM_CONTROLS))
    }

    @Test
    fun testLayoutSerializationCycle() {
        val original = GamepadLayout.defaultLayout()
        val modified = original.withElement(
            ControlElement(GamepadLayout.ID_DPAD, "D-Pad", 0.20f, 0.50f, 1.2f)
        )
        val json = modified.toJson()
        val restored = GamepadLayout.fromJson(json)

        val restoredDpad = restored.getElement(GamepadLayout.ID_DPAD)
        assertEquals(0.20f, restoredDpad.xPercent, 0.01f)
        assertEquals(0.50f, restoredDpad.yPercent, 0.01f)
        assertEquals(1.2f, restoredDpad.scale, 0.01f)
    }
}
