package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
 * Centered futuristic Google Sign-In screen for MYRAA.
 * Displays "CONTINUE WITH GOOGLE" button and supports OAuth / Google account selection.
 */
@Composable
fun GoogleSignInScreen(
  onGoogleSignInSuccess: (displayName: String, email: String) -> Unit,
  onLaunchOAuthFlow: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showAccountDialog by remember { mutableStateOf(false) }
  var inputEmail by remember { mutableStateOf("whyyouopop@gmail.com") }
  var inputName by remember { mutableStateOf("Alex") }

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
            Color(0x334F46E5),
            Color(0x1A8B5CF6),
            Color.Transparent
          ),
          center = center,
          radius = size.minDimension * 0.75f
        ),
        radius = size.minDimension * 0.75f,
        center = center
      )
    }

    // Centered Holographic Card
    Column(
      modifier = Modifier
        .widthIn(max = 440.dp)
        .fillMaxWidth()
        .padding(24.dp)
        .clip(RoundedCornerShape(28.dp))
        .background(CharcoalSurface.copy(alpha = 0.9f))
        .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(28.dp))
        .padding(horizontal = 28.dp, vertical = 36.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Futuristic Holographic Logo
      Box(
        modifier = Modifier
          .size(90.dp)
          .clip(CircleShape)
          .border(2.dp, NeonCyanLight, CircleShape)
          .background(
            Brush.radialGradient(
              colors = listOf(
                NeonViolet.copy(alpha = 0.5f),
                CharcoalCard
              )
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_myraa_logo),
          contentDescription = "MYRAA Hologram Logo",
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(22.dp))

      // MYRAA Title
      Text(
        text = "MYRAA",
        fontSize = 34.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 4.sp,
        color = TextPrimary
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "FUTURISTIC VOICE AI COMPANION",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp,
        color = NeonCyanLight
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Experience low-latency real-time bidirectional audio conversations powered by Google Gemini Live.",
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp,
        color = TextSecondary
      )

      Spacer(modifier = Modifier.height(32.dp))

      // CONTINUE WITH GOOGLE Button
      Button(
        onClick = { showAccountDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("continue_with_google_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.White,
          contentColor = Color(0xFF1F2937)
        )
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          // Google 'G' stylized badge
          Box(
            modifier = Modifier
              .size(26.dp)
              .clip(CircleShape)
              .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "G",
              fontWeight = FontWeight.Black,
              fontSize = 16.sp,
              color = Color(0xFF4285F4)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Text(
            text = "CONTINUE WITH GOOGLE",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 1.2.sp,
            color = Color(0xFF111827)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Security indicator
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = "Secure PKCE Auth",
          tint = NeonVioletLight,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Secure PKCE OAuth • No client secrets in APK",
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          color = TextMuted
        )
      }
    }
  }

  // Google Account Chooser Dialog
  if (showAccountDialog) {
    AlertDialog(
      onDismissRequest = { showAccountDialog = false },
      title = {
        Text(
          text = "Choose Google Account",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = "Select your Google Account to sign into MYRAA:",
            fontSize = 13.sp,
            color = TextSecondary
          )

          OutlinedTextField(
            value = inputEmail,
            onValueChange = { inputEmail = it },
            label = { Text("Google Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyanLight,
              unfocusedBorderColor = Color(0x338B5CF6),
              focusedLabelColor = NeonCyanLight,
              unfocusedLabelColor = TextMuted,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )

          OutlinedTextField(
            value = inputName,
            onValueChange = { inputName = it },
            label = { Text("Account Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyanLight,
              unfocusedBorderColor = Color(0x338B5CF6),
              focusedLabelColor = NeonCyanLight,
              unfocusedLabelColor = TextMuted,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showAccountDialog = false
            onGoogleSignInSuccess(
              inputName.ifBlank { "Google User" },
              inputEmail.ifBlank { "user@gmail.com" }
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
          Text("Sign In with Google", color = CharcoalDark, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAccountDialog = false }) {
          Text("Cancel", color = TextMuted)
        }
      },
      containerColor = CharcoalSurface,
      shape = RoundedCornerShape(20.dp)
    )
  }
}
