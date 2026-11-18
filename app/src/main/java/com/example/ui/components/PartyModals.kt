package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.GiftCategory
import com.example.model.GiftItem
import com.example.model.GuestGroup
import com.example.model.PartyConstants
import com.example.model.PartyEvent
import com.example.model.RsvpStatus
import com.example.ui.theme.LocalFestaColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyFormModal(
    existingParty: PartyEvent? = null,
    defaultHostSignature: String = "",
    onDismiss: () -> Unit,
    onConfirmSave: (
        childName: String,
        ageTurning: Int,
        title: String,
        subtitle: String,
        eventDateIso: String,
        dateDisplay: String,
        timeDisplay: String,
        venueName: String,
        fullAddress: String,
        rsvpDeadline: String,
        hostSignature: String
    ) -> Unit
) {
    val defaultFutureIso = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }
    val defaultFutureDisplay = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
        SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).format(cal.time)
    }

    var childName by remember { mutableStateOf(existingParty?.childName.orEmpty()) }
    var ageText by remember { mutableStateOf(existingParty?.ageTurning?.toString().orEmpty()) }
    var title by remember { mutableStateOf(existingParty?.title.orEmpty()) }
    var subtitle by remember { mutableStateOf(existingParty?.subtitle.orEmpty()) }
    var eventDateIso by remember { mutableStateOf(existingParty?.eventDateIso ?: defaultFutureIso) }
    var dateDisplay by remember { mutableStateOf(existingParty?.dateDisplay ?: defaultFutureDisplay) }
    var timeDisplay by remember { mutableStateOf(existingParty?.timeDisplay.orEmpty()) }
    var venueName by remember { mutableStateOf(existingParty?.venueName.orEmpty()) }
    var fullAddress by remember { mutableStateOf(existingParty?.fullAddress.orEmpty()) }
    var rsvpDeadline by remember { mutableStateOf(existingParty?.rsvpDeadline.orEmpty()) }
    var hostSignature by remember { mutableStateOf(existingParty?.hostSignature ?: defaultHostSignature) }
    var showDatePicker by remember { mutableStateOf(false) }

    val ageInt = ageText.toIntOrNull()
    val isIsoValid = Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(eventDateIso.trim())
    val canSubmit = childName.trim().isNotEmpty() &&
        ageInt != null && ageInt in 0..120 &&
        title.trim().length >= 2 &&
        isIsoValid &&
        dateDisplay.trim().length >= 2 &&
        timeDisplay.trim().length >= 2 &&
        venueName.trim().length >= 2 &&
        fullAddress.trim().length >= 2

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Rounded.Celebration,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (existingParty == null) "Criar Nova Festa" else "Personalizar Festa",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = childName,
                        onValueChange = { newVal ->
                            childName = newVal
                            if (existingParty == null && title.isBlank() && newVal.isNotBlank() && ageText.isNotBlank()) {
                                title = "Aniversário de $ageText Anos de ${newVal.trim()}"
                            }
                        },
                        label = { Text("Nome da criança *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("party_child_name_input")
                    )
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { input ->
                            ageText = input.filter { it.isDigit() }.take(3)
                        },
                        label = { Text("Idade *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("party_age_input")
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da festa *") },
                    placeholder = { Text("Ex: Aniversário de 5 Anos da Alice") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_title_input")
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Tema ou mensagem de boas-vindas") },
                    placeholder = { Text("Ex: Tarde de oficinas, música e diversão") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("party_pick_date_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Data do evento: $dateDisplay ($eventDateIso)")
                }

                OutlinedTextField(
                    value = dateDisplay,
                    onValueChange = { dateDisplay = it },
                    label = { Text("Texto da data no convite *") },
                    placeholder = { Text("Ex: Sábado, 12 de Dezembro") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = timeDisplay,
                    onValueChange = { timeDisplay = it },
                    label = { Text("Horário *") },
                    placeholder = { Text("Ex: 15h00 às 19h00") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_time_input")
                )

                OutlinedTextField(
                    value = venueName,
                    onValueChange = { venueName = it },
                    label = { Text("Nome do local / buffet *") },
                    placeholder = { Text("Ex: Salão Jardim Encantado") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_venue_input")
                )

                OutlinedTextField(
                    value = fullAddress,
                    onValueChange = { fullAddress = it },
                    label = { Text("Endereço completo *") },
                    placeholder = { Text("Ex: Rua das Palmeiras, 350 - Bairro Centro") },
                    minLines = 2,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_address_input")
                )

                OutlinedTextField(
                    value = rsvpDeadline,
                    onValueChange = { rsvpDeadline = it },
                    label = { Text("Prazo de confirmação (opcional)") },
                    placeholder = { Text("Ex: Confirmar até 05/12") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hostSignature,
                    onValueChange = { hostSignature = it },
                    label = { Text("Assinatura dos pais (opcional)") },
                    placeholder = { Text("Ex: Com carinho, Família Silva") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmSave(
                        childName,
                        ageInt ?: 1,
                        title,
                        subtitle,
                        eventDateIso,
                        dateDisplay,
                        timeDisplay,
                        venueName,
                        fullAddress,
                        rsvpDeadline,
                        hostSignature
                    )
                },
                enabled = canSubmit,
                modifier = Modifier.testTag("party_save_button")
            ) {
                Text(if (existingParty == null) "Criar Festa" else "Salvar Alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val utcFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            val ptFormat = SimpleDateFormat("EEEE, dd 'de' MMMM", Locale.forLanguageTag("pt-BR")).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            val dateObj = Date(millis)
                            eventDateIso = utcFormat.format(dateObj)
                            dateDisplay = ptFormat.format(dateObj).replaceFirstChar { it.uppercase() }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Selecionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
fun DeletePartyConfirmDialog(
    partyTitle: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val festaColors = LocalFestaColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = null,
                tint = festaColors.roseText
            )
        },
        title = { Text("Excluir festa?") },
        text = {
            Text(
                text = "Isso removerá \"$partyTitle\", seus convidados, respostas e lista de presentes da base de dados permanentemente.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = festaColors.roseText)
            ) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGuestModal(
    existingGuest: GuestGroup? = null,
    onDismiss: () -> Unit,
    onConfirmAdd: (
        primaryGuestName: String,
        adultsCount: Int,
        childrenCount: Int,
        phoneWhatsApp: String,
        status: RsvpStatus,
        dietaryTags: List<String>,
        dietaryNotes: String
    ) -> Unit
) {
    var name by remember(existingGuest?.id) { mutableStateOf(existingGuest?.primaryGuestName.orEmpty()) }
    var adults by remember(existingGuest?.id) {
        mutableIntStateOf((existingGuest?.adultsCount?.toInt() ?: 2).coerceAtLeast(0))
    }
    var children by remember(existingGuest?.id) {
        mutableIntStateOf((existingGuest?.childrenCount?.toInt() ?: 1).coerceAtLeast(0))
    }
    var phone by remember(existingGuest?.id) { mutableStateOf(existingGuest?.phoneWhatsApp.orEmpty()) }
    var status by remember(existingGuest?.id) {
        mutableStateOf(existingGuest?.rsvpStatus ?: RsvpStatus.PENDING)
    }
    var selectedTags by remember(existingGuest?.id) {
        mutableStateOf(existingGuest?.dietaryTags?.toSet().orEmpty())
    }
    var dietaryNotes by remember(existingGuest?.id) { mutableStateOf(existingGuest?.dietaryNotes.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = if (existingGuest == null) Icons.Rounded.PersonAdd else Icons.Rounded.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (existingGuest == null) "Adicionar Convidado" else "Editar Convidado",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do convidado principal *") },
                    placeholder = { Text("Ex: Ana & Pedro Lima") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_guest_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone / WhatsApp") },
                    placeholder = { Text("Ex: 11 99999-0000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_guest_phone_input")
                )

                StepperField(
                    label = "Adultos",
                    value = adults,
                    minValue = if (status == RsvpStatus.DECLINED) 0 else 1,
                    testTagPrefix = "modal_guest_adults",
                    onValueChange = { adults = it }
                )

                StepperField(
                    label = "Crianças",
                    value = children,
                    minValue = 0,
                    testTagPrefix = "modal_guest_children",
                    onValueChange = { children = it }
                )

                Text(
                    text = "Status do RSVP",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RsvpStatus.entries.forEach { option ->
                        FilterChip(
                            selected = status == option,
                            onClick = { status = option },
                            label = { Text(option.label) }
                        )
                    }
                }

                Text(
                    text = "Restrições alimentares / alergias",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PartyConstants.commonDietaryTags.forEach { tag ->
                        val selected = selectedTags.contains(tag)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedTags = if (selected) selectedTags - tag else selectedTags + tag
                            },
                            label = { Text(tag) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LocalFestaColors.current.amberContainer,
                                selectedLabelColor = LocalFestaColors.current.amberText
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = dietaryNotes,
                    onValueChange = { dietaryNotes = it },
                    label = { Text("Detalhes de alergias ou restrições") },
                    placeholder = { Text("Ex: Alergia severa a proteína do leite") },
                    minLines = 2,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_guest_dietary_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmAdd(
                        name,
                        adults,
                        children,
                        phone,
                        status,
                        selectedTags.toList(),
                        dietaryNotes
                    )
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("modal_guest_save_button")
            ) {
                Text(if (existingGuest == null) "Salvar Convidado" else "Salvar Alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGiftModal(
    existingGift: GiftItem? = null,
    onDismiss: () -> Unit,
    onConfirmAdd: (
        name: String,
        category: GiftCategory,
        sizeOrModelSuggestion: String,
        details: String
    ) -> Unit
) {
    var name by remember(existingGift?.id) { mutableStateOf(existingGift?.name.orEmpty()) }
    var category by remember(existingGift?.id) {
        mutableStateOf(existingGift?.giftCategory ?: GiftCategory.TOYS)
    }
    var sizeOrModel by remember(existingGift?.id) {
        mutableStateOf(existingGift?.sizeOrModelSuggestion.orEmpty())
    }
    var details by remember(existingGift?.id) { mutableStateOf(existingGift?.details.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = if (existingGift == null) Icons.Rounded.CardGiftcard else Icons.Rounded.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (existingGift == null) "Novo Item na Lista" else "Editar Presente",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do presente *") },
                    placeholder = { Text("Ex: Kit Pintura Aquarela") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_gift_name_input")
                )

                Text(
                    text = "Categoria",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GiftCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = sizeOrModel,
                    onValueChange = { sizeOrModel = it },
                    label = { Text("Sugestão de tamanho / modelo") },
                    placeholder = { Text("Ex: Tam. 8 anos ou Edição Ilustrada") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_gift_size_input")
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Observação para os convidados") },
                    placeholder = { Text("Ex: Preferência por cores neutras") },
                    minLines = 2,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmAdd(name, category, sizeOrModel, details) },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("modal_gift_save_button")
            ) {
                Text(if (existingGift == null) "Adicionar Presente" else "Salvar Alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ReserveGiftModal(
    gift: GiftItem,
    initialGuestName: String = "",
    isParentManualAction: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmReserve: (String) -> Unit
) {
    var guestName by remember(gift.id) { mutableStateOf(initialGuestName) }
    val festaColors = LocalFestaColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Rounded.CardGiftcard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (isParentManualAction) "Marcar como Reservado" else "Confirmar Presente",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = festaColors.indigoContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = gift.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = gift.sizeOrModelSuggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = festaColors.indigoText
                        )
                    }
                }

                Text(
                    text = if (isParentManualAction) {
                        "Informe quem já garantiu este item para bloqueá-lo contra duplicidades:"
                    } else {
                        "Digite seu nome para reservar este item exclusivamente e evitar presentes repetidos:"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = guestName,
                    onValueChange = { guestName = it },
                    label = { Text("Nome completo *") },
                    placeholder = { Text("Ex: Tia Paula") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reserve_gift_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmReserve(guestName) },
                enabled = guestName.isNotBlank(),
                modifier = Modifier.testTag("reserve_gift_confirm_button")
            ) {
                Text(if (isParentManualAction) "Marcar Reservado" else "Confirmar Reserva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Voltar")
            }
        }
    )
}
