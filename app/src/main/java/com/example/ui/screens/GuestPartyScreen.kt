package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.GiftAvailabilityFilter
import com.example.model.GiftCategory
import com.example.model.GiftItem
import com.example.model.GuestRsvpSubmission
import com.example.model.PartyConstants
import com.example.model.PartyEvent
import com.example.model.PartyUiState
import com.example.ui.components.CategoryBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ExternalActionHelper
import com.example.ui.components.StepperField
import com.example.ui.components.giftCategoryIcon
import com.example.ui.theme.LocalFestaColors

@Composable
fun GuestPartyScreen(
    uiState: PartyUiState,
    onSwitchToParents: () -> Unit,
    onSubmitRsvp: (
        guestName: String,
        phoneWhatsApp: String,
        attending: Boolean,
        adultsCount: Int,
        childrenCount: Int,
        dietaryTags: List<String>,
        dietaryNotes: String
    ) -> Unit,
    onClearRsvpReceipt: () -> Unit,
    onSelectGiftCategory: (GiftCategory?) -> Unit,
    onSelectGiftAvailability: (GiftAvailabilityFilter) -> Unit,
    onSelectGiftSizeFilter: (String?) -> Unit,
    onRequestReserveGift: (GiftItem, String) -> Unit,
    onFeedbackMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeParty = uiState.activeParty
    if (activeParty == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            EmptyStateCard(
                icon = Icons.Outlined.Celebration,
                title = "Convite ainda não configurado",
                subtitle = "Cadastre uma festa na Visão dos Pais para ativar o convite digital, o formulário de respostas e a lista de presentes.",
                actionContent = {
                    Button(
                        onClick = onSwitchToParents,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text("Ir para Visão dos Pais")
                    }
                }
            )
        }
        return
    }

    var typedGuestName by remember(activeParty.id) { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item(key = "digital_invite") {
            DigitalInviteCard(
                event = activeParty,
                onFeedbackMessage = onFeedbackMessage
            )
        }

        item(key = "express_rsvp") {
            ExpressRsvpCard(
                lastSubmission = uiState.lastSubmittedRsvp,
                guestName = typedGuestName,
                onGuestNameChange = { typedGuestName = it },
                onSubmitRsvp = onSubmitRsvp,
                onEditAgain = onClearRsvpReceipt
            )
        }

        item(key = "smart_gifts_header") {
            SmartGiftListSection(
                gifts = uiState.filteredGuestGifts,
                totalAvailable = uiState.gifts.count { it.available },
                selectedCategory = uiState.selectedGuestGiftCategory,
                selectedAvailability = uiState.selectedGuestGiftAvailability,
                sizeFilters = uiState.availableSizeFilters,
                selectedSizeFilter = uiState.selectedGuestSizeFilter,
                onSelectCategory = onSelectGiftCategory,
                onSelectAvailability = onSelectGiftAvailability,
                onSelectSizeFilter = onSelectGiftSizeFilter,
                onRequestReserveGift = { gift ->
                    val prefill = uiState.lastSubmittedRsvp?.guestName ?: typedGuestName
                    onRequestReserveGift(gift, prefill)
                }
            )
        }
    }
}

