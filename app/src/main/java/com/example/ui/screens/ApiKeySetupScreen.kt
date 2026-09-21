package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Screen 2: Gemini API Key setup screen.
 * Displays after "Continue with Google" so the user can enter their Gemini API key,
 * and then MYRAA comes!
 */
@Composable
fun ApiKeySetupScreen(
  userProfile: UserProfile,
  initialApiKey: String,
  initialBackendUrl: String,
  onSaveAndEnter: (apiKey: String, backendUrl: String) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  var apiKeyInput by remember { mutableStateOf(initialApiKey) }
  var backendUrlInput by remember { mutableStateOf(initialBackendUrl) }
  var isKeyVisible by remember { mutableStateOf(false) }
  var showAdvancedBackend by remember { mutableStateOf(initialBackendUrl.isNotBlank()) }
  var inputError by remember { mutableStateOf<String?>(null) }

  val clipboardManager = LocalClipboardManager.current

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CharcoalDark),
    contentAlignment = Alignment.Center
  ) {
    // Ambient cosmic sci-fi background aura
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0x2B06B6D4),
            Color(0x1F8B5CF6),
            Color.Transparent
          ),
          center = center,
          radius = size.minDimension * 0.75f
        ),
        radius = size.minDimension * 0.75f,
        center = center
      )
    }

    Column(
      modifier = Modifier
        .widthIn(max = 480.dp)
        .fillMaxWidth()
        .padding(20.dp)
        .verticalScroll(rememberScrollState())
        .clip(RoundedCornerShape(28.dp))
        .background(CharcoalSurface.copy(alpha = 0.92f))
        .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(28.dp))
        .padding(horizontal = 26.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Signed-in header chip
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(CharcoalCard)
          .border(0.8.dp, NeonCyanLight.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
          .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = "Signed In",
          tint = NeonCyan,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = "Signed in as: ${userProfile.displayName}",
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace,
          color = TextSecondary
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Key Icon Emblem
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(Brush.radialGradient(listOf(NeonCyan.copy(alpha = 0.25f), CharcoalCard)))
          .border(1.dp, NeonCyanLight, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Key,
          contentDescription = "API Key",
          tint = NeonCyanLight,
          modifier = Modifier.size(32.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "ENTER GEMINI API KEY",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp,
        color = TextPrimary
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Enter your Google Gemini API key to activate MYRAA's real-time voice connection.",
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp,
        color = TextSecondary
      )

      Spacer(modifier = Modifier.height(22.dp))

      // API Key TextField
      OutlinedTextField(
        value = apiKeyInput,
        onValueChange = {
          apiKeyInput = it
          inputError = null
        },
        label = { Text("Gemini API Key") },
        placeholder = { Text("AIzaSy...", color = TextMuted) },
        singleLine = true,
        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
          onDone = {
            if (apiKeyInput.isNotBlank() || backendUrlInput.isNotBlank()) {
              onSaveAndEnter(apiKeyInput, backendUrlInput)
            } else {
              inputError = "Please enter an API Key to continue."
            }
          }
        ),
        trailingIcon = {
          Row {
            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
              Icon(
                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (isKeyVisible) "Hide Key" else "Show Key",
                tint = NeonVioletLight
              )
            }
          }
        },
        isError = inputError != null,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("api_key_input_field"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = NeonCyanLight,
          unfocusedBorderColor = Color(0x338B5CF6),
          focusedLabelColor = NeonCyanLight,
          unfocusedLabelColor = TextMuted,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          cursorColor = NeonCyanLight
        )
      )

      // Quick Paste Helper
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.End
      ) {
        Text(
          text = "PASTE FROM CLIPBOARD",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = NeonCyanLight,
          modifier = Modifier
            .clickable {
              val text = clipboardManager.getText()?.text
              if (!text.isNullOrBlank()) {
                apiKeyInput = text.trim()
                inputError = null
              }
            }
            .padding(4.dp)
        )
      }

      if (inputError != null) {
        Text(
          text = inputError!!,
          fontSize = 12.sp,
          color = Color(0xFFEF4444),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Advanced Backend Server Toggle (Optional)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showAdvancedBackend = !showAdvancedBackend }
          .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Optional: Custom Backend Server",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = TextSecondary
        )
        Icon(
          imageVector = if (showAdvancedBackend) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = "Toggle Backend Settings",
          tint = TextSecondary,
          modifier = Modifier.size(20.dp)
        )
      }

      AnimatedVisibility(visible = showAdvancedBackend) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = backendUrlInput,
            onValueChange = { backendUrlInput = it },
            label = { Text("Backend URL (Optional)") },
            placeholder = { Text("https://my-backend.app", color = TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyanLight,
              unfocusedBorderColor = Color(0x338B5CF6),
              focusedLabelColor = NeonCyanLight,
              unfocusedLabelColor = TextMuted,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )
          Text(
            text = "If using a proxy server that stores the Gemini API key, leave the API key blank and enter the backend URL here.",
            fontSize = 11.sp,
            color = TextMuted,
            lineHeight = 15.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // ENTER MYRAA Button
      Button(
        onClick = {
          if (apiKeyInput.isNotBlank() || backendUrlInput.isNotBlank()) {
            onSaveAndEnter(apiKeyInput, backendUrlInput)
          } else {
            inputError = "Please enter your Gemini API Key."
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("enter_myraa_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = NeonCyan,
          contentColor = CharcoalDark
        )
      ) {
        Text(
          text = "ENTER MYRAA",
          fontWeight = FontWeight.ExtraBold,
          fontSize = 15.sp,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 2.sp,
          color = CharcoalDark
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Sign Out / Switch Account Button
      TextButton(
        onClick = onSignOut,
        modifier = Modifier.testTag("switch_google_account_button")
      ) {
        Text(
          text = "Sign Out / Change Account",
          fontSize = 12.sp,
          color = TextMuted
        )
      }
    }
  }
}
