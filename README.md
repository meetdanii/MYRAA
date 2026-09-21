# MYRAA - Futuristic Voice AI Companion for Android

MYRAA is a native Android application built with **Kotlin**, **Jetpack Compose**, and **Material 3** that connects directly to Google's **Gemini Live API** for real-time, low-latency, bidirectional voice conversations.

---

## 🚀 Key Features

1. **Google Sign-In with PKCE Flow**:
   - Secure authentication flow without shipping client secrets in the APK.
   - Saves profile state securely and displays the signed-in Google account in the futuristic header.
   - Interactive account switcher and full session logout.

2. **Dedicated API Key Setup Screen**:
   - Clean startup sequence: *Sign In with Google* ➔ *Enter Gemini API Key* ➔ *MYRAA Voice Companion*.
   - Injected via `.env` / Secrets Gradle Plugin or entered securely at runtime.
   - Optional custom backend relay toggle for private or enterprise deployments.

3. **Gemini Live Bidirectional Streaming**:
   - Direct WebSockets connection using the official Gemini Live endpoint (`BidiGenerateContent`).
   - Continuous 16 kHz 16-bit mono PCM microphone streaming via `AudioRecord`.
   - Real-time 24 kHz audio playback via low-latency `AudioTrack`.
   - Native audio interruption handling: if you speak while MYRAA is speaking, audio playback stops immediately.

4. **Mesmerizing Holographic Interface & 60 FPS Particle Swarm**:
   - Central animated holographic avatar with concentric glowing orbital rings and reactive cybernetic reticles.
   - Dual-layer particle system: an ambient cosmic background starfield and a voice-reactive particle swarm that dynamically expels glowing particles according to voice amplitude.
   - Voice-reactive dynamics: the avatar's scale and glow pulse organically with audio amplitude during both listening and speaking.
   - Small animated status orb in the top-right corner with 6 distinct states:
     - 🟣 **Idle / Disconnected**: Indigo (`#4F46E5`)
     - 🟡 **Connecting**: Amber (`#F59E0B`)
     - 🔵 **Listening**: Cyan (`#06B6D4`)
     - 🟣 **Thinking**: Violet (`#8B5CF6`)
     - 💖 **Speaking**: Pink / Fuchsia (`#EC4899`)
     - 🔴 **Error**: Red (`#EF4444`)

5. **Text Chat with Direct Voice Answers**:
   - Slide-up futuristic text chat overlay accessible anytime via the chat button in the control bar.
   - Users can type text messages directly, while MYRAA answers back in spoken voice audio!
   - Dual-engine audio delivery: Native Gemini voice model playback (24kHz PCM) with integrated Android Text-to-Speech fallback so she always speaks out loud.
   - Complete message history, timestamp badges, and quick-prompt suggestion chips.

6. **Sharp Live Captions**:
   - Positioned in the center of the screen without per-chunk blur.
   - **YOU SAID**: Distinct Cyan highlight (`#22D3EE`).
   - **MYRAA SAID**: Elegant White / Light Violet highlight (`#F1F5F9` / `#C7D2FE`).
   - Smooth entry fade-in and configurable fade-out timeout (3s, 6s, 10s).

7. **Futuristic Controls**:
   - Primary holographic Voice action button (Start / Stop).
   - Text Chat toggle button for quick typing with spoken voice answers.
   - Instant Microphone Mute / Unmute.
   - Disconnect button.
   - Screen-Share status indicator ("Coming soon" safe indicator).
   - Real-time connection badge.

---

## 🛠️ Architecture Overview

```
                   ┌──────────────────────────────────────┐
                   │           MainActivity.kt            │
                   │        (Handles OAuth redirect)      │
                   └──────────────────┬───────────────────┘
                                      │
                   ┌──────────────────▼───────────────────┐
                   │          MyraaViewModel.kt           │
                   │     (StateFlow, Audio, Sessions)     │
                   └──────────┬───────────────┬───────────┘
                              │               │
      ┌───────────────────────┼───────────────┼───────────────────────┐
      │                       │               │                       │
┌─────▼───────────────┐ ┌─────▼───────────────▼─────┐ ┌───────────────▼─────┐
│  AudioCaptureService│ │     GeminiLiveClient       │ │ AudioPlaybackService│
│  (16 kHz PCM Mic)   │ │(WebSocket BidiGenerate)    │ │(24 kHz AudioTrack)  │
└─────────────────────┘ └───────────────────────────┘ └─────────────────────┘
```

