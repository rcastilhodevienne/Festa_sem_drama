package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Toys
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppViewMode
import com.example.model.GiftCategory
import com.example.model.RsvpStatus
import com.example.ui.theme.LocalFestaColors

@Composable
fun TopModeSwitcherBar(
    currentMode: AppViewMode,
    userDisplayName: String,
    onModeSelected: (AppViewMode) -> Unit,
    onEditProfileClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val festaColors = LocalFestaColors.current
    var accountMenuOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Celebration,
                            contentDescription = "Ícone FestaSemDrama",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "FestaSemDrama",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Base em nuvem · $userDisplayName",
                            style = MaterialTheme.typography.bodySmall,
                            color = festaColors.mutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { accountMenuOpen = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("account_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Opções da conta",
                            tint = festaColors.mutedText
                        )
                    }
                    DropdownMenu(
                        expanded = accountMenuOpen,
                        onDismissRequest = { accountMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Meu Perfil") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                accountMenuOpen = false
                                onEditProfileClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sair da conta ($userDisplayName)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                accountMenuOpen = false
                                onSignOutClick()
                            }
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = festaColors.subtleSurface,
                border = BorderStroke(1.dp, festaColors.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ModePillButton(
                        selected = currentMode == AppViewMode.PARENTS_DASHBOARD,
                        label = "Visão dos Pais",
                        icon = Icons.Rounded.AdminPanelSettings,
                        testTag = "switch_parents_mode",
                        onClick = { onModeSelected(AppViewMode.PARENTS_DASHBOARD) },
                        modifier = Modifier.weight(1f)
                    )
                    ModePillButton(
                        selected = currentMode == AppViewMode.GUEST_PAGE,
                        label = "Visão de Respostas",
                        icon = Icons.Rounded.HowToReg,
                        testTag = "switch_guest_mode",
                        onClick = { onModeSelected(AppViewMode.GUEST_PAGE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModePillButton(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        label = "modePillBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else LocalFestaColors.current.mutedText,
        label = "modePillContent"
    )

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: RsvpStatus,
    modifier: Modifier = Modifier
) {
    val festaColors = LocalFestaColors.current
    val (bg, text) = when (status) {
        RsvpStatus.CONFIRMED -> festaColors.emeraldContainer to festaColors.emeraldText
        RsvpStatus.PENDING -> festaColors.amberContainer to festaColors.amberText
        RsvpStatus.DECLINED -> festaColors.roseContainer to festaColors.roseText
    }

    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(text)
            )
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelSmall,
                color = text,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DietaryTagChip(
    label: String,
    modifier: Modifier = Modifier
) {
    val festaColors = LocalFestaColors.current
    Surface(
        modifier = modifier,
        color = festaColors.amberContainer,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, festaColors.amberText.copy(alpha = 0.25f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = festaColors.amberText,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun giftCategoryIcon(category: GiftCategory): ImageVector = when (category) {
    GiftCategory.TOYS -> Icons.Outlined.Toys
    GiftCategory.CLOTHING -> Icons.Outlined.Checkroom
    GiftCategory.BOOKS -> Icons.Outlined.AutoStories
    GiftCategory.EXPERIENCES -> Icons.Outlined.Explore
}

@Composable
fun CategoryBadge(
    category: GiftCategory,
    modifier: Modifier = Modifier
) {
    val festaColors = LocalFestaColors.current
    Surface(
        modifier = modifier,
        color = festaColors.subtleSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = giftCategoryIcon(category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StepperField(
    label: String,
    value: Int,
    minValue: Int = 0,
    maxValue: Int = 50,
    testTagPrefix: String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val festaColors = LocalFestaColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = festaColors.subtleSurface,
        border = BorderStroke(1.dp, festaColors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { if (value > minValue) onValueChange(value - 1) },
                    enabled = value > minValue,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("${testTagPrefix}_minus")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Remove,
                        contentDescription = "Diminuir $label"
                    )
                }
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(
                    onClick = { if (value < maxValue) onValueChange(value + 1) },
                    enabled = value < maxValue,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("${testTagPrefix}_plus")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Aumentar $label"
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionContent: (@Composable () -> Unit)? = null
) {
    val festaColors = LocalFestaColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, festaColors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(festaColors.subtleSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = festaColors.mutedText,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = festaColors.mutedText
            )
            if (actionContent != null) {
                Spacer(modifier = Modifier.height(8.dp))
                actionContent()
            }
        }
    }
}

object ExternalActionHelper {

    fun openGoogleMaps(context: Context, query: String, onFallbackMessage: (String) -> Unit) {
        val encoded = Uri.encode(query)
        val geoUri = Uri.parse("geo:0,0?q=$encoded")
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=$encoded")
            )
            runCatching { context.startActivity(webIntent) }
                .onFailure {
                    copyToClipboard(context, "Endereço da festa", query)
                    onFallbackMessage("Endereço copiado para a área de transferência!")
                }
        }
    }

    fun openWhatsAppReminder(
        context: Context,
        phone: String,
        message: String,
        onResultMessage: (String) -> Unit
    ) {
        val digits = phone.filter { it.isDigit() }
        val normalizedPhone = if (digits.length in 10..11) "55$digits" else digits
        val encodedMessage = Uri.encode(message)
        val url = if (normalizedPhone.isNotBlank()) {
            "https://wa.me/$normalizedPhone?text=$encodedMessage"
        } else {
            "https://wa.me/?text=$encodedMessage"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        runCatching {
            context.startActivity(intent)
            onResultMessage("Abrindo WhatsApp com mensagem personalizada...")
        }.onFailure {
            copyToClipboard(context, "Mensagem WhatsApp", message)
            onResultMessage("Mensagem personalizada copiada para você enviar!")
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
