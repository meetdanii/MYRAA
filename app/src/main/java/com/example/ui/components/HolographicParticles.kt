package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.VoiceState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * A single stateful particle in the holographic avatar particle system.
 */
internal class AvatarParticle(
  var angle: Float,
  val baseRadiusRatio: Float,
  val speed: Float,
  val sizeDp: Float,
  val baseAlpha: Float,
  val radialWobbleFreq: Float,
  val isSpark: Boolean,
  var currentRadiusRatio: Float = baseRadiusRatio,
  var radialVelocity: Float = 0f
)

/**
 * High-performance, 60fps voice-reactive particle swarm surrounding the holographic avatar.
 * Zero-allocation in draw loop; driven by Android frame clock.
 */
@Composable
fun HolographicParticleField(
  voiceState: VoiceState,
  audioAmplitude: Float,
  modifier: Modifier = Modifier
) {
  val baseColor = voiceState.orbColor
  val animatedColor by animateColorAsState(
    targetValue = baseColor,
    animationSpec = tween(durationMillis = 350),
    label = "particleColor"
  )

  // Pre-allocate particles once with varied orbital properties
  val particles = remember {
    val rng = Random(42)
    List(55) { index ->
      val baseRatio = 0.35f + rng.nextFloat() * 0.65f // Between 0.35 and 1.0 of max radius
      val angle = (index.toFloat() / 55f) * (2f * PI.toFloat()) + rng.nextFloat() * 0.4f
      val speedSign = if (rng.nextBoolean()) 1f else -1f
      val speed = speedSign * (0.35f + rng.nextFloat() * 0.9f)
      val size = 1.6f + rng.nextFloat() * 2.8f
      val alpha = 0.35f + rng.nextFloat() * 0.55f
      val freq = 1f + rng.nextFloat() * 3f
      val isSpark = rng.nextFloat() > 0.65f
      AvatarParticle(
        angle = angle,
        baseRadiusRatio = baseRatio,
        speed = speed,
        sizeDp = size,
        baseAlpha = alpha,
        radialWobbleFreq = freq,
        isSpark = isSpark
      )
    }
  }

  var frameTimeMs by remember { mutableFloatStateOf(0f) }

  // 60/120 FPS continuous physics simulation loop
  LaunchedEffect(Unit) {
    var lastNanos = 0L
    while (true) {
      withFrameMillis { frameMs ->
        frameTimeMs = frameMs.toFloat()
      }
    }
  }

  Canvas(modifier = modifier) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = size.minDimension / 2f
    val t = frameTimeMs / 1000f

    // Determine state-specific physics parameters
    val speedMultiplier = when (voiceState) {
      VoiceState.THINKING -> 3.2f // rapid swirling vortex
      VoiceState.SPEAKING -> 1.8f
      VoiceState.LISTENING -> 1.4f
      VoiceState.CONNECTING -> 2.0f
      VoiceState.ERROR -> 0.5f
      VoiceState.DISCONNECTED -> 0.7f
    }

    val ampClamped = audioAmplitude.coerceIn(0f, 1f)
    val outwardPush = ampClamped * 0.32f

    // Draw all particles
    for (i in particles.indices) {
      val p = particles[i]

      // Update particle orbit angle
      val currentAngle = p.angle + (p.speed * speedMultiplier * 0.6f * t)

      // Radial position with organic harmonic oscillation + voice amplitude burst
      val harmonicWobble = sin(t * p.radialWobbleFreq + i) * 0.06f
      val stateRadiusFactor = when (voiceState) {
        VoiceState.SPEAKING -> 1f + outwardPush * 1.4f + (sin(t * 8f + i) * 0.08f * ampClamped)
        VoiceState.LISTENING -> 1f + outwardPush * 0.9f
        VoiceState.THINKING -> 0.85f + (sin(t * 4f + i) * 0.1f) // contracted inward
        else -> 1f
      }

      val radius = (maxRadius * (p.baseRadiusRatio + harmonicWobble) * stateRadiusFactor)
        .coerceIn(maxRadius * 0.15f, maxRadius * 1.15f)

      val px = center.x + radius * cos(currentAngle)
      val py = center.y + radius * sin(currentAngle)
      val particleCenter = Offset(px, py)

      // Dynamic alpha based on voice state and audio amplitude
      val ampAlphaBoost = if (voiceState == VoiceState.SPEAKING || voiceState == VoiceState.LISTENING) {
        ampClamped * 0.4f
      } else {
        0f
      }
      val dynamicAlpha = ((p.baseAlpha + ampAlphaBoost) * (0.75f + sin(t * 3f + i * 2f) * 0.25f))
        .coerceIn(0.1f, 1.0f)

      val particlePx = p.sizeDp.dp.toPx() * (1f + ampClamped * 0.6f)

      // 1. Soft glowing halo around particle
      drawCircle(
        color = animatedColor.copy(alpha = (dynamicAlpha * 0.32f)),
        radius = particlePx * 2.6f,
        center = particleCenter
      )

      // 2. Primary colored particle body
      drawCircle(
        color = animatedColor.copy(alpha = dynamicAlpha),
        radius = particlePx,
        center = particleCenter
      )

      // 3. Bright white specular spark core
      if (p.isSpark || dynamicAlpha > 0.7f) {
        drawCircle(
          color = Color.White.copy(alpha = (dynamicAlpha * 0.9f).coerceIn(0f, 1f)),
          radius = particlePx * 0.48f,
          center = particleCenter
        )
      }
    }
  }
}

