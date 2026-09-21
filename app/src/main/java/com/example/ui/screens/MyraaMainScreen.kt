package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.VoiceState
import com.example.ui.components.CosmicBackgroundParticles
import com.example.ui.components.FuturisticChatOverlay
import com.example.ui.components.FuturisticControls
import com.example.ui.components.HolographicAvatar
import com.example.ui.components.LiveCaptionDisplay
import com.example.ui.components.StatusOrb
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonVioletLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MyraaViewModel

@Composable
fun MyraaMainScreen(
  viewModel: MyraaViewModel,
  onLogOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val userProfile by viewModel.userProfile.collectAsState()
  val voiceState by viewModel.voiceState.collectAsState()
  val liveCaption by viewModel.liveCaption.collectAsState()
  val isMuted by viewModel.isMuted.collectAsState()
  val inputAmplitude by viewModel.inputAmplitude.collectAsState()
  val outputAmplitude by viewModel.outputAmplitude.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val apiKey by viewModel.geminiApiKey.collectAsState()
  val backendUrl by viewModel.backendUrl.collectAsState()
  val voiceName by viewModel.voiceName.collectAsState()
  val captionTimeout by viewModel.captionTimeoutSeconds.collectAsState()
  val chatMessages by viewModel.chatMessages.collectAsState()
  val isChatOpen by viewModel.isChatOpen.collectAsState()

  var showSettingsDialog by remember { mutableStateOf(false) }
  var showScreenShareNotice by remember { mutableStateOf(false) }
  var showLogOutConfirm by remember { mutableStateOf(false) }

  // Microphone permission launcher
  val micPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      viewModel.startVoiceSession()
    }
  }

  val activeAmplitude = when (voiceState) {
    VoiceState.SPEAKING -> outputAmplitude
    VoiceState.LISTENING -> inputAmplitude
    else -> 0f
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CharcoalDark)
  ) {
    // Ambient cosmic glow background
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.radialGradient(
            colors = listOf(
              Color(0x1F8B5CF6),
              Color(0x0F06B6D4),
              CharcoalDark
            ),
            radius = 1200f
          )
        )
    )

    // Ambient floating stardust & cosmic particle layer
    CosmicBackgroundParticles(
      modifier = Modifier.fillMaxSize(),
      tintColor = voiceState.orbColor
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. TOP BAR
      TopBar(
        displayName = userProfile.displayName,
        voiceState = voiceState,
        onOpenSettings = { showSettingsDialog = true },
        onLogOutClick = { showLogOutConfirm = true }
      )

      // Error Banner
      AnimatedVisibility(
        visible = errorMessage != null,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
      ) {
        errorMessage?.let { msg ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(NeonRed.copy(alpha = 0.2f))
              .border(1.dp, NeonRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
              .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = NeonRed,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = msg,
                fontSize = 12.sp,
                color = Color(0xFFFCA5A5),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
            }
            IconButton(
              onClick = { viewModel.clearError() },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss error",
                tint = Color(0xFFFCA5A5),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }

      // 2. CENTER AREA: Holographic Avatar & Live Captions
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Centered Holographic Avatar
          HolographicAvatar(
            voiceState = voiceState,
            audioAmplitude = activeAmplitude,
            size = 230.dp,
            modifier = Modifier.testTag("holographic_avatar")
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Centered Live Captions
          LiveCaptionDisplay(
            caption = liveCaption,
            modifier = Modifier.testTag("live_caption_display")
          )
        }
      }

      // 3. BOTTOM CONTROLS
      FuturisticControls(
        voiceState = voiceState,
        isMuted = isMuted,
        onStartVoice = {
          val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
          ) == PackageManager.PERMISSION_GRANTED

          if (hasPermission) {
            viewModel.startVoiceSession()
          } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
          }
        },
        onStopVoice = { viewModel.stopVoiceSession() },
        onToggleMute = { viewModel.toggleMute() },
        onDisconnect = { viewModel.stopVoiceSession() },
        onScreenShareClick = { showScreenShareNotice = true },
        onToggleChat = { viewModel.toggleChat() }
      )
    }

    // 4. Futuristic Text Chat Overlay (Direct Text Input with Voice Audio Responses)
    FuturisticChatOverlay(
      isOpen = isChatOpen,
      messages = chatMessages,
      voiceState = voiceState,
      onSendMessage = { text -> viewModel.sendTextMessage(text) },
      onClose = { viewModel.setChatOpen(false) },
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .testTag("futuristic_chat_overlay")
    )
  }

  // Settings Dialog
  if (showSettingsDialog) {
    SettingsDialog(
      currentApiKey = apiKey,
      currentBackendUrl = backendUrl,
      currentVoiceName = voiceName,
      currentCaptionTimeout = captionTimeout,
      onSave = { newKey, newBackend, newVoice, newTimeout ->
        viewModel.saveApiKey(newKey)
        viewModel.saveBackendUrl(newBackend)
        viewModel.saveVoiceName(newVoice)
        viewModel.saveCaptionTimeout(newTimeout)
      },
      onDismiss = { showSettingsDialog = false }
    )
  }

  // Screen Share "Coming Soon" Dialog
  if (showScreenShareNotice) {
    AlertDialog(
      onDismissRequest = { showScreenShareNotice = false },
      icon = {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = "Coming Soon",
          tint = NeonCyanLight,
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Text("Screen Sharing", color = TextPrimary, fontWeight = FontWeight.Bold)
      },
      text = {
        Text(
          "Holographic screen-sharing with Gemini Live is coming in the next update. Microphone audio streaming and real-time voice intelligence are currently fully active!",
          color = TextSecondary,
          fontSize = 14.sp,
          lineHeight = 20.sp
        )
      },
      confirmButton = {
        Button(
          onClick = { showScreenShareNotice = false },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
          Text("Got it", color = CharcoalDark, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = CharcoalSurface,
      shape = RoundedCornerShape(20.dp)
    )
  }

  // Log Out Confirmation Dialog
  if (showLogOutConfirm) {
    AlertDialog(
      onDismissRequest = { showLogOutConfirm = false },
      title = {
        Text("Sign Out of MYRAA?", color = TextPrimary, fontWeight = FontWeight.Bold)
      },
      text = {
        Text(
          "Logging out will end your voice session and return to the Google Sign-In screen.",
          color = TextSecondary,
          fontSize = 14.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showLogOutConfirm = false
            viewModel.logOut()
            onLogOut()
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
        ) {
          Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogOutConfirm = false }) {
          Text("Cancel", color = TextMuted)
        }
      },
      containerColor = CharcoalSurface,
      shape = RoundedCornerShape(20.dp)
    )
  }
}

@Composable
private fun TopBar(
  displayName: String,
  voiceState: VoiceState,
  onOpenSettings: () -> Unit,
  onLogOutClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left: Holographic MYRAA Title & Google User Badge
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text(
        text = "MYRAA",
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.5.sp,
        color = TextPrimary
      )

      // User pill
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(CharcoalCard)
          .border(0.8.dp, Color(0x338B5CF6), RoundedCornerShape(12.dp))
          .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(NeonCyanLight)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = displayName,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          color = TextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    // Right: Status Orb, Settings, and Log Out
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // Small animated status orb in top right
      StatusOrb(
        voiceState = voiceState,
        size = 14.dp,
        modifier = Modifier.testTag("status_orb_indicator")
      )

      // Settings Button
      IconButton(
        onClick = onOpenSettings,
        modifier = Modifier
          .size(40.dp)
          .testTag("settings_icon_button")
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = TextSecondary,
          modifier = Modifier.size(20.dp)
        )
      }

      // Log Out Button
      IconButton(
        onClick = onLogOutClick,
        modifier = Modifier
          .size(40.dp)
          .testTag("logout_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ExitToApp,
          contentDescription = "Log Out",
          tint = TextMuted,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
