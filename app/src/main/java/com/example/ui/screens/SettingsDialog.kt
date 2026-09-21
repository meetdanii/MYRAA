package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight
import com.example.ui.theme.NeonVioletLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsDialog(
  currentApiKey: String,
  currentBackendUrl: String,
  currentVoiceName: String,
  currentCaptionTimeout: Int,
  onSave: (apiKey: String, backendUrl: String, voiceName: String, captionTimeout: Int) -> Unit,
  onDismiss: () -> Unit
) {
  var apiKey by remember { mutableStateOf(currentApiKey) }
  var backendUrl by remember { mutableStateOf(currentBackendUrl) }
  var selectedVoice by remember { mutableStateOf(currentVoiceName) }
  var selectedTimeout by remember { mutableStateOf(currentCaptionTimeout) }

  val availableVoices = listOf("Aoede", "Puck", "Charon", "Kore", "Fenrir")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Settings",
            tint = NeonCyanLight,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "MYRAA SETTINGS",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.8.sp,
            color = TextPrimary
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = TextMuted
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Gemini API Key Field
        OutlinedTextField(
          value = apiKey,
          onValueChange = { apiKey = it },
          label = { Text("Gemini API Key") },
          placeholder = { Text("AIzaSy...", color = TextMuted) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyanLight,
            unfocusedBorderColor = Color(0x338B5CF6),
            focusedLabelColor = NeonCyanLight,
            unfocusedLabelColor = TextMuted,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        // Backend URL Field
        OutlinedTextField(
          value = backendUrl,
          onValueChange = { backendUrl = it },
          label = { Text("Custom Backend URL (Optional)") },
          placeholder = { Text("https://my-backend.app", color = TextMuted) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyanLight,
            unfocusedBorderColor = Color(0x338B5CF6),
            focusedLabelColor = NeonCyanLight,
            unfocusedLabelColor = TextMuted,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        // Voice Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "MYRAA VOICE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = NeonVioletLight
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            availableVoices.take(3).forEach { voice ->
              VoiceChip(
                name = voice,
                isSelected = selectedVoice == voice,
                onClick = { selectedVoice = voice },
                modifier = Modifier.weight(1f)
              )
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            availableVoices.drop(3).forEach { voice ->
              VoiceChip(
                name = voice,
                isSelected = selectedVoice == voice,
                onClick = { selectedVoice = voice },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Caption Timeout
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "CAPTION FADE DURATION",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = NeonVioletLight
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(3, 6, 10).forEach { sec ->
              VoiceChip(
                name = "${sec}s",
                isSelected = selectedTimeout == sec,
                onClick = { selectedTimeout = sec },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Audio Specs Info
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalCard)
            .padding(12.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "AUDIO ENGINE SPECIFICATIONS",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = NeonCyanLight
            )
            Text(
              text = "• Input: 16 kHz Mono 16-bit PCM streaming\n• Output: 24 kHz Low-latency AudioTrack\n• Model: gemini-2.5-flash-native-audio-preview / 3.1",
              fontSize = 11.sp,
              color = TextMuted,
              lineHeight = 16.sp
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(apiKey, backendUrl, selectedVoice, selectedTimeout)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
        modifier = Modifier.testTag("save_settings_button")
      ) {
        Text("Save Settings", color = CharcoalSurface, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextMuted)
      }
    },
    containerColor = CharcoalSurface,
    shape = RoundedCornerShape(24.dp)
  )
}

@Composable
private fun VoiceChip(
  name: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CharcoalCard)
      .border(
        1.dp,
        if (isSelected) NeonCyan else Color(0x228B5CF6),
        RoundedCornerShape(10.dp)
      )
      .clickable(onClick = onClick)
      .padding(vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = name,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = if (isSelected) NeonCyanLight else TextSecondary
    )
  }
}
