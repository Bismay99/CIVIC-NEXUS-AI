package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivicTopAppBar(
    title: String = "CIVIC NEXUS AI",
    subtitle: String = "Smart City Nervous System • Urban Governance Grid",
    unreadNotifCount: Int = 0,
    onEmergencyClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    val civicColors = MaterialTheme.civicColors

    Surface(
        color = civicColors.topBarBackground,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Animated Pulse Indicator
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.25f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(civicColors.accentTeal.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size((8 * pulseScale).dp)
                            .clip(CircleShape)
                            .background(civicColors.accentTeal)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = civicColors.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.accentTeal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Emergency Quick SOS button
                FilledTonalButton(
                    onClick = onEmergencyClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = PriorityCritical.copy(alpha = 0.15f),
                        contentColor = PriorityCritical
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("top_bar_sos_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Emergency SOS",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SOS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Notifications icon with badge
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("top_bar_notif_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = unreadNotifCount.toString(),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = civicColors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CivicBottomNavigation(
    currentTab: MainNavigationTab,
    onTabSelected: (MainNavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors

    NavigationBar(
        containerColor = civicColors.bottomBarBackground,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier.border(
            width = 1.dp,
            color = civicColors.cardBorder,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        )
    ) {
        val tabs = listOf(
            Triple(MainNavigationTab.HOME, Icons.Default.Home, Icons.Outlined.Home),
            Triple(MainNavigationTab.MAP, Icons.Default.Map, Icons.Outlined.Map),
            Triple(MainNavigationTab.REPORT, Icons.Default.AddCircle, Icons.Outlined.AddCircleOutline),
            Triple(MainNavigationTab.ASSISTANT, Icons.Default.Psychology, Icons.Outlined.Psychology),
            Triple(MainNavigationTab.PROFILE, Icons.Default.Person, Icons.Outlined.PersonOutline)
        )

        tabs.forEach { (tab, filledIcon, outlinedIcon) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) filledIcon else outlinedIcon,
                        contentDescription = tab.label
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = civicColors.textSecondary,
                    unselectedTextColor = civicColors.textSecondary
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors

    Card(
        colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.textSecondary,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = civicColors.textPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun IncidentItemCard(
    incident: Incident,
    onVerifyClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors
    val severityColor = when (incident.severity) {
        IncidentSeverity.CRITICAL -> PriorityCritical
        IncidentSeverity.HIGH -> PriorityHigh
        IncidentSeverity.MEDIUM -> PriorityMedium
        IncidentSeverity.LOW -> PriorityLow
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("incident_card_${incident.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                Surface(
                    color = civicColors.chipBackground,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(incident.category),
                            contentDescription = null,
                            tint = civicColors.accentTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = incident.category.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = civicColors.accentTeal
                        )
                    }
                }

                // Severity Badge
                Surface(
                    color = severityColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = incident.severity.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = severityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = incident.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = civicColors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = incident.description,
                style = MaterialTheme.typography.bodyMedium,
                color = civicColors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = civicColors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = incident.locationName,
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Verify Button
                FilledTonalButton(
                    onClick = onVerifyClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = civicColors.chipBackground,
                        contentColor = civicColors.textPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("verify_incident_${incident.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Verify",
                        tint = civicColors.accentTeal,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${incident.verifiedCount} Verified",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusTimelineView(
    timeline: List<TimelineStep>,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors

    Column(modifier = modifier.fillMaxWidth()) {
        timeline.forEachIndexed { index, step ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Timeline Node & Vertical Connector
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    step.isCompleted -> civicColors.accentTeal
                                    step.isCurrent -> PriorityHigh
                                    else -> civicColors.cardBorder
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (step.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else if (step.isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    if (index < timeline.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(38.dp)
                                .background(if (step.isCompleted) civicColors.accentTeal else civicColors.cardBorder)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (step.isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (step.isCurrent) PriorityHigh else civicColors.textPrimary
                            )
                        )
                        Text(
                            text = step.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.textMuted
                        )
                    }
                    Text(
                        text = step.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = civicColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun AiAnalysisCard(
    analysis: AiAnalysisResult,
    onEditSuggestionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors

    Card(
        colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.accentTeal.copy(alpha = 0.5f))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(civicColors.accentTeal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Analysis",
                            tint = civicColors.accentTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Incident Assessment",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                        Text(
                            text = "Neural Triage • ${analysis.confidenceScore}% Confidence",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.accentTeal
                        )
                    }
                }

                TextButton(
                    onClick = onEditSuggestionClick,
                    modifier = Modifier.testTag("edit_ai_suggestion_button")
                ) {
                    Text(
                        text = "Edit",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = civicColors.chipBackground,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Category", style = MaterialTheme.typography.labelSmall, color = civicColors.textMuted)
                        Text(
                            text = analysis.suggestedCategory.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = civicColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    color = civicColors.chipBackground,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Priority", style = MaterialTheme.typography.labelSmall, color = civicColors.textMuted)
                        Text(
                            text = analysis.suggestedPriority.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (analysis.suggestedPriority) {
                                    IncidentSeverity.CRITICAL -> PriorityCritical
                                    IncidentSeverity.HIGH -> PriorityHigh
                                    IncidentSeverity.MEDIUM -> PriorityMedium
                                    IncidentSeverity.LOW -> PriorityLow
                                }
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Department Routing:",
                style = MaterialTheme.typography.labelSmall,
                color = civicColors.textMuted
            )
            Text(
                text = analysis.recommendedDepartment,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = civicColors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AI Technical Reasoning:",
                style = MaterialTheme.typography.labelSmall,
                color = civicColors.textMuted
            )
            Text(
                text = analysis.reasoning,
                style = MaterialTheme.typography.bodyMedium,
                color = civicColors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = PriorityHigh.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PriorityHigh.copy(alpha = 0.3f)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = PriorityHigh,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Safety Precaution: ${analysis.suggestedPrecaution}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriorityHigh
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Advisory notice: AI classifications are recommendations subject to municipal officer review.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = civicColors.textMuted
            )
        }
    }
}

fun getCategoryIcon(category: IncidentCategory): ImageVector {
    return when (category) {
        IncidentCategory.POTHOLES -> Icons.Default.Construction
        IncidentCategory.GARBAGE -> Icons.Default.Delete
        IncidentCategory.FLOODING -> Icons.Default.WaterDamage
        IncidentCategory.FIRE -> Icons.Default.LocalFireDepartment
        IncidentCategory.TRAFFIC -> Icons.Default.Traffic
        IncidentCategory.STREETLIGHTS -> Icons.Default.Lightbulb
        IncidentCategory.WATER_BURST -> Icons.Default.Plumbing
        IncidentCategory.OTHER -> Icons.Default.ReportProblem
    }
}
