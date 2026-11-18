package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DietarySummaryGroup
import com.example.model.GiftCategory
import com.example.model.GiftItem
import com.example.model.GuestGroup
import com.example.model.RsvpStatus
import com.example.ui.components.CategoryBadge
import com.example.ui.components.DietaryTagChip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.giftCategoryIcon
import com.example.ui.theme.LocalFestaColors

@Composable
fun GuestsTabContent(
    guests: List<GuestGroup>,
    totalGuestsCount: Int,
    searchQuery: String,
    selectedStatusFilter: RsvpStatus?,
    onSearchQueryChange: (String) -> Unit,
    onSelectStatusFilter: (RsvpStatus?) -> Unit,
    onOpenAddGuest: () -> Unit,
    onEditGuest: (GuestGroup) -> Unit,
    onUpdateGuestStatus: (String, RsvpStatus) -> Unit,
    onRemoveGuest: (String) -> Unit,
    onWhatsAppReminder: (GuestGroup) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Gestão de Convidados",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$totalGuestsCount grupos cadastrados",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalFestaColors.current.mutedText
                )
            }

            Button(
                onClick = onOpenAddGuest,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("add_guest_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Convidado")
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Buscar convidado ou restrição (ex: APLV)...") },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = "Buscar")
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Limpar busca")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("guest_search_input")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedStatusFilter == null,
                onClick = { onSelectStatusFilter(null) },
                label = { Text("Todos") }
            )
            RsvpStatus.entries.forEach { status ->
                FilterChip(
                    selected = selectedStatusFilter == status,
                    onClick = {
                        onSelectStatusFilter(if (selectedStatusFilter == status) null else status)
                    },
                    label = { Text(status.label) },
                    modifier = Modifier.testTag("filter_status_${status.name.lowercase()}")
                )
            }
        }

        if (guests.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.Groups,
                title = "Nenhum convidado cadastrado",
                subtitle = "Cadastre convidados manualmente ou aguarde confirmações na Visão de Respostas."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                guests.forEach { guest ->
                    GuestManagementCard(
                        guest = guest,
                        onEdit = { onEditGuest(guest) },
                        onUpdateStatus = { newStatus -> onUpdateGuestStatus(guest.id, newStatus) },
                        onRemove = { onRemoveGuest(guest.id) },
                        onWhatsAppReminder = { onWhatsAppReminder(guest) }
                    )
                }
            }
        }
    }
}

