package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LiveCaption
import com.example.ui.theme.NeonCyanLight
import com.example.ui.theme.NeonVioletLight

/**
 * Centered live captions displaying user speech in Cyan and MYRAA speech in White/Light Violet.
 * Sharp and readable without per-chunk blur.
 * Fades in when new text begins, fades out after timeout.
 */
@Composable
fun LiveCaptionDisplay(
  caption: LiveCaption,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. User Speech Caption (Cyan)
    AnimatedVisibility(
      visible = caption.isUserVisible && caption.userText.isNotBlank(),
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Box(
        modifier = Modifier
          .widthIn(max = 540.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0x2B06B6D4))
          .border(1.dp, NeonCyanLight.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
          .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "YOU SAID",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.8.sp,
            color = NeonCyanLight.copy(alpha = 0.9f)
          )
          Text(
            text = "“${caption.userText}”",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 23.sp,
            color = NeonCyanLight
          )
        }
      }
    }

    // 2. MYRAA Speech Caption (Light Violet / White)
    AnimatedVisibility(
      visible = caption.isModelVisible && caption.modelText.isNotBlank(),
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Box(
        modifier = Modifier
          .widthIn(max = 560.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0x331E1538))
          .border(1.dp, NeonVioletLight.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
          .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "MYRAA SAID",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            color = NeonVioletLight.copy(alpha = 0.9f)
          )
          Text(
            text = caption.modelText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            lineHeight = 25.sp,
            color = Color(0xFFF1F5F9)
          )
        }
      }
    }
  }
}
