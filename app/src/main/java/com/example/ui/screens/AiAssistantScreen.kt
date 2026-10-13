package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Psychology
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
import com.example.model.ChatMessage
import com.example.ui.CivicNexusViewModel
import com.example.ui.theme.*

@Composable
fun AiAssistantScreen(
    viewModel: CivicNexusViewModel,
    onNavigateToReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val civicColors = MaterialTheme.civicColors
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isGeminiConfigured = viewModel.isGeminiConfigured

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickQuestions = listOf(
        "How do I report a pothole?",
        "Status of complaint CN-2026-1042",
        "Emergency helpline numbers",
        "How does the self-healing city AI work?",
        "Waste segregation guidelines"
    )

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(civicColors.background)
    ) {
        // AI Header Bar
        Surface(
            color = civicColors.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(civicColors.accentTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Assistant",
                            tint = civicColors.accentTealBright,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI City Assistant",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isGeminiConfigured) civicColors.accentTeal else civicColors.accentCyan)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGeminiConfigured) "Gemini 3.5 Flash Active" else "Civic Neural Engine Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isGeminiConfigured) civicColors.accentTealBright else civicColors.accentCyan
                            )
                        }
                    }
                }

                Surface(
                    color = civicColors.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Smart Grid",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = civicColors.textSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubble(
                    message = message,
                    onActionClick = { actionText ->
                        if (actionText.contains("Report", ignoreCase = true)) {
                            onNavigateToReport()
                        } else {
                            viewModel.sendChatMessage(actionText)
                        }
                    }
                )
            }
        }

        // Quick Suggestions Carousel
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(civicColors.surface)
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickQuestions) { question ->
                Surface(
                    color = civicColors.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.cardBorder)),
                    modifier = Modifier.clickable { viewModel.sendChatMessage(question) }
                ) {
                    Text(
                        text = question,
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.accentTeal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Bottom Input Bar
        Surface(
            color = civicColors.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask about complaints, services, emergencies...", color = civicColors.textMuted, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = civicColors.accentTeal,
                        unfocusedBorderColor = civicColors.inputBorder,
                        focusedTextColor = civicColors.textPrimary,
                        unfocusedTextColor = civicColors.textPrimary,
                        focusedContainerColor = civicColors.inputBackground,
                        unfocusedContainerColor = civicColors.inputBackground
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val toSend = inputText.trim()
                        if (toSend.isNotEmpty()) {
                            viewModel.sendChatMessage(toSend)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(civicColors.accentTeal)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onActionClick: (String) -> Unit
) {
    val civicColors = MaterialTheme.civicColors
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(civicColors.accentTeal.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = civicColors.accentTealBright,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                color = if (isUser) civicColors.surfaceVariant else civicColors.cardContainer,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isUser) civicColors.accentCyan.copy(alpha = 0.4f) else civicColors.cardBorder
                    )
                ),
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.isAiThinking) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = civicColors.accentTeal,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = civicColors.textMuted
                            )
                        }
                    } else {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = civicColors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isGeminiPowered) {
                            Text(
                                text = "Gemini 3.5 Flash",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = civicColors.accentTeal
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Text(
                            text = message.timestamp,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = civicColors.textMuted
                        )
                    }
                }
            }
        }

        // Suggested Action Pills
        if (!isUser && message.suggestedActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                message.suggestedActions.forEach { action ->
                    Surface(
                        color = civicColors.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(civicColors.accentTeal.copy(alpha = 0.5f))),
                        modifier = Modifier.clickable { onActionClick(action) }
                    ) {
                        Text(
                            text = action,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = civicColors.accentTeal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
