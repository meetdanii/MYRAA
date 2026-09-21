package com.example.gemini

import android.util.Base64
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Manages real-time bidirectional streaming with Gemini Live API via WebSockets.
 * Supports direct Gemini Live connection (BidiGenerateContent) or a custom backend relay.
 */
class GeminiLiveClient(
  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .readTimeout(0, TimeUnit.MILLISECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .pingInterval(20, TimeUnit.SECONDS)
    .build()
) {
  interface Listener {
    fun onConnected()
    fun onModelAudio(pcmData: ByteArray, sampleRate: Int)
    fun onUserTranscript(text: String)
    fun onModelTranscript(text: String)
    fun onTurnComplete()
    fun onInterrupted()
    fun onError(error: String)
    fun onClosed(reason: String)
  }

  private var webSocket: WebSocket? = null
  private var listener: Listener? = null
  private var isConnected = false

  fun isConnected(): Boolean = isConnected

  fun setListener(listener: Listener?) {
    this.listener = listener
  }

  fun connect(apiKey: String, backendUrl: String = "", voiceName: String = "Aoede") {
    disconnect()

    val wsUrl = if (backendUrl.isNotBlank()) {
      val cleanUrl = backendUrl.trimEnd('/')
      if (cleanUrl.startsWith("http://")) {
        "ws://" + cleanUrl.removePrefix("http://") + "/ws/live"
      } else if (cleanUrl.startsWith("https://")) {
        "wss://" + cleanUrl.removePrefix("https://") + "/ws/live"
      } else if (cleanUrl.startsWith("ws://") || cleanUrl.startsWith("wss://")) {
        cleanUrl
      } else {
        "wss://$cleanUrl/ws/live"
      }
    } else {
      if (apiKey.isBlank()) {
        listener?.onError("Gemini API Key is missing. Please enter your API key.")
        return
      }
      // Official Gemini Live API WebSocket endpoint
      "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=$apiKey"
    }

    Log.d(TAG, "Connecting to Gemini Live at: ${if (backendUrl.isNotBlank()) wsUrl else "Gemini Live API"}")

    val request = Request.Builder()
      .url(wsUrl)
      .build()

    webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
      override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "WebSocket connected")
        isConnected = true
        listener?.onConnected()

        // Send Setup handshake for direct Gemini Live connection
        if (backendUrl.isBlank()) {
          sendSetupHandshake(webSocket, voiceName)
        }
      }

      override fun onMessage(webSocket: WebSocket, text: String) {
        handleIncomingJson(text)
      }

      override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "WebSocket failure", t)
        isConnected = false
        val errorMsg = when {
          t.message?.contains("400") == true -> "API request error (400). Please check your API key or model permissions."
          t.message?.contains("403") == true -> "Access forbidden (403). Gemini Live API is not enabled for this key."
          t.message?.contains("404") == true -> "Endpoint not found (404)."
          else -> t.message ?: "Connection failure"
        }
        listener?.onError(errorMsg)
      }

      override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "WebSocket closed: $code / $reason")
        isConnected = false
        listener?.onClosed(reason.ifEmpty { "Connection closed" })
      }
    })
  }

  private fun sendSetupHandshake(ws: WebSocket, voiceName: String) {
    try {
      val setupJson = JSONObject().apply {
        val setupObj = JSONObject().apply {
          // Model: gemini-2.5-flash-native-audio-preview-12-2025 or gemini-3.1-flash-live-preview
          put("model", "models/gemini-2.5-flash-native-audio-preview-12-2025")
          val genConfig = JSONObject().apply {
            put("responseModalities", JSONArray().put("AUDIO"))
            val speechConfig = JSONObject().apply {
              val voiceConfig = JSONObject().apply {
                val prebuilt = JSONObject().apply {
                  put("voiceName", voiceName.ifEmpty { "Aoede" })
                }
                put("prebuiltVoiceConfig", prebuilt)
              }
              put("voiceConfig", voiceConfig)
            }
            put("speechConfig", speechConfig)
          }
          put("generationConfig", genConfig)

          val systemInstruction = JSONObject().apply {
            val parts = JSONArray().put(
              JSONObject().put("text", "You are MYRAA, a futuristic, intelligent voice AI companion. Speak with an engaging, concise, warm tone. Keep responses conversational.")
            )
            put("parts", parts)
          }
          put("systemInstruction", systemInstruction)
        }
        put("setup", setupObj)
      }

      ws.send(setupJson.toString())
      Log.d(TAG, "Sent setup handshake with voice: $voiceName")
    } catch (e: Exception) {
      Log.e(TAG, "Error sending setup handshake", e)
    }
  }

  fun sendAudioChunk(pcmChunk: ByteArray) {
    if (!isConnected || webSocket == null) return

    try {
      val base64Data = Base64.encodeToString(pcmChunk, Base64.NO_WRAP)
      val message = JSONObject().apply {
        val realtimeInput = JSONObject().apply {
          val mediaChunks = JSONArray().put(
            JSONObject().apply {
              put("mimeType", "audio/pcm;rate=16000")
              put("data", base64Data)
            }
          )
          put("mediaChunks", mediaChunks)
        }
        put("realtimeInput", realtimeInput)
      }
      webSocket?.send(message.toString())
    } catch (e: Exception) {
      Log.e(TAG, "Error sending audio chunk", e)
    }
  }

  fun sendTextPrompt(text: String) {
    if (!isConnected || webSocket == null) return

    try {
      val message = JSONObject().apply {
        val clientContent = JSONObject().apply {
          val turns = JSONArray().put(
            JSONObject().apply {
              put("role", "user")
              val parts = JSONArray().put(
                JSONObject().put("text", text)
              )
              put("parts", parts)
            }
          )
          put("turns", turns)
          put("turnComplete", true)
        }
        put("clientContent", clientContent)
      }
      webSocket?.send(message.toString())
      Log.d(TAG, "Sent text prompt via WebSocket: $text")
    } catch (e: Exception) {
      Log.e(TAG, "Error sending text prompt via WebSocket", e)
    }
  }

  private fun handleIncomingJson(jsonStr: String) {
    try {
      val root = JSONObject(jsonStr)

      // Handle serverContent
      if (root.has("serverContent")) {
        val serverContent = root.getJSONObject("serverContent")

        if (serverContent.optBoolean("interrupted", false)) {
          listener?.onInterrupted()
        }

        if (serverContent.has("modelTurn")) {
          val modelTurn = serverContent.getJSONObject("modelTurn")
          val parts = modelTurn.optJSONArray("parts")
          if (parts != null) {
            for (i in 0 until parts.length()) {
              val part = parts.getJSONObject(i)

              // Check for inlineData (audio)
              if (part.has("inlineData")) {
                val inlineData = part.getJSONObject("inlineData")
                val mimeType = inlineData.optString("mimeType", "")
                val dataBase64 = inlineData.optString("data", "")
                if (dataBase64.isNotEmpty()) {
                  val rawBytes = Base64.decode(dataBase64, Base64.DEFAULT)
                  val sampleRate = if (mimeType.contains("rate=16000")) 16000 else 24000
                  listener?.onModelAudio(rawBytes, sampleRate)
                }
              }

              // Check for text parts (captions / transcripts)
              if (part.has("text")) {
                val text = part.optString("text", "")
                if (text.isNotBlank()) {
                  listener?.onModelTranscript(text)
                }
              }
            }
          }
        }

        // Check for transcription events
        if (serverContent.has("turnComplete") && serverContent.getBoolean("turnComplete")) {
          listener?.onTurnComplete()
        }
      }

      // Check for direct transcription events if emitted by API
      if (root.has("userTurn")) {
        val userTurn = root.getJSONObject("userTurn")
        val parts = userTurn.optJSONArray("parts")
        if (parts != null) {
          for (i in 0 until parts.length()) {
            val p = parts.getJSONObject(i)
            if (p.has("text")) {
              val text = p.optString("text")
              if (text.isNotBlank()) {
                listener?.onUserTranscript(text)
              }
            }
          }
        }
      }

      if (root.has("inputTranscription")) {
        val text = root.getJSONObject("inputTranscription").optString("text", "")
        if (text.isNotBlank()) {
          listener?.onUserTranscript(text)
        }
      }

      if (root.has("outputTranscription")) {
        val text = root.getJSONObject("outputTranscription").optString("text", "")
        if (text.isNotBlank()) {
          listener?.onModelTranscript(text)
        }
      }

    } catch (e: Exception) {
      Log.e(TAG, "Error parsing incoming JSON", e)
    }
  }

  fun disconnect() {
    isConnected = false
    try {
      webSocket?.close(1000, "User disconnected")
    } catch (e: Exception) {
      Log.w(TAG, "Error closing WebSocket", e)
    } finally {
      webSocket = null
    }
  }

  companion object {
    private const val TAG = "GeminiLiveClient"
  }
}
