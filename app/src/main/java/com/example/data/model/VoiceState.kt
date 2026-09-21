package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet

/**
 * Represents the current state of MYRAA's voice interaction loop.
 */
enum class VoiceState(val label: String, val orbColor: Color) {
  DISCONNECTED("Idle", NeonIndigo),
  CONNECTING("Connecting to MYRAA...", NeonAmber),
  LISTENING("Listening...", NeonCyan),
  THINKING("Thinking...", NeonViolet),
  SPEAKING("Speaking...", NeonPink),
  ERROR("Error", NeonRed);

  val isStreamingActive: Boolean
    get() = this == LISTENING || this == THINKING || this == SPEAKING
}
