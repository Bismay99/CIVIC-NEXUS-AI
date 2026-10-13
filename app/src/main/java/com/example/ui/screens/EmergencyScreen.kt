package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.CoordinationStep
import com.example.model.EmergencyFacility
import com.example.ui.CivicNexusViewModel
import com.example.ui.theme.*

@Composable
fun EmergencyScreen(
    viewModel: CivicNexusViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors
    BackHandler { onBack() }

    val context = LocalContext.current
    val simulation by viewModel.simulation.collectAsState()
    val facilities by viewModel.emergencyFacilities.collectAsState()

    var expandedGuideId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(civicColors.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Navigation Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("emergency_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = civicColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Emergency Response Center",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = civicColors.textPrimary
                    )
                    Text(
                        text = "Multi-Agency Coordination & Direct Contacts",
                        style = MaterialTheme.typography.labelSmall,
                        color = PriorityCritical
                    )
                }
            }
        }

        // Safety Simulation Warning Banner
        item {
            Surface(
                color = PriorityCritical.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PriorityCritical.copy(alpha = 0.5f))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = PriorityCritical,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SIMULATION NOTICE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PriorityCritical
                        )
                        Text(
                            text = "For genuine life-threatening emergencies, call official services immediately (Dial 112). Multi-agency automated dispatch below is an active municipal coordination simulation sandbox.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = civicColors.textPrimary
                        )
                    }
                }
            }
        }

        // Verified Emergency Contacts
        item {
            Text(
                text = "Verified Municipal Helplines",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = civicColors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EmergencyContactRow(
                    name = "Unified Police & Disaster SOS",
                    number = "112",
                    agency = "Universal Emergency Dispatch",
                    icon = Icons.Default.LocalPolice,
                    accentColor = PriorityCritical,
                    onDial = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                        context.startActivity(intent)
                    }
                )

                EmergencyContactRow(
                    name = "Ambulance & Trauma Care",
                    number = "108",
                    agency = "State Emergency Medical Services",
                    icon = Icons.Default.LocalHospital,
                    accentColor = PriorityHigh,
                    onDial = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:108"))
                        context.startActivity(intent)
                    }
                )

                EmergencyContactRow(
                    name = "Fire & Disaster Rescue",
                    number = "101",
                    agency = "Odisha Fire Services",
                    icon = Icons.Default.LocalFireDepartment,
                    accentColor = PriorityCritical,
                    onDial = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:101"))
                        context.startActivity(intent)
                    }
                )

                EmergencyContactRow(
                    name = "Water Supply Emergency (WATCO)",
                    number = "1916",
                    agency = "Bhubaneswar Water Utility",
                    icon = Icons.Default.WaterDrop,
                    accentColor = civicColors.accentCyan,
                    onDial = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1916"))
                        context.startActivity(intent)
                    }
                )

                EmergencyContactRow(
                    name = "Electricity Grid Emergency (TPCODL)",
                    number = "1912",
                    agency = "Central Odisha Power Distribution",
                    icon = Icons.Default.ElectricBolt,
                    accentColor = PriorityMedium,
                    onDial = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1912"))
                        context.startActivity(intent)
                    }
                )
            }
        }

        // Multi-Agency Coordination Workflow Simulation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Simulated Multi-Agency Workflow",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = civicColors.textPrimary
                            )
                            Text(
                                text = "Cross-Department Self-Healing Orchestration",
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.accentTealBright
                            )
                        }

                        Surface(
                            color = if (simulation.isCompleted) PriorityLow.copy(alpha = 0.2f) else civicColors.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (simulation.isCompleted) "ALL RESOLVED" else if (simulation.isRunning) "DISPATCHING..." else "STANDBY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (simulation.isCompleted) PriorityLow else civicColors.accentCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = civicColors.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Scenario: ${simulation.incidentTitle}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = civicColors.textPrimary
                            )
                            Text(
                                text = simulation.location,
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Steps
                    simulation.steps.forEachIndexed { idx, step ->
                        CoordinationStepRow(step = step, isLast = idx == simulation.steps.lastIndex)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.startEmergencySimulation() },
                            enabled = !simulation.isRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = civicColors.accentTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_simulation_button")
                        ) {
                            Icon(
                                imageVector = if (simulation.isRunning) Icons.Default.HourglassEmpty else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (simulation.isRunning) "Coordinating..." else "Run Dispatch Drill",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetEmergencySimulation() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("reset_simulation_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = civicColors.textSecondary),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)
                            )
                        ) {
                            Text(text = "Reset")
                        }
                    }
                }
            }
        }

        // Safe Emergency Guidance Section
        item {
            Text(
                text = "Safe Emergency Guidance",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = civicColors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                defaultSafetyGuides.forEach { guide ->
                    EmergencyGuideItem(
                        id = guide.id,
                        title = guide.title,
                        summary = guide.summary,
                        steps = guide.steps,
                        isExpanded = expandedGuideId == guide.id,
                        onToggle = {
                            expandedGuideId = if (expandedGuideId == guide.id) null else guide.id
                        }
                    )
                }
            }
        }

        // Nearby Municipal Emergency Facilities
        item {
            Text(
                text = "Nearby Emergency Facilities",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = civicColors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        items(facilities) { fac ->
            FacilityItemCard(
                facility = fac,
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${fac.contactNumber}"))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun EmergencyContactRow(
    name: String,
    number: String,
    agency: String,
    icon: ImageVector,
    accentColor: Color,
    onDial: () -> Unit
) {
    val civicColors = MaterialTheme.civicColors

    Surface(
        color = civicColors.cardContainer,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = civicColors.textPrimary
                    )
                    Text(
                        text = agency,
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onDial,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("dial_$number")
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = number, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CoordinationStepRow(
    step: CoordinationStep,
    isLast: Boolean
) {
    val civicColors = MaterialTheme.civicColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        when (step.status) {
                            "Resolved" -> PriorityLow
                            "Active", "On Scene" -> civicColors.accentTeal
                            "Dispatched" -> PriorityHigh
                            else -> civicColors.cardBorder
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (step.status == "Resolved") {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(30.dp)
                        .background(if (step.status == "Resolved") PriorityLow else civicColors.cardBorder)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = step.agency.agencyName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = civicColors.textPrimary
                )
                Text(
                    text = "${step.status} (${step.timestamp})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = when (step.status) {
                        "Resolved" -> PriorityLow
                        "Active", "On Scene" -> civicColors.accentTealBright
                        "Dispatched" -> PriorityHigh
                        else -> civicColors.textMuted
                    }
                )
            }
            Text(
                text = step.action,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                color = civicColors.textSecondary
            )
        }
    }
}

@Composable
private fun EmergencyGuideItem(
    id: String,
    title: String,
    summary: String,
    steps: List<String>,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val civicColors = MaterialTheme.civicColors

    Card(
        colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = civicColors.textPrimary
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.textMuted
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = civicColors.textSecondary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = civicColors.dividerColor)
                    steps.forEachIndexed { i, s ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "${i + 1}. ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = civicColors.accentTeal
                            )
                            Text(
                                text = s,
                                style = MaterialTheme.typography.bodySmall,
                                color = civicColors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FacilityItemCard(
    facility: EmergencyFacility,
    onCall: () -> Unit
) {
    val civicColors = MaterialTheme.civicColors

    Card(
        colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = facility.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = civicColors.textPrimary
                )
                Text(
                    text = "${facility.type} • ${facility.distanceKm} km away",
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.accentTeal
                )
                Text(
                    text = facility.address,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = civicColors.textMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = onCall,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = civicColors.accentCyan.copy(alpha = 0.2f),
                    contentColor = civicColors.accentCyan
                ),
                modifier = Modifier.testTag("call_facility_${facility.id}")
            ) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class EmergencySafetyGuide(
    val id: String,
    val title: String,
    val summary: String,
    val steps: List<String>
)

val defaultSafetyGuides = listOf(
    EmergencySafetyGuide(
        id = "guide_flood",
        title = "Urban Flash Flood & Water Logging",
        summary = "Action protocol during heavy monsoon inundation",
        steps = listOf(
            "Avoid walking or driving through moving storm water.",
            "Stay away from downed electrical wires and waterlogged transformers.",
            "Report storm drain chokes and open manholes via Civic Nexus immediately.",
            "Move essential belongings to higher floors if near low-lying drains."
        )
    ),
    EmergencySafetyGuide(
        id = "guide_electrical",
        title = "Live Wire & Transformer Hazard",
        summary = "Precautionary steps for exposed electrical lines",
        steps = listOf(
            "Maintain at least a 10-meter clearance perimeter from downed wires.",
            "Do not touch metal fences, lampposts, or puddles near the line.",
            "Alert nearby citizens and dial TPCODL helpline (1912) immediately.",
            "Submit a Critical priority report on the Civic Nexus map."
        )
    ),
    EmergencySafetyGuide(
        id = "guide_fire",
        title = "Commercial / Domestic Fire Incident",
        summary = "Immediate evacuation and smoke defense",
        steps = listOf(
            "Evacuate calmly using designated stairwells; do not use elevators.",
            "Crawl low under smoke to preserve breathing air quality.",
            "Call Odisha Fire Services immediately at 101 or 112.",
            "Assemble at the nearest municipal open park or wide avenue."
        )
    )
)

