package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.ui.CivicNexusViewModel
import com.example.ui.theme.*

@Composable
fun AiAssistantScreen(
    viewModel: CivicNexusViewModel,
    onNavigateToReport: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }
    val civicColors = MaterialTheme.civicColors
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val isGeminiConfigured = viewModel.isGeminiConfigured

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickQuestions = listOf(
        "How do I report a broken streetlight?",
        "How do I report a pothole?",
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
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("ai_assistant_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = civicColors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(civicColors.accentTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SmartToy,
                            contentDescription = "AI City Copilot",
                            tint = civicColors.accentTealBright,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI City Copilot",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = civicColors.textPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isGeminiConfigured) civicColors.accentTeal else PriorityHigh)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isGeminiConfigured) "Gemini 3.5 Flash Active" else "API Key Unset • Offline Directory Active",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = if (isGeminiConfigured) civicColors.accentTealBright else PriorityHigh
                            )
                        }
                    }
                }

                // New Chat action button
                FilledTonalButton(
                    onClick = { viewModel.clearChat() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = civicColors.surfaceVariant,
                        contentColor = civicColors.accentTeal
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 36.dp)
                        .testTag("ai_assistant_new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "New Chat",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Chat",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
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
            items(chatMessages, key = { it.id }) { message ->
                ChatBubble(
                    message = message,
                    onActionClick = { actionText ->
                        when {
                            actionText.contains("Report", ignoreCase = true) -> onNavigateToReport()
                            actionText == "Retry" -> {
                                message.retryPrompt?.let { viewModel.retryChatMessage(it) }
                            }
                            actionText.contains("Offline", ignoreCase = true) -> {
                                viewModel.requestOfflineGuidance(message.retryPrompt ?: "general")
                            }
                            else -> viewModel.sendChatMessage(actionText)
                        }
                    },
                    onRetryClick = {
                        message.retryPrompt?.let { viewModel.retryChatMessage(it) }
                    },
                    onOfflineGuideClick = {
                        viewModel.requestOfflineGuidance(message.retryPrompt ?: "general")
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
                    modifier = Modifier.clickable(enabled = !isChatLoading) {
                        viewModel.sendChatMessage(question)
                    }
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
            modifier = Modifier.fillMaxWidth()
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
                    placeholder = {
                        Text(
                            text = if (isChatLoading) "AI Copilot is responding..." else "Ask about broken streetlights, potholes, SLAs...",
                            color = civicColors.textMuted,
                            fontSize = 13.sp
                        )
                    },
                    enabled = !isChatLoading,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            val toSend = inputText.trim()
                            if (toSend.isNotEmpty() && !isChatLoading) {
                                viewModel.sendChatMessage(toSend)
                                inputText = ""
                            }
                        }
                    ),
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
                        if (toSend.isNotEmpty() && !isChatLoading) {
                            viewModel.sendChatMessage(toSend)
                            inputText = ""
                        }
                    },
                    enabled = inputText.trim().isNotEmpty() && !isChatLoading,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.trim().isNotEmpty() && !isChatLoading) civicColors.accentTeal else civicColors.surfaceVariant
                        )
                        .testTag("chat_send_button")
                ) {
                    if (isChatLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.trim().isNotEmpty()) Color.White else civicColors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onActionClick: (String) -> Unit,
    onRetryClick: () -> Unit = {},
    onOfflineGuideClick: () -> Unit = {}
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
                        .background(
                            when {
                                message.isError -> PriorityCritical.copy(alpha = 0.2f)
                                message.isOfflineGuidance -> PriorityHigh.copy(alpha = 0.2f)
                                else -> civicColors.accentTeal.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            message.isError -> Icons.Default.Warning
                            message.isOfflineGuidance -> Icons.AutoMirrored.Filled.MenuBook
                            else -> Icons.Outlined.SmartToy
                        },
                        contentDescription = null,
                        tint = when {
                            message.isError -> PriorityCritical
                            message.isOfflineGuidance -> PriorityHigh
                            else -> civicColors.accentTealBright
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                color = when {
                    isUser -> civicColors.surfaceVariant
                    message.isError -> PriorityCritical.copy(alpha = 0.12f)
                    message.isOfflineGuidance -> PriorityHigh.copy(alpha = 0.08f)
                    else -> civicColors.cardContainer
                },
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        when {
                            isUser -> civicColors.accentCyan.copy(alpha = 0.4f)
                            message.isError -> PriorityCritical.copy(alpha = 0.5f)
                            message.isOfflineGuidance -> PriorityHigh.copy(alpha = 0.35f)
                            else -> civicColors.cardBorder
                        }
                    )
                ),
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.isAiThinking) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = civicColors.accentTeal,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = civicColors.accentTeal
                            )
                        }
                    } else if (message.isError) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = PriorityCritical,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Request Failed",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PriorityCritical
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = civicColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onRetryClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = PriorityCritical),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 32.dp)
                                        .testTag("ai_chat_retry_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onOfflineGuideClick,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 32.dp)
                                        .testTag("ai_chat_offline_button")
                                ) {
                                    Text("Offline Guide", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = civicColors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when {
                            message.isGeminiPowered -> {
                                Text(
                                    text = "Gemini 3.5 Flash",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                    color = civicColors.accentTeal
                                )
                            }
                            message.isOfflineGuidance -> {
                                Text(
                                    text = "Offline Municipal Guide",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                    color = PriorityHigh
                                )
                            }
                            else -> {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
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

        // Suggested Action Pills (only for non-error or guidance responses)
        if (!isUser && !message.isError && message.suggestedActions.isNotEmpty()) {
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
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(civicColors.accentTeal.copy(alpha = 0.5f))
                        ),
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
