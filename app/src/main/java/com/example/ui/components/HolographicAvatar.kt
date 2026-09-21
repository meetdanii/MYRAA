package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.VoiceState
import kotlin.math.cos
import kotlin.math.sin

/**
 * Centered holographic avatar for MYRAA.
 * Animated based on VoiceState and sound amplitude.
 * Modular design allowing future 3D/VRM avatar integration.
 */
@Composable
fun HolographicAvatar(
  voiceState: VoiceState,
  audioAmplitude: Float,
  modifier: Modifier = Modifier,
  size: Dp = 220.dp
) {
  val baseColor = voiceState.orbColor
  val animatedColor by animateColorAsState(
    targetValue = baseColor,
    animationSpec = tween(durationMillis = 400),
    label = "avatarColor"
  )

  // Smooth amplitude to avoid visual jitter
  val smoothedAmplitude by animateFloatAsState(
    targetValue = audioAmplitude.coerceIn(0f, 1f),
    animationSpec = tween(durationMillis = 100),
    label = "smoothedAmp"
  )

  val infiniteTransition = rememberInfiniteTransition(label = "avatarRotation")

  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 14000, easing = LinearEasing)
    ),
    label = "reticleRotation"
  )

  val reverseRotation by infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 18000, easing = LinearEasing)
    ),
    label = "outerRotation"
  )

  val idlePulse by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "idlePulse"
  )

  val auraPulse by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 0.7f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "auraPulse"
  )

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    // 0. Interactive 60fps holographic voice-reactive particle field
    HolographicParticleField(
      voiceState = voiceState,
      audioAmplitude = smoothedAmplitude,
      modifier = Modifier.size(size * 1.12f)
    )

    Canvas(modifier = Modifier.size(size)) {
      val center = Offset(this.size.width / 2f, this.size.height / 2f)
      val maxRadius = this.size.minDimension / 2f

      // Amplitude expansion multiplier
      val ampBoost = 1f + smoothedAmplitude * 0.45f
      val dynamicRadius = (maxRadius * 0.55f) * idlePulse * ampBoost

      // 1. Ambient holographic radial glow aura
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            animatedColor.copy(alpha = 0.45f * auraPulse * ampBoost),
            animatedColor.copy(alpha = 0.15f * auraPulse),
            Color.Transparent
          ),
          center = center,
          radius = maxRadius * 0.95f
        ),
        radius = maxRadius * 0.95f,
        center = center
      )

      // 2. Outer cybernetic dashed orbital ring
      rotate(degrees = reverseRotation, pivot = center) {
        drawCircle(
          color = animatedColor.copy(alpha = 0.35f),
          radius = maxRadius * 0.88f,
          center = center,
          style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 14f), 0f)
          )
        )
      }

      // 3. Middle technical reticle ring with tick marks
      rotate(degrees = rotationAngle, pivot = center) {
        drawCircle(
          color = animatedColor.copy(alpha = 0.5f),
          radius = maxRadius * 0.75f,
          center = center,
          style = Stroke(
            width = 1.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f, 10f, 20f), 0f)
          )
        )

        // Draw 6 orbital holographic node dots
        for (i in 0 until 6) {
          val angleRad = Math.toRadians((i * 60).toDouble())
          val nodeX = center.x + (maxRadius * 0.75f * cos(angleRad)).toFloat()
          val nodeY = center.y + (maxRadius * 0.75f * sin(angleRad)).toFloat()
          drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = 2.5.dp.toPx(),
            center = Offset(nodeX, nodeY)
          )
        }
      }

      // 4. Reactive sound-wave rings around the core (multi-tier ripples)
      val rippleCount = 3
      for (r in 1..rippleCount) {
        val rippleRadius = dynamicRadius + (r * 18.dp.toPx() * (0.4f + smoothedAmplitude))
        val rippleAlpha = (0.45f / r) * (0.6f + smoothedAmplitude * 0.8f)
        drawCircle(
          color = animatedColor.copy(alpha = rippleAlpha.coerceIn(0f, 1f)),
          radius = rippleRadius.coerceAtMost(maxRadius * 0.98f),
          center = center,
          style = Stroke(width = (1.5f + smoothedAmplitude * 2f).dp.toPx())
        )
      }

      // 5. Radiant holographic central orb core
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.White.copy(alpha = 0.95f),
            animatedColor.copy(alpha = 0.9f),
            animatedColor.copy(alpha = 0.6f),
            animatedColor.copy(alpha = 0.1f)
          ),
          center = center,
          radius = dynamicRadius
        ),
        radius = dynamicRadius,
        center = center
      )

      // 6. Central subtle bright focus highlight
      drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = (dynamicRadius * 0.35f) * (1f + smoothedAmplitude * 0.3f),
        center = center
      )
    }
  }
}
