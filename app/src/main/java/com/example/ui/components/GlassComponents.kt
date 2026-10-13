package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.civicColors

/**
 * Civic Nexus Glassmorphism Design System:
 * - Light Mode: Premium Frosted Glass (Soft misty translucency, fine borders, ambient light)
 * - Dark Mode: Midnight Glass (Deep navy/charcoal translucency, subtle glow, visible edge borders)
 */

val DefaultGlassCornerRadius = 16.dp
val DefaultGlassBorderWidth = 1.dp

/**
 * Modifier extension to safely apply background blur on supported Android API levels (>= 31).
 * On older devices, the translucent tint provides graceful visual fallback without performance penalty.
 */
fun Modifier.glassmorphicBlur(radius: Dp = 16.dp): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.blur(radius)
    } else {
        this
    }
}

/**
 * GlassSurface provides the base frosted glass layer with subtle gradient shimmer,
 * translucent fill, fine border, and controlled shadow.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(DefaultGlassCornerRadius),
    borderWidth: Dp = DefaultGlassBorderWidth,
    elevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val civicColors = MaterialTheme.civicColors
    val isDark = civicColors.isDark

    // Glass fill tint
    val glassFillColor = if (isDark) {
        civicColors.cardContainer.copy(alpha = 0.78f)
    } else {
        Color.White.copy(alpha = 0.82f)
    }

    // Elegant specular gradient border for frosted depth
    val borderBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                civicColors.accentTeal.copy(alpha = 0.35f),
                civicColors.cardBorder.copy(alpha = 0.60f),
                civicColors.accentBlue.copy(alpha = 0.20f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.95f),
                civicColors.cardBorder.copy(alpha = 0.50f),
                civicColors.accentCyan.copy(alpha = 0.25f)
            )
        }
    )

    Box(
        modifier = modifier
            .then(
                if (elevation > 0.dp) {
                    Modifier.shadow(
                        elevation = elevation,
                        shape = shape,
                        ambientColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.4f) else Color(0xFF64748B).copy(alpha = 0.12f),
                        spotColor = if (isDark) civicColors.accentTeal.copy(alpha = 0.15f) else Color(0xFF0F172A).copy(alpha = 0.08f)
                    )
                } else Modifier
            )
            .clip(shape)
            .background(glassFillColor)
            .border(width = borderWidth, brush = borderBrush, shape = shape),
        content = content
    )
}

/**
 * GlassCard wraps content in an elevated frosted glass panel with consistent padding
 * and optional click handling.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(DefaultGlassCornerRadius),
    onClick: (() -> Unit)? = null,
    elevation: Dp = 2.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    GlassSurface(
        modifier = modifier.then(clickableModifier),
        shape = shape,
        elevation = elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * GlassButton provides a tactile, luminous glass action button with accessible contrast,
 * fine edge highlights, and filled or outlined variants.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    testTag: String = "glass_button"
) {
    val civicColors = MaterialTheme.civicColors
    val isDark = civicColors.isDark

    val buttonShape = RoundedCornerShape(12.dp)

    val backgroundBrush = if (isPrimary) {
        Brush.horizontalGradient(
            colors = if (isDark) {
                listOf(civicColors.accentTeal, civicColors.accentBlue)
            } else {
                listOf(civicColors.accentTeal, civicColors.accentCyan)
            }
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                civicColors.cardContainer.copy(alpha = 0.70f),
                civicColors.surfaceVariant.copy(alpha = 0.70f)
            )
        )
    }

    val contentColor = if (isPrimary) {
        Color.White
    } else {
        civicColors.textPrimary
    }

    val borderModifier = if (!isPrimary) {
        Modifier.border(
            width = 1.dp,
            color = civicColors.cardBorder.copy(alpha = 0.7f),
            shape = buttonShape
        )
    } else {
        Modifier.border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.4f),
                    civicColors.accentTealBright.copy(alpha = 0.6f)
                )
            ),
            shape = buttonShape
        )
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .shadow(
                elevation = if (isPrimary) 3.dp else 1.dp,
                shape = buttonShape,
                spotColor = if (isPrimary) civicColors.accentTeal.copy(alpha = 0.35f) else Color.Transparent
            )
            .clip(buttonShape)
            .background(backgroundBrush)
            .then(borderModifier)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                ),
                color = contentColor
            )
        }
    }
}

/**
 * GlassTextField provides a clear, high-readability glass input field with distinct
 * focus states, label, placeholder, and error highlighting.
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    testTag: String = "glass_text_field"
) {
    val civicColors = MaterialTheme.civicColors
    val isDark = civicColors.isDark

    val inputShape = RoundedCornerShape(12.dp)
    val inputBg = if (isDark) {
        civicColors.inputBackground.copy(alpha = 0.75f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }

    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        else -> civicColors.inputBorder.copy(alpha = 0.8f)
    }

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = civicColors.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
                .clip(inputShape)
                .background(inputBg)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = inputShape
                )
                .padding(horizontal = 14.dp, vertical = 13.dp),
            textStyle = TextStyle(
                color = civicColors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(civicColors.accentTeal),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = if (isError) MaterialTheme.colorScheme.error else civicColors.accentTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && !placeholder.isNullOrBlank()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    color = civicColors.textMuted,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        trailingIcon()
                    }
                }
            }
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

/**
 * GlassDialog presents an alert or confirmation in a frosted glass modal container
 * with soft backdrop blur and crisp edge border.
 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    properties: DialogProperties = DialogProperties(),
    content: @Composable ColumnScope.() -> Unit
) {
    val civicColors = MaterialTheme.civicColors

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        GlassSurface(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(civicColors.accentTeal.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = civicColors.accentTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = civicColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

/**
 * GlassSectionHeader provides a consistent category/section label with an accent glow pill
 * and optional action label.
 */
@Composable
fun GlassSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val civicColors = MaterialTheme.civicColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Subtle glowing teal indicator pill
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(civicColors.accentTeal)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    ),
                    color = civicColors.textPrimary
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = civicColors.textSecondary
                    )
                }
            }
        }

        if (!actionLabel.isNullOrBlank() && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = civicColors.accentTeal,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
