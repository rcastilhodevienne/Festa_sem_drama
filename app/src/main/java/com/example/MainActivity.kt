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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.FirebaseInit
import com.example.model.AppViewMode
import com.example.model.DataLoadState
import com.example.model.GiftItem
import com.example.model.GuestGroup
import com.example.model.PartyEvent
import com.example.model.PartyUiState
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.EditProfileModal
import com.example.ui.auth.ProfileSetupScreen
import com.example.ui.auth.signOutFromFirebase
import com.example.ui.components.AddGiftModal
import com.example.ui.components.AddGuestModal
import com.example.ui.components.DeletePartyConfirmDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ExternalActionHelper
import com.example.ui.components.PartyFormModal
import com.example.ui.components.ReserveGiftModal
import com.example.ui.components.TopModeSwitcherBar
import com.example.ui.screens.GuestPartyScreen
import com.example.ui.screens.ParentsDashboardScreen
import com.example.ui.theme.FestaSemDramaTheme
import com.example.viewmodel.PartyViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseInit.ensureInitialized(this)
        enableEdgeToEdge()
        setContent {
            FestaSemDramaTheme {
                AppNavigation()
            }
        }
    }
}

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val auth = remember(context) { FirebaseInit.getAuth(context) }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            FestaSemDramaAuthenticatedApp(user = user)
        }
    }
}