/**
 * Ambient cosmic starfield/dust layer for the background.
 * Drifts across the screen with gentle breathing and twinkling stars.
 */
@Composable
fun CosmicBackgroundParticles(
  modifier: Modifier = Modifier,
  tintColor: Color = Color(0xFF8B5CF6)
) {
  // Pre-allocate background dust motes
  val stars = remember {
    val rng = Random(101)
    List(40) {
      BackgroundStar(
        initialXRatio = rng.nextFloat(),
        initialYRatio = rng.nextFloat(),
        driftSpeedY = 0.015f + rng.nextFloat() * 0.035f,
        driftSpeedX = (rng.nextFloat() - 0.5f) * 0.01f,
        sizeDp = 1.0f + rng.nextFloat() * 2.2f,
        twinkleFreq = 1.5f + rng.nextFloat() * 3.5f,
        baseAlpha = 0.15f + rng.nextFloat() * 0.45f,
        phase = rng.nextFloat() * (2f * PI.toFloat())
      )
    }
  }

  var frameTimeMs by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(Unit) {
    while (true) {
      withFrameMillis { frameMs ->
        frameTimeMs = frameMs.toFloat()
      }
    }
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    val t = frameTimeMs / 1000f

    for (star in stars) {
      // Calculate drifting positions with seamless wrapping
      val currentY = ((star.initialYRatio - t * star.driftSpeedY) % 1f).let { if (it < 0f) it + 1f else it }
      val currentX = ((star.initialXRatio + t * star.driftSpeedX) % 1f).let { if (it < 0f) it + 1f else it }

      val px = currentX * size.width
      val py = currentY * size.height
      val starOffset = Offset(px, py)

      // Twinkle calculation
      val twinkle = (sin(t * star.twinkleFreq + star.phase) * 0.5f + 0.5f)
      val alpha = (star.baseAlpha * (0.4f + twinkle * 0.6f)).coerceIn(0.05f, 0.85f)
      val starRadius = star.sizeDp.dp.toPx()

      // Subtle atmospheric glow
      drawCircle(
        color = tintColor.copy(alpha = alpha * 0.35f),
        radius = starRadius * 2.2f,
        center = starOffset
      )

      // Star point
      drawCircle(
        color = Color.White.copy(alpha = alpha),
        radius = starRadius,
        center = starOffset
      )
    }
  }
}

internal class BackgroundStar(
  val initialXRatio: Float,
  val initialYRatio: Float,
  val driftSpeedY: Float,
  val driftSpeedX: Float,
  val sizeDp: Float,
  val twinkleFreq: Float,
  val baseAlpha: Float,
  val phase: Float
)
