package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Native Android TextToSpeech engine providing instant, resilient speech synthesis
 * fallback so MYRAA always speaks back to the user out loud.
 */
class SpeechSynthesisService(context: Context) {
  private var tts: TextToSpeech? = null
  private var isInitialized = false

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private var onDoneCallback: (() -> Unit)? = null

  init {
    tts = TextToSpeech(context.applicationContext) { status ->
      if (status == TextToSpeech.SUCCESS) {
        tts?.language = Locale.US
        tts?.setPitch(1.1f)
        tts?.setSpeechRate(1.02f)
        isInitialized = true
        setupListener()
        Log.d(TAG, "Native TTS initialized successfully")
      } else {
        Log.e(TAG, "Native TTS initialization failed with status: $status")
      }
    }
  }

  private fun setupListener() {
    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
      override fun onStart(utteranceId: String?) {
        _isSpeaking.value = true
      }

      override fun onDone(utteranceId: String?) {
        _isSpeaking.value = false
        onDoneCallback?.invoke()
      }

      override fun onError(utteranceId: String?) {
        _isSpeaking.value = false
        onDoneCallback?.invoke()
      }
    })
  }

  fun speak(text: String, onDone: (() -> Unit)? = null) {
    if (!isInitialized || tts == null) {
      Log.w(TAG, "TTS not ready yet")
      onDone?.invoke()
      return
    }
    this.onDoneCallback = onDone
    val utteranceId = System.currentTimeMillis().toString()
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
  }

  fun stop() {
    tts?.stop()
    _isSpeaking.value = false
  }

  fun shutdown() {
    tts?.stop()
    tts?.shutdown()
    tts = null
  }

  companion object {
    private const val TAG = "SpeechSynthesisService"
  }
}
