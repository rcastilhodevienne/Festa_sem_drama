package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.PartyInitialData
import com.example.model.AppViewMode
import com.example.model.GiftAvailabilityFilter
import com.example.model.GiftCategory
import com.example.model.GiftItem
import com.example.model.GuestGroup
import com.example.model.GuestRsvpSubmission
import com.example.model.ParentTab
import com.example.model.PartyUiState
import com.example.model.RsvpStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class PartyViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        PartyUiState(
            event = PartyInitialData.initialEvent,
            guests = PartyInitialData.initialGuests,
            gifts = PartyInitialData.initialGifts
        )
    )
    val uiState: StateFlow<PartyUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun switchViewMode(mode: AppViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun selectParentTab(tab: ParentTab) {
        _uiState.update { it.copy(selectedParentTab = tab) }
    }

    fun updateGuestSearchQuery(query: String) {
        _uiState.update { it.copy(guestSearchQuery = query) }
    }

    fun selectStatusFilter(status: RsvpStatus?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
    }

    fun selectParentGiftCategory(category: GiftCategory?) {
        _uiState.update { it.copy(selectedParentGiftCategory = category) }
    }

    fun selectGuestGiftCategory(category: GiftCategory?) {
        _uiState.update { it.copy(selectedGuestGiftCategory = category) }
    }

    fun selectGuestGiftAvailability(filter: GiftAvailabilityFilter) {
        _uiState.update { it.copy(selectedGuestGiftAvailability = filter) }
    }

    fun selectGuestSizeFilter(sizeFilter: String?) {
        _uiState.update { it.copy(selectedGuestSizeFilter = sizeFilter) }
    }

    fun addGuest(
        primaryGuestName: String,
        adultsCount: Int,
        childrenCount: Int,
        phoneWhatsApp: String,
        status: RsvpStatus,
        dietaryTags: List<String>,
        dietaryNotes: String
    ) {
        val cleanName = primaryGuestName.trim()
        if (cleanName.isBlank()) return

        val newGuest = GuestGroup(
            id = "gst_${UUID.randomUUID().toString().take(8)}",
            primaryGuestName = cleanName,
            adultsCount = adultsCount.coerceAtLeast(1),
            childrenCount = childrenCount.coerceAtLeast(0),
            phoneWhatsApp = phoneWhatsApp.trim(),
            status = status,
            dietaryTags = dietaryTags.distinct(),
            dietaryNotes = dietaryNotes.trim()
        )

        _uiState.update { state ->
            state.copy(guests = listOf(newGuest) + state.guests)
        }
        _snackbarMessage.value = "$cleanName adicionado à lista de convidados."
    }

    fun updateGuestStatus(guestId: String, newStatus: RsvpStatus) {
        var updatedName = ""
        _uiState.update { state ->
            val updated = state.guests.map { guest ->
                if (guest.id == guestId) {
                    updatedName = guest.primaryGuestName
                    guest.copy(status = newStatus)
                } else {
                    guest
                }
            }
            state.copy(guests = updated)
        }
        if (updatedName.isNotBlank()) {
            _snackbarMessage.value = "Status de $updatedName atualizado para ${newStatus.label}."
        }
    }

    fun removeGuest(guestId: String) {
        var removedName = ""
        _uiState.update { state ->
            val target = state.guests.find { it.id == guestId }
            removedName = target?.primaryGuestName.orEmpty()
            state.copy(guests = state.guests.filterNot { it.id == guestId })
        }
        if (removedName.isNotBlank()) {
            _snackbarMessage.value = "$removedName removido da lista."
        }
    }

    fun addGift(
        name: String,
        category: GiftCategory,
        sizeOrModelSuggestion: String,
        details: String
    ) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return

        val newGift = GiftItem(
            id = "gft_${UUID.randomUUID().toString().take(8)}",
            name = cleanName,
            category = category,
            sizeOrModelSuggestion = sizeOrModelSuggestion.trim().ifBlank { "Livre escolha" },
            details = details.trim(),
            reservedByGuestName = null
        )

        _uiState.update { state ->
            state.copy(gifts = listOf(newGift) + state.gifts)
        }
        _snackbarMessage.value = "Presente \"$cleanName\" adicionado à lista."
    }

    fun reserveGift(giftId: String, guestName: String) {
        val cleanGuest = guestName.trim()
        if (cleanGuest.isBlank()) return

        var reservedGiftName = ""
        var alreadyReserved = false

        _uiState.update { state ->
            val updatedGifts = state.gifts.map { gift ->
                if (gift.id == giftId) {
                    if (!gift.isAvailable) {
                        alreadyReserved = true
                        gift
                    } else {
                        reservedGiftName = gift.name
                        gift.copy(reservedByGuestName = cleanGuest)
                    }
                } else {
                    gift
                }
            }
            state.copy(gifts = updatedGifts)
        }

        _snackbarMessage.value = when {
            alreadyReserved -> "Este presente já foi reservado por outro convidado."
            reservedGiftName.isNotBlank() -> "Presente \"$reservedGiftName\" reservado para $cleanGuest!"
            else -> null
        }
    }

    fun releaseGift(giftId: String) {
        var releasedGiftName = ""
        _uiState.update { state ->
            val updated = state.gifts.map { gift ->
                if (gift.id == giftId) {
                    releasedGiftName = gift.name
                    gift.copy(reservedByGuestName = null)
                } else {
                    gift
                }
            }
            state.copy(gifts = updated)
        }
        if (releasedGiftName.isNotBlank()) {
            _snackbarMessage.value = "\"$releasedGiftName\" voltou a ficar disponível."
        }
    }

    fun submitGuestRsvp(
        guestName: String,
        phoneWhatsApp: String,
        attending: Boolean,
        adultsCount: Int,
        childrenCount: Int,
        dietaryTags: List<String>,
        dietaryNotes: String
    ) {
        val cleanName = guestName.trim()
        if (cleanName.isBlank()) return

        val finalStatus = if (attending) RsvpStatus.CONFIRMED else RsvpStatus.DECLINED
        val finalAdults = if (attending) adultsCount.coerceAtLeast(1) else 0
        val finalChildren = if (attending) childrenCount.coerceAtLeast(0) else 0
        val finalTags = if (attending) dietaryTags.distinct() else emptyList()
        val finalNotes = if (attending) dietaryNotes.trim() else "Informou que não poderá comparecer."

        _uiState.update { state ->
            val existingIndex = state.guests.indexOfFirst {
                it.primaryGuestName.equals(cleanName, ignoreCase = true)
            }
            val updatedList = if (existingIndex >= 0) {
                state.guests.mapIndexed { index, guest ->
                    if (index == existingIndex) {
                        guest.copy(
                            adultsCount = finalAdults,
                            childrenCount = finalChildren,
                            phoneWhatsApp = phoneWhatsApp.trim().ifBlank { guest.phoneWhatsApp },
                            status = finalStatus,
                            dietaryTags = finalTags,
                            dietaryNotes = finalNotes
                        )
                    } else {
                        guest
                    }
                }
            } else {
                val newGuest = GuestGroup(
                    id = "gst_${UUID.randomUUID().toString().take(8)}",
                    primaryGuestName = cleanName,
                    adultsCount = finalAdults,
                    childrenCount = finalChildren,
                    phoneWhatsApp = phoneWhatsApp.trim(),
                    status = finalStatus,
                    dietaryTags = finalTags,
                    dietaryNotes = finalNotes
                )
                listOf(newGuest) + state.guests
            }

            val summaryText = buildList {
                addAll(finalTags)
                if (dietaryNotes.isNotBlank() && attending) add(dietaryNotes.trim())
            }.joinToString(" · ")

            state.copy(
                guests = updatedList,
                lastSubmittedRsvp = GuestRsvpSubmission(
                    guestName = cleanName,
                    attending = attending,
                    adultsCount = finalAdults,
                    childrenCount = finalChildren,
                    dietarySummary = summaryText
                )
            )
        }

        _snackbarMessage.value = if (attending) {
            "Presença confirmada! Obrigado, $cleanName."
        } else {
            "Resposta registrada. Sentiremos sua falta, $cleanName!"
        }
    }

    fun clearGuestRsvpReceipt() {
        _uiState.update { it.copy(lastSubmittedRsvp = null) }
    }

    fun buildWhatsAppReminderMessage(guest: GuestGroup): String {
        val event = _uiState.value.event
        val firstName = guest.primaryGuestName.split(" ", "&").firstOrNull().orEmpty().ifBlank { guest.primaryGuestName }
        return "Olá, $firstName! Tudo bem? Estamos organizando o ${event.title} (${event.dateDisplay}, às ${event.timeDisplay}) no ${event.venueName}. Para ajudarmos o buffet com as quantidades e restrições alimentares, pode confirmar sua presença pelo link? ${event.partyShareUrl}"
    }

    fun buildBuffetDietaryReport(): String {
        val state = _uiState.value
        val groups = state.dietarySummaryGroups
        return buildString {
            appendLine("RESUMO ALIMENTAR — ${state.event.title}")
            appendLine("Confirmados até o momento: ${state.totalConfirmedPeople} pessoas (${state.confirmedAdultsCount} adultos + ${state.confirmedChildrenCount} crianças)")
            appendLine()
            if (groups.isEmpty()) {
                appendLine("Nenhuma restrição alimentar reportada até o momento.")
            } else {
                groups.forEach { group ->
                    appendLine("• ${group.restrictionLabel} (${group.totalEntries} grupo(s)):")
                    group.guestEntries.forEach { entry ->
                        val statusTag = if (entry.isConfirmed) "Confirmado" else "Pendente"
                        val notesText = if (entry.notes.isNotBlank()) " — ${entry.notes}" else ""
                        appendLine("  - ${entry.primaryGuestName} [$statusTag]$notesText")
                    }
                }
            }
        }.trim()
    }
}
