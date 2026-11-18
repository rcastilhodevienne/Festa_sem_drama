package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.GiftCategory
import com.example.model.GiftItem
import com.example.model.GuestGroup
import com.example.model.GuestSource
import com.example.model.PartyEvent
import com.example.model.PartyUiState
import com.example.model.ProfileRole
import com.example.model.RsvpStatus
import com.example.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FestaSemDrama", appName)
    }

    @Test
    fun `initial PartyUiState starts empty with zero mock data`() {
        val emptyState = PartyUiState()
        assertEquals(null, emptyState.activeParty)
        assertTrue(emptyState.parties.isEmpty())
        assertTrue(emptyState.guests.isEmpty())
        assertTrue(emptyState.gifts.isEmpty())
        assertEquals(0, emptyState.totalConfirmedPeople)
        assertEquals(0, emptyState.pendingGroupsCount)
        assertEquals(0, emptyState.activeDietaryAlertsCount)
    }

    @Test
    fun `PartyUiState computes metrics and dietary grouping from real entries`() {
        val event = PartyEvent(
            id = "p1",
            userId = "u1",
            childName = "Helena",
            ageTurning = 5L,
            title = "5 Anos da Helena"
        )
        val guests = listOf(
            GuestGroup(
                id = "g1",
                userId = "u1",
                partyId = "p1",
                primaryGuestName = "Familia Costa",
                adultsCount = 2L,
                childrenCount = 1L,
                status = RsvpStatus.CONFIRMED.name,
                dietaryTags = listOf("APLV"),
                source = GuestSource.GUEST_FORM.name
            ),
            GuestGroup(
                id = "g2",
                userId = "u1",
                partyId = "p1",
                primaryGuestName = "Tio Marcos",
                adultsCount = 1L,
                childrenCount = 0L,
                status = RsvpStatus.PENDING.name,
                source = GuestSource.PARENTS.name
            )
        )
        val gifts = listOf(
            GiftItem(
                id = "gf1",
                userId = "u1",
                partyId = "p1",
                name = "Livro Ilustrado",
                category = GiftCategory.BOOKS.name,
                reservedByGuestName = "Familia Costa"
            ),
            GiftItem(
                id = "gf2",
                userId = "u1",
                partyId = "p1",
                name = "Patins",
                category = GiftCategory.TOYS.name,
                reservedByGuestName = ""
            )
        )

        val state = PartyUiState(
            parties = listOf(event),
            activeParty = event,
            guests = guests,
            gifts = gifts
        )

        assertEquals(3, state.totalConfirmedPeople)
        assertEquals(1, state.pendingGroupsCount)
        assertEquals(1, state.formResponses.size)
        assertEquals(1, state.reservedGiftsCount)
        assertEquals(2, state.totalGiftsCount)
        assertEquals(1, state.activeDietaryAlertsCount)
        assertEquals(1, state.dietarySummaryGroups.size)
        assertFalse(gifts[0].available)
        assertTrue(gifts[1].available)
    }

    @Test
    fun `UserProfile defaults and ProfileRole parsing stay resilient`() {
        val profile = UserProfile(
            id = "u1",
            userId = "u1",
            displayName = "Ana Paula",
            email = "ana@example.com",
            familyOrHostName = "Família Paula",
            profileRole = ProfileRole.ORGANIZER.name,
            profileCompleted = true
        )
        assertEquals(ProfileRole.ORGANIZER, profile.role)
        assertTrue(profile.profileCompleted)

        val unknownRoleProfile = UserProfile(profileRole = "UNKNOWN_LEGACY")
        assertEquals(ProfileRole.PARENT, unknownRoleProfile.role)
        assertFalse(unknownRoleProfile.profileCompleted)
    }
}