@Composable
fun FestaSemDramaAuthenticatedApp(user: FirebaseUser) {
    val viewModel: PartyViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                PartyViewModel(
                    repository = FirebaseInit.createPartyRepository(app),
                    currentUserId = user.uid
                )
            }
        }
    )

    val loadState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var showPartyFormModal by remember { mutableStateOf<Pair<Boolean, PartyEvent?>>(false to null) }
    var partyPendingDelete by remember { mutableStateOf<PartyEvent?>(null) }
    var showGuestModal by remember { mutableStateOf<Pair<Boolean, GuestGroup?>>(false to null) }
    var showGiftModal by remember { mutableStateOf<Pair<Boolean, GiftItem?>>(false to null) }
    var giftPendingReservation by remember { mutableStateOf<Pair<GiftItem, Pair<String, Boolean>>?>(null) }
    var showEditProfileModal by remember { mutableStateOf(false) }

    LaunchedEffect(user.uid) {
        viewModel.ensureUserProfile(user)
    }

    LaunchedEffect(snackbarMessage) {
        val message = snackbarMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    val successState = (loadState as? DataLoadState.Success<PartyUiState>)?.data
    val activeProfile = successState?.userProfile
    val isProfileReady = activeProfile?.profileCompleted == true

    BackHandler(enabled = isProfileReady && successState.viewMode == AppViewMode.GUEST_PAGE) {
        viewModel.switchViewMode(AppViewMode.PARENTS_DASHBOARD)
    }

    val userLabel = activeProfile?.displayName?.takeIf { it.isNotBlank() }
        ?: user.displayName?.takeIf { it.isNotBlank() }
        ?: user.email?.takeIf { it.isNotBlank() }
        ?: "Conta Google"

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
                if (isProfileReady) {
                    TopModeSwitcherBar(
                        currentMode = successState.viewMode,
                        userDisplayName = userLabel,
                        onModeSelected = viewModel::switchViewMode,
                        onEditProfileClick = { showEditProfileModal = true },
                        onSignOutClick = {
                            signOutFromFirebase(
                                context = context,
                                credentialManager = credentialManager,
                                scope = scope
                            )
                        }
                    )
                }

                when (val current = loadState) {
                    is DataLoadState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Carregando sua base de dados...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is DataLoadState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            EmptyStateCard(
                                icon = Icons.Outlined.CloudOff,
                                title = "Erro ao conectar à base de dados",
                                subtitle = current.message
                            )
                        }
                    }

                    is DataLoadState.Success -> {
                        val uiState = current.data
                        if (uiState.userProfile == null || !uiState.userProfile.profileCompleted) {
                            ProfileSetupScreen(
                                initialProfile = uiState.userProfile,
                                authDisplayName = user.displayName.orEmpty(),
                                authEmail = user.email.orEmpty(),
                                isSaving = uiState.isMutating,
                                onSaveProfile = viewModel::saveUserProfile,
                                onSignOutClick = {
                                    signOutFromFirebase(
                                        context = context,
                                        credentialManager = credentialManager,
                                        scope = scope
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        } else {
                            val activePartyId = uiState.activeParty?.id

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
                                            onSelectParty = viewModel::selectParty,
                                            onOpenCreateParty = { showPartyFormModal = true to null },
                                            onOpenEditParty = { party -> showPartyFormModal = true to party },
                                            onOpenDeleteParty = { party -> partyPendingDelete = party },
                                            onSelectTab = viewModel::selectParentTab,
                                            onSearchQueryChange = viewModel::updateGuestSearchQuery,
                                            onSelectStatusFilter = viewModel::selectStatusFilter,
                                            onOpenAddGuest = { showGuestModal = true to null },
                                            onOpenEditGuest = { guest -> showGuestModal = true to guest },
                                            onUpdateGuestStatus = { guestId, newStatus ->
                                                if (activePartyId != null) {
                                                    viewModel.updateGuestStatus(activePartyId, guestId, newStatus)
                                                }
                                            },
                                            onRemoveGuest = { guestId ->
                                                if (activePartyId != null) {
                                                    viewModel.removeGuest(activePartyId, guestId)
                                                }
                                            },
                                            onWhatsAppReminder = { event, guest ->
                                                val msg = viewModel.buildWhatsAppReminderMessage(event, guest)
                                                ExternalActionHelper.openWhatsAppReminder(
                                                    context = context,
                                                    phone = guest.phoneWhatsApp,
                                                    message = msg,
                                                    onResultMessage = viewModel::showMessage
                                                )
                                            },
                                            onSelectGiftCategory = viewModel::selectParentGiftCategory,
                                            onOpenAddGift = { showGiftModal = true to null },
                                            onOpenEditGift = { gift -> showGiftModal = true to gift },
                                            onRequestManualReserveGift = { gift ->
                                                giftPendingReservation = gift to ("" to true)
                                            },
                                            onReleaseGift = { giftId ->
                                                if (activePartyId != null) {
                                                    viewModel.releaseGift(activePartyId, giftId)
                                                }
                                            },
                                            onRemoveGift = { giftId ->
                                                if (activePartyId != null) {
                                                    viewModel.removeGift(activePartyId, giftId)
                                                }
                                            },
                                            onCopyBuffetReport = {
                                                val report = viewModel.buildBuffetDietaryReport(uiState)
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
                                            onSwitchToParents = {
                                                viewModel.switchViewMode(AppViewMode.PARENTS_DASHBOARD)
                                            },
                                            onSubmitRsvp = { name, phone, attending, adults, children, tags, notes ->
                                                if (activePartyId != null) {
                                                    viewModel.submitGuestRsvp(
                                                        partyId = activePartyId,
                                                        existingGuests = uiState.guests,
                                                        guestName = name,
                                                        phoneWhatsApp = phone,
                                                        attending = attending,
                                                        adultsCount = adults,
                                                        childrenCount = children,
                                                        dietaryTags = tags,
                                                        dietaryNotes = notes
                                                    )
                                                }
                                            },
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

                            if (showEditProfileModal) {
                                EditProfileModal(
                                    profile = uiState.userProfile,
                                    fallbackEmail = user.email.orEmpty(),
                                    onDismiss = { showEditProfileModal = false },
                                    onConfirmSave = { displayName, email, family, phone, role ->
                                        viewModel.saveUserProfile(
                                            displayName = displayName,
                                            email = email,
                                            familyOrHostName = family,
                                            phoneWhatsApp = phone,
                                            profileRole = role
                                        )
                                        showEditProfileModal = false
                                    }
                                )
                            }

                            if (showPartyFormModal.first) {
                                val editingParty = showPartyFormModal.second
                                PartyFormModal(
                                    existingParty = editingParty,
                                    defaultHostSignature = uiState.userProfile.familyOrHostName,
                                onDismiss = { showPartyFormModal = false to null },
                                onConfirmSave = { child, age, title, sub, iso, dateDisp, timeDisp, venue, addr, deadline, sig ->
                                    if (editingParty == null) {
                                        viewModel.createParty(
                                            childName = child,
                                            ageTurning = age,
                                            title = title,
                                            subtitle = sub,
                                            eventDateIso = iso,
                                            dateDisplay = dateDisp,
                                            timeDisplay = timeDisp,
                                            venueName = venue,
                                            fullAddress = addr,
                                            rsvpDeadline = deadline,
                                            hostSignature = sig
                                        )
                                    } else {
                                        viewModel.updateParty(
                                            partyId = editingParty.id,
                                            childName = child,
                                            ageTurning = age,
                                            title = title,
                                            subtitle = sub,
                                            eventDateIso = iso,
                                            dateDisplay = dateDisp,
                                            timeDisplay = timeDisp,
                                            venueName = venue,
                                            fullAddress = addr,
                                            rsvpDeadline = deadline,
                                            hostSignature = sig
                                        )
                                    }
                                    showPartyFormModal = false to null
                                }
                            )
                        }

                        partyPendingDelete?.let { party ->
                            DeletePartyConfirmDialog(
                                partyTitle = party.title,
                                onDismiss = { partyPendingDelete = null },
                                onConfirmDelete = {
                                    viewModel.deleteParty(party.id)
                                    partyPendingDelete = null
                                }
                            )
                        }

                        if (showGuestModal.first && activePartyId != null) {
                            val editingGuest = showGuestModal.second
                            AddGuestModal(
                                existingGuest = editingGuest,
                                onDismiss = { showGuestModal = false to null },
                                onConfirmAdd = { name, adults, children, phone, status, tags, notes ->
                                    if (editingGuest == null) {
                                        viewModel.addGuest(
                                            partyId = activePartyId,
                                            primaryGuestName = name,
                                            adultsCount = adults,
                                            childrenCount = children,
                                            phoneWhatsApp = phone,
                                            status = status,
                                            dietaryTags = tags,
                                            dietaryNotes = notes
                                        )
                                    } else {
                                        viewModel.updateGuest(
                                            partyId = activePartyId,
                                            guestId = editingGuest.id,
                                            primaryGuestName = name,
                                            adultsCount = adults,
                                            childrenCount = children,
                                            phoneWhatsApp = phone,
                                            status = status,
                                            dietaryTags = tags,
                                            dietaryNotes = notes,
                                            source = editingGuest.guestSource
                                        )
                                    }
                                    showGuestModal = false to null
                                }
                            )
                        }

                        if (showGiftModal.first && activePartyId != null) {
                            val editingGift = showGiftModal.second
                            AddGiftModal(
                                existingGift = editingGift,
                                onDismiss = { showGiftModal = false to null },
                                onConfirmAdd = { name, category, sizeOrModel, details ->
                                    if (editingGift == null) {
                                        viewModel.addGift(
                                            partyId = activePartyId,
                                            name = name,
                                            category = category,
                                            sizeOrModelSuggestion = sizeOrModel,
                                            details = details
                                        )
                                    } else {
                                        viewModel.updateGift(
                                            partyId = activePartyId,
                                            giftId = editingGift.id,
                                            name = name,
                                            category = category,
                                            sizeOrModelSuggestion = sizeOrModel,
                                            details = details
                                        )
                                    }
                                    showGiftModal = false to null
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
                                    if (activePartyId != null) {
                                        viewModel.reserveGift(activePartyId, gift.id, guestName)
                                    }
                                    giftPendingReservation = null
                                }
                            )
                        }
                        }
                    }
                }
            }
        }
    }
}
