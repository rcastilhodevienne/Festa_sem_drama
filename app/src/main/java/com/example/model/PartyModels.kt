package com.example.model

enum class AppViewMode {
    PARENTS_DASHBOARD,
    GUEST_PAGE
}

enum class ParentTab(val label: String) {
    GUESTS("Convidados"),
    GIFTS("Presentes"),
    DIETARY("Alimentar")
}

enum class RsvpStatus(val label: String) {
    CONFIRMED("Confirmado"),
    PENDING("Pendente"),
    DECLINED("Recusado")
}

enum class GiftCategory(val label: String) {
    TOYS("Brinquedos"),
    CLOTHING("Roupas/Calçados"),
    BOOKS("Livros"),
    EXPERIENCES("Experiências")
}

enum class GiftAvailabilityFilter(val label: String) {
    ALL("Todos"),
    AVAILABLE_ONLY("Disponíveis"),
    RESERVED_ONLY("Reservados")
}

data class PartyEvent(
    val id: String,
    val childName: String,
    val ageTurning: Int,
    val title: String,
    val subtitle: String,
    val dateDisplay: String,
    val timeDisplay: String,
    val daysRemaining: Int,
    val venueName: String,
    val fullAddress: String,
    val mapsQuery: String,
    val partyShareUrl: String,
    val rsvpDeadline: String,
    val hostSignature: String
)

data class GuestGroup(
    val id: String,
    val primaryGuestName: String,
    val adultsCount: Int,
    val childrenCount: Int,
    val phoneWhatsApp: String,
    val status: RsvpStatus,
    val dietaryTags: List<String> = emptyList(),
    val dietaryNotes: String = ""
) {
    val totalPeople: Int
        get() = adultsCount + childrenCount

    val hasDietaryRestrictions: Boolean
        get() = dietaryTags.isNotEmpty() || dietaryNotes.isNotBlank()
}

data class GiftItem(
    val id: String,
    val name: String,
    val category: GiftCategory,
    val sizeOrModelSuggestion: String,
    val details: String,
    val reservedByGuestName: String? = null
) {
    val isAvailable: Boolean
        get() = reservedByGuestName.isNullOrBlank()
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

data class PartyUiState(
    val viewMode: AppViewMode = AppViewMode.PARENTS_DASHBOARD,
    val selectedParentTab: ParentTab = ParentTab.GUESTS,
    val event: PartyEvent,
    val guests: List<GuestGroup>,
    val gifts: List<GiftItem>,
    val guestSearchQuery: String = "",
    val selectedStatusFilter: RsvpStatus? = null,
    val selectedParentGiftCategory: GiftCategory? = null,
    val selectedGuestGiftCategory: GiftCategory? = null,
    val selectedGuestGiftAvailability: GiftAvailabilityFilter = GiftAvailabilityFilter.ALL,
    val selectedGuestSizeFilter: String? = null,
    val lastSubmittedRsvp: GuestRsvpSubmission? = null
) {
    val confirmedGuests: List<GuestGroup>
        get() = guests.filter { it.status == RsvpStatus.CONFIRMED }

    val confirmedAdultsCount: Int
        get() = confirmedGuests.sumOf { it.adultsCount }

    val confirmedChildrenCount: Int
        get() = confirmedGuests.sumOf { it.childrenCount }

    val totalConfirmedPeople: Int
        get() = confirmedAdultsCount + confirmedChildrenCount

    val pendingGroupsCount: Int
        get() = guests.count { it.status == RsvpStatus.PENDING }

    val declinedGroupsCount: Int
        get() = guests.count { it.status == RsvpStatus.DECLINED }

    val reservedGiftsCount: Int
        get() = gifts.count { !it.isAvailable }

    val totalGiftsCount: Int
        get() = gifts.size

    val activeDietaryAlertsCount: Int
        get() = guests.count { it.status != RsvpStatus.DECLINED && it.hasDietaryRestrictions }

    val filteredGuests: List<GuestGroup>
        get() = guests.filter { guest ->
            val matchesStatus = selectedStatusFilter == null || guest.status == selectedStatusFilter
            val query = guestSearchQuery.trim()
            val matchesQuery = query.isBlank() ||
                guest.primaryGuestName.contains(query, ignoreCase = true) ||
                guest.dietaryTags.any { it.contains(query, ignoreCase = true) } ||
                guest.dietaryNotes.contains(query, ignoreCase = true)
            matchesStatus && matchesQuery
        }

    val filteredParentGifts: List<GiftItem>
        get() = gifts.filter { gift ->
            selectedParentGiftCategory == null || gift.category == selectedParentGiftCategory
        }

    val availableSizeFilters: List<String>
        get() = gifts.mapNotNull { gift ->
            val text = gift.sizeOrModelSuggestion
            when {
                text.contains("Tam.", ignoreCase = true) -> text.substringAfter("Tam.").trim().split(" ").firstOrNull()?.let { "Tam. $it" }
                text.contains("anos", ignoreCase = true) -> "7+ anos"
                else -> null
            }
        }.distinct()

    val filteredGuestGifts: List<GiftItem>
        get() = gifts.filter { gift ->
            val matchesCategory = selectedGuestGiftCategory == null || gift.category == selectedGuestGiftCategory
            val matchesAvailability = when (selectedGuestGiftAvailability) {
                GiftAvailabilityFilter.ALL -> true
                GiftAvailabilityFilter.AVAILABLE_ONLY -> gift.isAvailable
                GiftAvailabilityFilter.RESERVED_ONLY -> !gift.isAvailable
            }
            val matchesSize = selectedGuestSizeFilter == null ||
                gift.sizeOrModelSuggestion.contains(selectedGuestSizeFilter, ignoreCase = true)
            matchesCategory && matchesAvailability && matchesSize
        }

    val dietarySummaryGroups: List<DietarySummaryGroup>
        get() {
            val activeGuests = guests.filter { it.status != RsvpStatus.DECLINED && it.hasDietaryRestrictions }
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
                            adultsCount = guest.adultsCount,
                            childrenCount = guest.childrenCount,
                            notes = guest.dietaryNotes,
                            isConfirmed = guest.status == RsvpStatus.CONFIRMED
                        )
                    )
                }
            }

            return grouped.entries
                .sortedByDescending { it.value.size }
                .map { DietarySummaryGroup(restrictionLabel = it.key, guestEntries = it.value) }
        }
}
