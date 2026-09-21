package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VoiceChat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VoiceState
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

/**
 * Bottom control panel for MYRAA:
 * - Primary Voice Action (Start / Stop) with glowing holographic ripple
 * - Mute / Unmute microphone
 * - Disconnect button
 * - Screen Share button (Coming Soon dialog / indicator)
 * - Connection status pill
 */
@Composable
fun FuturisticControls(
  voiceState: VoiceState,
  isMuted: Boolean,
  onStartVoice: () -> Unit,
  onStopVoice: () -> Unit,
  onToggleMute: () -> Unit,
  onDisconnect: () -> Unit,
  onScreenShareClick: () -> Unit,
  onToggleChat: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Connection Status Pill
    ConnectionStatusBadge(voiceState = voiceState)

    // 2. Control Actions Row
    Row(
      modifier = Modifier
        .widthIn(max = 420.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(32.dp))
        .background(CharcoalSurface.copy(alpha = 0.85f))
        .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(32.dp))
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Screen Share Button ("Coming Soon" safe indicator)
      IconButton(
        onClick = onScreenShareClick,
        modifier = Modifier
          .size(48.dp)
          .testTag("screen_share_button")
      ) {
        Icon(
          imageVector = Icons.Default.ScreenShare,
          contentDescription = "Screen Share",
          tint = TextMuted.copy(alpha = 0.7f),
          modifier = Modifier.size(24.dp)
        )
      }

      // Mute / Unmute Microphone
      IconButton(
        onClick = onToggleMute,
        enabled = voiceState.isStreamingActive,
        modifier = Modifier
          .size(48.dp)
          .testTag("mute_toggle_button")
      ) {
        val micColor = if (isMuted) NeonRed else if (voiceState.isStreamingActive) NeonCyan else TextMuted
        Icon(
          imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
          contentDescription = if (isMuted) "Unmute Microphone" else "Mute Microphone",
          tint = micColor,
          modifier = Modifier.size(24.dp)
        )
      }

      // Primary Start / Stop Voice Button
      val isSessionActive = voiceState.isStreamingActive || voiceState == VoiceState.CONNECTING
      PrimaryVoiceActionButton(
        isActive = isSessionActive,
        voiceState = voiceState,
        onClick = {
          if (isSessionActive) {
            onStopVoice()
          } else {
            onStartVoice()
          }
        }
      )

      // Text Chat Button (Type to MYRAA with speech response)
      IconButton(
        onClick = onToggleChat,
        modifier = Modifier
          .size(48.dp)
          .testTag("chat_toggle_button")
      ) {
        Icon(
          imageVector = Icons.Default.Chat,
          contentDescription = "Text Chat with MYRAA",
          tint = NeonCyan,
          modifier = Modifier.size(24.dp)
        )
      }

      // Disconnect Button
      IconButton(
        onClick = onDisconnect,
        enabled = isSessionActive,
        modifier = Modifier
          .size(48.dp)
          .testTag("disconnect_button")
      ) {
        Icon(
          imageVector = Icons.Default.CallEnd,
          contentDescription = "Disconnect",
          tint = if (isSessionActive) NeonRed else TextMuted.copy(alpha = 0.5f),
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}

@Composable
private fun PrimaryVoiceActionButton(
  isActive: Boolean,
  voiceState: VoiceState,
  onClick: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "btnPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (isActive) 1.14f else 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "btnPulseScale"
  )

  val buttonColor = when (voiceState) {
    VoiceState.CONNECTING -> NeonAmber
    VoiceState.SPEAKING -> NeonPink
    VoiceState.LISTENING -> NeonCyan
    VoiceState.THINKING -> Color(0xFFA78BFA)
    VoiceState.ERROR -> NeonRed
    VoiceState.DISCONNECTED -> NeonCyan
  }

  Box(
    modifier = Modifier
      .size(68.dp)
      .testTag("primary_voice_button"),
    contentAlignment = Alignment.Center
  ) {
    // Glowing animated border ring
    Box(
      modifier = Modifier
        .size(64.dp)
        .scale(pulseScale)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(
              buttonColor.copy(alpha = if (isActive) 0.5f else 0.2f),
              Color.Transparent
            )
          )
        )
    )

    // Inner Button
    Box(
      modifier = Modifier
        .size(56.dp)
        .clip(CircleShape)
        .background(
          if (isActive) {
            Brush.linearGradient(
              colors = listOf(buttonColor, buttonColor.copy(alpha = 0.8f))
            )
          } else {
            Brush.linearGradient(
              colors = listOf(CharcoalCard, Color(0xFF221A3B))
            )
          }
        )
        .border(1.5.dp, buttonColor, CircleShape)
        .clickable(onClick = onClick),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isActive) Icons.Default.Stop else Icons.Default.VoiceChat,
        contentDescription = if (isActive) "Stop Voice" else "Start Voice",
        tint = if (isActive) Color.White else buttonColor,
        modifier = Modifier.size(28.dp)
      )
    }
  }
}

@Composable
private fun ConnectionStatusBadge(voiceState: VoiceState) {
  val badgeColor by animateColorAsState(
    targetValue = voiceState.orbColor,
    animationSpec = tween(300),
    label = "badgeColor"
  )

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(CharcoalSurface.copy(alpha = 0.7f))
      .border(0.8.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
      .padding(horizontal = 14.dp, vertical = 6.dp)
  ) {
    // Pulse dot
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(badgeColor)
    )

    Text(
      text = voiceState.label.uppercase(),
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      fontFamily = FontFamily.Monospace,
      letterSpacing = 1.2.sp,
      color = TextSecondary
    )
  }
}
