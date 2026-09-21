package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioCaptureService
import com.example.audio.AudioPlaybackService
import com.example.audio.SpeechSynthesisService
import com.example.auth.AuthManager
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSender
import com.example.data.model.LiveCaption
import com.example.data.model.UserProfile
import com.example.data.model.VoiceState
import com.example.data.storage.SessionStorage
import com.example.gemini.GeminiLiveClient
import com.example.gemini.GeminiVoiceRestService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyraaViewModel(application: Application) : AndroidViewModel(application) {
  val sessionStorage = SessionStorage(application)
  val authManager = AuthManager(application, sessionStorage)

  private val audioCaptureService = AudioCaptureService()
  private val audioPlaybackService = AudioPlaybackService()
  private val geminiLiveClient = GeminiLiveClient()
  private val geminiVoiceRestService = GeminiVoiceRestService()
  private val speechSynthesisService = SpeechSynthesisService(application)

  // User Profile
  val userProfile: StateFlow<UserProfile> = sessionStorage.userProfile
  val geminiApiKey: StateFlow<String> = sessionStorage.geminiApiKey
  val backendUrl: StateFlow<String> = sessionStorage.backendUrl
  val voiceName: StateFlow<String> = sessionStorage.voiceName
  val captionTimeoutSeconds: StateFlow<Int> = sessionStorage.captionTimeoutSeconds

  // Voice Companion State
  private val _voiceState = MutableStateFlow(VoiceState.DISCONNECTED)
  val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

  // Chat History & Text Chat Overlay
  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  private val _isChatOpen = MutableStateFlow(false)
  val isChatOpen: StateFlow<Boolean> = _isChatOpen.asStateFlow()

  // Live Captions
  private val _liveCaption = MutableStateFlow(LiveCaption())
  val liveCaption: StateFlow<LiveCaption> = _liveCaption.asStateFlow()

  private var captionFadeJob: Job? = null

  // Audio State & Amplitudes
  val isMuted: StateFlow<Boolean> = audioCaptureService.isMuted
  val inputAmplitude: StateFlow<Float> = audioCaptureService.inputAmplitude
  val outputAmplitude: StateFlow<Float> = audioPlaybackService.outputAmplitude

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _isScreenSharing = MutableStateFlow(false)
  val isScreenSharing: StateFlow<Boolean> = _isScreenSharing.asStateFlow()

  init {
    setupGeminiListener()
  }

  private fun setupGeminiListener() {
    geminiLiveClient.setListener(object : GeminiLiveClient.Listener {
      override fun onConnected() {
        Log.d(TAG, "Gemini Live Connected")
        _voiceState.value = VoiceState.LISTENING
        _errorMessage.value = null
      }

      override fun onModelAudio(pcmData: ByteArray, sampleRate: Int) {
        _voiceState.value = VoiceState.SPEAKING
        audioPlaybackService.enqueueAudio(pcmData, sampleRate)
      }

      override fun onUserTranscript(text: String) {
        updateUserCaption(text)
        appendChatMessage(ChatSender.USER, text)
        _voiceState.value = VoiceState.THINKING
      }

      override fun onModelTranscript(text: String) {
        updateModelCaption(text)
        appendOrUpdateModelChatMessage(text)
        if (_voiceState.value != VoiceState.SPEAKING) {
          _voiceState.value = VoiceState.SPEAKING
        }
      }

      override fun onTurnComplete() {
        Log.d(TAG, "Gemini Turn Complete")
        _voiceState.value = VoiceState.LISTENING
      }

      override fun onInterrupted() {
        Log.d(TAG, "Gemini Speech Interrupted by User")
        audioPlaybackService.stopAndFlush()
        _voiceState.value = VoiceState.LISTENING
      }

      override fun onError(error: String) {
        Log.e(TAG, "Gemini error: $error")
        _voiceState.value = VoiceState.ERROR
        _errorMessage.value = error
      }

      override fun onClosed(reason: String) {
        Log.d(TAG, "Gemini connection closed: $reason")
        if (_voiceState.value != VoiceState.ERROR) {
          _voiceState.value = VoiceState.DISCONNECTED
        }
      }
    })
  }

  fun startVoiceSession() {
    val key = sessionStorage.geminiApiKey.value
    val backend = sessionStorage.backendUrl.value

    if (key.isBlank() && backend.isBlank()) {
      _errorMessage.value = "Please enter your Gemini API Key in Settings to connect."
      _voiceState.value = VoiceState.ERROR
      return
    }

    _errorMessage.value = null
    _voiceState.value = VoiceState.CONNECTING

    // Start audio playback loop
    audioPlaybackService.startPlaybackLoop(viewModelScope)

    // Connect to Gemini Live
    geminiLiveClient.connect(
      apiKey = key,
      backendUrl = backend,
      voiceName = sessionStorage.voiceName.value
    )

    // Start audio capture
    audioCaptureService.startCapture(
      scope = viewModelScope,
      onAudioChunk = { pcmChunk ->
        geminiLiveClient.sendAudioChunk(pcmChunk)
      },
      onError = { err ->
        _errorMessage.value = err
        _voiceState.value = VoiceState.ERROR
      }
    )
  }

  fun stopVoiceSession() {
    audioCaptureService.stopCapture()
    audioPlaybackService.stopAndFlush()
    geminiLiveClient.disconnect()
    _voiceState.value = VoiceState.DISCONNECTED
  }

  fun toggleMute() {
    audioCaptureService.toggleMute()
  }

  fun toggleScreenSharing() {
    _isScreenSharing.value = !_isScreenSharing.value
  }

  fun saveApiKey(apiKey: String) {
    sessionStorage.saveApiKey(apiKey)
    _errorMessage.value = null
  }

  fun saveBackendUrl(url: String) {
    sessionStorage.saveBackendUrl(url)
  }

  fun saveVoiceName(voice: String) {
    sessionStorage.saveVoiceName(voice)
  }

  fun saveCaptionTimeout(seconds: Int) {
    sessionStorage.saveCaptionTimeout(seconds)
  }

  fun signInWithGoogle(displayName: String, email: String, photoUrl: String? = null) {
    authManager.signInWithGoogleAccount(displayName, email, photoUrl)
  }

  fun logOut() {
    stopVoiceSession()
    authManager.signOut()
    _liveCaption.value = LiveCaption()
    _errorMessage.value = null
  }

  fun clearError() {
    _errorMessage.value = null
    if (_voiceState.value == VoiceState.ERROR) {
      _voiceState.value = VoiceState.DISCONNECTED
    }
  }

  private fun updateUserCaption(text: String) {
    val current = _liveCaption.value
    val updated = current.copy(
      userText = text,
      userTimestampMillis = System.currentTimeMillis(),
      isUserVisible = true
    )
    _liveCaption.value = updated
    scheduleCaptionFadeOut()
  }

  private fun updateModelCaption(text: String) {
    val current = _liveCaption.value
    val newModelText = if (current.modelText.isEmpty()) text else current.modelText + " " + text
    val updated = current.copy(
      modelText = newModelText,
      modelTimestampMillis = System.currentTimeMillis(),
      isModelVisible = true
    )
    _liveCaption.value = updated
    scheduleCaptionFadeOut()
  }

  private fun scheduleCaptionFadeOut() {
    captionFadeJob?.cancel()
    val timeoutMillis = (sessionStorage.captionTimeoutSeconds.value * 1000L).coerceAtLeast(3000L)
    captionFadeJob = viewModelScope.launch {
      delay(timeoutMillis)
      _liveCaption.value = _liveCaption.value.copy(
        isUserVisible = false,
        isModelVisible = false
      )
    }
  }

  fun toggleChat() {
    _isChatOpen.value = !_isChatOpen.value
  }

  fun setChatOpen(isOpen: Boolean) {
    _isChatOpen.value = isOpen
  }

  fun sendTextMessage(text: String) {
    val cleanText = text.trim()
    if (cleanText.isBlank()) return

    // 1. Add user message to conversation history
    appendChatMessage(ChatSender.USER, cleanText)
    updateUserCaption(cleanText)

    // 2. Set companion state to THINKING
    _voiceState.value = VoiceState.THINKING
    _errorMessage.value = null

    // 3. If Gemini Live is connected via WebSocket, send text prompt into the live stream
    if (geminiLiveClient.isConnected()) {
      geminiLiveClient.sendTextPrompt(cleanText)
      return
    }

    // 4. Otherwise, generate speech response via Gemini Audio REST fallback or Native TTS
    val key = sessionStorage.geminiApiKey.value
    if (key.isBlank()) {
      _errorMessage.value = "Gemini API Key is missing. Please enter your API key in Settings."
      _voiceState.value = VoiceState.ERROR
      return
    }

    viewModelScope.launch {
      // Start audio playback engine
      audioPlaybackService.startPlaybackLoop(this)

      val voice = sessionStorage.voiceName.value
      val result = geminiVoiceRestService.askVoice(cleanText, key, voice)

      result.onSuccess { voiceResp ->
        val spokenText = voiceResp.transcript.ifBlank { "I have received your message." }
        appendOrUpdateModelChatMessage(spokenText)
        updateModelCaption(spokenText)

        if (voiceResp.pcmAudio != null && voiceResp.pcmAudio.isNotEmpty()) {
          _voiceState.value = VoiceState.SPEAKING
          audioPlaybackService.enqueueAudio(voiceResp.pcmAudio, voiceResp.sampleRate)

          // Reset to DISCONNECTED after audio finishes
          val durationMs = (voiceResp.pcmAudio.size.toLong() * 1000L) / (voiceResp.sampleRate * 2L)
          launch {
            delay(durationMs + 400L)
            if (_voiceState.value == VoiceState.SPEAKING) {
              _voiceState.value = VoiceState.DISCONNECTED
            }
          }
        } else {
          // Native TTS fallback: speak the answer out loud!
          _voiceState.value = VoiceState.SPEAKING
          speechSynthesisService.speak(spokenText) {
            _voiceState.value = VoiceState.DISCONNECTED
          }
        }
      }.onFailure { err ->
        Log.e(TAG, "Failed to get Gemini voice response, falling back to conversational speech", err)
        val errMsg = err.message ?: ""
        val isKeyError = errMsg.contains("API_KEY_INVALID", ignoreCase = true) ||
          errMsg.contains("API key not valid", ignoreCase = true) ||
          errMsg.contains("400", ignoreCase = true) && errMsg.contains("key", ignoreCase = true)

        val spokenResponse = when {
          isKeyError -> {
            _errorMessage.value = "Gemini API key is invalid. Please check your key in Settings."
            "I heard you say: $cleanText. However, your Gemini API key seems invalid. Please check your key in Settings."
          }
          cleanText.matches(Regex("(?i)^(hi|hello|hey|greetings|howdy).*")) -> {
            "Hello there! I'm MYRAA, your futuristic AI voice companion. It's great to connect with you!"
          }
          cleanText.matches(Regex("(?i).*(who are you|what is your name).*")) -> {
            "I am MYRAA, your futuristic AI companion. I'm here to talk, brainstorm, and explore ideas with you."
          }
          cleanText.matches(Regex("(?i).*(how are you|how's it going).*")) -> {
            "All systems are operating at peak efficiency! How are you doing today?"
          }
          else -> {
            _errorMessage.value = "Connection issue: Unable to reach Gemini cloud."
            "I received your message: \"$cleanText\", but I'm having trouble reaching the Gemini cloud server. Please check your connection or API key."
          }
        }

        appendOrUpdateModelChatMessage(spokenResponse)
        updateModelCaption(spokenResponse)
        _voiceState.value = VoiceState.SPEAKING
        speechSynthesisService.speak(spokenResponse) {
          _voiceState.value = VoiceState.DISCONNECTED
        }
      }
    }
  }

  private fun appendChatMessage(sender: ChatSender, text: String) {
    val message = ChatMessage(sender = sender, text = text)
    _chatMessages.value = _chatMessages.value + message
  }

  private fun appendOrUpdateModelChatMessage(text: String) {
    val currentList = _chatMessages.value
    val lastMsg = currentList.lastOrNull()
    if (lastMsg != null && lastMsg.sender == ChatSender.MYRAA && (System.currentTimeMillis() - lastMsg.timestampMillis < 5000)) {
      // Update the ongoing MYRAA speech message
      val updated = lastMsg.copy(text = if (lastMsg.text.isEmpty()) text else lastMsg.text + " " + text)
      _chatMessages.value = currentList.dropLast(1) + updated
    } else {
      _chatMessages.value = currentList + ChatMessage(sender = ChatSender.MYRAA, text = text)
    }
  }

  override fun onCleared() {
    super.onCleared()
    stopVoiceSession()
    audioPlaybackService.releaseTrack()
    speechSynthesisService.shutdown()
  }

  companion object {
    private const val TAG = "MyraaViewModel"
  }
}
