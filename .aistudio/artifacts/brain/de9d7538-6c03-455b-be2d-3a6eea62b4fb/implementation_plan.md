# Android Gamepad Controller for Windows PC

A responsive, customizable Android gamepad that connects over Wi-Fi via WebSockets to control games on a Windows PC. Features a landscape dark-mode UI with customizable button layouts (Arrange Mode), analog dual-stick simulation, trigger controls, haptic feedback, and a companion Windows Python server script.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification:
> - **Face Buttons**: Xbox standard configuration (Y on top, X on left, B on right, A on bottom).
> - **Tactile Feedback**: Subtle haptic vibration on button presses and thumbstick clicks.
> - **Windows Companion**: A ready-to-run Python WebSocket server (`companion_server.py`) with ViGEmBus Xbox 360 virtual controller emulation and fallback keyboard mapping will be bundled with complete documentation.

---

## 1. Overview & Core Concept

- **What It Does**: Turns any Android smartphone into an ultra-low-latency Wi-Fi gamepad for PC gaming. The app renders a landscape gamepad with analog sticks, D-pad, face buttons (A/B/X/Y), shoulder triggers (L1/L2/R1/R2), and system buttons (Create/Home/Options).
- **Target Audience**: PC gamers who want a wireless controller without buying extra hardware, or players who want customizable on-screen touch controls tailored to their hand size.
- **Key Value**: Low-latency WebSocket input streaming, fully editable layout positioning and sizing (Arrange Mode), automatic state reset on disconnect, and instant plug-and-play with Windows games.

---

## 2. User Experience & Visual Design

### Gamepad UI Layout (Landscape Full-Screen)
- **Aesthetic Direction**: Sleek, tactile, hardware-grade controller interface. Deep matte black background (`#0D0F12`), dark graphite button surfaces (`#1A1D24` to `#242832`), subtle 1dp border outlines (`#2E3440`), and cool blue-gray typography and icons (`#8892B0` / `#94A3B8`). Active pressed states illuminate with cyan/blue glow accents (`#38BDF8`).
- **Control Layout Composition**:
  - **Top-Left**: L2 (trigger slider / button) and L1 (bumper).
  - **Top-Right**: R2 (trigger slider / button) and R1 (bumper).
  - **Left Section**: 4-way cross D-Pad (Up, Down, Left, Right) with distinct directional facets; Left Analog Thumbstick below with interactive floating thumbcap and clickable L3 stick button.
  - **Right Section**: Diamond ABXY button cluster (Y top, X left, B right, A bottom) with bold Xbox-colored letter accents; Right Analog Thumbstick below with R3 stick button.
  - **Center Top**: Create (Share/Select), Home (Logo/Guide), Options (Start/Menu).
  - **Center Core**: Connection status pill (showing connection state and host address), with a Quick-Lock toggle below it to prevent accidental control modifications.
  - **Center Bottom**: **Arrange** button (enters customizable layout editor) and **Setup** button (opens IP/port connection dialog & settings).
  - **Top-Center HUD**: Small telemetry status display showing ping latency (ms), sent packet count, and Wi-Fi link status.

### Arrange Mode (Layout Customization)
- **Visual State**: A subtle dashed grid appears over the canvas, and draggable bounding handles encircle each control group.
- **Interactions**:
  - Drag to reposition any control group across the screen.
  - Pinch or use the on-screen slider to scale individual control clusters or resize the entire layout uniformly.
  - Screen boundary clamping keeps all controls fully on-screen.
  - Top action bar provides **Save Layout**, **Cancel Changes**, and **Reset to Default**.
  - Layout coordinates and scales are persisted locally in Android SharedPreferences / JSON storage and automatically reloaded on next launch.

### Setup & Connection Dialog
- Modal dialog / bottom sheet allowing entry of Windows PC IP address (with quick-select for recent IPs) and port (default `8080`).
- Direct "Connect" / "Disconnect" toggles with live status feedback (Connecting, Connected, Disconnected, Connection Error).
- Haptics intensity toggle and vibration switch.

---

## 3. Key Product Decisions & Trade-Offs

- **Networking: WebSockets over Wi-Fi (OkHttp)**:
  - *Chosen Approach*: Lightweight WebSocket stream over local Wi-Fi sending compact JSON payloads.
  - *Why*: Native bi-directional communication, instant reconnection detection, low round-trip latency (<5ms on local Wi-Fi), and simple integration with Python, Node.js, or C# on Windows.
  - *Alternatives Considered*: Bluetooth HID (requires rooting or complex Bluetooth HID Device profile APIs not supported on all Android versions) vs. Raw UDP (packet loss and no native keep-alive handshake).
