package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Calendar
import java.util.concurrent.TimeUnit

object PartyConstants {
    val commonDietaryTags = listOf(
        "APLV",
        "Celíaco",
        "Vegetariano",
        "Sem Lactose",
        "Alergia a Amendoim"
    )
}

enum class AppViewMode {
    PARENTS_DASHBOARD,
    GUEST_PAGE
}

enum class ProfileRole(val label: String) {
    PARENT("Pai / Mãe"),
    ORGANIZER("Organizador(a)");

    companion object {
        fun fromRaw(raw: String): ProfileRole =
            entries.firstOrNull { it.name == raw } ?: PARENT
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val familyOrHostName: String = "",
    val phoneWhatsApp: String = "",
    val profileRole: String = ProfileRole.PARENT.name,
    val profileCompleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val role: ProfileRole
        get() = ProfileRole.fromRaw(profileRole)
}

enum class ParentTab(val label: String) {
    GUESTS("Convidados"),
    RESPONSES("Respostas"),
    GIFTS("Presentes"),
    DIETARY("Alimentar")
}

enum class RsvpStatus(val label: String) {
    CONFIRMED("Confirmado"),
    PENDING("Pendente"),
    DECLINED("Recusado");

    companion object {
        fun fromRaw(raw: String): RsvpStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class GuestSource(val label: String) {
    PARENTS("Cadastrado pelos Pais"),
    GUEST_FORM("Resposta Online");

    companion object {
        fun fromRaw(raw: String): GuestSource =
            entries.firstOrNull { it.name == raw } ?: PARENTS
    }
}

enum class GiftCategory(val label: String) {
    TOYS("Brinquedos"),
    CLOTHING("Roupas/Calçados"),
    BOOKS("Livros"),
    EXPERIENCES("Experiências");

    companion object {
        fun fromRaw(raw: String): GiftCategory =
            entries.firstOrNull { it.name == raw } ?: TOYS
    }
}

enum class GiftAvailabilityFilter(val label: String) {
    ALL("Todos"),
    AVAILABLE_ONLY("Disponíveis"),
    RESERVED_ONLY("Reservados")
}

@Keep
@IgnoreExtraProperties
data class PartyEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val childName: String = "",
    val ageTurning: Long = 1L,
    val title: String = "",
    val subtitle: String = "",
    val eventDateIso: String = "",
    val dateDisplay: String = "",
    val timeDisplay: String = "",
    val venueName: String = "",
    val fullAddress: String = "",
    val rsvpDeadline: String = "",
    val hostSignature: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val daysRemaining: Int
        get() = computeDaysUntil(eventDateIso)

    val mapsQuery: String
        get() = listOf(venueName, fullAddress)
            .filter { it.isNotBlank() }
            .joinToString(", ")

    val shareText: String
        get() = buildString {
            append("$title ($dateDisplay · $timeDisplay)")
            if (venueName.isNotBlank()) append(" — $venueName")
            if (fullAddress.isNotBlank()) append(", $fullAddress")
        }

    companion object {
        fun computeDaysUntil(isoDate: String): Int {
            val parts = isoDate.split("-")
            if (parts.size != 3) return 0
            val year = parts[0].toIntOrNull() ?: return 0
            val month = parts[1].toIntOrNull() ?: return 0
            val day = parts[2].toIntOrNull() ?: return 0

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val target = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val diffMillis = target.timeInMillis - today.timeInMillis
            return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt().coerceAtLeast(0)
        }
    }
}

@Keep
@IgnoreExtraProperties
data class GuestGroup(
    @DocumentId val id: String = "",
    val userId: String = "",
    val partyId: String = "",
    val primaryGuestName: String = "",
    val adultsCount: Long = 1L,
    val childrenCount: Long = 0L,
    val phoneWhatsApp: String = "",
    val status: String = RsvpStatus.PENDING.name,
    val dietaryTags: List<String> = emptyList(),
    val dietaryNotes: String = "",
    val source: String = GuestSource.PARENTS.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val rsvpStatus: RsvpStatus
        get() = RsvpStatus.fromRaw(status)

    val guestSource: GuestSource
        get() = GuestSource.fromRaw(source)

    val totalPeople: Int
        get() = (adultsCount + childrenCount).toInt()

    val hasDietaryRestrictions: Boolean
        get() = dietaryTags.isNotEmpty() || dietaryNotes.isNotBlank()
}

@Keep
@IgnoreExtraProperties
data class GiftItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val partyId: String = "",
    val name: String = "",
    val category: String = GiftCategory.TOYS.name,
    val sizeOrModelSuggestion: String = "",
    val details: String = "",
    val reservedByGuestName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val giftCategory: GiftCategory
        get() = GiftCategory.fromRaw(category)

