package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmergencyFacility
import com.example.model.Incident
import com.example.model.IncidentCategory
import com.example.model.IncidentSeverity
import com.example.ui.CivicNexusViewModel
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.*

@Composable
fun MapScreen(
    viewModel: CivicNexusViewModel,
    onNavigateToReport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors
    val context = LocalContext.current
    val incidents by viewModel.incidents.collectAsState()
    val facilities by viewModel.emergencyFacilities.collectAsState()
    val categoryFilter by viewModel.mapCategoryFilter.collectAsState()
    val selectedIncident by viewModel.selectedMapIncident.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isListView by remember { mutableStateOf(false) }
    var heatmapMode by remember { mutableStateOf(false) }
    var showFacilitiesOnly by remember { mutableStateOf(false) }
    var selectedFacility by remember { mutableStateOf<EmergencyFacility?>(null) }

    val filteredIncidents = remember(incidents, categoryFilter, searchQuery) {
        incidents.filter { inc ->
            val matchesCategory = categoryFilter == null || inc.category == categoryFilter
            val matchesQuery = searchQuery.isBlank() ||
                    inc.title.contains(searchQuery, ignoreCase = true) ||
                    inc.description.contains(searchQuery, ignoreCase = true) ||
                    inc.locationName.contains(searchQuery, ignoreCase = true) ||
                    inc.department.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(civicColors.background)
    ) {
        // Top Filter Bar
        Surface(
            color = civicColors.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Smart City Spatial Grid",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                        Text(
                            text = "Bhubaneswar Sector Network • Geo-Telemetry",
                            style = MaterialTheme.typography.labelSmall,
                            color = civicColors.accentTealBright
                        )
                    }

                    // Heatmap & Facility Mode Toggles
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = heatmapMode,
                            onClick = { heatmapMode = !heatmapMode },
                            label = { Text(text = "Heatmap", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PriorityHigh.copy(alpha = 0.3f),
                                selectedLabelColor = PriorityHigh,
                                containerColor = civicColors.cardContainer,
                                labelColor = civicColors.textSecondary
                            )
                        )

                        FilterChip(
                            selected = showFacilitiesOnly,
                            onClick = { showFacilitiesOnly = !showFacilitiesOnly },
                            label = { Text(text = "Facilities", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = civicColors.accentCyan.copy(alpha = 0.3f),
                                selectedLabelColor = civicColors.accentCyan,
                                containerColor = civicColors.cardContainer,
                                labelColor = civicColors.textSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = categoryFilter == null && !showFacilitiesOnly,
                            onClick = {
                                viewModel.setMapCategoryFilter(null)
                                showFacilitiesOnly = false
                            },
                            label = { Text(text = "All Incidents (${incidents.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = civicColors.accentTeal,
                                selectedLabelColor = Color.White,
                                containerColor = civicColors.surfaceVariant,
                                labelColor = civicColors.textSecondary
                            )
                        )
                    }

                    items(IncidentCategory.entries) { category ->
                        val count = incidents.count { it.category == category }
                        FilterChip(
                            selected = categoryFilter == category,
                            onClick = {
                                viewModel.setMapCategoryFilter(if (categoryFilter == category) null else category)
                                showFacilitiesOnly = false
                            },
                            label = { Text(text = "${category.displayName} ($count)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = civicColors.accentTeal,
                                selectedLabelColor = Color.White,
                                containerColor = civicColors.surfaceVariant,
                                labelColor = civicColors.textSecondary
                            )
                        )
                    }
                }
            }
        }

        // Interactive Spatial Grid Canvas
        val canvasBgColor = if (civicColors.isDark) Color(0xFF060A14) else Color(0xFFE2E8F0)
        val gridLineColor = if (civicColors.isDark) Color(0xFF131D31) else Color(0xFFCBD5E1)
        val roadLineColor = if (civicColors.isDark) Color(0xFF1E2D4A) else Color(0xFF94A3B8)
        val innerCrossColor = if (civicColors.isDark) Color(0xFF080E1E) else Color.White

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(canvasBgColor)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // Hit test for nearest incident or facility on canvas
                            val w = size.width
                            val h = size.height

                            var tappedInc: Incident? = null
                            filteredIncidents.forEachIndexed { i, inc ->
                                val normX = 0.15f + (i * 0.14f) % 0.7f
                                val normY = 0.2f + ((i * 37) % 60) / 100f
                                val px = normX * w
                                val py = normY * h
                                val distSq = (offset.x - px) * (offset.x - px) + (offset.y - py) * (offset.y - py)
                                if (distSq < 1600f) { // 40px radius
                                    tappedInc = inc
                                }
                            }

                            if (tappedInc != null) {
                                viewModel.selectMapIncident(tappedInc)
                                selectedFacility = null
                            } else {
                                // test facility
                                facilities.forEachIndexed { j, fac ->
                                    val fx = (0.25f + (j * 0.18f) % 0.6f) * w
                                    val fy = (0.35f + ((j * 23) % 45) / 100f) * h
                                    val dist = (offset.x - fx) * (offset.x - fx) + (offset.y - fy) * (offset.y - fy)
                                    if (dist < 1600f) {
                                        selectedFacility = fac
                                        viewModel.selectMapIncident(null)
                                    }
                                }
                            }
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height

                // 1. Tech Grid Lines
                val gridSpacing = 48.dp.toPx()
                var x = 0f
                while (x < canvasW) {
                    drawLine(
                        color = gridLineColor,
                        start = Offset(x, 0f),
                        end = Offset(x, canvasH),
                        strokeWidth = 1f
                    )
                    x += gridSpacing
                }

                var y = 0f
                while (y < canvasH) {
                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, y),
                        end = Offset(canvasW, y),
                        strokeWidth = 1f
                    )
                    y += gridSpacing
                }

                // 2. Simulated Smart City Arterial Highways & Corridors
                drawLine(
                    color = roadLineColor,
                    start = Offset(0f, canvasH * 0.35f),
                    end = Offset(canvasW, canvasH * 0.35f),
                    strokeWidth = 8f
                )
                drawLine(
                    color = roadLineColor,
                    start = Offset(canvasW * 0.45f, 0f),
                    end = Offset(canvasW * 0.45f, canvasH),
                    strokeWidth = 8f
                )
                drawLine(
                    color = roadLineColor.copy(alpha = 0.7f),
                    start = Offset(0f, canvasH * 0.75f),
                    end = Offset(canvasW, canvasH * 0.65f),
                    strokeWidth = 6f
                )

                // 3. Diagonal Ring Road
                val ringPath = Path().apply {
                    moveTo(canvasW * 0.1f, canvasH * 0.15f)
                    quadraticTo(canvasW * 0.7f, canvasH * 0.25f, canvasW * 0.85f, canvasH * 0.85f)
                }
                drawPath(path = ringPath, color = roadLineColor.copy(alpha = 0.8f), style = Stroke(width = 6f))

                // 4. Heatmap intensity zones if active
                if (heatmapMode) {
                    drawCircle(
                        color = PriorityCritical.copy(alpha = 0.18f),
                        radius = 90.dp.toPx(),
                        center = Offset(canvasW * 0.45f, canvasH * 0.35f)
                    )
                    drawCircle(
                        color = PriorityHigh.copy(alpha = 0.15f),
                        radius = 70.dp.toPx(),
                        center = Offset(canvasW * 0.7f, canvasH * 0.6f)
                    )
                }

                // 5. Emergency Facilities Markers
                if (showFacilitiesOnly || !heatmapMode) {
                    facilities.forEachIndexed { idx, fac ->
                        val fx = (0.25f + (idx * 0.18f) % 0.6f) * canvasW
                        val fy = (0.35f + ((idx * 23) % 45) / 100f) * canvasH

                        // Cyan square
                        drawRect(
                            color = civicColors.accentCyan.copy(alpha = 0.25f),
                            topLeft = Offset(fx - 14.dp.toPx(), fy - 14.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(28.dp.toPx(), 28.dp.toPx())
                        )
                        drawRect(
                            color = civicColors.accentCyan,
                            topLeft = Offset(fx - 10.dp.toPx(), fy - 10.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(20.dp.toPx(), 20.dp.toPx())
                        )
                        // Inner cross
                        drawLine(
                            color = innerCrossColor,
                            start = Offset(fx, fy - 6.dp.toPx()),
                            end = Offset(fx, fy + 6.dp.toPx()),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = innerCrossColor,
                            start = Offset(fx - 6.dp.toPx(), fy),
                            end = Offset(fx + 6.dp.toPx(), fy),
                            strokeWidth = 3f
                        )
                    }
                }

                // 6. Incident Markers
                if (!showFacilitiesOnly) {
                    filteredIncidents.forEachIndexed { idx, inc ->
                        val normX = 0.15f + (idx * 0.14f) % 0.7f
                        val normY = 0.2f + ((idx * 37) % 60) / 100f
                        val px = normX * canvasW
                        val py = normY * canvasH

                        val markerColor = when (inc.severity) {
                            IncidentSeverity.CRITICAL -> PriorityCritical
                            IncidentSeverity.HIGH -> PriorityHigh
                            IncidentSeverity.MEDIUM -> PriorityMedium
                            IncidentSeverity.LOW -> PriorityLow
                        }

                        val isTargeted = inc.id == selectedIncident?.id

                        // Outer glowing ripple
                        drawCircle(
                            color = markerColor.copy(alpha = if (isTargeted) 0.45f else 0.2f),
                            radius = if (isTargeted) 22.dp.toPx() else 14.dp.toPx(),
                            center = Offset(px, py)
                        )

                        // Core Pin
                        drawCircle(
                            color = markerColor,
                            radius = if (isTargeted) 10.dp.toPx() else 7.dp.toPx(),
                            center = Offset(px, py)
                        )

                        // Center white core
                        drawCircle(
                            color = Color.White,
                            radius = if (isTargeted) 4.dp.toPx() else 2.5.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }
            }

            // Compass & Coordinate HUD Overlay
            Surface(
                color = civicColors.surface.copy(alpha = 0.88f),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Compass",
                            tint = civicColors.accentTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "20.2961° N, 85.8245° E",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                    }
                    Text(
                        text = "Bhubaneswar Smart Hub Grid",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = civicColors.textMuted
                    )
                }
            }

            // Legend Overlay (Bottom Left of Canvas)
            Surface(
                color = civicColors.surface.copy(alpha = 0.88f),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = PriorityCritical, label = "Critical")
                    LegendItem(color = PriorityHigh, label = "High")
                    LegendItem(color = PriorityMedium, label = "Medium")
                    LegendItem(color = civicColors.accentCyan, label = "Facility")
                }
            }
        }

        // Incident or Facility Detail Drawer Card
        AnimatedVisibility(visible = selectedIncident != null || selectedFacility != null) {
            Surface(
                color = civicColors.surface,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp)
            ) {
                if (selectedIncident != null) {
                    val inc = selectedIncident!!
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = when (inc.severity) {
                                        IncidentSeverity.CRITICAL -> PriorityCritical.copy(alpha = 0.2f)
                                        IncidentSeverity.HIGH -> PriorityHigh.copy(alpha = 0.2f)
                                        IncidentSeverity.MEDIUM -> PriorityMedium.copy(alpha = 0.2f)
                                        IncidentSeverity.LOW -> PriorityLow.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = inc.severity.label.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = when (inc.severity) {
                                            IncidentSeverity.CRITICAL -> PriorityCritical
                                            IncidentSeverity.HIGH -> PriorityHigh
                                            IncidentSeverity.MEDIUM -> PriorityMedium
                                            IncidentSeverity.LOW -> PriorityLow
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = inc.category.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = civicColors.accentTeal
                                )
                            }

                            IconButton(
                                onClick = { viewModel.selectMapIncident(null) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = civicColors.textSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = inc.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                        Text(
                            text = inc.locationName,
                            style = MaterialTheme.typography.bodySmall,
                            color = civicColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = inc.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = civicColors.textSecondary,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Department: ${inc.department}",
                                style = MaterialTheme.typography.labelSmall,
                                color = civicColors.accentCyan
                            )

                            Button(
                                onClick = { viewModel.upvoteIncident(inc.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = civicColors.accentTeal),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ThumbUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Verify (${inc.verifiedCount})", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (selectedFacility != null) {
                    val fac = selectedFacility!!
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = civicColors.accentCyan.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = fac.type.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = civicColors.accentCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = { selectedFacility = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = civicColors.textSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = fac.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                        Text(
                            text = "${fac.distanceKm} km away • ${fac.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = civicColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${fac.contactNumber}"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = civicColors.accentCyan),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Call ${fac.contactNumber}", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    val civicColors = MaterialTheme.civicColors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = civicColors.textSecondary
        )
    }
}
