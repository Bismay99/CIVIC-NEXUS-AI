package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.model.IncidentCategory
import com.example.model.IncidentSeverity
import com.example.ui.CivicNexusViewModel
import com.example.ui.SubScreen
import com.example.ui.components.AiAnalysisCard
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.*

@Composable
fun ReportScreen(
    viewModel: CivicNexusViewModel,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors
    val selectedCategory by viewModel.reportCategory.collectAsState()
    val reportTitle by viewModel.reportTitle.collectAsState()
    val reportDescription by viewModel.reportDescription.collectAsState()
    val reportAddress by viewModel.reportAddress.collectAsState()
    val reportLatitude by viewModel.reportLatitude.collectAsState()
    val reportLongitude by viewModel.reportLongitude.collectAsState()
    val reportHasPhoto by viewModel.reportHasPhoto.collectAsState()
    val reportPhotoUri by viewModel.reportPhotoUri.collectAsState()
    val aiAnalysis by viewModel.reportAiAnalysis.collectAsState()
    val isAnalyzingAi by viewModel.isAnalyzingAi.collectAsState()
    val isSubmitting by viewModel.isSubmittingReport.collectAsState()
    val lastSubmittedComplaint by viewModel.lastSubmittedComplaint.collectAsState()
    val submissionError by viewModel.submissionError.collectAsState()
    val activeDraft by viewModel.activeDraft.collectAsState()
    val incidents by viewModel.incidents.collectAsState()

    var showEditAiDialog by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember { mutableStateOf(true) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var draftSavedNotice by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            viewModel.setReportCoordinates(20.3548, 85.8195, "KIIT Main Avenue, Sector 3, Patia")
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setReportPhotoUri(uri.toString())
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenTexts = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = spokenTexts?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                val current = reportDescription
                viewModel.setReportDescription(if (current.isBlank()) spoken else "$current $spoken")
            }
        }
    }

    // Duplicate detection based on existing active incidents in same category
    val nearbyDuplicates = remember(incidents, selectedCategory) {
        incidents.filter { it.category == selectedCategory }
    }

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
                    text = "AI Civic Complaint Reporting",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = civicColors.textPrimary
                )
                Text(
                    text = "Smart City Neural Triage • Self-Healing Dispatch",
                    style = MaterialTheme.typography.labelMedium,
                    color = civicColors.accentTealBright
                )
            }
        }

        // Offline Draft Recovery Banner
        val currentDraft = activeDraft
        if (currentDraft != null) {
            item {
                Surface(
                    color = civicColors.accentCyan.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(civicColors.accentCyan.copy(alpha = 0.4f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = civicColors.accentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Saved Offline Draft Available",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = civicColors.textPrimary
                                )
                                val draftSummary = if (currentDraft.title.isNotBlank()) currentDraft.title else currentDraft.category
                                Text(
                                    text = draftSummary,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = civicColors.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { viewModel.restoreDraft(currentDraft) },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Restore", color = civicColors.accentCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = { viewModel.discardDraft() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Discard draft", tint = civicColors.textMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Duplicate Awareness Banner
        if (nearbyDuplicates.isNotEmpty()) {
            item {
                Surface(
                    color = civicColors.accentTeal.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(civicColors.accentTeal.copy(alpha = 0.3f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.ShareLocation, contentDescription = null, tint = civicColors.accentTealBright, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Duplicate Awareness Alert",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = civicColors.accentTealBright
                            )
                            Text(
                                text = "A similar issue '${nearbyDuplicates.first().title}' is already logged in this sector with ${nearbyDuplicates.first().verifiedCount} verifications.",
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // 1. Complaint Category Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. Select Category",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = civicColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(IncidentCategory.entries) { category ->
                            val isSelected = selectedCategory == category
                            Surface(
                                color = if (isSelected) civicColors.accentTeal else civicColors.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                                modifier = Modifier
                                    .clickable { viewModel.setReportCategory(category) }
                                    .testTag("report_category_${category.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(category),
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else civicColors.accentTealBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else civicColors.textPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Incident Description & Title
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. Incident Details",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                        IconButton(
                            onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the civic issue...")
                                }
                                try {
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    // Speech recognition fallback
                                }
                            },
                            modifier = Modifier.testTag("report_voice_input_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Assisted Input",
                                tint = civicColors.accentTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = reportTitle,
                        onValueChange = {
                            viewModel.setReportTitle(it)
                            validationError = null
                        },
                        label = { Text("Incident Title / Headline (Optional)", color = civicColors.textSecondary) },
                        placeholder = { Text("e.g. Deep pothole damaging vehicles", color = civicColors.textMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = civicColors.accentTeal,
                            unfocusedBorderColor = civicColors.inputBorder,
                            focusedTextColor = civicColors.textPrimary,
                            unfocusedTextColor = civicColors.textPrimary,
                            focusedContainerColor = civicColors.inputBackground,
                            unfocusedContainerColor = civicColors.inputBackground
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = reportDescription,
                        onValueChange = {
                            viewModel.setReportDescription(it)
                            validationError = null
                        },
                        label = { Text("Detailed Description *", color = civicColors.textSecondary) },
                        placeholder = { Text("Describe the hazard, location markers, depth, water level, or urgency...", color = civicColors.textMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("report_description_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = civicColors.accentTeal,
                            unfocusedBorderColor = civicColors.inputBorder,
                            focusedTextColor = civicColors.textPrimary,
                            unfocusedTextColor = civicColors.textPrimary,
                            focusedContainerColor = civicColors.inputBackground,
                            unfocusedContainerColor = civicColors.inputBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = validationError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = PriorityCritical
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Trigger AI Incident Analysis Button
                    Button(
                        onClick = { viewModel.triggerAiAnalysis() },
                        enabled = !isAnalyzingAi,
                        colors = ButtonDefaults.buttonColors(containerColor = civicColors.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_analyze_ai_button")
                    ) {
                        if (isAnalyzingAi) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = civicColors.accentTeal,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analyzing with Civic Neural Triage...",
                                style = MaterialTheme.typography.labelMedium,
                                color = civicColors.accentTeal
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = civicColors.accentTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analyze with Civic AI (Auto-Triage)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = civicColors.accentTeal
                            )
                        }
                    }
                }
            }
        }

        // 3. AI Incident Analysis Result Card
        aiAnalysis?.let { analysis ->
            item {
                AiAnalysisCard(
                    analysis = analysis,
                    onEditSuggestionClick = { showEditAiDialog = true }
                )
            }
        }

        // 4. Photographic Evidence
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "3. Photographic Evidence",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = civicColors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (reportHasPhoto) {
                        Surface(
                            color = civicColors.surfaceVariant,
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
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(civicColors.accentTeal.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = null,
                                            tint = civicColors.accentTealBright,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (reportPhotoUri != null) "selected_evidence_photo.jpg" else "civic_incident_capture.jpg",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = civicColors.textPrimary
                                        )
                                        Text(
                                            text = if (reportPhotoUri != null) "Gallery Selected • Geo-Tagged" else "Geo-Tagged • 1.8 MB • Timestamped",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = civicColors.accentTealBright
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.setReportPhotoUri(null)
                                        viewModel.toggleReportHasPhoto(false)
                                    },
                                    modifier = Modifier.testTag("remove_photo_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove photo",
                                        tint = PriorityCritical
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("attach_photo_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = civicColors.accentCyan),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Capture / Pick Evidence Photo (Gallery)")
                        }
                    }
                }
            }
        }

        // 5. GPS Location & Address Review
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = civicColors.cardContainer),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "4. GPS Location",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )

                        // GPS Fetch trigger
                        FilledTonalButton(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = civicColors.accentTeal.copy(alpha = 0.2f),
                                contentColor = civicColors.accentTealBright
                            ),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Update GPS", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // GPS Coordinates Pill
                    Surface(
                        color = civicColors.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = civicColors.accentTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lat: $reportLatitude° N, Lng: $reportLongitude° E (Accuracy: ±4.2m)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = civicColors.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = reportAddress,
                        onValueChange = { viewModel.setReportAddress(it) },
                        label = { Text("Civic Address / Landmark", color = civicColors.textSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_address_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = civicColors.accentTeal,
                            unfocusedBorderColor = civicColors.inputBorder,
                            focusedTextColor = civicColors.textPrimary,
                            unfocusedTextColor = civicColors.textPrimary,
                            focusedContainerColor = civicColors.inputBackground,
                            unfocusedContainerColor = civicColors.inputBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Error Banner
        if (submissionError != null) {
            item {
                Surface(
                    color = PriorityCritical.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PriorityCritical.copy(alpha = 0.4f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = PriorityCritical,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = submissionError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriorityCritical,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                validationError = null
                                viewModel.clearSubmissionError()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = PriorityCritical,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Submit Button
        item {
            Button(
                onClick = {
                    validationError = null
                    viewModel.clearSubmissionError()
                    if (reportDescription.isBlank() && reportTitle.isBlank()) {
                        validationError = "Please provide an incident description or title before submitting."
                    } else {
                        viewModel.submitComplaint()
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = civicColors.accentTeal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("report_submit_complaint_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Submitting to Civic Nexus...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Civic Complaint",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    viewModel.saveDraft()
                    draftSavedNotice = true
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("save_offline_draft_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = civicColors.accentCyan),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)
                )
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (draftSavedNotice) "Draft Saved to Device ✓" else "Save as Offline Draft",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Submission Confirmation Dialog
    lastSubmittedComplaint?.let { complaint ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuccessDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(civicColors.accentTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Complaint Submitted!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                        Text(
                            text = "Reference: ${complaint.referenceId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.accentTealBright
                        )
                    }
                }
            },
            text = {
                Column {
                    Surface(
                        color = civicColors.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Department Routing: ${complaint.aiAnalysis.recommendedDepartment}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = civicColors.accentCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Estimated Triage: Initial review in < 15 mins",
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Location: ${complaint.address}",
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "You can track real-time resolution progress, crew dispatch notes, and officer updates under 'My Complaints'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = civicColors.textSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissSuccessDialog()
                        viewModel.openSubScreen(SubScreen.ComplaintDetail(complaint))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = civicColors.accentTeal)
                ) {
                    Text(text = "Track Status", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSuccessDialog() }) {
                    Text(text = "Done", color = civicColors.textSecondary)
                }
            },
            containerColor = civicColors.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Edit AI Analysis Dialog
    if (showEditAiDialog && aiAnalysis != null) {
        var editedCat by remember { mutableStateOf(aiAnalysis!!.suggestedCategory) }
        var editedPrio by remember { mutableStateOf(aiAnalysis!!.suggestedPriority) }
        var editedDept by remember { mutableStateOf(aiAnalysis!!.recommendedDepartment) }

        AlertDialog(
            onDismissRequest = { showEditAiDialog = false },
            title = {
                Text(
                    text = "Review & Edit AI Suggestions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = civicColors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Modify category:", style = MaterialTheme.typography.labelSmall, color = civicColors.textMuted)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(IncidentCategory.entries) { cat ->
                            FilterChip(
                                selected = editedCat == cat,
                                onClick = { editedCat = cat },
                                label = { Text(cat.displayName, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = civicColors.accentTeal,
                                    selectedLabelColor = Color.White,
                                    containerColor = civicColors.surfaceVariant,
                                    labelColor = civicColors.textSecondary
                                )
                            )
                        }
                    }

                    Text(text = "Modify priority:", style = MaterialTheme.typography.labelSmall, color = civicColors.textMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IncidentSeverity.entries.forEach { prio ->
                            FilterChip(
                                selected = editedPrio == prio,
                                onClick = { editedPrio = prio },
                                label = { Text(prio.label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = civicColors.accentTeal,
                                    selectedLabelColor = Color.White,
                                    containerColor = civicColors.surfaceVariant,
                                    labelColor = civicColors.textSecondary
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editedDept,
                        onValueChange = { editedDept = it },
                        label = { Text("Department", color = civicColors.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = civicColors.accentTeal,
                            unfocusedBorderColor = civicColors.inputBorder,
                            focusedTextColor = civicColors.textPrimary,
                            unfocusedTextColor = civicColors.textPrimary,
                            focusedContainerColor = civicColors.inputBackground,
                            unfocusedContainerColor = civicColors.inputBackground
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAiAnalysisReview(editedCat, editedPrio, editedDept)
                        showEditAiDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = civicColors.accentTeal)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditAiDialog = false }) {
                    Text("Cancel", color = civicColors.textSecondary)
                }
            },
            containerColor = civicColors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
