package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppNotification
import com.example.ui.CivicNexusViewModel
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
    viewModel: CivicNexusViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors
    BackHandler { onBack() }

    val notifications by viewModel.notifications.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(civicColors.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notifs_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = civicColors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Notifications & Broadcasts",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                        Text(
                            text = "Municipal Updates & Telemetry Alerts",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.accentTealBright
                        )
                    }
                }

                TextButton(
                    onClick = { viewModel.markAllNotificationsAsRead() },
                    modifier = Modifier.testTag("mark_all_read_button")
                ) {
                    Text(text = "Mark Read", color = civicColors.accentCyan, fontSize = 12.sp)
                }
            }
        }

        // Live vs Simulated Notice Banner
        item {
            Surface(
                color = civicColors.cardContainer,
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = civicColors.accentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Broadcasts tagged [SIMULATED DRILL] are municipal preparedness exercises and test events.",
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.textSecondary
                    )
                }
            }
        }

        if (notifications.isEmpty()) {
            item {
                Surface(
                    color = civicColors.cardContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = null, tint = civicColors.textMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "No new notifications", style = MaterialTheme.typography.titleMedium, color = civicColors.textPrimary)
                    }
                }
            }
        } else {
            items(notifications) { notif ->
                NotificationItemCard(
                    notification = notif,
                    onClick = { viewModel.markNotificationAsRead(notif.id) }
                )
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val civicColors = MaterialTheme.civicColors
    val isRead = notification.isRead

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isRead) civicColors.cardContainer.copy(alpha = 0.7f) else civicColors.cardContainer
        ),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (!isRead) civicColors.accentTeal.copy(alpha = 0.5f) else civicColors.cardBorder)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when (notification.category) {
                            "Emergency Broadcast" -> PriorityCritical.copy(alpha = 0.15f)
                            "Complaint Update" -> civicColors.accentTeal.copy(alpha = 0.15f)
                            else -> civicColors.accentCyan.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.category) {
                        "Emergency Broadcast" -> Icons.Default.Warning
                        "Complaint Update" -> Icons.Default.AssignmentTurnedIn
                        else -> Icons.Default.Campaign
                    },
                    contentDescription = null,
                    tint = when (notification.category) {
                        "Emergency Broadcast" -> PriorityCritical
                        "Complaint Update" -> civicColors.accentTealBright
                        else -> civicColors.accentCyan
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (!isRead) FontWeight.Bold else FontWeight.SemiBold,
                            color = civicColors.textPrimary
                        )
                    )

                    Surface(
                        color = if (notification.isSimulated) civicColors.surfaceVariant else civicColors.accentTeal.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (notification.isSimulated) "SIMULATED" else "VERIFIED",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = if (notification.isSimulated) civicColors.accentCyan else civicColors.accentTealBright,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (!isRead) civicColors.textPrimary else civicColors.textSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notification.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.accentTealBright
                    )
                    Text(
                        text = notification.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.textMuted
                    )
                }
            }
        }
    }
}
