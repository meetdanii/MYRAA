package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.ApiKeySetupScreen
import com.example.ui.screens.GoogleSignInScreen
import com.example.ui.screens.MyraaMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MyraaViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: MyraaViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    handleIncomingIntent(intent)

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MyraaApp(viewModel = viewModel)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleIncomingIntent(intent)
  }

  private fun handleIncomingIntent(intent: Intent?) {
    val data = intent?.data ?: return
    viewModel.authManager.handleOAuthRedirect(data)
  }
}

/**
 * Main application navigation coordinator:
 * 1. Google Sign-In Screen (Continue with Google)
 * 2. API Key Screen (Enter Gemini API Key)
 * 3. MYRAA Main Screen (Futuristic Voice AI Companion)
 */
@Composable
fun MyraaApp(viewModel: MyraaViewModel) {
  val userProfile by viewModel.userProfile.collectAsState()
  val apiKey by viewModel.geminiApiKey.collectAsState()
  val backendUrl by viewModel.backendUrl.collectAsState()

  // Track whether the user has confirmed entry past the API Key screen for this session
  var hasConfirmedApiKeyScreen by remember { mutableStateOf(false) }

  val showApiKeyScreen = userProfile.isSignedIn && (apiKey.isBlank() && backendUrl.isBlank() || !hasConfirmedApiKeyScreen)

  Crossfade(
    targetState = when {
      !userProfile.isSignedIn -> AppScreen.SIGN_IN
      showApiKeyScreen -> AppScreen.API_KEY
      else -> AppScreen.MAIN
    },
    label = "appScreenTransition"
  ) { screen ->
    when (screen) {
      AppScreen.SIGN_IN -> {
        GoogleSignInScreen(
          onGoogleSignInSuccess = { name, email ->
            viewModel.signInWithGoogle(name, email)
          },
          onLaunchOAuthFlow = {
            // Can be configured with Cloud Console Client ID
            viewModel.authManager.initiateGoogleOAuth("myraa-client-id")
          }
        )
      }

      AppScreen.API_KEY -> {
        ApiKeySetupScreen(
          userProfile = userProfile,
          initialApiKey = apiKey,
          initialBackendUrl = backendUrl,
          onSaveAndEnter = { key, backend ->
            viewModel.saveApiKey(key)
            viewModel.saveBackendUrl(backend)
            hasConfirmedApiKeyScreen = true
          },
          onSignOut = {
            viewModel.logOut()
            hasConfirmedApiKeyScreen = false
          }
        )
      }

      AppScreen.MAIN -> {
        MyraaMainScreen(
          viewModel = viewModel,
          onLogOut = {
            hasConfirmedApiKeyScreen = false
          }
        )
      }
    }
  }
}

private enum class AppScreen {
  SIGN_IN,
  API_KEY,
  MAIN
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

