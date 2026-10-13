package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
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
fun CitizenRegisterScreen(
    viewModel: CivicNexusViewModel,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors
    val focusManager = LocalFocusManager.current
    val authState by viewModel.authUiState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Back Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.setAuthScreenMode(AuthScreenMode.LOGIN) },
                    modifier = Modifier.testTag("register_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Sign In",
                        tint = civicColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Back to Sign In",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = civicColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Branding Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(civicColors.cardContainer)
                    .border(1.dp, civicColors.accentTeal.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AppRegistration,
                    contentDescription = "Citizen Registration",
                    tint = civicColors.accentTealBright,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "CIVIC NEXUS AI",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = civicColors.textPrimary
            )

            Text(
                text = "Citizen Account Registration",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = civicColors.accentTeal
            )

            Text(
                text = "Join your city's neural network for proactive civic action.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = civicColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Registration Card
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
                    // Citizen-Only Badge
                    Surface(
                        color = civicColors.accentTeal.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = civicColors.accentTealBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Citizen Access Tier • Municipal Public Portal",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = civicColors.accentTealBright
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Validation or Backend Error Notice
                    if (validationError != null || authState.errorMessage != null) {
                        val msg = validationError ?: authState.errorMessage ?: ""
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

                    // Full Name Field
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            validationError = null
                        },
                        label = { Text("Full Legal Name", color = civicColors.textSecondary) },
                        placeholder = { Text("e.g. Subhransu Mohapatra", color = civicColors.textMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = civicColors.accentTeal
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
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
                            .testTag("register_fullname_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            validationError = null
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
                            .testTag("register_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            validationError = null
                        },
                        label = { Text("Create Password (min 6 chars)", color = civicColors.textSecondary) },
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
                            .testTag("register_password_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            validationError = null
                        },
                        label = { Text("Confirm Password", color = civicColors.textSecondary) },
                        placeholder = { Text("••••••••", color = civicColors.textMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = civicColors.accentTeal
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password",
                                    tint = civicColors.textSecondary
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                performCitizenRegistration(
                                    fullName, email, password, confirmPassword,
                                    { validationError = it },
                                    { viewModel.registerWithEmail(fullName, email, password) }
                                )
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
                            .testTag("register_confirm_password_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Registration Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            performCitizenRegistration(
                                fullName, email, password, confirmPassword,
                                { validationError = it },
                                { viewModel.registerWithEmail(fullName, email, password) }
                            )
                        },
                        enabled = !authState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = civicColors.accentTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_registration_button")
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Creating Citizen Account...",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Create Citizen Account",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Switch back to Login
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Already have an account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = civicColors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = civicColors.accentTeal
                            ),
                            modifier = Modifier
                                .clickable {
                                    viewModel.clearAuthMessages()
                                    viewModel.setAuthScreenMode(AuthScreenMode.LOGIN)
                                }
                                .testTag("register_goto_login_button")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun performCitizenRegistration(
    name: String,
    email: String,
    pass: String,
    confirmPass: String,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    if (name.trim().isEmpty()) {
        onError("Please enter your legal name")
        return
    }
    val trimmedEmail = email.trim()
    if (trimmedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
        onError("Please enter a valid citizen email address")
        return
    }
    if (pass.length < 6) {
        onError("Password must be at least 6 characters")
        return
    }
    if (pass != confirmPass) {
        onError("Passwords do not match")
        return
    }
    onSuccess()
}
