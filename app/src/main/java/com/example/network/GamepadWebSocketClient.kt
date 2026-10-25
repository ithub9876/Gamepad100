package com.example.network

import android.os.SystemClock
import com.example.model.GamepadEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data class Connecting(val url: String) : ConnectionState
    data class Connected(val url: String) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

data class ConnectionStats(
    val packetsSent: Long = 0,
    val pingMs: Long = 0,
    val isLive: Boolean = false
)

class GamepadWebSocketClient(
    private val coroutineScope: CoroutineScope
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(4, TimeUnit.SECONDS)
        .pingInterval(3, TimeUnit.SECONDS)
        .build()

    private var activeSocket: WebSocket? = null
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _stats = MutableStateFlow(ConnectionStats())
    val stats: StateFlow<ConnectionStats> = _stats.asStateFlow()

    private val packetsCounter = AtomicLong(0)
    private var pingJob: Job? = null
    private var lastPingSentTime: Long = 0

    fun connect(ip: String, port: Int) {
        val cleanIp = ip.trim()
        if (cleanIp.isEmpty()) {
            _connectionState.value = ConnectionState.Error("IP address cannot be empty")
            return
        }

        disconnect()

        val url = "ws://$cleanIp:$port"
        _connectionState.value = ConnectionState.Connecting(url)

        val request = try {
            Request.Builder().url(url).build()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("Invalid URL: ${e.message}")
            return
        }

        activeSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.Connected(url)
                _stats.value = _stats.value.copy(isLive = true)
                startPingLoop()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (text.contains("pong") || text.contains("status")) {
                    val rtt = SystemClock.elapsedRealtime() - lastPingSentTime
                    if (rtt in 0..5000) {
                        _stats.value = _stats.value.copy(pingMs = rtt)
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                stopPingLoop()
                _connectionState.value = ConnectionState.Error(t.message ?: "Connection failed")
                _stats.value = _stats.value.copy(isLive = false)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                stopPingLoop()
                _connectionState.value = ConnectionState.Disconnected
                _stats.value = _stats.value.copy(isLive = false)
            }
        })
    }

    fun disconnect() {
        stopPingLoop()
        safetyReset()
        try {
            activeSocket?.close(1000, "User disconnected")
        } catch (_: Exception) {}
        activeSocket = null
        _connectionState.value = ConnectionState.Disconnected
        _stats.value = _stats.value.copy(isLive = false, pingMs = 0)
    }

    fun sendEvent(event: GamepadEvent) {
        val json = event.toJson()
        sendRawJson(json)
    }

    fun sendRawJson(json: String): Boolean {
        val socket = activeSocket ?: return false
        val sent = socket.send(json)
        if (sent) {
            val count = packetsCounter.incrementAndGet()
            if (count % 5L == 0L) {
                _stats.value = _stats.value.copy(packetsSent = count)
            }
        }
        return sent
    }

    /**
     * Critical safety reset: releases all buttons and resets analog sticks to neutral (0, 0)
     */
    fun safetyReset() {
        val socket = activeSocket ?: return
        try {
            // Neutral sticks
            socket.send("""{"type":"stick","stick":"left","x":0.000,"y":0.000}""")
            socket.send("""{"type":"stick","stick":"right","x":0.000,"y":0.000}""")
            // Neutral triggers
            socket.send("""{"type":"trigger","trigger":"LT","value":0.000}""")
            socket.send("""{"type":"trigger","trigger":"RT","value":0.000}""")
            // Release common buttons
            listOf("A", "B", "X", "Y", "L1", "R1", "L3", "R3", "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT", "CREATE", "OPTIONS", "HOME").forEach { btn ->
                socket.send("""{"type":"button","button":"$btn","pressed":false}""")
            }
        } catch (_: Exception) {}
    }

    private fun startPingLoop() {
        pingJob?.cancel()
        pingJob = coroutineScope.launch(Dispatchers.IO) {
            while (true) {
                delay(2000)
                if (_connectionState.value is ConnectionState.Connected) {
                    lastPingSentTime = SystemClock.elapsedRealtime()
                    activeSocket?.send("""{"type":"ping","timestamp":$lastPingSentTime}""")
                } else {
                    break
                }
            }
        }
    }

    private fun stopPingLoop() {
        pingJob?.cancel()
        pingJob = null
    }
}
