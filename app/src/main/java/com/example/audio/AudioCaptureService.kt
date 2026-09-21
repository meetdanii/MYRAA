package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Service responsible for low-latency 16 kHz mono 16-bit PCM microphone capture.
 */
class AudioCaptureService {
  private val sampleRate = 16000
  private val channelConfig = AudioFormat.CHANNEL_IN_MONO
  private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

  private var audioRecord: AudioRecord? = null
  private var recordingJob: Job? = null

  private val _isRecording = MutableStateFlow(false)
  val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

  private val _isMuted = MutableStateFlow(false)
  val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

  private val _inputAmplitude = MutableStateFlow(0f)
  val inputAmplitude: StateFlow<Float> = _inputAmplitude.asStateFlow()

  fun setMuted(muted: Boolean) {
    _isMuted.value = muted
  }

  fun toggleMute(): Boolean {
    val newMuted = !_isMuted.value
    _isMuted.value = newMuted
    return newMuted
  }

  @SuppressLint("MissingPermission")
  fun startCapture(
    scope: CoroutineScope,
    onAudioChunk: (ByteArray) -> Unit,
    onError: (String) -> Unit
  ) {
    if (_isRecording.value) return

    val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
      onError("Microphone hardware buffer calculation failed.")
      return
    }

    // Use 100ms chunk size = 1600 samples * 2 bytes = 3200 bytes
    val bufferSize = maxOf(minBufferSize, 3200)

    try {
      audioRecord = AudioRecord(
        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
        sampleRate,
        channelConfig,
        audioFormat,
        bufferSize
      )

      if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
        audioRecord?.release()
        audioRecord = null
        onError("AudioRecord failed to initialize. Check microphone permission.")
        return
      }

      audioRecord?.startRecording()
      _isRecording.value = true

      recordingJob = scope.launch(Dispatchers.IO) {
        val buffer = ByteArray(3200)
        while (isActive && _isRecording.value) {
          val record = audioRecord ?: break
          val bytesRead = record.read(buffer, 0, buffer.size)

          if (bytesRead > 0) {
            if (_isMuted.value) {
              _inputAmplitude.value = 0f
              // When muted, we do not stream voice or send zeroed buffer
            } else {
              val amp = calculateRms(buffer, bytesRead)
              _inputAmplitude.value = amp

              val chunk = buffer.copyOf(bytesRead)
              onAudioChunk(chunk)
            }
          }
        }
      }
    } catch (e: SecurityException) {
      Log.e(TAG, "Permission denied for AudioRecord", e)
      onError("Microphone permission denied.")
      stopCapture()
    } catch (e: Exception) {
      Log.e(TAG, "Audio capture start error", e)
      onError("Audio capture error: ${e.message}")
      stopCapture()
    }
  }

  fun stopCapture() {
    _isRecording.value = false
    _inputAmplitude.value = 0f
    recordingJob?.cancel()
    recordingJob = null

    try {
      audioRecord?.let {
        if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
          it.stop()
        }
        it.release()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error releasing AudioRecord", e)
    } finally {
      audioRecord = null
    }
  }

  private fun calculateRms(buffer: ByteArray, length: Int): Float {
    var sum = 0.0
    val numSamples = length / 2
    if (numSamples == 0) return 0f

    for (i in 0 until length - 1 step 2) {
      val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
      val shortSample = sample.toShort()
      sum += (shortSample * shortSample).toDouble()
    }

    val rms = sqrt(sum / numSamples)
    // Normalize to 0.0f - 1.0f (32767 is max short value)
    val normalized = (rms / 8000.0).toFloat().coerceIn(0f, 1f)
    return normalized
  }

  companion object {
    private const val TAG = "AudioCaptureService"
  }
}