    val available: Boolean
        get() = reservedByGuestName.isBlank()
}

data class DietarySummaryGroup(
    val restrictionLabel: String,
    val guestEntries: List<DietaryGuestEntry>
) {
    val totalEntries: Int
        get() = guestEntries.size
}

data class DietaryGuestEntry(
    val guestGroupId: String,
    val primaryGuestName: String,
    val adultsCount: Int,
    val childrenCount: Int,
    val notes: String,
    val isConfirmed: Boolean
)

data class GuestRsvpSubmission(
    val guestName: String,
    val attending: Boolean,
    val adultsCount: Int,
    val childrenCount: Int,
    val dietarySummary: String
)

sealed interface DataLoadState<out T> {
    data object Loading : DataLoadState<Nothing>
    data class Success<T>(val data: T) : DataLoadState<T>
    data class Error(val message: String) : DataLoadState<Nothing>
}

data class PartyUiState(
    val userProfile: UserProfile? = null,
    val viewMode: AppViewMode = AppViewMode.PARENTS_DASHBOARD,
    val selectedParentTab: ParentTab = ParentTab.GUESTS,
    val parties: List<PartyEvent> = emptyList(),
    val activeParty: PartyEvent? = null,
    val guests: List<GuestGroup> = emptyList(),
    val gifts: List<GiftItem> = emptyList(),
    val guestSearchQuery: String = "",
    val selectedStatusFilter: RsvpStatus? = null,
    val selectedParentGiftCategory: GiftCategory? = null,
    val selectedGuestGiftCategory: GiftCategory? = null,
    val selectedGuestGiftAvailability: GiftAvailabilityFilter = GiftAvailabilityFilter.ALL,
    val selectedGuestSizeFilter: String? = null,
    val lastSubmittedRsvp: GuestRsvpSubmission? = null,
    val isMutating: Boolean = false
) {
    val confirmedGuests: List<GuestGroup>
        get() = guests.filter { it.rsvpStatus == RsvpStatus.CONFIRMED }

    val confirmedAdultsCount: Int
        get() = confirmedGuests.sumOf { it.adultsCount.toInt() }

    val confirmedChildrenCount: Int
        get() = confirmedGuests.sumOf { it.childrenCount.toInt() }

    val totalConfirmedPeople: Int
        get() = confirmedAdultsCount + confirmedChildrenCount

    val pendingGroupsCount: Int
        get() = guests.count { it.rsvpStatus == RsvpStatus.PENDING }

    val declinedGroupsCount: Int
        get() = guests.count { it.rsvpStatus == RsvpStatus.DECLINED }

    val formResponses: List<GuestGroup>
        get() = guests.filter { it.guestSource == GuestSource.GUEST_FORM }

    val reservedGiftsCount: Int
        get() = gifts.count { !it.available }

    val totalGiftsCount: Int
        get() = gifts.size

    val activeDietaryAlertsCount: Int
        get() = guests.count { it.rsvpStatus != RsvpStatus.DECLINED && it.hasDietaryRestrictions }

    val filteredGuests: List<GuestGroup>
        get() = guests.filter { guest ->
            val matchesStatus = selectedStatusFilter == null || guest.rsvpStatus == selectedStatusFilter
            val query = guestSearchQuery.trim()
            val matchesQuery = query.isBlank() ||
                guest.primaryGuestName.contains(query, ignoreCase = true) ||
                guest.dietaryTags.any { it.contains(query, ignoreCase = true) } ||
                guest.dietaryNotes.contains(query, ignoreCase = true)
            matchesStatus && matchesQuery
        }

    val filteredParentGifts: List<GiftItem>
        get() = gifts.filter { gift ->
            selectedParentGiftCategory == null || gift.giftCategory == selectedParentGiftCategory
        }

    val availableSizeFilters: List<String>
        get() = gifts.mapNotNull { gift ->
            val text = gift.sizeOrModelSuggestion
            when {
                text.contains("Tam.", ignoreCase = true) -> text.substringAfter("Tam.").trim().split(" ").firstOrNull()?.let { "Tam. $it" }
                text.contains("anos", ignoreCase = true) -> text.split("·").firstOrNull { it.contains("anos", ignoreCase = true) }?.trim()
                else -> null
            }
        }.distinct()

    val filteredGuestGifts: List<GiftItem>
        get() = gifts.filter { gift ->
            val matchesCategory = selectedGuestGiftCategory == null || gift.giftCategory == selectedGuestGiftCategory
            val matchesAvailability = when (selectedGuestGiftAvailability) {
                GiftAvailabilityFilter.ALL -> true
                GiftAvailabilityFilter.AVAILABLE_ONLY -> gift.available
                GiftAvailabilityFilter.RESERVED_ONLY -> !gift.available
            }
            val matchesSize = selectedGuestSizeFilter == null ||
                gift.sizeOrModelSuggestion.contains(selectedGuestSizeFilter, ignoreCase = true)
            matchesCategory && matchesAvailability && matchesSize
        }

    val dietarySummaryGroups: List<DietarySummaryGroup>
        get() {
            val activeGuests = guests.filter { it.rsvpStatus != RsvpStatus.DECLINED && it.hasDietaryRestrictions }
            val grouped = linkedMapOf<String, MutableList<DietaryGuestEntry>>()

            for (guest in activeGuests) {
                val tags = if (guest.dietaryTags.isNotEmpty()) {
                    guest.dietaryTags
                } else {
                    listOf("Restrição Específica")
                }

                for (tag in tags) {
                    val bucket = grouped.getOrPut(tag) { mutableListOf() }
                    bucket.add(
                        DietaryGuestEntry(
                            guestGroupId = guest.id,
                            primaryGuestName = guest.primaryGuestName,
                            adultsCount = guest.adultsCount.toInt(),
                            childrenCount = guest.childrenCount.toInt(),
                            notes = guest.dietaryNotes,
                            isConfirmed = guest.rsvpStatus == RsvpStatus.CONFIRMED
                        )
                    )
                }
            }

            return grouped.entries
                .sortedByDescending { it.value.size }
                .map { DietarySummaryGroup(restrictionLabel = it.key, guestEntries = it.value) }
        }
}