@Composable
fun ResponsesTabContent(
    responses: List<GuestGroup>,
    onEditGuest: (GuestGroup) -> Unit,
    onUpdateGuestStatus: (String, RsvpStatus) -> Unit,
    onRemoveGuest: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(
                text = "Respostas Recebidas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${responses.size} resposta(s) enviada(s) pelo formulário público",
                style = MaterialTheme.typography.bodySmall,
                color = LocalFestaColors.current.mutedText
            )
        }

        if (responses.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.HowToReg,
                title = "Nenhuma resposta recebida ainda",
                subtitle = "Quando os convidados responderem na aba \"Visão de Respostas\", as confirmações, recusas e alergias aparecerão aqui em tempo real."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                responses.forEach { guest ->
                    GuestManagementCard(
                        guest = guest,
                        onEdit = { onEditGuest(guest) },
                        onUpdateStatus = { newStatus -> onUpdateGuestStatus(guest.id, newStatus) },
                        onRemove = { onRemoveGuest(guest.id) },
                        onWhatsAppReminder = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GuestManagementCard(
    guest: GuestGroup,
    onEdit: () -> Unit,
    onUpdateStatus: (RsvpStatus) -> Unit,
    onRemove: () -> Unit,
    onWhatsAppReminder: () -> Unit
) {
    val festaColors = LocalFestaColors.current
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guest_card_${guest.id}"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, festaColors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Text(
                        text = guest.primaryGuestName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${guest.adultsCount} adulto(s) · ${guest.childrenCount} criança(s)" +
                            if (guest.phoneWhatsApp.isNotBlank()) " · ${guest.phoneWhatsApp}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = festaColors.mutedText
                    )
                    Text(
                        text = guest.guestSource.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = festaColors.indigoText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = guest.rsvpStatus)
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Ações do convidado"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar convidado") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            RsvpStatus.entries.forEach { option ->
                                if (option != guest.rsvpStatus) {
                                    DropdownMenuItem(
                                        text = { Text("Marcar como ${option.label}") },
                                        onClick = {
                                            menuExpanded = false
                                            onUpdateStatus(option)
                                        }
                                    )
                                }
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Remover convidado",
                                        color = festaColors.roseText
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = null,
                                        tint = festaColors.roseText
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onRemove()
                                }
                            )
                        }
                    }
                }
            }

            if (guest.hasDietaryRestrictions) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = festaColors.amberContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, festaColors.amberText.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (guest.dietaryTags.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                guest.dietaryTags.forEach { tag ->
                                    DietaryTagChip(label = tag)
                                }
                            }
                        }
                        if (guest.dietaryNotes.isNotBlank()) {
                            Text(
                                text = guest.dietaryNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = festaColors.amberText
                            )
                        }
                    }
                }
            }

            if (guest.rsvpStatus == RsvpStatus.PENDING) {
                FilledTonalButton(
                    onClick = onWhatsAppReminder,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = festaColors.emeraldContainer,
                        contentColor = festaColors.emeraldText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("whatsapp_rsvp_${guest.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cobrar RSVP via WhatsApp",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ParentsGiftsTabContent(
    gifts: List<GiftItem>,
    selectedCategory: GiftCategory?,
    onSelectCategory: (GiftCategory?) -> Unit,
    onOpenAddGift: () -> Unit,
    onEditGift: (GiftItem) -> Unit,
    onRequestManualReserve: (GiftItem) -> Unit,
    onReleaseGift: (String) -> Unit,
    onRemoveGift: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lista de Presentes",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Controle sem itens duplicados",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalFestaColors.current.mutedText
                )
            }

            Button(
                onClick = onOpenAddGift,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("add_gift_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Presente")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
                label = { Text("Todas") }
            )
            GiftCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        onSelectCategory(if (selectedCategory == category) null else category)
                    },
                    label = { Text(category.label) }
                )
            }
        }

        if (gifts.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.CardGiftcard,
                title = "Nenhum presente cadastrado",
                subtitle = "Cadastre sugestões de presentes para que os convidados reservem sem duplicidade."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                gifts.forEach { gift ->
                    ParentGiftRowCard(
                        gift = gift,
                        onEdit = { onEditGift(gift) },
                        onManualReserve = { onRequestManualReserve(gift) },
                        onRelease = { onReleaseGift(gift.id) },
                        onRemove = { onRemoveGift(gift.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentGiftRowCard(
    gift: GiftItem,
    onEdit: () -> Unit,
    onManualReserve: () -> Unit,
    onRelease: () -> Unit,
    onRemove: () -> Unit
) {
    val festaColors = LocalFestaColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, festaColors.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (gift.available) festaColors.subtleSurface else festaColors.indigoContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = giftCategoryIcon(gift.giftCategory),
                        contentDescription = null,
                        tint = if (gift.available) festaColors.mutedText else festaColors.indigoText,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = gift.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = gift.sizeOrModelSuggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = festaColors.mutedText
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    CategoryBadge(category = gift.giftCategory)
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("edit_gift_${gift.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Editar presente",
                            tint = festaColors.mutedText
                        )
                    }
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Excluir presente",
                            tint = festaColors.mutedText
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (gift.available) {
                    Surface(
                        color = festaColors.emeraldContainer,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = "Livre na lista",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = festaColors.emeraldText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onManualReserve,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Marcar Reservado")
                    }
                } else {
                    Surface(
                        color = festaColors.indigoContainer,
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = "Reservado por ${gift.reservedByGuestName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = festaColors.indigoText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onRelease,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Liberar")
                    }
                }
            }
        }
    }
}

@Composable
fun DietarySummaryTabContent(
    summaryGroups: List<DietarySummaryGroup>,
    totalConfirmedPeople: Int,
    onCopyBuffetReport: () -> Unit
) {
    val festaColors = LocalFestaColors.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = festaColors.amberContainer,
            border = BorderStroke(1.dp, festaColors.amberText.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = festaColors.amberText
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Painel Estratégico para Buffet & Confeitaria",
                            style = MaterialTheme.typography.titleMedium,
                            color = festaColors.amberText
                        )
                        Text(
                            text = "Base atual: $totalConfirmedPeople pessoas confirmadas. Dados consolidados por tipo de restrição.",
                            style = MaterialTheme.typography.bodySmall,
                            color = festaColors.amberText.copy(alpha = 0.9f)
                        )
                    }
                }

                Button(
                    onClick = onCopyBuffetReport,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("copy_buffet_report_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copiar Resumo Completo para Buffet")
                }
            }
        }

        if (summaryGroups.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.Restaurant,
                title = "Nenhuma restrição alimentar registrada",
                subtitle = "Quando algum convidado informar alergia ou dieta especial, ela aparecerá agrupada aqui."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                summaryGroups.forEach { group ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, festaColors.cardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.restrictionLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    color = festaColors.amberContainer,
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Text(
                                        text = "${group.totalEntries} ocorrência(s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = festaColors.amberText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            group.guestEntries.forEach { entry ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = festaColors.subtleSurface
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = entry.primaryGuestName,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                color = if (entry.isConfirmed) {
                                                    festaColors.emeraldContainer
                                                } else {
                                                    MaterialTheme.colorScheme.surface
                                                },
                                                shape = RoundedCornerShape(999.dp)
                                            ) {
                                                Text(
                                                    text = if (entry.isConfirmed) "Confirmado" else "Pendente",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (entry.isConfirmed) {
                                                        festaColors.emeraldText
                                                    } else {
                                                        festaColors.mutedText
                                                    },
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        if (entry.notes.isNotBlank()) {
                                            Text(
                                                text = entry.notes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
