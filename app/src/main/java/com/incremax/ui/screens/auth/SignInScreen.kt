package com.incremax.ui.screens.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.incremax.domain.model.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onSignInComplete: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    isOnboarding: Boolean = true,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    // Log when screen loads
    LaunchedEffect(Unit) {
        Log.d("SignInScreen", "========== SIGN IN SCREEN LOADED ==========")
        Log.d("SignInScreen", "uiState: isLoading=${uiState.isLoading}, error=${uiState.error}, authState=${uiState.authState}")
    }

    // Google Sign-In
    val oneTapClient = remember { Identity.getSignInClient(context) }
    val signInRequest = remember {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("276805170608-ih5ooa017c7dj21pb1i1437qm61ltl97.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    viewModel.signInWithGoogle(idToken)
                } else {
                    viewModel.setError("No ID token received from Google")
                }
            } catch (e: Exception) {
                viewModel.setError("Failed to get credential: ${e.message}")
            }
        } else {
            viewModel.setError("Google sign-in cancelled or failed")
        }
    }

    // Handle sign-in complete
    LaunchedEffect(uiState.signInComplete) {
        if (uiState.signInComplete) {
            onSignInComplete()
        }
    }

    // Show data conflict dialog
    if (uiState.showLocalDataWarning) {
        LocalDataWarningDialog(
            onUseCloudData = { viewModel.confirmUseCloudData() },
            onKeepLocalData = { viewModel.confirmKeepLocalData() }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onBack, enabled = !uiState.isLoading) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Icon
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (uiState.isSignUpMode) "Create Account" else "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (uiState.isSignUpMode)
                    "Sign up to sync your progress across devices"
                else
                    "Sign in to access your workout data",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Google Sign-In Button
            OutlinedButton(
                onClick = {
                    Log.d("SignInScreen", "Google sign-in button clicked")
                    oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener { result ->
                            Log.d("SignInScreen", "Google beginSignIn success, launching intent")
                            googleSignInLauncher.launch(
                                IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.e("SignInScreen", "Google beginSignIn FAILED: ${e.javaClass.simpleName}: ${e.message}", e)
                            val errorMsg = "${e.javaClass.simpleName}: ${e.message}"
                            viewModel.setError(errorMsg)
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continue with Google",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "or",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Email field
            OutlinedTextField(
                value = uiState.emailInput,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = uiState.passwordInput,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.signInWithEmail()
                    }
                ),
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            // Error message
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // DEBUG INFO - remove after fixing
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "DEBUG: authState=${uiState.authState::class.simpleName}, isLoading=${uiState.isLoading}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )

            // Forgot password (only in sign-in mode)
            if (!uiState.isSignUpMode) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { viewModel.sendPasswordResetEmail() },
                    enabled = !uiState.isLoading
                ) {
                    Text("Forgot Password?")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In / Sign Up button
            Button(
                onClick = {
                    Log.d("SignInScreen", "Sign In/Up button clicked, isSignUpMode=${uiState.isSignUpMode}")
                    viewModel.signInWithEmail()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (uiState.isSignUpMode) "Create Account" else "Sign In",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle sign up / sign in
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isSignUpMode) "Already have an account?" else "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { viewModel.toggleSignUpMode() },
                    enabled = !uiState.isLoading
                ) {
                    Text(if (uiState.isSignUpMode) "Sign In" else "Sign Up")
                }
            }

            if (isOnboarding) {
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        viewModel.skipSignIn()
                        onSkip()
                    },
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = "Skip for now",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LocalDataWarningDialog(
    onUseCloudData: () -> Unit,
    onKeepLocalData: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Data Conflict",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "You have existing data on this device and in the cloud. Which would you like to keep?\n\n" +
                        "Cloud Data: Use your synced data from another device.\n\n" +
                        "Local Data: Keep data on this device and upload it to the cloud.",
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            Button(
                onClick = onUseCloudData
            ) {
                Text("Use Cloud Data")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onKeepLocalData
            ) {
                Text("Keep Local Data")
            }
        }
    )
}
