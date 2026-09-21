package com.example.data.model

/**
 * Holds authenticated user profile information.
 */
data class UserProfile(
  val displayName: String = "Explorer",
  val email: String = "user@example.com",
  val photoUrl: String? = null,
  val isSignedIn: Boolean = false
)
