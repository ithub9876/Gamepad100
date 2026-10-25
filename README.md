# Android Gamepad for Windows PC

A high-performance, customizable Android touch gamepad that connects over local Wi-Fi to control games on a Windows PC. Built with Kotlin and Jetpack Compose.

---

## Features

- **Tactile Landscape Gamepad Layout**:
  - **Left Side**: 4-way D-Pad (Up, Down, Left, Right) and Left Analog Thumbstick with clickable L3.
  - **Right Side**: Xbox standard face buttons (Y, X, B, A) and Right Analog Thumbstick with clickable R3.
  - **Shoulder Triggers**: L1 bumper & L2 analog trigger slider on top-left; R1 bumper & R2 analog trigger slider on top-right.
  - **System Controls**: Create (Share/Select), Home (Guide), and Options (Start/Menu) centered near the top.
  - **Status Pill & Lock Control**: Centered status display showing connection state and IP, with a layout lock toggle.
  - **Telemetry HUD**: Real-time latency tracking (ms), packet count, and Wi-Fi link status.
- **Arrange Mode (Custom Layout)**:
  - Drag controls anywhere on screen with automatic edge boundary clamping.
  - Scale individual controls (`0.6x` to `1.8x`) or adjust global layout size (`0.7x` to `1.4x`).
  - **Save**, **Cancel**, and **Reset to Default** actions.
  - Layout positions and sizes persist locally across app reboots.
  - Lock control prevents accidental repositioning during active gameplay.
- **Low-Latency Wi-Fi WebSocket Streaming**:
  - Direct connection to Windows companion server with automatic reconnect and keep-alive pings.
  - Automatic safety reset: all pressed buttons are released and sticks zeroed on disconnect or when the app loses focus.
- **Haptic Tactile Feedback**:
  - Crisp vibrations for button presses, thumbstick clicks, and trigger pulls (toggleable in Setup).

---

## WebSocket JSON Protocol

The Android app streams compact JSON messages over a WebSocket connection to `ws://<PC_IP>:<PORT>`:

### 1. Button Event
Dispatched immediately on press (`true`) and release (`false`):
```json
{"type":"button","button":"A","pressed":true}
```
**Supported Buttons**: `A`, `B`, `X`, `Y`, `DPAD_UP`, `DPAD_DOWN`, `DPAD_LEFT`, `DPAD_RIGHT`, `L1`, `R1`, `L3`, `R3`, `CREATE`, `OPTIONS`, `HOME`.

### 2. Analog Thumbstick Event
Dispatched continuously while an analog stick is deflected:
```json
{"type":"stick","stick":"left","x":-0.250,"y":0.800}
```
- `stick`: `"left"` or `"right"`
- `x`: Horizontal axis from `-1.000` (full left) to `1.000` (full right)
- `y`: Vertical axis from `-1.000` (full up) to `1.000` (full down)

### 3. Analog Trigger Event
Dispatched on trigger squeeze:
```json
{"type":"trigger","trigger":"LT","value":0.600}
```
- `trigger`: `"LT"` (or `L2`) and `"RT"` (or `R2`)
- `value`: Normalized pressure from `0.000` to `1.000`

### 4. Latency Ping / Pong
The Android app periodically sends:
```json
{"type":"ping","timestamp":1730000000}
```
The server replies:
```json
{"type":"pong","timestamp":1730000000}
```

---

## Windows Companion Server Setup

The Android app sends control events to a companion server running on your Windows PC. A complete, ready-to-run Python server is included in `companion_server.py`.

### Prerequisites on Windows
1. Install Python 3.8+: [python.org](https://www.python.org/)
2. Install required WebSocket library:
   ```bash
   pip install websockets
   ```
3. *(Recommended for native game compatibility)* Install `vgamepad` and the ViGEmBus virtual controller driver:
   - Install `vgamepad`:
     ```bash
     pip install vgamepad
     ```
   - Download and run the ViGEmBus installer: [ViGEmBus Releases](https://github.com/nefarius/ViGEmBus/releases)

### Running the Server
On your Windows PC, open Command Prompt or PowerShell in the repository root and run:
```bash
python companion_server.py
```

The script will display your local IP address:
```
================================================================
   🎮 ANDROID GAMEPAD PC COMPANION SERVER 🎮
================================================================
  Status: Listening on all interfaces (port 8080)
  Enter this IP in your Android Gamepad Setup screen:
  --> IP:   192.168.1.100
  --> Port: 8080
================================================================
```

---

## Connecting from Android

1. Ensure your Android phone and Windows PC are connected to the **same Wi-Fi network**.
2. Open the **Gamepad PC** app on your phone.
3. Tap the **SETUP** button (or tap the center **DISCONNECTED** pill).
4. Enter your PC's IP address and Port (default `8080`).
5. Tap **CONNECT TO PC**.
6. The status pill will turn **green**, and you are ready to play!

---

## Android Build Instructions

To build the APK locally or via Gradle:
```bash
gradle assembleDebug
```
The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
