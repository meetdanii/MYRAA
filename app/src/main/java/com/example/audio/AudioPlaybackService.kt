package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.sqrt

/**
 * Low-latency audio playback service using Android AudioTrack for streaming PCM chunks from Gemini.
 */
class AudioPlaybackService {
  private var audioTrack: AudioTrack? = null
  private var currentSampleRate: Int = 24000
  private val audioQueue = LinkedBlockingQueue<ByteArray>()
  private var playbackJob: Job? = null

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _outputAmplitude = MutableStateFlow(0f)
  val outputAmplitude: StateFlow<Float> = _outputAmplitude.asStateFlow()

  fun initTrack(sampleRate: Int = 24000) {
    if (audioTrack != null && currentSampleRate == sampleRate) return
    releaseTrack()

    currentSampleRate = sampleRate
    val channelConfig = AudioFormat.CHANNEL_OUT_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    val bufferSize = maxOf(minBufferSize, sampleRate * 2) // 1 second buffer

    val attributes = AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
      .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
      .build()

    val format = AudioFormat.Builder()
      .setSampleRate(sampleRate)
      .setEncoding(audioFormat)
      .setChannelMask(channelConfig)
      .build()

    try {
      audioTrack = AudioTrack(
        attributes,
        format,
        bufferSize,
        AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE
      )
      audioTrack?.play()
    } catch (e: Exception) {
      Log.e(TAG, "Failed to initialize AudioTrack", e)
    }
  }

  fun startPlaybackLoop(scope: CoroutineScope) {
    if (playbackJob?.isActive == true) return
    initTrack(currentSampleRate)

    playbackJob = scope.launch(Dispatchers.IO) {
      while (isActive) {
        val chunk = audioQueue.poll()
        if (chunk != null) {
          _isPlaying.value = true
          val amp = calculateRms(chunk)
          _outputAmplitude.value = amp

          audioTrack?.write(chunk, 0, chunk.size)
        } else {
          if (_isPlaying.value && audioQueue.isEmpty()) {
            _isPlaying.value = false
            _outputAmplitude.value = 0f
          }
          // Brief sleep to avoid busy-spin when queue is empty
          kotlinx.coroutines.delay(10)
        }
      }
    }
  }

  fun enqueueAudio(pcmChunk: ByteArray, sampleRate: Int = 24000) {
    if (currentSampleRate != sampleRate) {
      initTrack(sampleRate)
    }
    audioQueue.offer(pcmChunk)
  }

  fun stopAndFlush() {
    audioQueue.clear()
    _isPlaying.value = false
    _outputAmplitude.value = 0f
    try {
      audioTrack?.pause()
      audioTrack?.flush()
      audioTrack?.play()
    } catch (e: Exception) {
      Log.w(TAG, "Error flushing audio track", e)
    }
  }

  fun releaseTrack() {
    playbackJob?.cancel()
    playbackJob = null
    audioQueue.clear()
    _isPlaying.value = false
    _outputAmplitude.value = 0f

    try {
      audioTrack?.stop()
      audioTrack?.release()
    } catch (e: Exception) {
      Log.w(TAG, "Error releasing audio track", e)
    } finally {
      audioTrack = null
    }
  }

  private fun calculateRms(buffer: ByteArray): Float {
    var sum = 0.0
    val numSamples = buffer.size / 2
    if (numSamples == 0) return 0f

    for (i in 0 until buffer.size - 1 step 2) {
      val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
      val shortSample = sample.toShort()
      sum += (shortSample * shortSample).toDouble()
    }

    val rms = sqrt(sum / numSamples)
    return (rms / 9000.0).toFloat().coerceIn(0f, 1f)
  }

  companion object {
    private const val TAG = "AudioPlaybackService"
  }
}
