package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.GamepadScreen
import com.example.ui.theme.GamepadBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GamepadViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GamepadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep screen on during gaming
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Hide system status and navigation bars for immersive fullscreen gamepad
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MyApplicationTheme {
                val isArrangeMode by viewModel.isArrangeMode.collectAsState()
                val showSetupDialog by viewModel.showSetupDialog.collectAsState()

                // Intercept back button when in arrange mode or setup dialog
                BackHandler(enabled = isArrangeMode || showSetupDialog) {
                    if (showSetupDialog) {
                        viewModel.closeSetupDialog()
                    } else if (isArrangeMode) {
                        viewModel.cancelArrangeChanges()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = GamepadBackground
                ) {
                    GamepadScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-hide system bars when returning to game
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onPause() {
        super.onPause()
        // Safety: release all buttons and neutral sticks when app loses focus
        viewModel.webSocketClient.safetyReset()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.webSocketClient.disconnect()
    }
}
