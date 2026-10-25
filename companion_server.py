#!/usr/bin/env python3
"""
Windows Companion WebSocket Server for Android Gamepad Controller
-----------------------------------------------------------------
Receives low-latency controller inputs from the Android Gamepad app over Wi-Fi
and feeds them to a virtual Xbox 360 controller on Windows using ViGEmBus.

Requirements:
    pip install websockets

Optional (for native virtual Xbox 360 controller emulation in Windows games):
    pip install vgamepad
    (ViGEmBus driver: https://github.com/nefarius/ViGEmBus/releases)

Usage:
    python companion_server.py
    python companion_server.py --port 8080 --host 0.0.0.0
"""

import argparse
import asyncio
import json
import socket
import sys

# Try importing vgamepad for native virtual Xbox 360 controller emulation
try:
    import vgamepad as vg
    HAVE_VGAMEPAD = True
    gamepad = vg.VX360Gamepad()
    print("[INFO] ViGEmBus Virtual Xbox 360 controller initialized successfully!")
except ImportError:
    HAVE_VGAMEPAD = False
    gamepad = None
    print("[WARN] 'vgamepad' not found. Running in simulation/logging mode.")
    print("       To enable virtual controller for PC games, run: pip install vgamepad")

# Mapping of Android button names to vgamepad buttons
BUTTON_MAP = {}
if HAVE_VGAMEPAD:
    BUTTON_MAP = {
        "A": vg.XUSB_BUTTON.XUSB_GAMEPAD_A,
        "B": vg.XUSB_BUTTON.XUSB_GAMEPAD_B,
        "X": vg.XUSB_BUTTON.XUSB_GAMEPAD_X,
        "Y": vg.XUSB_BUTTON.XUSB_GAMEPAD_Y,
        "DPAD_UP": vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_UP,
        "DPAD_DOWN": vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_DOWN,
        "DPAD_LEFT": vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_LEFT,
        "DPAD_RIGHT": vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_RIGHT,
        "L1": vg.XUSB_BUTTON.XUSB_GAMEPAD_LEFT_SHOULDER,
        "R1": vg.XUSB_BUTTON.XUSB_GAMEPAD_RIGHT_SHOULDER,
        "L3": vg.XUSB_BUTTON.XUSB_GAMEPAD_LEFT_THUMB,
        "R3": vg.XUSB_BUTTON.XUSB_GAMEPAD_RIGHT_THUMB,
        "CREATE": vg.XUSB_BUTTON.XUSB_GAMEPAD_BACK,
        "OPTIONS": vg.XUSB_BUTTON.XUSB_GAMEPAD_START,
        "HOME": vg.XUSB_BUTTON.XUSB_GAMEPAD_GUIDE,
    }


def get_local_ip():
    """Finds the local IP address of this machine."""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"


def reset_controller():
    """Resets all virtual buttons and sticks to neutral."""
    if not HAVE_VGAMEPAD or gamepad is None:
        return
    try:
        gamepad.reset()
        gamepad.update()
    except Exception as e:
        print(f"[ERROR] Failed to reset controller: {e}")


async def handle_gamepad_client(websocket):
    client_ip = websocket.remote_address[0]
    print(f"\n[CONNECTED] Android Gamepad connected from {client_ip}")

    # Safety reset on connect
    reset_controller()

    try:
        async for raw_message in websocket:
            try:
                msg = json.loads(raw_message)
                msg_type = msg.get("type")

                if msg_type == "button":
                    btn = msg.get("button", "").upper()
                    pressed = bool(msg.get("pressed", False))

                    if HAVE_VGAMEPAD and gamepad and btn in BUTTON_MAP:
                        vg_btn = BUTTON_MAP[btn]
                        if pressed:
                            gamepad.press_button(button=vg_btn)
                        else:
                            gamepad.release_button(button=vg_btn)
                        gamepad.update()
                    else:
                        print(f"  [BTN] {btn} = {'PRESSED' if pressed else 'RELEASED'}")

                elif msg_type == "stick":
                    stick = msg.get("stick", "left").lower()
                    x = float(msg.get("x", 0.0))
                    # Inverted Y for gamepad stick standards if needed
                    y = float(msg.get("y", 0.0))

                    if HAVE_VGAMEPAD and gamepad:
                        if stick == "left":
                            gamepad.left_joystick_float(x_value_float=x, y_value_float=-y)
                        else:
                            gamepad.right_joystick_float(x_value_float=x, y_value_float=-y)
                        gamepad.update()
                    else:
                        print(f"  [STICK] {stick.upper()}: x={x:.2f}, y={y:.2f}")

                elif msg_type == "trigger":
                    trigger = msg.get("trigger", "").upper()
                    val = float(msg.get("value", 0.0))

                    if HAVE_VGAMEPAD and gamepad:
                        if trigger in ("LT", "L2"):
                            gamepad.left_trigger_float(value_float=val)
                        elif trigger in ("RT", "R2"):
                            gamepad.right_trigger_float(value_float=val)
                        gamepad.update()
                    else:
                        print(f"  [TRIGGER] {trigger}: {val:.2f}")

                elif msg_type == "ping":
                    # Respond with pong for latency tracking
                    pong = json.dumps({"type": "pong", "timestamp": msg.get("timestamp")})
                    await websocket.send(pong)

            except json.JSONDecodeError:
                pass
            except Exception as e:
                print(f"[ERROR] Event handling error: {e}")

    except Exception as e:
        print(f"[INFO] Client session ended: {e}")
    finally:
        print(f"[DISCONNECTED] Android Gamepad disconnected ({client_ip})")
        reset_controller()


async def main():
    parser = argparse.ArgumentParser(description="Android Gamepad PC Companion Server")
    parser.add_argument("--host", default="0.0.0.0", help="Host interface to bind (default: 0.0.0.0)")
    parser.add_argument("--port", type=int, default=8080, help="Port to listen on (default: 8080)")
    args = parser.parse_args()

    try:
        import websockets
    except ImportError:
        print("[FATAL] 'websockets' library is required.")
        print("Please run: pip install websockets")
        sys.exit(1)

    local_ip = get_local_ip()
    print("=" * 64)
    print("   🎮 ANDROID GAMEPAD PC COMPANION SERVER 🎮   ")
    print("=" * 64)
    print(f"  Status: Listening on all interfaces (port {args.port})")
    print(f"  Enter this IP in your Android Gamepad Setup screen:")
    print(f"  --> IP:   {local_ip}")
    print(f"  --> Port: {args.port}")
    print("=" * 64)

    async with websockets.serve(handle_gamepad_client, args.host, args.port):
        await asyncio.Future()  # run forever


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n[INFO] Server stopped by user.")
        reset_controller()
