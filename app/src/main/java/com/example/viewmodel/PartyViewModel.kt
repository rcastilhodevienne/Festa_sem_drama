package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PartyRepository
import com.example.model.AppViewMode
import com.example.model.DataLoadState
import com.example.model.GiftAvailabilityFilter
import com.example.model.GiftCategory
import com.example.model.GiftItem
import com.example.model.GuestGroup
import com.example.model.GuestRsvpSubmission
import com.example.model.GuestSource
import com.example.model.ParentTab
import com.example.model.PartyEvent
import com.example.model.PartyUiState
import com.example.model.ProfileRole
import com.example.model.RsvpStatus
import com.example.model.UserProfile
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class LocalFilterState(
    val viewMode: AppViewMode = AppViewMode.PARENTS_DASHBOARD,
    val selectedParentTab: ParentTab = ParentTab.GUESTS,
    val selectedPartyId: String? = null,
    val guestSearchQuery: String = "",
    val selectedStatusFilter: RsvpStatus? = null,
    val selectedParentGiftCategory: GiftCategory? = null,
    val selectedGuestGiftCategory: GiftCategory? = null,
    val selectedGuestGiftAvailability: GiftAvailabilityFilter = GiftAvailabilityFilter.ALL,
    val selectedGuestSizeFilter: String? = null,
    val lastSubmittedRsvp: GuestRsvpSubmission? = null,
    val isMutating: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class PartyViewModel(
    private val repository: PartyRepository,
    private val currentUserId: String
) : ViewModel() {

    private val localState = MutableStateFlow(LocalFilterState())

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val userProfileStream: Flow<UserProfile?> = repository.observeUserProfile(currentUserId)
    private val partiesStream: Flow<List<PartyEvent>> = repository.observeParties(currentUserId)

    private val activePartyAndCollections: Flow<Triple<List<PartyEvent>, PartyEvent?, Pair<List<GuestGroup>, List<GiftItem>>>> =
        combine(
            partiesStream,
            localState.map { it.selectedPartyId }
        ) { parties, selectedId ->
            val active = parties.firstOrNull { it.id == selectedId } ?: parties.firstOrNull()
            parties to active
        }.flatMapLatest { (parties, active) ->
            if (active == null) {
                flowOf(Triple(parties, null, emptyList<GuestGroup>() to emptyList<GiftItem>()))
            } else {
                combine(
                    repository.observeGuests(currentUserId, active.id),
                    repository.observeGifts(currentUserId, active.id)
                ) { guests, gifts ->
                    Triple(parties, active, guests to gifts)
                }
            }
        }

    val screenState: StateFlow<DataLoadState<PartyUiState>> =
        combine(
            userProfileStream,
            activePartyAndCollections,
            localState
        ) { userProfile, (parties, active, collections), local ->
            val (guests, gifts) = collections
            DataLoadState.Success(
                PartyUiState(
                    userProfile = userProfile,
                    viewMode = local.viewMode,
                    selectedParentTab = local.selectedParentTab,
                    parties = parties,
                    activeParty = active,
                    guests = guests,
                    gifts = gifts,
                    guestSearchQuery = local.guestSearchQuery,
                    selectedStatusFilter = local.selectedStatusFilter,
                    selectedParentGiftCategory = local.selectedParentGiftCategory,
                    selectedGuestGiftCategory = local.selectedGuestGiftCategory,
                    selectedGuestGiftAvailability = local.selectedGuestGiftAvailability,
                    selectedGuestSizeFilter = local.selectedGuestSizeFilter,
                    lastSubmittedRsvp = local.lastSubmittedRsvp,
                    isMutating = local.isMutating
                )
            ) as DataLoadState<PartyUiState>
        }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing party data", rootError)
                emit(
                    DataLoadState.Error(
                        rootError.localizedMessage ?: "Não foi possível carregar os dados do banco."
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = DataLoadState.Loading
            )

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun switchViewMode(mode: AppViewMode) {
        localState.update { it.copy(viewMode = mode) }
    }

    fun selectParentTab(tab: ParentTab) {
        localState.update { it.copy(selectedParentTab = tab) }
    }

    fun selectParty(partyId: String) {
        localState.update {
            it.copy(
                selectedPartyId = partyId,
                lastSubmittedRsvp = null,
                guestSearchQuery = "",
                selectedStatusFilter = null,
                selectedParentGiftCategory = null,
                selectedGuestGiftCategory = null,
                selectedGuestSizeFilter = null
            )
        }
    }

    fun updateGuestSearchQuery(query: String) {
        localState.update { it.copy(guestSearchQuery = query) }
    }

    fun selectStatusFilter(status: RsvpStatus?) {
        localState.update { it.copy(selectedStatusFilter = status) }
    }

    fun selectParentGiftCategory(category: GiftCategory?) {
        localState.update { it.copy(selectedParentGiftCategory = category) }
    }

    fun selectGuestGiftCategory(category: GiftCategory?) {
        localState.update { it.copy(selectedGuestGiftCategory = category) }
    }

    fun selectGuestGiftAvailability(filter: GiftAvailabilityFilter) {
        localState.update { it.copy(selectedGuestGiftAvailability = filter) }
    }

    fun selectGuestSizeFilter(sizeFilter: String?) {
        localState.update { it.copy(selectedGuestSizeFilter = sizeFilter) }
    }

    fun clearGuestRsvpReceipt() {
        localState.update { it.copy(lastSubmittedRsvp = null) }
    }

    fun ensureUserProfile(user: FirebaseUser) {
        viewModelScope.launch {
            repository.ensureUserProfileOnSignIn(user).onFailure { err ->
                Log.w(TAG, "Unable to auto-create user profile", err)
            }
        }
    }

    fun saveUserProfile(
        displayName: String,
        email: String,
        familyOrHostName: String,
        phoneWhatsApp: String,
        profileRole: ProfileRole
    ) {
        viewModelScope.launch {
            localState.update { it.copy(isMutating = true) }
            val result = repository.saveUserProfile(
                displayName = displayName,
                email = email,
                familyOrHostName = familyOrHostName,
                phoneWhatsApp = phoneWhatsApp,
                profileRole = profileRole,
                profileCompleted = true
            )
            localState.update { it.copy(isMutating = false) }
            result.onSuccess {
                _snackbarMessage.value = "Perfil salvo e acesso liberado!"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao salvar perfil."
            }
        }
    }

    fun createParty(
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
    ) {
        viewModelScope.launch {
            localState.update { it.copy(isMutating = true) }
            val result = repository.createParty(
                childName = childName,
                ageTurning = ageTurning,
                title = title,
                subtitle = subtitle,
                eventDateIso = eventDateIso,
                dateDisplay = dateDisplay,
                timeDisplay = timeDisplay,
                venueName = venueName,
                fullAddress = fullAddress,
                rsvpDeadline = rsvpDeadline,
                hostSignature = hostSignature
            )
            localState.update { it.copy(isMutating = false) }
            result.onSuccess { newId ->
                localState.update { it.copy(selectedPartyId = newId, lastSubmittedRsvp = null) }
                _snackbarMessage.value = "Festa \"$title\" salva na base de dados!"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao salvar festa."
            }
        }
    }

    fun updateParty(
        partyId: String,
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
    ) {
        viewModelScope.launch {
            localState.update { it.copy(isMutating = true) }
            val result = repository.updateParty(
                partyId = partyId,
                childName = childName,
                ageTurning = ageTurning,
                title = title,
                subtitle = subtitle,
                eventDateIso = eventDateIso,
                dateDisplay = dateDisplay,
                timeDisplay = timeDisplay,
                venueName = venueName,
                fullAddress = fullAddress,
                rsvpDeadline = rsvpDeadline,
                hostSignature = hostSignature
            )
            localState.update { it.copy(isMutating = false) }
            result.onSuccess {
                _snackbarMessage.value = "Dados da festa atualizados!"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao atualizar festa."
            }
        }
    }

    fun deleteParty(partyId: String) {
        viewModelScope.launch {
            localState.update { it.copy(isMutating = true) }
            val result = repository.deleteParty(partyId)
            localState.update { it.copy(isMutating = false, selectedPartyId = null) }
            result.onSuccess {
                _snackbarMessage.value = "Festa removida da base de dados."
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao remover festa."
            }
        }
    }

    fun addGuest(
        partyId: String,
        primaryGuestName: String,
        adultsCount: Int,
        childrenCount: Int,
        phoneWhatsApp: String,
        status: RsvpStatus,
        dietaryTags: List<String>,
        dietaryNotes: String
    ) {
        viewModelScope.launch {
            localState.update { it.copy(isMutating = true) }
            val result = repository.addGuest(
                partyId = partyId,
                primaryGuestName = primaryGuestName,
                adultsCount = adultsCount,
                childrenCount = childrenCount,
                phoneWhatsApp = phoneWhatsApp,
                status = status,
                dietaryTags = dietaryTags,
                dietaryNotes = dietaryNotes,
                source = GuestSource.PARENTS
            )
            localState.update { it.copy(isMutating = false) }
            result.onSuccess {
                _snackbarMessage.value = "${primaryGuestName.trim()} salvo na lista!"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao cadastrar convidado."
            }
        }
    }

    fun updateGuest(
        partyId: String,
        guestId: String,
        primaryGuestName: String,
        adultsCount: Int,
        childrenCount: Int,
        phoneWhatsApp: String,
        status: RsvpStatus,
        dietaryTags: List<String>,
        dietaryNotes: String,
        source: GuestSource
    ) {
        viewModelScope.launch {
            localState.update { it.copy(isMutating = true) }
            val result = repository.updateGuest(
                partyId = partyId,
                guestId = guestId,
                primaryGuestName = primaryGuestName,
                adultsCount = adultsCount,
                childrenCount = childrenCount,
                phoneWhatsApp = phoneWhatsApp,
                status = status,
                dietaryTags = dietaryTags,
                dietaryNotes = dietaryNotes,
                source = source
            )
            localState.update { it.copy(isMutating = false) }
            result.onSuccess {
                _snackbarMessage.value = "Convidado \"${primaryGuestName.trim()}\" atualizado!"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao atualizar convidado."
            }
        }
    }

    fun updateGuestStatus(partyId: String, guestId: String, newStatus: RsvpStatus) {
        viewModelScope.launch {
            val result = repository.updateGuestStatus(partyId, guestId, newStatus)
            result.onSuccess {
                _snackbarMessage.value = "Status atualizado para ${newStatus.label}."
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao atualizar status."
            }
        }
    }

    fun removeGuest(partyId: String, guestId: String) {
        viewModelScope.launch {
            val result = repository.removeGuest(partyId, guestId)
            result.onSuccess {
                _snackbarMessage.value = "Convidado removido da base de dados."
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao remover convidado."
            }
        }
    }

    fun addGift(
        partyId: String,
        name: String,
        category: GiftCategory,
        sizeOrModelSuggestion: String,
        details: String
    ) {
        viewModelScope.launch {
            localState.update { it.copy(isMutating = true) }
            val result = repository.addGift(
                partyId = partyId,
                name = name,
                category = category,
                sizeOrModelSuggestion = sizeOrModelSuggestion,
                details = details
            )
            localState.update { it.copy(isMutating = false) }
            result.onSuccess {
                _snackbarMessage.value = "Presente \"${name.trim()}\" cadastrado!"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao cadastrar presente."
            }
        }
    }

    fun updateGift(
        partyId: String,
        giftId: String,
        name: String,
        category: GiftCategory,
        sizeOrModelSuggestion: String,
        details: String
    ) {
        viewModelScope.launch {
            localState.update { it.copy(isMutating = true) }
            val result = repository.updateGift(
                partyId = partyId,
                giftId = giftId,
                name = name,
                category = category,
                sizeOrModelSuggestion = sizeOrModelSuggestion,
                details = details
            )
            localState.update { it.copy(isMutating = false) }
            result.onSuccess {
                _snackbarMessage.value = "Presente \"${name.trim()}\" atualizado!"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao atualizar presente."
            }
        }
    }

    fun reserveGift(partyId: String, giftId: String, guestName: String) {
        viewModelScope.launch {
            localState.update { it.copy(isMutating = true) }
            val result = repository.reserveGift(partyId, giftId, guestName)
            localState.update { it.copy(isMutating = false) }
            result.onSuccess {
                _snackbarMessage.value = "Presente reservado para ${guestName.trim()}!"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Este presente já foi reservado."
            }
        }
    }

    fun releaseGift(partyId: String, giftId: String) {
        viewModelScope.launch {
            val result = repository.releaseGift(partyId, giftId)
            result.onSuccess {
                _snackbarMessage.value = "Presente liberado novamente."
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao liberar presente."
            }
        }
    }

    fun removeGift(partyId: String, giftId: String) {
        viewModelScope.launch {
            val result = repository.removeGift(partyId, giftId)
            result.onSuccess {
                _snackbarMessage.value = "Presente removido da lista."
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao remover presente."
            }
        }
    }

    fun submitGuestRsvp(
        partyId: String,
        existingGuests: List<GuestGroup>,
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

        viewModelScope.launch {
            localState.update { it.copy(isMutating = true) }
            val existingMatch = existingGuests.firstOrNull {
                it.primaryGuestName.equals(cleanName, ignoreCase = true)
            }
            val result = if (existingMatch != null) {
                repository.updateGuest(
                    partyId = partyId,
                    guestId = existingMatch.id,
                    primaryGuestName = cleanName,
                    adultsCount = finalAdults,
                    childrenCount = finalChildren,
                    phoneWhatsApp = phoneWhatsApp.trim().ifBlank { existingMatch.phoneWhatsApp },
                    status = finalStatus,
                    dietaryTags = finalTags,
                    dietaryNotes = finalNotes,
                    source = GuestSource.GUEST_FORM
                )
            } else {
                repository.addGuest(
                    partyId = partyId,
                    primaryGuestName = cleanName,
                    adultsCount = finalAdults,
                    childrenCount = finalChildren,
                    phoneWhatsApp = phoneWhatsApp,
                    status = finalStatus,
                    dietaryTags = finalTags,
                    dietaryNotes = finalNotes,
                    source = GuestSource.GUEST_FORM
                ).map { Unit }
            }
            localState.update { it.copy(isMutating = false) }

            result.onSuccess {
                val summaryText = buildList {
                    addAll(finalTags)
                    if (finalNotes.isNotBlank() && attending) add(finalNotes)
                }.joinToString(" · ")

                localState.update {
                    it.copy(
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
                    "Resposta salva! Presença confirmada para $cleanName."
                } else {
                    "Resposta registrada na base de dados."
                }
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Erro ao enviar resposta."
            }
        }
    }

    fun buildWhatsAppReminderMessage(event: PartyEvent, guest: GuestGroup): String {
        val firstName = guest.primaryGuestName.split(" ", "&").firstOrNull().orEmpty().ifBlank { guest.primaryGuestName }
        return "Olá, $firstName! Tudo bem? Estamos organizando ${event.title} (${event.dateDisplay}, às ${event.timeDisplay}) em ${event.venueName} (${event.fullAddress}). Pode nos confirmar quantos adultos e crianças vão e se alguém possui restrição alimentar?"
    }

    fun buildBuffetDietaryReport(state: PartyUiState): String {
        val event = state.activeParty ?: return ""
        val groups = state.dietarySummaryGroups
        return buildString {
            appendLine("RESUMO ALIMENTAR — ${event.title}")
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

    private companion object {
        const val TAG = "PartyVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
