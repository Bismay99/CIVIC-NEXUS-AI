package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.CivicNexusViewModel
import com.example.ui.SubScreen
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: CivicNexusViewModel,
    onNavigateToReport: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onOpenEmergency: () -> Unit,
    onOpenComplaints: () -> Unit,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors
    val isDark = civicColors.isDark

    val cityPulse by viewModel.cityPulse.collectAsState()
    val emergencyAlerts by viewModel.emergencyAlerts.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val complaints by viewModel.complaints.collectAsState()

    var selectedIncidentForDetail by remember { mutableStateOf<Incident?>(null) }

    // Subtle atmospheric background gradient
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF080E1E),
                Color(0xFF0C162E),
                Color(0xFF080E1E)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8FAFC),
                Color(0xFFF1F5F9),
                Color(0xFFF8FAFC)
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Smart City Overview & Pulse Banner (GlassCard)
        item {
            GlassCard(
                shape = RoundedCornerShape(18.dp),
                elevation = 3.dp,
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with sandbox name, grid title and pulse gauge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Bhubaneswar Smart Sandbox",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.2.sp
                                ),
                                color = civicColors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Metropolitan Smart Core Grid",
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.textMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Subtle simulated telemetry label for transparency per Section 9
                            Surface(
                                color = civicColors.accentTeal.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SIMULATED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = civicColors.accentTeal,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Pulse Score Gauge Pill
                    Surface(
                        color = civicColors.accentTeal.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    civicColors.accentTeal.copy(alpha = 0.5f),
                                    civicColors.accentCyan.copy(alpha = 0.3f)
                                )
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(civicColors.accentTeal)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${cityPulse.pulseScore}/100 Pulse",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = civicColors.accentTealBright
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Autonomous Smart City Telemetry Row
                GlassSurface(
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = civicColors.surfaceVariant,
                    backgroundAlpha = if (isDark) 0.60f else 0.70f,
                    elevation = 0.dp,
                    specularHighlight = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${cityPulse.selfHealingActionsCount}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = civicColors.accentCyan
                            )
                            Text(
                                text = "Auto-Healed Today",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = civicColors.textMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(civicColors.cardBorder.copy(alpha = 0.6f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${cityPulse.sensorHealthPercent}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = civicColors.accentTealBright
                            )
                            Text(
                                text = "Sensor Grid Health",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = civicColors.textMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(civicColors.cardBorder.copy(alpha = 0.6f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${cityPulse.avgResponseMinutes}m",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PriorityMedium
                            )
                            Text(
                                text = "Avg Response Time",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = civicColors.textMuted
                            )
                        }
                    }
                }
            }
        }

        // 2. Quick Actions
        item {
            GlassSectionHeader(
                title = "Quick Actions",
                subtitle = "Fast Municipal Response Dispatch"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    title = "Report Issue",
                    subtitle = "AI Triage",
                    icon = Icons.Default.AddCircle,
                    accentColor = civicColors.accentTeal,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReport
                )

                QuickActionButton(
                    title = "City Map",
                    subtitle = "Sensors & GPS",
                    icon = Icons.Default.Map,
                    accentColor = civicColors.accentCyan,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMap
                )

                QuickActionButton(
                    title = "Emergency",
                    subtitle = "Safe Guides",
                    icon = Icons.Default.Warning,
                    accentColor = PriorityCritical,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenEmergency
                )

                QuickActionButton(
                    title = "My Reports",
                    subtitle = "${complaints.size} Active",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    accentColor = civicColors.accentBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenComplaints
                )
            }
        }

        // 3. City Incident Intelligence (with simulated tag for transparency)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassSectionHeader(
                    title = "City Incident Intelligence",
                    subtitle = "Metropolitan Telemetry Grid",
                    modifier = Modifier.weight(1f, fill = false)
                )

                Surface(
                    color = civicColors.chipBackground,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "DEMO DATA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = civicColors.textMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = "Active Incidents",
                    value = "${incidents.count { it.status == "Active" }}",
                    subtitle = "Grid Telemetry",
                    icon = Icons.Default.CrisisAlert,
                    accentColor = PriorityHigh,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_active_incidents"
                )

                StatMetricCard(
                    title = "Resolved Today",
                    value = "${cityPulse.resolvedToday}",
                    subtitle = "Civil Works",
                    icon = Icons.Default.CheckCircle,
                    accentColor = PriorityLow,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_resolved_today"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = "AI Dispatched",
                    value = "88.4%",
                    subtitle = "Autonomous Routing",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = civicColors.accentCyan,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_ai_dispatched"
                )

                StatMetricCard(
                    title = "Critical Hazards",
                    value = "${incidents.count { it.severity == IncidentSeverity.CRITICAL }}",
                    subtitle = "SOS Monitored",
                    icon = Icons.Default.FlashOn,
                    accentColor = PriorityCritical,
                    modifier = Modifier.weight(1f),
                    testTag = "stat_critical_hazards"
                )
            }
        }

        // 4. Live Emergency Broadcast Alerts Carousel (if any active alerts)
        if (emergencyAlerts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PriorityCritical)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live Emergency Broadcasts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                    }

                    TextButton(onClick = onOpenEmergency) {
                        Text(text = "View All", color = PriorityCritical, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(emergencyAlerts) { alert ->
                        AlertCard(alert = alert, onEmergencyClick = onOpenEmergency)
                    }
                }
            }
        }

        // 5. User's Recent Complaint Progress Tracker
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassSectionHeader(
                    title = "My Recent Complaint Tracker",
                    subtitle = "Live Lifecycle Audit",
                    modifier = Modifier.weight(1f, fill = false)
                )

                TextButton(onClick = onOpenComplaints) {
                    Text(text = "All Reports", color = civicColors.accentCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val recentComplaint = complaints.firstOrNull()
            if (recentComplaint != null) {
                GlassCard(
                    shape = RoundedCornerShape(14.dp),
                    elevation = 2.dp,
                    onClick = { viewModel.openSubScreen(SubScreen.ComplaintDetail(recentComplaint)) },
                    contentPadding = PaddingValues(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = civicColors.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = recentComplaint.referenceId,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = civicColors.accentCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = recentComplaint.submittedDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.textMuted
                            )
                        }

                        // Status badge
                        Surface(
                            color = when (recentComplaint.status) {
                                ComplaintStatus.SUBMITTED -> StatusSubmitted.copy(alpha = 0.2f)
                                ComplaintStatus.UNDER_REVIEW -> StatusUnderReview.copy(alpha = 0.2f)
                                ComplaintStatus.ASSIGNED -> StatusAssigned.copy(alpha = 0.2f)
                                ComplaintStatus.IN_PROGRESS -> StatusInProgress.copy(alpha = 0.2f)
                                ComplaintStatus.RESOLVED -> StatusResolved.copy(alpha = 0.2f)
                                ComplaintStatus.REOPENED -> StatusSubmitted.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = recentComplaint.status.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = when (recentComplaint.status) {
                                    ComplaintStatus.SUBMITTED -> StatusSubmitted
                                    ComplaintStatus.UNDER_REVIEW -> StatusUnderReview
                                    ComplaintStatus.ASSIGNED -> StatusAssigned
                                    ComplaintStatus.IN_PROGRESS -> StatusInProgress
                                    ComplaintStatus.RESOLVED -> StatusResolved
                                    ComplaintStatus.REOPENED -> StatusSubmitted
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = recentComplaint.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = civicColors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = recentComplaint.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = civicColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress indicator bar for steps
                    LinearProgressIndicator(
                        progress = { (recentComplaint.status.stepIndex + 1) / 5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = civicColors.accentTeal,
                        trackColor = civicColors.cardBorder
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Step ${recentComplaint.status.stepIndex + 1} of 5 • ${recentComplaint.assignedOfficer}",
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.textMuted
                    )
                }
            } else {
                GlassCard(
                    shape = RoundedCornerShape(14.dp),
                    elevation = 2.dp,
                    onClick = { onNavigateToReport() },
                    contentPadding = PaddingValues(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = civicColors.accentTeal.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = civicColors.accentTealBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "No complaints filed yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = civicColors.textPrimary
                            )
                            Text(
                                text = "Tap here to report an issue with AI triage assistance",
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.textMuted
                            )
                        }
                    }
                }
            }
        }

        // 6. Live Municipal Incidents Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassSectionHeader(
                    title = "Live Incident Stream",
                    subtitle = "Real-time citizen reports and sensor telemetry",
                    modifier = Modifier.weight(1f, fill = false)
                )

                TextButton(onClick = onNavigateToMap) {
                    Text(text = "View Map", color = civicColors.accentCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        items(incidents) { incident ->
            IncidentItemCard(
                incident = incident,
                onVerifyClick = { viewModel.upvoteIncident(incident.id) },
                onClick = { selectedIncidentForDetail = incident }
            )
        }
    }

    // Incident Detail Dialog with frosted glass styling
    selectedIncidentForDetail?.let { inc ->
        GlassDialog(
            onDismissRequest = { selectedIncidentForDetail = null },
            title = inc.title,
            icon = getCategoryIcon(inc.category)
        ) {
            Surface(
                color = civicColors.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Location: ${inc.locationName}", style = MaterialTheme.typography.labelSmall, color = civicColors.textPrimary)
                    Text(text = "Coordinates: (${inc.latitude}, ${inc.longitude})", style = MaterialTheme.typography.labelSmall, color = civicColors.textMuted)
                    Text(text = "Department: ${inc.department}", style = MaterialTheme.typography.labelSmall, color = civicColors.accentCyan)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = inc.description,
                style = MaterialTheme.typography.bodyMedium,
                color = civicColors.textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reported: ${inc.reportedTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.textMuted
                )
                Text(
                    text = "${inc.verifiedCount} Citizen Verifications",
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.accentTealBright
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { selectedIncidentForDetail = null }) {
                    Text(text = "Close", color = civicColors.textSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                GlassButton(
                    text = "Verify Incident",
                    icon = Icons.Default.ThumbUp,
                    onClick = {
                        viewModel.upvoteIncident(inc.id)
                        selectedIncidentForDetail = null
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors

    GlassCard(
        shape = RoundedCornerShape(14.dp),
        elevation = 2.dp,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        testTag = "quick_action_${title.lowercase().replace(" ", "_")}",
        modifier = modifier.defaultMinSize(minHeight = 52.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = civicColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = civicColors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AlertCard(
    alert: EmergencyAlert,
    onEmergencyClick: () -> Unit
) {
    val civicColors = MaterialTheme.civicColors
    val isCritical = alert.severity == IncidentSeverity.CRITICAL

    val borderColor = when (alert.severity) {
        IncidentSeverity.CRITICAL -> PriorityCritical
        IncidentSeverity.HIGH -> PriorityHigh
        else -> civicColors.accentCyan
    }

    GlassCard(
        shape = RoundedCornerShape(14.dp),
        elevation = 3.dp,
        borderColor = borderColor.copy(alpha = if (isCritical) 0.65f else 0.45f),
        onClick = onEmergencyClick,
        contentPadding = PaddingValues(12.dp),
        modifier = Modifier.width(280.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = borderColor.copy(alpha = 0.18f),
                shape = RoundedCornerShape(6.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(borderColor.copy(alpha = 0.4f))
                )
            ) {
                Text(
                    text = alert.severity.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = borderColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Text(
                text = alert.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = civicColors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = alert.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
            color = civicColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = alert.details,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
            color = civicColors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = civicColors.textMuted,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = alert.area,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = civicColors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
