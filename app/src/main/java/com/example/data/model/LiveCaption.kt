package com.example.data.model

/**
 * Holds separate live captions for the user and MYRAA.
 */
data class LiveCaption(
  val userText: String = "",
  val modelText: String = "",
  val userTimestampMillis: Long = 0L,
  val modelTimestampMillis: Long = 0L,
  val isUserVisible: Boolean = false,
  val isModelVisible: Boolean = false
)
