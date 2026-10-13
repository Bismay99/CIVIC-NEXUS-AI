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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
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
 * - Light Mode: Premium Frosted Glass (Soft cool-blue mist, controlled translucency, fine top illumination, soft ambient shadows)
 * - Dark Mode: Midnight Glass (Layered navy-slate translucency, fine illuminated border, gentle specular reflection)
 */

val DefaultGlassCornerRadius = 16.dp
val DefaultGlassBorderWidth = 1.dp

/**
 * Modifier extension to safely apply background blur on supported Android API levels (>= 31).
 * On older devices, the translucent tint provides a graceful visual fallback without performance penalty.
 */
fun Modifier.glassmorphicBlur(radius: Dp = 16.dp): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.blur(radius)
    } else {
        this
    }
}

/**
 * GlassSurface provides the base frosted glass layer with subtle specular edge highlights,
 * translucent fill, fine gradient border, and controlled ambient shadow.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(DefaultGlassCornerRadius),
    backgroundColor: Color? = null,
    backgroundAlpha: Float? = null,
    borderWidth: Dp = DefaultGlassBorderWidth,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    elevation: Dp = 0.dp,
    specularHighlight: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val civicColors = MaterialTheme.civicColors
    val isDark = civicColors.isDark

    // Base glass fill with controlled translucency
    val defaultFill = if (isDark) {
        civicColors.glassSurface
    } else {
        Color.White.copy(alpha = 0.86f)
    }

    val glassFillColor = when {
        backgroundColor != null && backgroundAlpha != null -> backgroundColor.copy(alpha = backgroundAlpha)
        backgroundColor != null -> backgroundColor
        backgroundAlpha != null -> defaultFill.copy(alpha = backgroundAlpha)
        else -> defaultFill
    }

    // Refined specular gradient border for physical frosted glass refraction
    val resolvedBorderBrush = borderBrush ?: Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                civicColors.glassSpecular,
                (borderColor ?: civicColors.glassBorder).copy(alpha = 0.70f),
                (borderColor ?: civicColors.glassBorder).copy(alpha = 0.40f)
            )
        } else {
            listOf(
                civicColors.glassSpecular,
                (borderColor ?: civicColors.glassBorder).copy(alpha = 0.65f),
                (borderColor ?: civicColors.glassBorder).copy(alpha = 0.40f)
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
                        ambientColor = civicColors.glassShadow,
                        spotColor = if (isDark) civicColors.accentTeal.copy(alpha = 0.12f) else civicColors.accentCyan.copy(alpha = 0.08f)
                    )
                } else Modifier
            )
            .clip(shape)
            .background(glassFillColor)
            .border(width = borderWidth, brush = resolvedBorderBrush, shape = shape)
    ) {
        // Specular top hairline reflection for frosted glass depth
        if (specularHighlight) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isDark) {
                                listOf(
                                    civicColors.accentCyan.copy(alpha = 0.05f),
                                    civicColors.accentCyan.copy(alpha = 0.30f),
                                    civicColors.accentTeal.copy(alpha = 0.05f)
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.20f),
                                    Color.White.copy(alpha = 0.85f),
                                    Color.White.copy(alpha = 0.20f)
                                )
                            }
                        )
                    )
            )
        }

        content()
    }
}

/**
 * GlassCard wraps content in an elevated frosted glass panel with consistent padding
 * and optional click handling with ripple feedback.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(DefaultGlassCornerRadius),
    onClick: (() -> Unit)? = null,
    elevation: Dp = 2.dp,
    backgroundColor: Color? = null,
    backgroundAlpha: Float? = null,
    borderWidth: Dp = DefaultGlassBorderWidth,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = onClick
        )
    } else {
        Modifier
    }

    val tagModifier = if (testTag != null) {
        Modifier.testTag(testTag)
    } else {
        Modifier
    }

    GlassSurface(
        modifier = modifier
            .then(tagModifier)
            .then(clickableModifier),
        shape = shape,
        elevation = elevation,
        backgroundColor = backgroundColor,
        backgroundAlpha = backgroundAlpha,
        borderWidth = borderWidth,
        borderColor = borderColor,
        borderBrush = borderBrush
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
 * minimum 48dp touch target, fine edge highlights, and filled or outlined variants.
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
                    Color.White.copy(alpha = 0.45f),
                    civicColors.accentTealBright.copy(alpha = 0.65f)
                )
            ),
            shape = buttonShape
        )
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
            .shadow(
                elevation = if (isPrimary) 3.dp else 1.dp,
                shape = buttonShape,
                ambientColor = civicColors.glassShadow,
                spotColor = if (isPrimary) civicColors.accentTeal.copy(alpha = 0.35f) else Color.Transparent
            )
            .clip(buttonShape)
            .background(backgroundBrush)
            .then(borderModifier)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
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

    var isFocused by remember { mutableStateOf(false) }

    val inputShape = RoundedCornerShape(12.dp)
    val inputBg = if (isDark) {
        civicColors.inputBackground.copy(alpha = 0.78f)
    } else {
        Color.White.copy(alpha = 0.90f)
    }

    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFocused -> civicColors.accentTeal
        else -> civicColors.inputBorder.copy(alpha = 0.75f)
    }

    val borderWidth = if (isFocused || isError) 1.5.dp else 1.dp

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isError) MaterialTheme.colorScheme.error else civicColors.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
                .defaultMinSize(minHeight = 48.dp)
                .onFocusChanged { isFocused = it.isFocused }
                .clip(inputShape)
                .background(inputBg)
                .border(
                    width = borderWidth,
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
                            tint = when {
                                isError -> MaterialTheme.colorScheme.error
                                isFocused -> civicColors.accentTeal
                                else -> civicColors.accentTeal.copy(alpha = 0.8f)
                            },
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
 * with soft backdrop blur, fine edge illumination, and accessible actions.
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
            elevation = 8.dp,
            specularHighlight = true
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
                                .size(38.dp)
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Glowing teal indicator pill
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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = onActionClick
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