### State Machine
- `DISCONNECTED`: Audio hardware is idle; awaiting user interaction.
- `CONNECTING`: WebSocket handshake initiated with Gemini Live or backend relay.
- `LISTENING`: Mic active, streaming 16 kHz PCM chunks every 100ms.
- `THINKING`: User utterance received; model processing speech synthesis.
- `SPEAKING`: Receiving PCM audio buffers from Gemini Live; playing immediately via `AudioTrack`.
- `ERROR`: Network or permission error displayed with retry action.

---

## 🔑 Gemini API & Configuration

### Direct Mode (Default)
Enter your Gemini API key on the **Gemini API Setup Screen** or in the Settings dialog.
- Recommended Live Model: `gemini-2.5-flash-native-audio-preview-12-2025` or `gemini-3.1-flash-live-preview`.
- Prebuilt Voice Options: **Aoede** (default), **Puck**, **Charon**, **Kore**, **Fenrir**.

### Custom Backend Mode (Optional)
If running a private proxy relay (e.g. Node.js/Go/Python server that protects your API key):
1. In the **Gemini API Setup** or **Settings** dialog, enter your backend URL (e.g. `https://my-proxy.app`).
2. MYRAA will establish a live streaming connection via `wss://my-proxy.app/ws/live`.

---

## 🔐 Google OAuth Configuration for Android

To enable Google OAuth in production:
1. Open [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
2. Create an **OAuth 2.0 Client ID** of type **Android**:
   - **Package Name**: `com.aistudio.myraa.app` (from `app/build.gradle.kts`)
   - **SHA-1 Fingerprint**:
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
3. Set the Redirect URI to `myraa://auth`.
4. *Important*: Client secrets are never shipped in the APK. The app uses standard **PKCE** (Proof Key for Code Exchange) with `S256` hashing.

---

## 📦 Building & Running the App

### Prerequisites
- Android SDK with Min SDK 26 (Android 8.0+) and Target SDK 36.
- Java 17 / 21.

### 1. Run on Android Emulator
1. Start an Android Virtual Device (AVD) from Android Studio.
2. Build and install:
   ```bash
   gradle assembleDebug
   ```
3. Grant microphone permission when prompted.

### 2. Run on Physical Device
1. Enable **Developer Options** and **USB Debugging** on your Android device.
2. Connect via USB or Wireless ADB.
3. Install and launch the generated APK.

### 3. Generate Debug APK
```bash
# Linux / macOS
gradle assembleDebug

# Windows
gradlew.bat assembleDebug
```
Output location:
`app/build/outputs/apk/debug/app-debug.apk`

### 4. Generate Signed Release APK
Configure your release signing keystore in `app/build.gradle.kts` and run:
```bash
# Linux / macOS
gradle assembleRelease

# Windows
gradlew.bat assembleRelease
```
Output location:
`app/build/outputs/apk/release/app-release-unsigned.apk` (or signed APK).

---

## ❓ Troubleshooting

| Issue | Cause | Solution |
|---|---|---|
| **No sound / Mic error** | Missing microphone permission | Go to Android App Info ➔ Permissions ➔ Allow Microphone. |
| **API Error (400 / 403)** | Invalid key or Live API not enabled | Verify your Gemini API key in Google AI Studio; confirm Gemini Live preview model access. |
| **Connection Closed** | Network firewall or WebSocket proxy issue | Ensure outbound WebSockets are permitted, or enter a Custom Backend URL in Settings. |
| **OAuth redirect does not return** | Custom scheme not handled | Ensure `myraa://auth` intent-filter is declared in `AndroidManifest.xml` (already configured). |
