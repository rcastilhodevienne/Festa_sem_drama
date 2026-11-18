package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppViewMode
import com.example.model.GiftItem
import com.example.ui.components.AddGiftModal
import com.example.ui.components.AddGuestModal
import com.example.ui.components.ExternalActionHelper
import com.example.ui.components.ReserveGiftModal
import com.example.ui.components.TopModeSwitcherBar
import com.example.ui.screens.GuestPartyScreen
import com.example.ui.screens.ParentsDashboardScreen
import com.example.ui.theme.FestaSemDramaTheme
import com.example.viewmodel.PartyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FestaSemDramaTheme {
                FestaSemDramaApp()
            }
        }
    }
}

@Composable
fun FestaSemDramaApp(
    viewModel: PartyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showAddGuestModal by remember { mutableStateOf(false) }
    var showAddGiftModal by remember { mutableStateOf(false) }
    var giftPendingReservation by remember { mutableStateOf<Pair<GiftItem, Pair<String, Boolean>>?>(null) }

    LaunchedEffect(snackbarMessage) {
        val message = snackbarMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    // Return to Parents Dashboard on back press when previewing Guest view
    BackHandler(enabled = uiState.viewMode == AppViewMode.GUEST_PAGE) {
        viewModel.switchViewMode(AppViewMode.PARENTS_DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
                TopModeSwitcherBar(
                    currentMode = uiState.viewMode,
                    onModeSelected = viewModel::switchViewMode
                )

                AnimatedContent(
                    targetState = uiState.viewMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    label = "appViewMode"
                ) { mode ->
                    when (mode) {
                        AppViewMode.PARENTS_DASHBOARD -> {
                            ParentsDashboardScreen(
                                uiState = uiState,
                                onSelectTab = viewModel::selectParentTab,
                                onSearchQueryChange = viewModel::updateGuestSearchQuery,
                                onSelectStatusFilter = viewModel::selectStatusFilter,
                                onOpenAddGuest = { showAddGuestModal = true },
                                onUpdateGuestStatus = viewModel::updateGuestStatus,
                                onRemoveGuest = viewModel::removeGuest,
                                onWhatsAppReminder = { guest ->
                                    val msg = viewModel.buildWhatsAppReminderMessage(guest)
                                    ExternalActionHelper.openWhatsAppReminder(
                                        context = context,
                                        phone = guest.phoneWhatsApp,
                                        message = msg,
                                        onResultMessage = viewModel::showMessage
                                    )
                                },
                                onSelectGiftCategory = viewModel::selectParentGiftCategory,
                                onOpenAddGift = { showAddGiftModal = true },
                                onRequestManualReserveGift = { gift ->
                                    giftPendingReservation = gift to ("" to true)
                                },
                                onReleaseGift = viewModel::releaseGift,
                                onCopyBuffetReport = {
                                    val report = viewModel.buildBuffetDietaryReport()
                                    ExternalActionHelper.copyToClipboard(
                                        context = context,
                                        label = "Resumo Alimentar FestaSemDrama",
                                        text = report
                                    )
                                    viewModel.showMessage("Resumo alimentar copiado para enviar ao buffet!")
                                },
                                onFeedbackMessage = viewModel::showMessage
                            )
                        }

                        AppViewMode.GUEST_PAGE -> {
                            GuestPartyScreen(
                                uiState = uiState,
                                onSubmitRsvp = viewModel::submitGuestRsvp,
                                onClearRsvpReceipt = viewModel::clearGuestRsvpReceipt,
                                onSelectGiftCategory = viewModel::selectGuestGiftCategory,
                                onSelectGiftAvailability = viewModel::selectGuestGiftAvailability,
                                onSelectGiftSizeFilter = viewModel::selectGuestSizeFilter,
                                onRequestReserveGift = { gift, prefillName ->
                                    giftPendingReservation = gift to (prefillName to false)
                                },
                                onFeedbackMessage = viewModel::showMessage
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddGuestModal) {
        AddGuestModal(
            onDismiss = { showAddGuestModal = false },
            onConfirmAdd = { name, adults, children, phone, status, tags, notes ->
                viewModel.addGuest(name, adults, children, phone, status, tags, notes)
                showAddGuestModal = false
            }
        )
    }

    if (showAddGiftModal) {
        AddGiftModal(
            onDismiss = { showAddGiftModal = false },
            onConfirmAdd = { name, category, sizeOrModel, details ->
                viewModel.addGift(name, category, sizeOrModel, details)
                showAddGiftModal = false
            }
        )
    }

    giftPendingReservation?.let { (gift, meta) ->
        val (prefillName, isParentManual) = meta
        ReserveGiftModal(
            gift = gift,
            initialGuestName = prefillName,
            isParentManualAction = isParentManual,
            onDismiss = { giftPendingReservation = null },
            onConfirmReserve = { guestName ->
                viewModel.reserveGift(gift.id, guestName)
                giftPendingReservation = null
            }
        )
    }
}
