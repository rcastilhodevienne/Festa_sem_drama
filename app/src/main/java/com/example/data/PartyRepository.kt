package com.example.data

import android.content.Context
import com.example.model.GiftCategory
import com.example.model.GiftItem
import com.example.model.GuestGroup
import com.example.model.GuestSource
import com.example.model.PartyEvent
import com.example.model.ProfileRole
import com.example.model.RsvpStatus
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PartyRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseInit.getFirestore(context),
        auth = FirebaseInit.getAuth(context)
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Usuário precisa estar autenticado com o Google.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        require(userId.isNotBlank()) { "userId inválido" }
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
        )
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            require(uid == userId) { "Usuário autenticado diferente do perfil solicitado." }
            val snap = db.collection("users").document(uid).get().await()
            val profile = if (snap.exists()) {
                snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
            Result.success(profile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docPath)
            Result.failure(e)
        }
    }

    suspend fun ensureUserProfileOnSignIn(user: FirebaseUser): Result<UserProfile> {
        val uid = user.uid
        val docRef = db.collection("users").document(uid)
        val docPath = docRef.path
        return try {
            val existingSnap = docRef.get().await()
            val existing = if (existingSnap.exists()) {
                existingSnap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
            if (existing != null) {
                return Result.success(existing)
            }

            val fallbackName = user.displayName
                ?.trim()
                ?.takeIf { it.length >= 2 }
                ?: user.email?.substringBefore("@")?.trim()?.takeIf { it.length >= 2 }
                ?: "Anfitrião"

            val cleanName = fallbackName.take(100)
            val cleanEmail = (user.email ?: "").trim().take(160)
            val payload = mapOf(
                "userId" to uid,
                "displayName" to cleanName,
                "email" to cleanEmail,
                "familyOrHostName" to "",
                "phoneWhatsApp" to "",
                "profileRole" to ProfileRole.PARENT.name,
                "profileCompleted" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(
                UserProfile(
                    id = uid,
                    userId = uid,
                    displayName = cleanName,
                    email = cleanEmail,
                    familyOrHostName = "",
                    phoneWhatsApp = "",
                    profileRole = ProfileRole.PARENT.name,
                    profileCompleted = false
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(
        displayName: String,
        email: String,
        familyOrHostName: String,
        phoneWhatsApp: String,
        profileRole: ProfileRole,
        profileCompleted: Boolean = true
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val cleanName = displayName.trim().take(100)
            require(cleanName.length >= 2) { "Seu nome precisa ter ao menos 2 caracteres." }

            val cleanEmail = email.trim().take(160)
            val cleanFamily = familyOrHostName.trim().take(120)
            val cleanPhone = phoneWhatsApp.trim().take(30)

            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val exists = docRef.get().await().exists()

            if (exists) {
                docRef.update(
                    mapOf(
                        "displayName" to cleanName,
                        "email" to cleanEmail,
                        "familyOrHostName" to cleanFamily,
                        "phoneWhatsApp" to cleanPhone,
                        "profileRole" to profileRole.name,
                        "profileCompleted" to profileCompleted,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "displayName" to cleanName,
                        "email" to cleanEmail,
                        "familyOrHostName" to cleanFamily,
                        "phoneWhatsApp" to cleanPhone,
                        "profileRole" to profileRole.name,
                        "profileCompleted" to profileCompleted,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    fun observeParties(userId: String): Flow<List<PartyEvent>> = flow {
        require(userId.isNotBlank()) { "userId inválido" }
        emitAll(
            db.collection("users").document(userId).collection("parties")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PartyEvent::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
        )
    }

    fun observeGuests(userId: String, partyId: String): Flow<List<GuestGroup>> = flow {
        require(userId.isNotBlank() && partyId.isNotBlank()) { "Identificadores inválidos" }
        emitAll(
            db.collection("users").document(userId)
                .collection("parties").document(partyId)
                .collection("guests")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        GuestGroup::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
        )
    }

    fun observeGifts(userId: String, partyId: String): Flow<List<GiftItem>> = flow {
        require(userId.isNotBlank() && partyId.isNotBlank()) { "Identificadores inválidos" }
        emitAll(
            db.collection("users").document(userId)
                .collection("parties").document(partyId)
                .collection("gifts")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        GiftItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
        )
    }

    suspend fun getParty(partyId: String): Result<PartyEvent?> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId"
        return try {
            val uid = requireUserId()
            require(partyId.isNotBlank()) { "partyId inválido" }
            val docRef = db.collection("users").document(uid).collection("parties").document(partyId)
            docPath = docRef.path
            val snap = docRef.get().await()
            val party = snap.toObject(PartyEvent::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            Result.success(party)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docPath)
            Result.failure(e)
        }
    }

    suspend fun getGuest(partyId: String, guestId: String): Result<GuestGroup?> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId/guests/$guestId"
        return try {
            val uid = requireUserId()
            require(partyId.isNotBlank() && guestId.isNotBlank()) { "IDs inválidos" }
            val docRef = db.collection("users").document(uid)
                .collection("parties").document(partyId)
                .collection("guests").document(guestId)
            docPath = docRef.path
            val snap = docRef.get().await()
            val guest = snap.toObject(GuestGroup::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            Result.success(guest)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docPath)
            Result.failure(e)
        }
    }

    suspend fun getGift(partyId: String, giftId: String): Result<GiftItem?> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId/gifts/$giftId"
        return try {
            val uid = requireUserId()
            require(partyId.isNotBlank() && giftId.isNotBlank()) { "IDs inválidos" }
            val docRef = db.collection("users").document(uid)
                .collection("parties").document(partyId)
                .collection("gifts").document(giftId)
            docPath = docRef.path
            val snap = docRef.get().await()
            val gift = snap.toObject(GiftItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            Result.success(gift)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docPath)
            Result.failure(e)
        }
    }

    suspend fun createParty(
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
    ): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties"
        return try {
            val uid = requireUserId()
            val cleanChild = childName.trim().take(60)
            val cleanTitle = title.trim().take(120)
            val cleanVenue = venueName.trim().take(120)
            val cleanAddress = fullAddress.trim().take(240)
            require(cleanChild.isNotEmpty()) { "Nome da criança é obrigatório." }
            require(cleanTitle.length >= 2) { "Título da festa precisa de ao menos 2 caracteres." }
            require(Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(eventDateIso.trim())) {
                "Data da festa deve estar no formato AAAA-MM-DD."
            }
            require(dateDisplay.trim().length >= 2) { "Descrição da data é obrigatória." }
            require(timeDisplay.trim().length >= 2) { "Horário é obrigatório." }
            require(cleanVenue.length >= 2) { "Nome do local é obrigatório." }
            require(cleanAddress.length >= 2) { "Endereço é obrigatório." }

            val docRef = db.collection("users").document(uid).collection("parties").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "childName" to cleanChild,
                "ageTurning" to ageTurning.coerceIn(0, 120).toLong(),
                "title" to cleanTitle,
                "subtitle" to subtitle.trim().take(240),
                "eventDateIso" to eventDateIso.trim(),
                "dateDisplay" to dateDisplay.trim().take(80),
                "timeDisplay" to timeDisplay.trim().take(60),
                "venueName" to cleanVenue,
                "fullAddress" to cleanAddress,
                "rsvpDeadline" to rsvpDeadline.trim().take(80),
                "hostSignature" to hostSignature.trim().take(120),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateParty(
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
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId"
        return try {
            val uid = requireUserId()
            require(partyId.isNotBlank()) { "partyId inválido" }
            val cleanChild = childName.trim().take(60)
            val cleanTitle = title.trim().take(120)
            require(cleanChild.isNotEmpty()) { "Nome da criança é obrigatório." }
            require(cleanTitle.length >= 2) { "Título da festa é obrigatório." }
            require(Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(eventDateIso.trim())) {
                "Data da festa deve estar no formato AAAA-MM-DD."
            }

            val docRef = db.collection("users").document(uid).collection("parties").document(partyId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "childName" to cleanChild,
                    "ageTurning" to ageTurning.coerceIn(0, 120).toLong(),
                    "title" to cleanTitle,
                    "subtitle" to subtitle.trim().take(240),
                    "eventDateIso" to eventDateIso.trim(),
                    "dateDisplay" to dateDisplay.trim().take(80),
                    "timeDisplay" to timeDisplay.trim().take(60),
                    "venueName" to venueName.trim().take(120),
                    "fullAddress" to fullAddress.trim().take(240),
                    "rsvpDeadline" to rsvpDeadline.trim().take(80),
                    "hostSignature" to hostSignature.trim().take(120),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteParty(partyId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId"
        return try {
            val uid = requireUserId()
            require(partyId.isNotBlank()) { "partyId inválido" }
            val partyRef = db.collection("users").document(uid).collection("parties").document(partyId)
            docPath = partyRef.path

            // Delete child guests & gifts first to avoid orphaned subcollections
            val guestsSnap = partyRef.collection("guests").get().await()
            val giftsSnap = partyRef.collection("gifts").get().await()
            val batch = db.batch()
            guestsSnap.documents.forEach { batch.delete(it.reference) }
            giftsSnap.documents.forEach { batch.delete(it.reference) }
            batch.delete(partyRef)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addGuest(
        partyId: String,
        primaryGuestName: String,
        adultsCount: Int,
        childrenCount: Int,
        phoneWhatsApp: String,
        status: RsvpStatus,
        dietaryTags: List<String>,
        dietaryNotes: String,
        source: GuestSource
    ): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId/guests"
        return try {
            val uid = requireUserId()
            val cleanName = primaryGuestName.trim().take(100)
            require(partyId.isNotBlank()) { "Selecione uma festa primeiro." }
            require(cleanName.isNotEmpty()) { "Nome do convidado é obrigatório." }

            val docRef = db.collection("users").document(uid)
                .collection("parties").document(partyId)
                .collection("guests").document()
            docPath = docRef.path

            val boundedTags = dietaryTags.map { it.trim().take(50) }.filter { it.isNotEmpty() }.distinct().take(10)
            val payload = mapOf(
                "userId" to uid,
                "partyId" to partyId,
                "primaryGuestName" to cleanName,
                "adultsCount" to adultsCount.coerceIn(0, 50).toLong(),
                "childrenCount" to childrenCount.coerceIn(0, 50).toLong(),
                "phoneWhatsApp" to phoneWhatsApp.trim().take(30),
                "status" to status.name,
                "dietaryTags" to boundedTags,
                "dietaryNotes" to dietaryNotes.trim().take(400),
                "source" to source.name,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateGuest(
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
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId/guests/$guestId"
        return try {
            val uid = requireUserId()
            val cleanName = primaryGuestName.trim().take(100)
            require(partyId.isNotBlank() && guestId.isNotBlank()) { "IDs inválidos" }
            require(cleanName.isNotEmpty()) { "Nome do convidado é obrigatório." }

            val docRef = db.collection("users").document(uid)
                .collection("parties").document(partyId)
                .collection("guests").document(guestId)
            docPath = docRef.path

            val boundedTags = dietaryTags.map { it.trim().take(50) }.filter { it.isNotEmpty() }.distinct().take(10)
            docRef.update(
                mapOf(
                    "primaryGuestName" to cleanName,
                    "adultsCount" to adultsCount.coerceIn(0, 50).toLong(),
                    "childrenCount" to childrenCount.coerceIn(0, 50).toLong(),
                    "phoneWhatsApp" to phoneWhatsApp.trim().take(30),
                    "status" to status.name,
                    "dietaryTags" to boundedTags,
                    "dietaryNotes" to dietaryNotes.trim().take(400),
                    "source" to source.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateGuestStatus(
        partyId: String,
        guestId: String,
        status: RsvpStatus
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId/guests/$guestId"
        return try {
            val uid = requireUserId()
            require(partyId.isNotBlank() && guestId.isNotBlank()) { "IDs inválidos" }
            val docRef = db.collection("users").document(uid)
                .collection("parties").document(partyId)
                .collection("guests").document(guestId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun removeGuest(partyId: String, guestId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId/guests/$guestId"
        return try {
            val uid = requireUserId()
            require(partyId.isNotBlank() && guestId.isNotBlank()) { "IDs inválidos" }
            val docRef = db.collection("users").document(uid)
                .collection("parties").document(partyId)
                .collection("guests").document(guestId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addGift(
        partyId: String,
        name: String,
        category: GiftCategory,
        sizeOrModelSuggestion: String,
        details: String
    ): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId/gifts"
        return try {
            val uid = requireUserId()
            val cleanName = name.trim().take(120)
            require(partyId.isNotBlank()) { "Selecione uma festa primeiro." }
            require(cleanName.isNotEmpty()) { "Nome do presente é obrigatório." }

            val docRef = db.collection("users").document(uid)
                .collection("parties").document(partyId)
                .collection("gifts").document()
            docPath = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "partyId" to partyId,
                "name" to cleanName,
                "category" to category.name,
                "sizeOrModelSuggestion" to sizeOrModelSuggestion.trim().ifBlank { "Livre escolha" }.take(120),
                "details" to details.trim().take(300),
                "reservedByGuestName" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateGift(
        partyId: String,
        giftId: String,
        name: String,
        category: GiftCategory,
        sizeOrModelSuggestion: String,
        details: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId/gifts/$giftId"
        return try {
            val uid = requireUserId()
            val cleanName = name.trim().take(120)
            require(partyId.isNotBlank() && giftId.isNotBlank()) { "IDs inválidos" }
            require(cleanName.isNotEmpty()) { "Nome do presente é obrigatório." }

            val docRef = db.collection("users").document(uid)
                .collection("parties").document(partyId)
                .collection("gifts").document(giftId)
            docPath = docRef.path

            docRef.update(
                mapOf(
                    "name" to cleanName,
                    "category" to category.name,
                    "sizeOrModelSuggestion" to sizeOrModelSuggestion.trim().ifBlank { "Livre escolha" }.take(120),
                    "details" to details.trim().take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun reserveGift(
        partyId: String,
        giftId: String,
        guestName: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId/gifts/$giftId"
        return try {
            val uid = requireUserId()
            val cleanGuest = guestName.trim().take(100)
            require(partyId.isNotBlank() && giftId.isNotBlank()) { "IDs inválidos" }
            require(cleanGuest.isNotEmpty()) { "Informe seu nome para reservar o presente." }

            val docRef = db.collection("users").document(uid)
                .collection("parties").document(partyId)
                .collection("gifts").document(giftId)
            docPath = docRef.path

            docRef.update(
                mapOf(
                    "reservedByGuestName" to cleanGuest,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun releaseGift(partyId: String, giftId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId/gifts/$giftId"
        return try {
            val uid = requireUserId()
            require(partyId.isNotBlank() && giftId.isNotBlank()) { "IDs inválidos" }
            val docRef = db.collection("users").document(uid)
                .collection("parties").document(partyId)
                .collection("gifts").document(giftId)
            docPath = docRef.path

            docRef.update(
                mapOf(
                    "reservedByGuestName" to "",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun removeGift(partyId: String, giftId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/parties/$partyId/gifts/$giftId"
        return try {
            val uid = requireUserId()
            require(partyId.isNotBlank() && giftId.isNotBlank()) { "IDs inválidos" }
            val docRef = db.collection("users").document(uid)
                .collection("parties").document(partyId)
                .collection("gifts").document(giftId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
