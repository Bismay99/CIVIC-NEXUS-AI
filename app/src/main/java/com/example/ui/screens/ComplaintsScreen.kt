package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.ui.CivicNexusViewModel
import com.example.ui.components.StatusTimelineView
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.*

@Composable
fun ComplaintsScreen(
    viewModel: CivicNexusViewModel,
    onBack: () -> Unit,
    onNavigateToReport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors
    BackHandler { onBack() }

    val complaints by viewModel.complaints.collectAsState()
    val statusFilter by viewModel.complaintStatusFilter.collectAsState()
    val searchQuery by viewModel.complaintSearchQuery.collectAsState()

    var selectedComplaintDetail by remember { mutableStateOf<Complaint?>(null) }
    var complaintToReopen by remember { mutableStateOf<Complaint?>(null) }
    var reopenFeedbackText by remember { mutableStateOf("") }
    var isReopening by remember { mutableStateOf(false) }

    val filteredComplaints = remember(complaints, statusFilter, searchQuery) {
        complaints.filter { comp ->
            val matchesStatus = statusFilter == null || comp.status == statusFilter
            val matchesQuery = searchQuery.isBlank() ||
                    comp.referenceId.contains(searchQuery, ignoreCase = true) ||
                    comp.title.contains(searchQuery, ignoreCase = true) ||
                    comp.address.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(civicColors.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    IconButton(onClick = onBack, modifier = Modifier.testTag("complaints_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = civicColors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "My Civic Complaints",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                        Text(
                            text = "${complaints.size} Tracked Municipal Reports",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.accentTealBright
                        )
                    }
                }

                if (onNavigateToReport != null) {
                    FilledTonalButton(
                        onClick = onNavigateToReport,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = civicColors.accentTeal.copy(alpha = 0.2f),
                            contentColor = civicColors.accentTealBright
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("complaints_report_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Report", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setComplaintSearchQuery(it) },
                placeholder = { Text("Search by reference (e.g. CN-2026) or street...", color = civicColors.textMuted) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = civicColors.textSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setComplaintSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = civicColors.textSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("complaint_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = civicColors.accentTeal,
                    unfocusedBorderColor = civicColors.inputBorder,
                    focusedTextColor = civicColors.textPrimary,
                    unfocusedTextColor = civicColors.textPrimary,
                    focusedContainerColor = civicColors.inputBackground,
                    unfocusedContainerColor = civicColors.inputBackground
                ),
                singleLine = true
            )
        }

        // Status Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { viewModel.setComplaintStatusFilter(null) },
                        label = { Text("All (${complaints.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = civicColors.accentTeal,
                            selectedLabelColor = Color.White,
                            containerColor = civicColors.cardContainer,
                            labelColor = civicColors.textSecondary
                        )
                    )
                }

                items(ComplaintStatus.entries) { status ->
                    val count = complaints.count { it.status == status }
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = {
                            viewModel.setComplaintStatusFilter(if (statusFilter == status) null else status)
                        },
                        label = { Text("${status.displayName} ($count)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = civicColors.accentTeal,
                            selectedLabelColor = Color.White,
                            containerColor = civicColors.cardContainer,
                            labelColor = civicColors.textSecondary
                        )
                    )
                }
            }
        }

        // Complaint List or Empty State
        if (complaints.isEmpty()) {
            item {
                Surface(
                    color = civicColors.cardContainer,
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentLate,
                            contentDescription = null,
                            tint = civicColors.accentTeal,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Complaints Filed Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "All submitted municipal complaints are permanently persisted and accessible across app sessions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = civicColors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (onNavigateToReport != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToReport,
                                colors = ButtonDefaults.buttonColors(containerColor = civicColors.accentTeal),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("empty_report_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Report First Issue", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else if (filteredComplaints.isEmpty()) {
            item {
                Surface(
                    color = civicColors.cardContainer,
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, tint = civicColors.textMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "No matching complaints found", style = MaterialTheme.typography.titleMedium, color = civicColors.textPrimary)
                        Text(text = "Try adjusting your search query or status filter", style = MaterialTheme.typography.labelSmall, color = civicColors.textMuted)
                    }
                }
            }
        } else {
            items(filteredComplaints) { complaint ->
                ComplaintCard(
                    complaint = complaint,
                    onClick = { selectedComplaintDetail = complaint }
                )
            }
        }
    }

    // Detail Dialog
    selectedComplaintDetail?.let { comp ->
        AlertDialog(
            onDismissRequest = { selectedComplaintDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = civicColors.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = comp.referenceId,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.accentCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = comp.category.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = civicColors.textPrimary
                    )
                }
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        Text(
                            text = comp.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = civicColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = comp.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = civicColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = civicColors.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Location: ${comp.address}", style = MaterialTheme.typography.labelSmall, color = civicColors.textPrimary)
                                Text(text = "Assigned Officer: ${comp.assignedOfficer}", style = MaterialTheme.typography.labelSmall, color = civicColors.accentCyan)
                                Text(text = "Submitted: ${comp.submittedDate}", style = MaterialTheme.typography.labelSmall, color = civicColors.textMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Resolution Lifecycle Timeline",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.accentTealBright
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        StatusTimelineView(timeline = comp.timeline)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (comp.status == ComplaintStatus.RESOLVED) {
                        OutlinedButton(
                            onClick = {
                                complaintToReopen = comp
                                reopenFeedbackText = ""
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PriorityCritical),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(PriorityCritical.copy(alpha = 0.5f))
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reopen Grievance", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = { selectedComplaintDetail = null },
                        colors = ButtonDefaults.buttonColors(containerColor = civicColors.accentTeal),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            containerColor = civicColors.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Reopen Confirmation Dialog
    complaintToReopen?.let { comp ->
        AlertDialog(
            onDismissRequest = { if (!isReopening) complaintToReopen = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.WarningAmber, contentDescription = null, tint = PriorityCritical)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reopen Complaint ${comp.referenceId}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = civicColors.textPrimary
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Please describe why this issue requires further municipal remediation (e.g., recurrence, incomplete fix, residual hazard):",
                        style = MaterialTheme.typography.bodySmall,
                        color = civicColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reopenFeedbackText,
                        onValueChange = { reopenFeedbackText = it },
                        placeholder = { Text("e.g. Road surface caved in again after rain...", color = civicColors.textMuted) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = civicColors.accentTeal,
                            unfocusedBorderColor = civicColors.inputBorder,
                            focusedTextColor = civicColors.textPrimary,
                            unfocusedTextColor = civicColors.textPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isReopening = true
                        viewModel.reopenComplaint(comp.referenceId, reopenFeedbackText) {
                            isReopening = false
                            complaintToReopen = null
                            selectedComplaintDetail = null
                        }
                    },
                    enabled = !isReopening,
                    colors = ButtonDefaults.buttonColors(containerColor = PriorityCritical),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isReopening) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Submit Escalation", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { complaintToReopen = null }) {
                    Text("Cancel", color = civicColors.textSecondary)
                }
            },
            containerColor = civicColors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ComplaintCard(
    complaint: Complaint,
    onClick: () -> Unit
) {
    val civicColors = MaterialTheme.civicColors
    val statusColor = when (complaint.status) {
        ComplaintStatus.SUBMITTED -> StatusSubmitted
        ComplaintStatus.UNDER_REVIEW -> StatusUnderReview
        ComplaintStatus.ASSIGNED -> StatusAssigned
        ComplaintStatus.IN_PROGRESS -> StatusInProgress
        ComplaintStatus.RESOLVED -> StatusResolved
        ComplaintStatus.REOPENED -> StatusSubmitted
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("complaint_item_${complaint.referenceId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            text = complaint.referenceId,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.accentCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = complaint.submittedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.textMuted
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = complaint.status.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getCategoryIcon(complaint.category),
                    contentDescription = null,
                    tint = civicColors.accentTealBright,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = complaint.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = civicColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = complaint.address,
                style = MaterialTheme.typography.bodyMedium,
                color = civicColors.textSecondary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step Progress Bar
            LinearProgressIndicator(
                progress = { (complaint.status.stepIndex + 1) / 5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = civicColors.accentTeal,
                trackColor = civicColors.cardBorder
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Step ${complaint.status.stepIndex + 1} of 5",
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.accentTealBright
                )
                Text(
                    text = complaint.assignedOfficer,
                    style = MaterialTheme.typography.labelSmall,
                    color = civicColors.textMuted
                )
            }
        }
    }
}
