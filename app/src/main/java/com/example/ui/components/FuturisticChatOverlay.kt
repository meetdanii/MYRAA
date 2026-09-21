package com.example.ui.components

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSender
import com.example.data.model.VoiceState
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Slide-up futuristic text chat overlay.
 * Allows typing text prompts to MYRAA with direct audio speech responses.
 */
@Composable
fun FuturisticChatOverlay(
  isOpen: Boolean,
  messages: List<ChatMessage>,
  voiceState: VoiceState,
  onSendMessage: (String) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  // Auto-scroll to latest message
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  AnimatedVisibility(
    visible = isOpen,
    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    modifier = modifier
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.68f)
        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
      color = CharcoalDark.copy(alpha = 0.96f)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .imePadding()
      ) {
        // 1. HEADER
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(CharcoalSurface.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (voiceState == VoiceState.SPEAKING) NeonPink else NeonCyan)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "TEXT CHAT • VOICE RESPONSE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = NeonCyanLight,
                letterSpacing = 1.sp
              )
              Text(
                text = "MYRAA will speak her answers out loud",
                fontSize = 11.sp,
                color = TextMuted
              )
            }
          }

          IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("chat_close_button")
          ) {
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = "Collapse chat",
              tint = TextSecondary
            )
          }
        }

        // 2. CONVERSATION MESSAGE LIST
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          if (messages.isEmpty()) {
            // Empty state placeholder
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(NeonViolet.copy(alpha = 0.15f))
                  .border(1.dp, NeonViolet.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.VolumeUp,
                  contentDescription = null,
                  tint = NeonVioletLight,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Type below to speak with MYRAA",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "She answers all typed questions via voice speech",
                color = TextMuted,
                fontSize = 12.sp
              )

              Spacer(modifier = Modifier.height(16.dp))
              // Quick suggestion prompt chips
              val suggestions = listOf(
                "Who are you?",
                "Tell me an interesting science fact",
                "Give me a futuristic greeting"
              )
              suggestions.forEach { prompt ->
                AssistChip(
                  onClick = { onSendMessage(prompt) },
                  label = {
                    Text(
                      text = prompt,
                      fontSize = 11.sp,
                      color = NeonCyanLight
                    )
                  },
                  colors = AssistChipDefaults.assistChipColors(
                    containerColor = CharcoalCard.copy(alpha = 0.7f),
                    labelColor = NeonCyanLight
                  ),
                  border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = NeonCyan.copy(alpha = 0.25f)
                  ),
                  modifier = Modifier.padding(vertical = 2.dp)
                )
              }
            }
          } else {
            LazyColumn(
              state = listState,
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
              contentPadding = PaddingValues(vertical = 12.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(messages, key = { it.id }) { msg ->
                ChatMessageItem(message = msg)
              }
            }
          }
        }

        // 3. INPUT BAR
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(CharcoalSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = {
              Text(
                text = "Type to chat (MYRAA will speak)...",
                fontSize = 13.sp,
                color = TextMuted
              )
            },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedContainerColor = CharcoalCard,
              unfocusedContainerColor = CharcoalCard,
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = Color(0x3306B6D4)
            ),
            shape = RoundedCornerShape(24.dp),
            maxLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
              onSend = {
                if (inputText.isNotBlank()) {
                  onSendMessage(inputText)
                  inputText = ""
                }
              }
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("chat_text_input")
          )

          Spacer(modifier = Modifier.width(8.dp))

          // Send button
          IconButton(
            onClick = {
              if (inputText.isNotBlank()) {
                onSendMessage(inputText)
                inputText = ""
              }
            },
            enabled = inputText.isNotBlank(),
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(
                if (inputText.isNotBlank()) {
                  Brush.linearGradient(listOf(NeonCyan, NeonViolet))
                } else {
                  Brush.linearGradient(listOf(Color(0xFF27272A), Color(0xFF27272A)))
                }
              )
              .testTag("chat_send_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send text message",
              tint = if (inputText.isNotBlank()) Color.White else TextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
  val isUser = message.sender == ChatSender.USER

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      // Small MYRAA avatar dot
      Box(
        modifier = Modifier
          .padding(top = 4.dp, end = 6.dp)
          .size(24.dp)
          .clip(CircleShape)
          .background(NeonViolet.copy(alpha = 0.25f))
          .border(1.dp, NeonViolet, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.VolumeUp,
          contentDescription = null,
          tint = NeonVioletLight,
          modifier = Modifier.size(12.dp)
        )
      }
    }

    Column(
      horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
      modifier = Modifier.fillMaxWidth(0.85f)
    ) {
      Box(
        modifier = Modifier
          .clip(
            RoundedCornerShape(
              topStart = 16.dp,
              topEnd = 16.dp,
              bottomStart = if (isUser) 16.dp else 4.dp,
              bottomEnd = if (isUser) 4.dp else 16.dp
            )
          )
          .background(
            if (isUser) {
              Brush.linearGradient(
                listOf(
                  Color(0xFF0284C7),
                  Color(0xFF0891B2)
                )
              )
            } else {
              Brush.linearGradient(
                listOf(
                  Color(0xFF1E1B4B),
                  Color(0xFF2E1065)
                )
              )
            }
          )
          .border(
            width = 1.dp,
            color = if (isUser) NeonCyan.copy(alpha = 0.4f) else NeonViolet.copy(alpha = 0.5f),
            shape = RoundedCornerShape(
              topStart = 16.dp,
              topEnd = 16.dp,
              bottomStart = if (isUser) 16.dp else 4.dp,
              bottomEnd = if (isUser) 4.dp else 16.dp
            )
          )
          .padding(horizontal = 14.dp, vertical = 10.dp)
      ) {
        Column {
          if (!isUser) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(bottom = 4.dp)
            ) {
              Text(
                text = "MYRAA (Speaking)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = NeonPink,
                letterSpacing = 0.5.sp
              )
            }
          }
          Text(
            text = message.text,
            fontSize = 14.sp,
            color = if (isUser) Color.White else Color(0xFFF1F5F9),
            lineHeight = 20.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = message.formattedTime,
        fontSize = 10.sp,
        color = TextMuted,
        modifier = Modifier.padding(horizontal = 4.dp)
      )
    }
  }
}
