package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.PartyInitialData
import com.example.model.GiftCategory
import com.example.model.GiftItem
import com.example.model.RsvpStatus
import com.example.ui.theme.LocalFestaColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGuestModal(
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
    var name by remember { mutableStateOf("") }
    var adults by remember { mutableIntStateOf(2) }
    var children by remember { mutableIntStateOf(1) }
    var phone by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(RsvpStatus.PENDING) }
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var dietaryNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Rounded.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Adicionar Convidado",
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
                    minValue = 1,
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
                    PartyInitialData.commonDietaryTags.forEach { tag ->
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
                Text("Salvar Convidado")
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
    onDismiss: () -> Unit,
    onConfirmAdd: (
        name: String,
        category: GiftCategory,
        sizeOrModelSuggestion: String,
        details: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(GiftCategory.TOYS) }
    var sizeOrModel by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

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
                text = "Novo Item na Lista",
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
                Text("Adicionar Presente")
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
