package com.afterlight.madeproject.ui

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.BuildConfig
import com.afterlight.madeproject.ui.components.SmoothButton
import com.afterlight.madeproject.ui.components.SmoothTextField
import com.afterlight.madeproject.ui.theme.*
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    onProfileSetup: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (!idToken.isNullOrBlank()) {
                viewModel.signInWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            Log.w("AuthScreen", "Google sign-in failed", e)
        }
    }

    LaunchedEffect(state.nextAction) {
        when (state.nextAction) {
            AuthNextAction.HOME -> {
                viewModel.setNextAction(null)
                onAuthenticated()
            }
            AuthNextAction.PROFILE_SETUP -> {
                viewModel.setNextAction(null)
                onProfileSetup()
            }
            null -> Unit
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .imePadding()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(bottom = 48.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Paperlike.",
                    style = GatherTypography.displayLarge,
                    color = Coal
                )
                Text(
                    text = if (state.mode == AuthMode.LOGIN) {
                        "Welcome back • Sign in"
                    } else {
                        "New account • Create profile"
                    },
                    style = GatherTypography.bodyLarge,
                    color = LightTextMuted
                )

                // Smooth Toggle Switch
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Pearl.copy(alpha = 0.4f)
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        SmoothButton(
                            text = "Log In",
                            onClick = { viewModel.setMode(AuthMode.LOGIN) },
                            containerColor = if (state.mode == AuthMode.LOGIN) Pearl else Color.Transparent,
                            contentColor = if (state.mode == AuthMode.LOGIN) Coal else LightTextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        SmoothButton(
                            text = "Sign Up",
                            onClick = { viewModel.setMode(AuthMode.SIGN_UP) },
                            containerColor = if (state.mode == AuthMode.SIGN_UP) Pearl else Color.Transparent,
                            contentColor = if (state.mode == AuthMode.SIGN_UP) Coal else LightTextMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Pearl),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = if (state.mode == AuthMode.LOGIN) "Login to your account" else "Create your account",
                        style = GatherTypography.titleLarge,
                        color = Coal
                    )

                    if (state.mode == AuthMode.SIGN_UP) {
                        SmoothTextField(
                            value = state.name,
                            onValueChange = viewModel::updateName,
                            label = "Full Name"
                        )
                    }

                    SmoothTextField(
                        value = state.email,
                        onValueChange = viewModel::updateEmail,
                        label = "College Email"
                    )

                    SmoothTextField(
                        value = state.password,
                        onValueChange = viewModel::updatePassword,
                        label = "Password"
                    )

                    if (state.mode == AuthMode.SIGN_UP) {
                        SmoothTextField(
                            value = state.confirmPassword,
                            onValueChange = viewModel::updateConfirmPassword,
                            label = "Confirm Password"
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    SmoothButton(
                        text = if (state.mode == AuthMode.LOGIN) "Log In" else "Create Account",
                        onClick = if (state.mode == AuthMode.LOGIN) viewModel::login else viewModel::signUp,
                        containerColor = if (state.mode == AuthMode.LOGIN) Coal else Moss,
                        contentColor = Snow,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Pearl)
                    Text(
                        text = "Or continue with",
                        style = GatherTypography.bodyMedium,
                        color = LightTextMuted
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Pearl)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Google Sign-In
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(enabled = webClientId.isNotBlank()) {
                                val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(webClientId)
                                    .requestEmail()
                                    .build()
                                val signInClient = GoogleSignIn.getClient(context, options)
                                googleLauncher.launch(signInClient.signInIntent)
                            },
                        color = Pearl,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            AsyncImage(
                                model = "https://img.icons8.com/color/48/000000/google-logo.png",
                                contentDescription = "Google",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Google", style = GatherTypography.labelLarge, color = Coal)
                        }
                    }

                    // Developer Test Mode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.continueInTestMode() },
                        color = Slate.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("Test Mode", style = GatherTypography.labelLarge, color = Slate)
                        }
                    }
                }
            }
        }

        item {
            state.error?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Wine.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Error: $it",
                        style = GatherTypography.bodyMedium,
                        color = Wine,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}