package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuthScreenMode
import com.example.ui.CivicNexusViewModel
import com.example.ui.theme.*

@Composable
fun CitizenLoginScreen(
    viewModel: CivicNexusViewModel,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val authState by viewModel.authUiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(civicColors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Branding Crest & App Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(civicColors.cardContainer)
                    .border(1.dp, civicColors.accentTeal.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Civic Nexus Shield",
                    tint = civicColors.accentTealBright,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CIVIC NEXUS AI",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = civicColors.textPrimary
            )

            Text(
                text = "Your City. Your Voice. Your Future.",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = civicColors.accentTeal,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Report civic issues, track progress, and help build a better city.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = civicColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Citizen Login Card
            Card(
                colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = civicColors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Sign in to continue to your citizen account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = civicColors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Diagnostic Notice when Firebase is pending configuration
                    if (!authState.isFirebaseConfigured) {
                        Surface(
                            color = PriorityMedium.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(PriorityMedium.copy(alpha = 0.4f))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PriorityMedium,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Cloud Database Connected",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = PriorityMedium
                                    )
                                    Text(
                                        text = "Sign in with Google or your citizen credentials.",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = civicColors.textSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Error Message Banner
                    if (localValidationError != null || authState.errorMessage != null) {
                        val msg = localValidationError ?: authState.errorMessage ?: ""
                        Surface(
                            color = PriorityCritical.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(PriorityCritical.copy(alpha = 0.4f))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = PriorityCritical,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PriorityCritical
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Success Message Banner
                    if (authState.successMessage != null) {
                        Surface(
                            color = PriorityLow.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(PriorityLow.copy(alpha = 0.4f))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PriorityLow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authState.successMessage ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PriorityLow
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Primary Google Sign-In Button
                    OutlinedButton(
                        onClick = {
                            viewModel.clearAuthMessages()
                            localValidationError = null
                            (context as? Activity)?.let { act ->
                                viewModel.signInWithGoogle(act)
                            }
                        },
                        enabled = !authState.isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0F172A)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google Icon",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign in with Google",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Or with Email Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = civicColors.dividerColor
                        )
                        Text(
                            text = "  OR SIGN IN WITH EMAIL  ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = civicColors.textMuted
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = civicColors.dividerColor
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            localValidationError = null
                        },
                        label = { Text("Citizen Email Address", color = civicColors.textSecondary) },
                        placeholder = { Text("name@example.com", color = civicColors.textMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = civicColors.accentTeal
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = civicColors.accentTeal,
                            unfocusedBorderColor = civicColors.inputBorder,
                            focusedTextColor = civicColors.textPrimary,
                            unfocusedTextColor = civicColors.textPrimary,
                            focusedContainerColor = civicColors.inputBackground,
                            unfocusedContainerColor = civicColors.inputBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localValidationError = null
                        },
                        label = { Text("Password", color = civicColors.textSecondary) },
                        placeholder = { Text("••••••••", color = civicColors.textMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = civicColors.accentTeal
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                    tint = civicColors.textSecondary
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                performEmailSignIn(email, password, { localValidationError = it }) {
                                    viewModel.signInWithEmail(email, password)
                                }
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = civicColors.accentTeal,
                            unfocusedBorderColor = civicColors.inputBorder,
                            focusedTextColor = civicColors.textPrimary,
                            unfocusedTextColor = civicColors.textPrimary,
                            focusedContainerColor = civicColors.inputBackground,
                            unfocusedContainerColor = civicColors.inputBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Forgot Password Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showForgotPasswordDialog = true },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("forgot_password_button")
                        ) {
                            Text(
                                text = "Forgot Password?",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = civicColors.accentCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sign In Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            performEmailSignIn(email, password, { localValidationError = it }) {
                                viewModel.signInWithEmail(email, password)
                            }
                        },
                        enabled = !authState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = civicColors.accentTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("email_sign_in_button")
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Signing In...",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Sign In as Citizen",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Switch to Register
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "New citizen reporter?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = civicColors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = civicColors.accentTeal
                            ),
                            modifier = Modifier
                                .clickable {
                                    viewModel.clearAuthMessages()
                                    viewModel.setAuthScreenMode(AuthScreenMode.REGISTER)
                                }
                                .testTag("goto_register_button")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Municipal Footer Terms Link
            Text(
                text = "By signing in, you agree to Bhubaneswar Smart City Citizen Guidelines & Municipal Data Privacy Standards.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = civicColors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable { showTermsDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Forgot Password Dialog
        if (showForgotPasswordDialog) {
            var resetEmail by remember { mutableStateOf(email) }
            var resetError by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = {
                    Text(
                        text = "Reset Citizen Password",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = civicColors.textPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Enter your registered email address to receive password recovery instructions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = civicColors.textSecondary
                        )

                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = {
                                resetEmail = it
                                resetError = null
                            },
                            label = { Text("Email Address", color = civicColors.textSecondary) },
                            singleLine = true,
                            isError = resetError != null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = civicColors.accentTeal,
                                unfocusedBorderColor = civicColors.inputBorder,
                                focusedTextColor = civicColors.textPrimary,
                                unfocusedTextColor = civicColors.textPrimary
                            )
                        )

                        if (resetError != null) {
                            Text(
                                text = resetError ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = PriorityCritical
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (resetEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                                resetError = "Please enter a valid email address"
                            } else {
                                viewModel.sendPasswordReset(resetEmail.trim())
                                showForgotPasswordDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = civicColors.accentTeal)
                    ) {
                        Text("Send Recovery Email", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel", color = civicColors.textSecondary)
                    }
                },
                containerColor = civicColors.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Terms and Guidelines Dialog
        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                title = {
                    Text(
                        text = "Smart City Governance Guidelines",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = civicColors.textPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "1. Proactive Reporting: Incidents submitted are routed automatically to zonal municipal officers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = civicColors.textSecondary
                        )
                        Text(
                            text = "2. Sensor Verification: GPS markers attach precision location data for verified crew dispatch.",
                            style = MaterialTheme.typography.bodySmall,
                            color = civicColors.textSecondary
                        )
                        Text(
                            text = "3. Trust Score: Verified civic contributions enhance reporter credibility and prioritize resolution dispatch.",
                            style = MaterialTheme.typography.bodySmall,
                            color = civicColors.textSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showTermsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = civicColors.accentTeal)
                    ) {
                        Text("Understood", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = civicColors.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

private fun performEmailSignIn(
    email: String,
    pass: String,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    val trimmedEmail = email.trim()
    if (trimmedEmail.isEmpty()) {
        onError("Please enter your citizen email address")
        return
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
        onError("Please enter a valid email format (e.g. name@domain.com)")
        return
    }
    if (pass.isEmpty()) {
        onError("Please enter your account password")
        return
    }
    if (pass.length < 6) {
        onError("Password must be at least 6 characters")
        return
    }
    onSuccess()
}
