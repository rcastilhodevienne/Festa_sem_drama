package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.GiftCategory
import com.example.model.GiftItem
import com.example.model.GuestGroup
import com.example.model.ParentTab
import com.example.model.PartyEvent
import com.example.model.PartyUiState
import com.example.model.RsvpStatus
import com.example.ui.components.ExternalActionHelper
import com.example.ui.theme.LocalFestaColors

@Composable
fun ParentsDashboardScreen(
    uiState: PartyUiState,
    onSelectTab: (ParentTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectStatusFilter: (RsvpStatus?) -> Unit,
    onOpenAddGuest: () -> Unit,
    onUpdateGuestStatus: (String, RsvpStatus) -> Unit,
    onRemoveGuest: (String) -> Unit,
    onWhatsAppReminder: (GuestGroup) -> Unit,
    onSelectGiftCategory: (GiftCategory?) -> Unit,
    onOpenAddGift: () -> Unit,
    onRequestManualReserveGift: (GiftItem) -> Unit,
    onReleaseGift: (String) -> Unit,
    onCopyBuffetReport: () -> Unit,
    onFeedbackMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "event_header") {
            ParentEventHeaderCard(
                event = uiState.event,
                onFeedbackMessage = onFeedbackMessage
            )
        }

        item(key = "metrics_grid") {
            ParentMetricsGrid(
                uiState = uiState,
                onTapPending = {
                    onSelectTab(ParentTab.GUESTS)
                    onSelectStatusFilter(RsvpStatus.PENDING)
                },
                onTapGifts = {
                    onSelectTab(ParentTab.GIFTS)
                },
                onTapDietary = {
                    onSelectTab(ParentTab.DIETARY)
                }
            )
        }

        item(key = "parent_tabs") {
            ParentTabSelector(
                selectedTab = uiState.selectedParentTab,
                guestsCount = uiState.guests.size,
                giftsCount = uiState.gifts.size,
                dietaryCount = uiState.activeDietaryAlertsCount,
                onSelectTab = onSelectTab
            )
        }

        item(key = "tab_content") {
            AnimatedContent(
                targetState = uiState.selectedParentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "parentTabTransition"
            ) { tab ->
                when (tab) {
                    ParentTab.GUESTS -> GuestsTabContent(
                        guests = uiState.filteredGuests,
                        totalGuestsCount = uiState.guests.size,
                        searchQuery = uiState.guestSearchQuery,
                        selectedStatusFilter = uiState.selectedStatusFilter,
                        onSearchQueryChange = onSearchQueryChange,
                        onSelectStatusFilter = onSelectStatusFilter,
                        onOpenAddGuest = onOpenAddGuest,
                        onUpdateGuestStatus = onUpdateGuestStatus,
                        onRemoveGuest = onRemoveGuest,
                        onWhatsAppReminder = onWhatsAppReminder
                    )

                    ParentTab.GIFTS -> ParentsGiftsTabContent(
                        gifts = uiState.filteredParentGifts,
                        selectedCategory = uiState.selectedParentGiftCategory,
                        onSelectCategory = onSelectGiftCategory,
                        onOpenAddGift = onOpenAddGift,
                        onRequestManualReserve = onRequestManualReserveGift,
                        onReleaseGift = onReleaseGift
                    )

                    ParentTab.DIETARY -> DietarySummaryTabContent(
                        summaryGroups = uiState.dietarySummaryGroups,
                        totalConfirmedPeople = uiState.totalConfirmedPeople,
                        onCopyBuffetReport = onCopyBuffetReport
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentEventHeaderCard(
    event: PartyEvent,
    onFeedbackMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val festaColors = LocalFestaColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, festaColors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = festaColors.indigoContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${event.childName} · ${event.ageTurning} anos",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = festaColors.indigoText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = event.rsvpDeadline,
                            style = MaterialTheme.typography.labelSmall,
                            color = festaColors.mutedText
                        )
                    }

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Visual countdown widget
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${event.daysRemaining}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "dias",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                EventMetaRow(
                    icon = Icons.Outlined.CalendarToday,
                    text = event.dateDisplay
                )
                EventMetaRow(
                    icon = Icons.Outlined.AccessTime,
                    text = event.timeDisplay
                )
                EventMetaRow(
                    icon = Icons.Outlined.LocationOn,
                    text = "${event.venueName} — ${event.fullAddress}"
                )
            }

            FilledTonalButton(
                onClick = {
                    ExternalActionHelper.openGoogleMaps(
                        context = context,
                        query = event.mapsQuery,
                        onFallbackMessage = onFeedbackMessage
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = festaColors.subtleSurface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("parent_open_maps_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Navigation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Abrir local no Google Maps",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun EventMetaRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LocalFestaColors.current.mutedText,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ParentMetricsGrid(
    uiState: PartyUiState,
    onTapPending: () -> Unit,
    onTapGifts: () -> Unit,
    onTapDietary: () -> Unit
) {
    val festaColors = LocalFestaColors.current
    val giftProgress = if (uiState.totalGiftsCount > 0) {
        uiState.reservedGiftsCount.toFloat() / uiState.totalGiftsCount.toFloat()
    } else 0f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Confirmados",
                value = "${uiState.totalConfirmedPeople}",
                subtitle = "${uiState.confirmedAdultsCount} adultos + ${uiState.confirmedChildrenCount} crianças",
                icon = Icons.Outlined.Groups,
                accentColor = festaColors.emeraldText,
                containerColor = festaColors.emeraldContainer,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Pendentes",
                value = "${uiState.pendingGroupsCount}",
                subtitle = "famílias sem resposta",
                icon = Icons.Outlined.HourglassEmpty,
                accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = festaColors.subtleSurface,
                onClick = onTapPending,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onTapGifts),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, festaColors.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Presentes",
                            style = MaterialTheme.typography.labelMedium,
                            color = festaColors.mutedText
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(festaColors.indigoContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CardGiftcard,
                                contentDescription = null,
                                tint = festaColors.indigoText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${uiState.reservedGiftsCount} / ${uiState.totalGiftsCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LinearProgressIndicator(
                        progress = { giftProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = festaColors.subtleSurface
                    )
                    Text(
                        text = "itens reservados",
                        style = MaterialTheme.typography.bodySmall,
                        color = festaColors.mutedText
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onTapDietary)
                    .testTag("metric_dietary_alerts"),
                shape = RoundedCornerShape(18.dp),
                color = if (uiState.activeDietaryAlertsCount > 0) festaColors.amberContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (uiState.activeDietaryAlertsCount > 0) festaColors.amberText.copy(alpha = 0.35f) else festaColors.cardBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alertas Alimentares",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (uiState.activeDietaryAlertsCount > 0) festaColors.amberText else festaColors.mutedText,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (uiState.activeDietaryAlertsCount > 0) {
                                        festaColors.amberText.copy(alpha = 0.14f)
                                    } else {
                                        festaColors.subtleSurface
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.activeDietaryAlertsCount > 0) {
                                    Icons.Rounded.WarningAmber
                                } else {
                                    Icons.Outlined.Restaurant
                                },
                                contentDescription = null,
                                tint = festaColors.amberText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${uiState.activeDietaryAlertsCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.activeDietaryAlertsCount > 0) festaColors.amberText else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (uiState.activeDietaryAlertsCount > 0) {
                            "Toque para ver resumo"
                        } else {
                            "Nenhuma restrição"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.activeDietaryAlertsCount > 0) festaColors.amberText else festaColors.mutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val festaColors = LocalFestaColors.current
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, festaColors.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = festaColors.mutedText
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = festaColors.mutedText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ParentTabSelector(
    selectedTab: ParentTab,
    guestsCount: Int,
    giftsCount: Int,
    dietaryCount: Int,
    onSelectTab: (ParentTab) -> Unit
) {
    val festaColors = LocalFestaColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = festaColors.subtleSurface,
        border = BorderStroke(1.dp, festaColors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ParentTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                val count = when (tab) {
                    ParentTab.GUESTS -> guestsCount
                    ParentTab.GIFTS -> giftsCount
                    ParentTab.DIETARY -> dietaryCount
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTab(tab) }
                        .testTag("parent_tab_${tab.name.lowercase()}"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (selected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.primary else festaColors.mutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Surface(
                            color = if (selected) festaColors.indigoContainer else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) festaColors.indigoText else festaColors.mutedText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
