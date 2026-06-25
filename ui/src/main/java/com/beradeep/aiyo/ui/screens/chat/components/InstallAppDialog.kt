package com.beradeep.aiyo.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beradeep.aiyo.ui.AiyoTheme
import com.beradeep.aiyo.ui.LocalTypography
import com.beradeep.aiyo.ui.basics.components.AlertDialog
import com.beradeep.aiyo.ui.basics.components.Button
import com.beradeep.aiyo.ui.basics.components.ButtonVariant
import com.beradeep.aiyo.ui.basics.components.Text
import com.beradeep.aiyo.ui.basics.components.card.Card
import com.beradeep.aiyo.ui.basics.components.textfield.OutlinedTextField

@Composable
fun InstallAppDialog(
    initial: String = "Aiyo Web App",
    onInstall: (String, String, String) -> Unit, // passes appName, emoji, colorHex
    onDismiss: () -> Unit
) {
    var appName by remember { mutableStateOf(initial) }
    var selectedEmoji by remember { mutableStateOf("📱") }
    var customEmoji by remember { mutableStateOf("") }

    // Predefined colors
    val colorsList = remember {
        listOf(
            "#5E35B1" to Color(0xFF5E35B1), // Indigo
            "#D81B60" to Color(0xFFD81B60), // Pink
            "#0284C7" to Color(0xFF0284C7), // Blue
            "#10B981" to Color(0xFF10B981), // Emerald
            "#FFB300" to Color(0xFFFFB300), // Amber
            "#1F2937" to Color(0xFF1F2937) // Charcoal
        )
    }
    var selectedColor by remember { mutableStateOf(colorsList[0]) }

    val activeEmoji = if (customEmoji.isNotBlank()) {
        customEmoji.trim()
    } else {
        selectedEmoji
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        onConfirmClick = { onInstall(appName.takeIf { it.isNotBlank() } ?: initial, activeEmoji, selectedColor.first) },
        title = "Install Application",
        text = "Customize your home screen shortcut",
        confirmButtonText = "Install",
        dismissButtonText = "Cancel",
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "Customize Icon", style = LocalTypography.current.h3)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Icon Preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(selectedColor.second, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeEmoji,
                                style = LocalTypography.current.h1.copy(fontSize = 40.sp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // App Name Input
                    Text(text = "App Name", style = LocalTypography.current.body2)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        placeholder = { Text("Aiyo Web App") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Color Picker
                    Text(text = "Icon Background Color", style = LocalTypography.current.body2)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        colorsList.forEach { colorPair ->
                            val isColorSelected = colorPair.first == selectedColor.first
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(colorPair.second, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable { selectedColor = colorPair }
                                    .border(
                                        width = if (isColorSelected) 3.dp else 1.dp,
                                        color = if (isColorSelected) AiyoTheme.colors.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Emoji Picker
                    Text(text = "Predefined Icons", style = LocalTypography.current.body2)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("📱", "🎮", "📝", "🧮", "⏱️", "🛠️").forEach { emoji ->
                            EmojiChip(
                                emoji = emoji,
                                isSelected = emoji == selectedEmoji && customEmoji.isBlank(),
                                onClick = {
                                    selectedEmoji = emoji
                                    customEmoji = ""
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("🎵", "🎨", "📊", "🔋", "🌦️", "💬").forEach { emoji ->
                            EmojiChip(
                                emoji = emoji,
                                isSelected = emoji == selectedEmoji && customEmoji.isBlank(),
                                onClick = {
                                    selectedEmoji = emoji
                                    customEmoji = ""
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Emoji Input
                    Text(text = "Or type a custom Emoji", style = LocalTypography.current.body2)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customEmoji,
                        onValueChange = { customEmoji = it.take(2) }, // allow small text or composite emoji
                        placeholder = { Text("Example: 🚀") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            variant = ButtonVariant.Ghost,
                            text = "Cancel",
                            onClick = { onDismiss() }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            text = "Install",
                            onClick = {
                                onInstall(
                                    appName.takeIf { it.isNotBlank() } ?: initial,
                                    activeEmoji,
                                    selectedColor.first
                                )
                            },
                            enabled = appName.isNotBlank() && activeEmoji.isNotBlank()
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun EmojiChip(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                if (isSelected) {
                    AiyoTheme.colors.primary.copy(alpha = 0.2f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) AiyoTheme.colors.primary else AiyoTheme.colors.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, style = LocalTypography.current.body1.copy(fontSize = 18.sp))
    }
}