- **Local Layout Persistence (JSON in SharedPreferences)**:
  - *Chosen Approach*: Serialize custom control positions and scales as a structured JSON model in private preferences.
  - *Why*: Fast, zero database overhead, survives app reboots, easily reset to factory defaults.
- **Clean Disconnect Safety**:
  - *Chosen Approach*: Whenever the app enters background, disconnects, or exits the gamepad screen, all pressed buttons are released and analog sticks are sent neutral `(0.0, 0.0)` events to prevent character walk/spin glitches in games.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Android Gamepad UI                            │
│  ┌───────────────────────┐  ┌──────────────┐  ┌─────────────────────┐  │
│  │ Left: L1/L2, D-Pad,   │  │ Center HUD & │  │ Right: R1/R2, ABXY, │  │
│  │ Left Thumbstick (L3)  │  │ Status Pill  │  │ Right Stick (R3)    │  │
│  └───────────┬───────────┘  └──────┬───────┘  └──────────┬──────────┘  │
│              │                     │                     │             │
│              ▼                     ▼                     ▼             │
│        GamepadTouchEngine (Pointer Input & Multi-Touch Dispatch)       │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
                                     ▼
        ┌─────────────────────────────────────────────────────────┐
        │                   GamepadViewModel                      │
        │ - Controller State (Buttons, Sticks, Triggers)          │
        │ - Arrange Mode State & Dynamic Coordinate System        │
        │ - Haptic Feedback Controller (Vibrator)                 │
        │ - LayoutStorageRepository (Save / Restore / Reset)      │
        └────────────────────────────┬────────────────────────────┘
                                     │
                                     ▼
        ┌─────────────────────────────────────────────────────────┐
        │             WebSocketClient (OkHttp Client)             │
        │ - Connect / Reconnect / Disconnect                      │
        │ - Queued JSON Event Stream                              │
        └────────────────────────────┬────────────────────────────┘
                                     │ Wi-Fi WebSocket (port 8080)
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     Windows Companion Application                      │
│                      (python companion_server.py)                      │
│  - Receives JSON: {"type":"button", "button":"A", "pressed":true}       │
│  - Feeds virtual Xbox 360 controller via ViGEmClient or keyboard/mouse │
└────────────────────────────────────────────────────────────────────────┘
```

### JSON Event Protocol Specifications
1. **Button Event**:
   ```json
   {"type":"button","button":"A","pressed":true}
   ```
   Supported buttons: `A`, `B`, `X`, `Y`, `DPAD_UP`, `DPAD_DOWN`, `DPAD_LEFT`, `DPAD_RIGHT`, `L1`, `R1`, `L3`, `R3`, `CREATE`, `OPTIONS`, `HOME`.
2. **Thumbstick Event**:
   ```json
   {"type":"stick","stick":"left","x":-0.25,"y":0.80}
   ```
   Normalized coordinates: `-1.0` to `1.0` (with deadzone smoothing).
3. **Trigger Event**:
   ```json
   {"type":"trigger","trigger":"LT","value":0.85}
   ```
   Normalized trigger pressure: `0.0` to `1.0`.

---

## 5. Staged Implementation Steps

### STAGE 1: Visual Design & Arrange Mode
1. Configure Android project metadata, lock orientation to landscape, and enable edge-to-edge layout.
2. Build all individual gamepad UI components:
   - D-Pad 4-way cross control with directional tactile feedback.
   - Dual Analog Thumbsticks with dynamic drag offsets, deadzone logic, and center snapback.
   - Face buttons (ABXY) in diamond Xbox layout.
   - Bumpers & Triggers (L1/L2, R1/R2).
   - System buttons (Create, Home, Options).
   - Status pill, HUD telemetry, Arrange & Setup controls.
3. Build the Arrange Mode engine:
   - Draggable control containers with position constraints.
   - Scaling controls for each element and global layout scale.
   - Save, Cancel, and Reset to Default functionality backed by local storage.
4. Test and verify UI fidelity in landscape mode.

### STAGE 2: Networking & Controller Input Engine
1. Implement `WebSocketClient` with OkHttp for reliable connection handling, pings, and error reporting.
2. Hook multi-touch pointer events to input dispatcher emitting formatted JSON messages.
3. Add safety neutralizer on pause/disconnect.
4. Add tactile haptic feedback using Android `Vibrator` / `VibratorManager`.
5. Create `companion_server.py` Windows script with ViGEmBus and keyboard fallbacks.
6. Write a comprehensive `README.md` with instructions, connection troubleshooting, and protocol specs.
7. Run `compile_applet` and verify zero errors.
