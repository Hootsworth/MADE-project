package com.afterlight.madeproject.data.repository

import com.afterlight.madeproject.domain.model.UserProfile
import com.afterlight.madeproject.domain.model.UserRole
import com.afterlight.madeproject.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("User not authenticated")
        firestore.collection("users").document(uid).set(
            mapOf(
                "email" to profile.email,
                "name" to profile.name,
                "year" to profile.year,
                "department" to profile.department,
                "interests" to profile.interests,
                "role" to profile.role.name.lowercase(),
                "referralCode" to profile.referralCode,
                "badgesEarned" to profile.badgesEarned,
                "createdAt" to profile.createdAt
            ),
            SetOptions.merge()
        ).await()
    }

    override fun observeCurrentUser(): Flow<UserProfile?> = callbackFlow {
        var registration: com.google.firebase.firestore.ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            registration?.remove()
            registration = null

            val uid = firebaseAuth.currentUser?.uid
            if (uid == null) {
                trySend(null)
                return@AuthStateListener
            }

            registration = firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        trySend(null)
                    } else {
                        val user = runCatching { snapshot.toUser(uid) }.getOrNull()
                        trySend(user)
                    }
                }
        }

        auth.addAuthStateListener(authListener)
        auth.currentUser?.uid?.let { uid ->
            registration = firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        trySend(null)
                    } else {
                        val user = runCatching { snapshot.toUser(uid) }.getOrNull()
                        trySend(user)
                    }
                }
        } ?: trySend(null)

        awaitClose {
            registration?.remove()
            auth.removeAuthStateListener(authListener)
        }
    }

    override suspend fun getCurrentUser(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null
        val snapshot = firestore.collection("users").document(uid).get().await()
        return if (snapshot.exists()) snapshot.toUser(uid) else null
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(uid: String): UserProfile {
        return UserProfile(
            uid = uid,
            email = getString("email").orEmpty(),
            name = getString("name").orEmpty(),
            year = getString("year").orEmpty(),
            department = getString("department").orEmpty(),
            interests = getStringList("interests"),
            role = if (getString("role") == "host") UserRole.HOST else UserRole.STUDENT,
            referralCode = getString("referralCode").orEmpty(),
            badgesEarned = getStringList("badgesEarned"),
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.getStringList(field: String): List<String> {
        return (get(field) as? List<*>).orEmpty().mapNotNull { it as? String }
    }
}
