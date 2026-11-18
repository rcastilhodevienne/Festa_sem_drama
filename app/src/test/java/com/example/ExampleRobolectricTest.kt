package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.RsvpStatus
import com.example.viewmodel.PartyViewModel
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
    fun `gift reservation locks gift against duplicate selection`() {
        val vm = PartyViewModel()
        val availableGift = vm.uiState.value.gifts.first { it.isAvailable }

        vm.reserveGift(availableGift.id, "Tia Clara")
        val reservedGift = vm.uiState.value.gifts.first { it.id == availableGift.id }
        assertFalse(reservedGift.isAvailable)
        assertEquals("Tia Clara", reservedGift.reservedByGuestName)

        // Second reservation attempt must not overwrite the original guest
        vm.reserveGift(availableGift.id, "Outro Convidado")
        val stillReserved = vm.uiState.value.gifts.first { it.id == availableGift.id }
        assertEquals("Tia Clara", stillReserved.reservedByGuestName)
    }

    @Test
    fun `guest rsvp submission updates confirmed headcount and dietary summary`() {
        val vm = PartyViewModel()
        val initialConfirmed = vm.uiState.value.totalConfirmedPeople
        val initialAlerts = vm.uiState.value.activeDietaryAlertsCount

        vm.submitGuestRsvp(
            guestName = "Mariana Teste",
            phoneWhatsApp = "11999998888",
            attending = true,
            adultsCount = 2,
            childrenCount = 1,
            dietaryTags = listOf("Celíaco"),
            dietaryNotes = "Sem glúten"
        )

        val updated = vm.uiState.value
        assertEquals(initialConfirmed + 3, updated.totalConfirmedPeople)
        assertEquals(initialAlerts + 1, updated.activeDietaryAlertsCount)
        assertTrue(updated.guests.any { it.primaryGuestName == "Mariana Teste" && it.status == RsvpStatus.CONFIRMED })
    }
}
