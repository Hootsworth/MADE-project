package com.afterlight.madeproject.ui

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.BuildConfig
import com.afterlight.madeproject.ui.components.BrutalistButton
import com.afterlight.madeproject.ui.components.BrutalistTextField
import com.afterlight.madeproject.ui.theme.*
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun AuthScreen(
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PAPERLIKE.",
                    style = GatherTypography.displayLarge,
                    color = Coal
                )
                Text(
                    text = "ACCESS PANEL // IDENTIFICATION",
                    style = GatherTypography.labelMedium,
                    color = LightTextMuted
                )
            }
        }

        item {
            // Brutalist Login Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(backgroundColor = Pearl, shadowOffset = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "ENTER CREDENTIALS",
                        style = GatherTypography.labelLarge,
                        color = Coal
                    )

                    BrutalistTextField(
                        value = state.name,
                        onValueChange = viewModel::updateName,
                        label = "Full Name (Sign Up Only)"
                    )

                    BrutalistTextField(
                        value = state.email,
                        onValueChange = viewModel::updateEmail,
                        label = "College Email"
                    )

                    BrutalistTextField(
                        value = state.password,
                        onValueChange = viewModel::updatePassword,
                        label = "Password"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        BrutalistButton(
                            text = "Log In",
                            onClick = viewModel::login,
                            modifier = Modifier.weight(1f)
                        )
                        BrutalistButton(
                            text = "Sign Up",
                            onClick = viewModel::signUp,
                            backgroundColor = Sand,
                            textColor = Coal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HorizontalDivider(thickness = 4.dp, color = Coal)
                Text(
                    text = "OR CONTINUE WITH",
                    style = GatherTypography.labelMedium,
                    color = Coal
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Google sign-in — premium icon button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .neoBrutalism(backgroundColor = Snow, shadowOffset = 6.dp)
                            .clickable(enabled = webClientId.isNotBlank()) {
                                val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(webClientId)
                                    .requestEmail()
                                    .build()
                                val signInClient = GoogleSignIn.getClient(context, options)
                                googleLauncher.launch(signInClient.signInIntent)
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = "https://img.icons8.com/color/48/000000/google-logo.png",
                                contentDescription = "Google",
                                modifier = Modifier.size(24.dp)
                            )
                            Text("GOOGLE", style = GatherTypography.labelLarge, color = Coal)
                        }
                    }

                    // Developer test mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .neoBrutalism(backgroundColor = Slate, shadowOffset = 6.dp)
                            .clickable { viewModel.continueInTestMode() }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("TEST MODE", style = GatherTypography.labelLarge, color = Snow)
                    }
                }
            }
        }

        item {
            state.error?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalism(backgroundColor = Wine, shadowOffset = 4.dp)
                        .padding(16.dp)
                ) {
                    Text(text = "ERROR: $it", style = GatherTypography.labelMedium, color = Snow)
                }
            }

            if (state.emailVerified) {
                Spacer(modifier = Modifier.height(16.dp))
                BrutalistButton(
                    text = "COMPLETE SETUP",
                    onClick = onProfileSetup,
                    backgroundColor = Moss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}