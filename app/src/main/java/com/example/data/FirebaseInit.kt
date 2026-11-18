package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings

/**
 * Centraliza a inicialização segura do Firebase (App, Auth e Firestore com o
 * database ID provisionado em R.string.firestore_database_id).
 */
object FirebaseInit {

    @Volatile
    private var cachedFirestore: FirebaseFirestore? = null

    fun ensureInitialized(context: Context): FirebaseApp? {
        val appContext = context.applicationContext
        return if (FirebaseApp.getApps(appContext).isEmpty()) {
            FirebaseApp.initializeApp(appContext)
        } else {
            FirebaseApp.getInstance()
        }
    }

    fun getAuth(context: Context): FirebaseAuth {
        ensureInitialized(context)
        return Firebase.auth
    }

    fun getFirestore(context: Context): FirebaseFirestore {
        cachedFirestore?.let { return it }
        synchronized(this) {
            cachedFirestore?.let { return it }
            val appContext = context.applicationContext
            ensureInitialized(appContext)
            val databaseId = appContext.getString(R.string.firestore_database_id)
            val instance = FirebaseFirestore.getInstance(databaseId).apply {
                firestoreSettings = firestoreSettings {
                    setLocalCacheSettings(persistentCacheSettings {})
                }
            }
            cachedFirestore = instance
            return instance
        }
    }

    fun createPartyRepository(context: Context): PartyRepository {
        val appContext = context.applicationContext
        return PartyRepository(
            db = getFirestore(appContext),
            auth = getAuth(appContext)
        )
    }
}
