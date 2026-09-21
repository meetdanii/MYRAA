package com.example.gemini

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Direct REST fallback for generating voice and text responses from Gemini
 * when WebSockets are unavailable.
 */
class GeminiVoiceRestService(
  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(20, TimeUnit.SECONDS)
    .build()
) {
  data class VoiceResponse(
    val transcript: String,
    val pcmAudio: ByteArray?,
    val sampleRate: Int
  )

  suspend fun askVoice(
    prompt: String,
    apiKey: String,
    voiceName: String = "Aoede"
  ): Result<VoiceResponse> = withContext(Dispatchers.IO) {
    try {
      // 1. First attempt: generate content with native audio modality on gemini-2.5-flash
      val audioResult = tryGenerateAudio(prompt, apiKey, voiceName)
      if (audioResult != null && (audioResult.pcmAudio != null || audioResult.transcript.isNotBlank())) {
        return@withContext Result.success(audioResult)
      }

      // 2. Second attempt: generate text on gemini-2.5-flash (which will be spoken via TTS)
      val textResult = tryGenerateText(prompt, apiKey)
      if (textResult.isNotBlank()) {
        return@withContext Result.success(
          VoiceResponse(transcript = textResult, pcmAudio = null, sampleRate = 24000)
        )
      }

      Result.failure(Exception("Empty response from Gemini"))
    } catch (e: Exception) {
      Log.e(TAG, "Gemini REST error", e)
      Result.failure(e)
    }
  }

  private fun tryGenerateAudio(
    prompt: String,
    apiKey: String,
    voiceName: String
  ): VoiceResponse? {
    try {
      val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
      val requestBodyJson = JSONObject().apply {
        val contents = JSONArray().put(
          JSONObject().apply {
            val parts = JSONArray().put(
              JSONObject().put("text", prompt)
            )
            put("parts", parts)
          }
        )
        put("contents", contents)

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
            JSONObject().put("text", "You are MYRAA, a warm, futuristic, intelligent voice AI companion. Speak concisely in natural spoken language (1-2 sentences).")
          )
          put("parts", parts)
        }
        put("systemInstruction", systemInstruction)
      }

      val request = Request.Builder()
        .url(url)
        .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = okHttpClient.newCall(request).execute()
      if (!response.isSuccessful) {
        Log.w(TAG, "Audio modality request unsuccessful (${response.code}), falling back to text")
        return null
      }

      val respJson = JSONObject(response.body?.string() ?: "{}")
      return parseGeminiResponse(respJson)
    } catch (e: Exception) {
      Log.w(TAG, "Audio modality failed, fallback to text", e)
      return null
    }
  }

  private fun tryGenerateText(
    prompt: String,
    apiKey: String
  ): String {
    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
    val requestBodyJson = JSONObject().apply {
      val contents = JSONArray().put(
        JSONObject().apply {
          val parts = JSONArray().put(
            JSONObject().put("text", prompt)
          )
          put("parts", parts)
        }
      )
      put("contents", contents)

      val systemInstruction = JSONObject().apply {
        val parts = JSONArray().put(
          JSONObject().put("text", "You are MYRAA, a warm, futuristic, intelligent voice AI companion. Respond directly and conversationally in 1 to 2 engaging sentences as if speaking aloud to the user.")
        )
        put("parts", parts)
      }
      put("systemInstruction", systemInstruction)
    }

    val request = Request.Builder()
      .url(url)
      .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
      .build()

    val response = okHttpClient.newCall(request).execute()
    if (!response.isSuccessful) {
      val errBody = response.body?.string() ?: ""
      throw Exception("Gemini API HTTP ${response.code}: $errBody")
    }

    val respJson = JSONObject(response.body?.string() ?: "{}")
    val candidates = respJson.optJSONArray("candidates")
    if (candidates != null && candidates.length() > 0) {
      val candidate = candidates.getJSONObject(0)
      val content = candidate.optJSONObject("content")
      val parts = content?.optJSONArray("parts")
      if (parts != null) {
        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
          val part = parts.getJSONObject(i)
          if (part.has("text")) {
            sb.append(part.optString("text"))
          }
        }
        return sb.toString().trim()
      }
    }
    return ""
  }

  private fun parseGeminiResponse(respJson: JSONObject): VoiceResponse {
    val candidates = respJson.optJSONArray("candidates")
    var transcript = ""
    var pcmBytes: ByteArray? = null
    var sampleRate = 24000

    if (candidates != null && candidates.length() > 0) {
      val candidate = candidates.getJSONObject(0)
      val content = candidate.optJSONObject("content")
      val parts = content?.optJSONArray("parts")
      if (parts != null) {
        for (i in 0 until parts.length()) {
          val part = parts.getJSONObject(i)
          if (part.has("text")) {
            transcript += part.optString("text")
          }
          if (part.has("inlineData")) {
            val inlineData = part.getJSONObject("inlineData")
            val mimeType = inlineData.optString("mimeType", "")
            sampleRate = if (mimeType.contains("rate=16000")) 16000 else 24000
            val dataB64 = inlineData.optString("data", "")
            if (dataB64.isNotEmpty()) {
              pcmBytes = Base64.decode(dataB64, Base64.DEFAULT)
            }
          }
        }
      }
    }

    return VoiceResponse(transcript = transcript.trim(), pcmAudio = pcmBytes, sampleRate = sampleRate)
  }

  companion object {
    private const val TAG = "GeminiVoiceRestService"
  }
}
