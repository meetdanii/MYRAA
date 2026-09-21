package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ChatSender {
  USER,
  MYRAA
}

data class ChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val sender: ChatSender,
  val text: String,
  val timestampMillis: Long = System.currentTimeMillis(),
  val isAudioResponse: Boolean = true
) {
  val formattedTime: String
    get() {
      val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
      return sdf.format(Date(timestampMillis))
    }
}
