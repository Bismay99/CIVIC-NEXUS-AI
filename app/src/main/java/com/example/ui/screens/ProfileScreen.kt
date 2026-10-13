package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backend.BackendConfigurationStatus
import com.example.model.AppThemeMode
import com.example.model.CitizenProfile
import com.example.ui.CivicNexusViewModel
import com.example.ui.SubScreen
import com.example.ui.theme.*
import com.example.util.AppLanguage

@Composable
fun ProfileScreen(
    viewModel: CivicNexusViewModel,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors
    val profile by viewModel.profile.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isGeminiConfigured = viewModel.isGeminiConfigured

    var pushNotifsEnabled by remember { mutableStateOf(true) }
    var emergencyAudioEnabled by remember { mutableStateOf(true) }
    var gpsAutoTagEnabled by remember { mutableStateOf(true) }
    var showBackendStatus by remember { mutableStateOf(false) }

    val backendStatus = remember { BackendConfigurationStatus() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(civicColors.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Citizen Profile & Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = civicColors.textPrimary
                )
                Text(
                    text = "Smart City Verified Reporter • Civic Nexus",
                    style = MaterialTheme.typography.labelMedium,
                    color = civicColors.accentTealBright
                )
            }
        }

        // Citizen ID Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.accentTeal.copy(alpha = 0.4f))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(civicColors.accentTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = civicColors.accentTealBright,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = civicColors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Citizen",
                                    tint = civicColors.accentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Citizen ID: ${profile.citizenId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.accentCyan
                            )
                            if (profile.email.isNotBlank()) {
                                Text(
                                    text = profile.email,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = civicColors.textSecondary
                                )
                            }
                            Text(
                                text = profile.ward,
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Trust Score & Metrics Row
                    Surface(
                        color = civicColors.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${profile.trustScore}%",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = civicColors.accentTealBright
                                )
                                Text(text = "Trust Score", style = MaterialTheme.typography.labelSmall, color = civicColors.textMuted)
                            }

                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(civicColors.cardBorder))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${profile.reportsCount}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = civicColors.textPrimary
                                )
                                Text(text = "Reports", style = MaterialTheme.typography.labelSmall, color = civicColors.textMuted)
                            }

                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(civicColors.cardBorder))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${profile.impactPoints}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = civicColors.accentCyan
                                )
                                Text(text = "Impact Pts", style = MaterialTheme.typography.labelSmall, color = civicColors.textMuted)
                            }
                        }
                    }
                }
            }
        }

        // Quick Navigation: My Complaints
        item {
            Surface(
                color = civicColors.cardContainer,
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openSubScreen(SubScreen.MyComplaints) }
                    .testTag("profile_my_complaints_button")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(civicColors.accentBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = civicColors.accentBlue, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "My Complaint History & Tracking", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = civicColors.textPrimary)
                            Text(text = "View 5-stage lifecycle resolution timelines", style = MaterialTheme.typography.labelSmall, color = civicColors.textMuted)
                        }
                    }

                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = civicColors.textSecondary)
                }
            }
        }

        // Theme Switcher Section (Light, Dark, System Default)
        item {
            Column {
                Text(
                    text = "Display Theme",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = civicColors.textPrimary
                )
                Text(
                    text = "Choose your preferred appearance mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.textMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val themeOptions = listOf(
                            Triple(AppThemeMode.LIGHT, "Light Mode", Icons.Default.LightMode),
                            Triple(AppThemeMode.DARK, "Dark Mode", Icons.Default.DarkMode),
                            Triple(AppThemeMode.SYSTEM, "System Default", Icons.Default.BrightnessAuto)
                        )

                        themeOptions.forEachIndexed { index, (mode, label, icon) ->
                            val isSelected = currentThemeMode == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(horizontal = 8.dp, vertical = 10.dp)
                                    .testTag("theme_option_${mode.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) civicColors.accentBlue.copy(alpha = 0.2f)
                                                else civicColors.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isSelected) civicColors.accentBlue else civicColors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = civicColors.textPrimary
                                        )
                                        val subtitle = when (mode) {
                                            AppThemeMode.LIGHT -> "Soft off-white background with dark slate typography"
                                            AppThemeMode.DARK -> "Deep charcoal and navy night palette"
                                            AppThemeMode.SYSTEM -> "Automatically follow your Android system theme"
                                        }
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = civicColors.textMuted
                                        )
                                    }
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = civicColors.accentBlue,
                                        unselectedColor = civicColors.textMuted
                                    )
                                )
                            }

                            if (index < themeOptions.lastIndex) {
                                HorizontalDivider(
                                    color = civicColors.dividerColor,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Language & Regional Localization
        item {
            Column {
                Text(
                    text = "Language & Regional Localization",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = civicColors.textPrimary
                )
                Text(
                    text = "Select preferred interface language (English, Odia, Hindi)",
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.textMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        AppLanguage.entries.forEachIndexed { idx, lang ->
                            val isSelected = currentLanguage == lang
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setLanguage(lang) }
                                    .padding(horizontal = 8.dp, vertical = 10.dp)
                                    .testTag("language_option_${lang.code}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = null,
                                        tint = if (isSelected) civicColors.accentTeal else civicColors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = lang.displayName,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = civicColors.textPrimary
                                        )
                                        Text(
                                            text = when (lang) {
                                                AppLanguage.ENGLISH -> "Default official municipal language"
                                                AppLanguage.ODIA -> "ରାଜ୍ୟ ସରକାରୀ ଭାଷା (State official language)"
                                                AppLanguage.HINDI -> "राष्ट्रीय संपर्क भाषा (National link language)"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = civicColors.textMuted
                                        )
                                    }
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setLanguage(lang) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = civicColors.accentTeal,
                                        unselectedColor = civicColors.textMuted
                                    )
                                )
                            }
                            if (idx < AppLanguage.entries.lastIndex) {
                                HorizontalDivider(color = civicColors.dividerColor, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        // Citizen Preferences & Privacy
        item {
            Text(
                text = "Preferences & Privacy",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = civicColors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    PreferenceToggleRow(
                        title = "Push Notifications",
                        subtitle = "Receive officer assignment & resolution status alerts",
                        checked = pushNotifsEnabled,
                        onCheckedChange = { pushNotifsEnabled = it }
                    )

                    HorizontalDivider(color = civicColors.dividerColor, modifier = Modifier.padding(vertical = 8.dp))

                    PreferenceToggleRow(
                        title = "Emergency Siren Broadcasts",
                        subtitle = "Audible high-priority flash flood & hazard warnings",
                        checked = emergencyAudioEnabled,
                        onCheckedChange = { emergencyAudioEnabled = it }
                    )

                    HorizontalDivider(color = civicColors.dividerColor, modifier = Modifier.padding(vertical = 8.dp))

                    PreferenceToggleRow(
                        title = "Automatic GPS Coordinates Tagging",
                        subtitle = "Attach precision coordinates to all civic complaints",
                        checked = gpsAutoTagEnabled,
                        onCheckedChange = { gpsAutoTagEnabled = it }
                    )
                }
            }
        }

        // Backend Integration Readiness Inspector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.accentTeal.copy(alpha = 0.5f))),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBackendStatus = !showBackendStatus }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(civicColors.accentTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = civicColors.accentTealBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Backend Architecture Readiness",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = civicColors.textPrimary
                                )
                                Text(
                                    text = "Firebase • Gemini 3.5 Flash • Maps Platform",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = civicColors.accentTealBright
                                )
                            }
                        }

                        Icon(
                            imageVector = if (showBackendStatus) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = civicColors.accentTeal
                        )
                    }

                    AnimatedVisibility(visible = showBackendStatus) {
                        Column(
                            modifier = Modifier.padding(top = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BackendStatusItem(
                                title = "Firebase Authentication",
                                status = backendStatus.firebaseAuthStatus,
                                icon = Icons.Default.Lock
                            )
                            BackendStatusItem(
                                title = "Cloud Firestore Database",
                                status = backendStatus.firestoreStatus,
                                icon = Icons.Default.Storage
                            )
                            BackendStatusItem(
                                title = "Firebase Storage",
                                status = backendStatus.cloudStorageStatus,
                                icon = Icons.Default.FolderZip
                            )
                            BackendStatusItem(
                                title = "Firebase Cloud Messaging (FCM)",
                                status = backendStatus.fcmStatus,
                                icon = Icons.Default.NotificationAdd
                            )
                            BackendStatusItem(
                                title = "Google Maps Platform",
                                status = backendStatus.mapsPlatformStatus,
                                icon = Icons.Default.Map
                            )
                            BackendStatusItem(
                                title = "Gemini 3.5 Flash API",
                                status = if (isGeminiConfigured) "LIVE via REST API Key (BuildConfig)" else "Active (Autonomous Civic Engine + Secret Ready)",
                                icon = Icons.Default.AutoAwesome
                            )
                        }
                    }
                }
            }
        }

        // Citizen Sign Out Section
        item {
            var showSignOutConfirmDialog by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showSignOutConfirmDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PriorityCritical),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(PriorityCritical.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("profile_sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sign Out",
                    tint = PriorityCritical,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out of Citizen Account",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = PriorityCritical
                )
            }

            if (showSignOutConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showSignOutConfirmDialog = false },
                    title = {
                        Text(
                            text = "Sign Out",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to sign out? Your cloud complaints and tracked progress remain securely preserved.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = civicColors.textSecondary
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSignOutConfirmDialog = false
                                viewModel.signOut()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PriorityCritical)
                        ) {
                            Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSignOutConfirmDialog = false }) {
                            Text("Cancel", color = civicColors.textSecondary)
                        }
                    },
                    containerColor = civicColors.surface,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val civicColors = MaterialTheme.civicColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = civicColors.textPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = civicColors.textMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = civicColors.accentTeal,
                uncheckedThumbColor = civicColors.textMuted,
                uncheckedTrackColor = civicColors.surfaceVariant
            )
        )
    }
}

@Composable
private fun BackendStatusItem(
    title: String,
    status: String,
    icon: ImageVector
) {
    val civicColors = MaterialTheme.civicColors

    Surface(
        color = civicColors.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = civicColors.accentCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = civicColors.textPrimary)
                Text(text = status, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = civicColors.textSecondary)
            }
        }
    }
}
