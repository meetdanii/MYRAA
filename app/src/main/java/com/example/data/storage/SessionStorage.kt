package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionStorage(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("myraa_secure_session", Context.MODE_PRIVATE)

  private val _userProfile = MutableStateFlow(loadUserProfile())
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  private val _geminiApiKey = MutableStateFlow(loadApiKey())
  val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

  private val _backendUrl = MutableStateFlow(prefs.getString(KEY_BACKEND_URL, "") ?: "")
  val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

  private val _voiceName = MutableStateFlow(prefs.getString(KEY_VOICE_NAME, "Aoede") ?: "Aoede")
  val voiceName: StateFlow<String> = _voiceName.asStateFlow()

  private val _captionTimeoutSeconds =
    MutableStateFlow(prefs.getInt(KEY_CAPTION_TIMEOUT, 6))
  val captionTimeoutSeconds: StateFlow<Int> = _captionTimeoutSeconds.asStateFlow()

  private fun loadUserProfile(): UserProfile {
    val isSignedIn = prefs.getBoolean(KEY_IS_SIGNED_IN, false)
    val name = prefs.getString(KEY_DISPLAY_NAME, "Explorer") ?: "Explorer"
    val email = prefs.getString(KEY_EMAIL, "") ?: ""
    val photo = prefs.getString(KEY_PHOTO_URL, null)
    return UserProfile(displayName = name, email = email, photoUrl = photo, isSignedIn = isSignedIn)
  }

  private fun loadApiKey(): String {
    val stored = prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
    if (stored.isNotBlank()) return stored
    // Check BuildConfig if defined and not placeholder
    val buildKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Throwable) {
      ""
    }
    return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
  }

  fun saveUserProfile(profile: UserProfile) {
    prefs.edit()
      .putBoolean(KEY_IS_SIGNED_IN, profile.isSignedIn)
      .putString(KEY_DISPLAY_NAME, profile.displayName)
      .putString(KEY_EMAIL, profile.email)
      .putString(KEY_PHOTO_URL, profile.photoUrl)
      .apply()
    _userProfile.value = profile
  }

  fun saveApiKey(key: String) {
    val trimmed = key.trim()
    prefs.edit().putString(KEY_GEMINI_API_KEY, trimmed).apply()
    _geminiApiKey.value = trimmed
  }

  fun saveBackendUrl(url: String) {
    val trimmed = url.trim()
    prefs.edit().putString(KEY_BACKEND_URL, trimmed).apply()
    _backendUrl.value = trimmed
  }

  fun saveVoiceName(name: String) {
    prefs.edit().putString(KEY_VOICE_NAME, name).apply()
    _voiceName.value = name
  }

  fun saveCaptionTimeout(seconds: Int) {
    prefs.edit().putInt(KEY_CAPTION_TIMEOUT, seconds).apply()
    _captionTimeoutSeconds.value = seconds
  }

  fun clearSession() {
    prefs.edit()
      .remove(KEY_IS_SIGNED_IN)
      .remove(KEY_DISPLAY_NAME)
      .remove(KEY_EMAIL)
      .remove(KEY_PHOTO_URL)
      .apply()
    _userProfile.value = UserProfile(isSignedIn = false)
  }

  companion object {
    private const val KEY_IS_SIGNED_IN = "is_signed_in"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_EMAIL = "email"
    private const val KEY_PHOTO_URL = "photo_url"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    private const val KEY_BACKEND_URL = "backend_url"
    private const val KEY_VOICE_NAME = "voice_name"
    private const val KEY_CAPTION_TIMEOUT = "caption_timeout"
  }
}
