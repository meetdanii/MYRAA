package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.VoiceState

/**
 * Small animated glowing status orb in the top-right corner.
 * Changes color based on MYRAA state:
 * - Idle/Disconnected: Indigo
 * - Connecting: Amber
 * - Listening: Cyan
 * - Thinking: Violet
 * - Speaking: Pink/Fuchsia
 * - Error: Red
 */
@Composable
fun StatusOrb(
  voiceState: VoiceState,
  modifier: Modifier = Modifier,
  size: Dp = 16.dp
) {
  val targetColor = voiceState.orbColor
  val animatedColor by animateColorAsState(
    targetValue = targetColor,
    animationSpec = tween(durationMillis = 400),
    label = "statusOrbColor"
  )

  val infiniteTransition = rememberInfiniteTransition(label = "orbPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.92f,
    targetValue = if (voiceState.isStreamingActive) 1.25f else 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = if (voiceState == VoiceState.SPEAKING) 500 else 1200,
        easing = FastOutSlowInEasing
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orbScale"
  )

  val haloAlpha by infiniteTransition.animateFloat(
    initialValue = 0.25f,
    targetValue = 0.65f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = if (voiceState == VoiceState.SPEAKING) 500 else 1200,
        easing = FastOutSlowInEasing
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orbHaloAlpha"
  )

  Box(
    modifier = modifier.size(size + 14.dp),
    contentAlignment = Alignment.Center
  ) {
    // Outer animated glow halo
    Box(
      modifier = Modifier
        .size(size + 10.dp)
        .scale(pulseScale)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(
              animatedColor.copy(alpha = haloAlpha),
              animatedColor.copy(alpha = 0f)
            )
          )
        )
    )

    // Inner glowing ring
    Box(
      modifier = Modifier
        .size(size + 4.dp)
        .border(1.dp, animatedColor.copy(alpha = 0.8f), CircleShape)
    )

    // Core solid orb
    Box(
      modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.9f),
              animatedColor,
              animatedColor.copy(alpha = 0.8f)
            )
          )
        )
    )
  }
}