@Composable
private fun DigitalInviteCard(
    event: PartyEvent,
    onFeedbackMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val festaColors = LocalFestaColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, festaColors.cardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_party_hero),
                    contentDescription = "Ilustração do convite de aniversário de ${event.childName}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.08f),
                                    Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                )

                Surface(
                    color = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Convite Oficial · Faltam ${event.daysRemaining} dias",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (event.subtitle.isNotBlank()) {
                        Text(
                            text = event.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InviteInfoTile(
                        icon = Icons.Outlined.CalendarToday,
                        label = "Data",
                        value = event.dateDisplay,
                        modifier = Modifier.weight(1f)
                    )
                    InviteInfoTile(
                        icon = Icons.Outlined.AccessTime,
                        label = "Horário",
                        value = event.timeDisplay,
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = festaColors.subtleSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = event.venueName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = event.fullAddress,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (event.hostSignature.isNotBlank()) {
                    Text(
                        text = event.hostSignature,
                        style = MaterialTheme.typography.bodySmall,
                        color = festaColors.mutedText
                    )
                }

                Button(
                    onClick = {
                        ExternalActionHelper.openGoogleMaps(
                            context = context,
                            query = event.mapsQuery,
                            onFallbackMessage = onFeedbackMessage
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("guest_open_gps_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Abrir no GPS")
                }
            }
        }
    }
}

@Composable
private fun InviteInfoTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val festaColors = LocalFestaColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = festaColors.subtleSurface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = festaColors.mutedText
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpressRsvpCard(
    lastSubmission: GuestRsvpSubmission?,
    guestName: String,
    onGuestNameChange: (String) -> Unit,
    onSubmitRsvp: (
        guestName: String,
        phoneWhatsApp: String,
        attending: Boolean,
        adultsCount: Int,
        childrenCount: Int,
        dietaryTags: List<String>,
        dietaryNotes: String
    ) -> Unit,
    onEditAgain: () -> Unit
) {
    val festaColors = LocalFestaColors.current
    var attending by remember { mutableStateOf<Boolean?>(true) }
    var phone by remember { mutableStateOf("") }
    var adults by remember { mutableIntStateOf(2) }
    var children by remember { mutableIntStateOf(1) }
    var selectedDietaryTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var dietaryNotes by remember { mutableStateOf("") }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Confirmação Express (RSVP)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (lastSubmission != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (lastSubmission.attending) festaColors.emeraldContainer else festaColors.subtleSurface,
                    border = BorderStroke(
                        1.dp,
                        if (lastSubmission.attending) festaColors.emeraldText.copy(alpha = 0.3f) else festaColors.cardBorder
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = if (lastSubmission.attending) festaColors.emeraldText else festaColors.mutedText
                            )
                            Text(
                                text = if (lastSubmission.attending) {
                                    "Presença confirmada!"
                                } else {
                                    "Resposta registrada"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (lastSubmission.attending) festaColors.emeraldText else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (lastSubmission.attending) {
                                "${lastSubmission.guestName}: ${lastSubmission.adultsCount} adulto(s) e ${lastSubmission.childrenCount} criança(s)." +
                                    if (lastSubmission.dietarySummary.isNotBlank()) "\nRestrições: ${lastSubmission.dietarySummary}" else ""
                            } else {
                                "${lastSubmission.guestName} informou que não poderá comparecer. Obrigado por avisar!"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = onEditAgain) {
                            Text("Enviar nova resposta / alterar")
                        }
                    }
                }
            } else {
                Text(
                    text = "Você irá ao aniversário?",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RsvpChoiceButton(
                        selected = attending == true,
                        label = "Sim, com certeza",
                        icon = Icons.Outlined.CheckCircle,
                        activeContainer = festaColors.emeraldContainer,
                        activeContent = festaColors.emeraldText,
                        testTag = "rsvp_yes_button",
                        onClick = { attending = true },
                        modifier = Modifier.weight(1f)
                    )
                    RsvpChoiceButton(
                        selected = attending == false,
                        label = "Não poderei ir",
                        icon = Icons.Outlined.Close,
                        activeContainer = festaColors.roseContainer,
                        activeContent = festaColors.roseText,
                        testTag = "rsvp_no_button",
                        onClick = { attending = false },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = guestName,
                    onValueChange = onGuestNameChange,
                    label = { Text("Seu nome completo *") },
                    placeholder = { Text("Ex: Paula & Rodrigo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rsvp_guest_name_input")
                )

                AnimatedVisibility(visible = attending == true) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("WhatsApp para avisos (opcional)") },
                            placeholder = { Text("Ex: 11 99999-0000") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        StepperField(
                            label = "Quantos adultos vão?",
                            value = adults,
                            minValue = 1,
                            testTagPrefix = "rsvp_adults",
                            onValueChange = { adults = it }
                        )

                        StepperField(
                            label = "Quantas crianças vão?",
                            value = children,
                            minValue = 0,
                            testTagPrefix = "rsvp_children",
                            onValueChange = { children = it }
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = festaColors.amberContainer.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, festaColors.amberText.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Restaurant,
                                        contentDescription = null,
                                        tint = festaColors.amberText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Alguém do grupo possui alguma alergia ou restrição alimentar?",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = festaColors.amberText
                                    )
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PartyConstants.commonDietaryTags.forEach { tag ->
                                        val isSelected = selectedDietaryTags.contains(tag)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedDietaryTags = if (isSelected) {
                                                    selectedDietaryTags - tag
                                                } else {
                                                    selectedDietaryTags + tag
                                                }
                                            },
                                            label = { Text(tag) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = festaColors.amberText,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = dietaryNotes,
                                    onValueChange = { dietaryNotes = it },
                                    label = { Text("Descreva alergias ou observações (opcional)") },
                                    placeholder = { Text("Ex: Sem proteína do leite para o Miguel") },
                                    minLines = 2,
                                    maxLines = 3,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("rsvp_dietary_notes_input")
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        onSubmitRsvp(
                            guestName,
                            phone,
                            attending ?: true,
                            adults,
                            children,
                            selectedDietaryTags.toList(),
                            dietaryNotes
                        )
                    },
                    enabled = guestName.isNotBlank() && attending != null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("rsvp_submit_button")
                ) {
                    Text(
                        text = if (attending == true) "Confirmar Presença" else "Enviar Resposta",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun RsvpChoiceButton(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    activeContainer: Color,
    activeContent: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val festaColors = LocalFestaColors.current
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) activeContainer else festaColors.subtleSurface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) activeContent else festaColors.cardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) activeContent else festaColors.mutedText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) activeContent else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SmartGiftListSection(
    gifts: List<GiftItem>,
    totalAvailable: Int,
    selectedCategory: GiftCategory?,
    selectedAvailability: GiftAvailabilityFilter,
    sizeFilters: List<String>,
    selectedSizeFilter: String?,
    onSelectCategory: (GiftCategory?) -> Unit,
    onSelectAvailability: (GiftAvailabilityFilter) -> Unit,
    onSelectSizeFilter: (String?) -> Unit,
    onRequestReserveGift: (GiftItem) -> Unit
) {
    val festaColors = LocalFestaColors.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lista de Presentes Inteligente",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = festaColors.emeraldContainer,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "$totalAvailable livres",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = festaColors.emeraldText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = "Ao escolher um item, ele é travado automaticamente na base de dados para ninguém dar o mesmo presente.",
                style = MaterialTheme.typography.bodySmall,
                color = festaColors.mutedText
            )
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
                label = { Text("Todas categorias") }
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GiftAvailabilityFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedAvailability == filter,
                    onClick = { onSelectAvailability(filter) },
                    label = { Text(filter.label) }
                )
            }
            sizeFilters.forEach { sizeFilter ->
                FilterChip(
                    selected = selectedSizeFilter == sizeFilter,
                    onClick = {
                        onSelectSizeFilter(if (selectedSizeFilter == sizeFilter) null else sizeFilter)
                    },
                    label = { Text(sizeFilter) }
                )
            }
        }

        if (gifts.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.CardGiftcard,
                title = "Nenhum presente disponível aqui",
                subtitle = "Cadastre sugestões de presentes na Visão dos Pais ou ajuste os filtros acima."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                gifts.forEach { gift ->
                    GuestGiftCard(
                        gift = gift,
                        onChooseGift = { onRequestReserveGift(gift) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GuestGiftCard(
    gift: GiftItem,
    onChooseGift: () -> Unit
) {
    val festaColors = LocalFestaColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guest_gift_card_${gift.id}"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (gift.available) festaColors.cardBorder else festaColors.cardBorder.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (gift.available) festaColors.indigoContainer else festaColors.subtleSurface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = giftCategoryIcon(gift.giftCategory),
                        contentDescription = gift.giftCategory.label,
                        tint = if (gift.available) MaterialTheme.colorScheme.primary else festaColors.mutedText,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CategoryBadge(category = gift.giftCategory)
                    Text(
                        text = gift.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (gift.available) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            festaColors.mutedText
                        }
                    )
                    Text(
                        text = gift.sizeOrModelSuggestion,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (gift.details.isNotBlank()) {
                        Text(
                            text = gift.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = festaColors.mutedText
                        )
                    }
                }
            }

            if (gift.available) {
                Button(
                    onClick = onChooseGift,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("choose_gift_${gift.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CardGiftcard,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quero Presentear")
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = festaColors.emeraldContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = festaColors.emeraldText,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Já Escolhido por ${gift.reservedByGuestName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = festaColors.emeraldText
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            disabledContentColor = festaColors.mutedText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("gift_locked_${gift.id}")
                    ) {
                        Text("Item indisponível (Reservado)")
                    }
                }
            }
        }
    }
}
