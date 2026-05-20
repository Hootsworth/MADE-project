package com.afterlight.madeproject.data.repository

import com.afterlight.madeproject.BuildConfig
import com.afterlight.madeproject.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override fun authStateChanges(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser?.uid)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("User creation failed")
        val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
            displayName = name
        }
        user.updateProfile(profileUpdates).await()
    }

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    override suspend fun signInAnonymously(): Result<Unit> = runCatching {
        auth.signInAnonymously().await()
    }

    override suspend fun sendEmailVerification(): Result<Unit> = runCatching {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    override suspend fun isEmailVerified(): Boolean {
        auth.currentUser?.reload()?.await()
        return auth.currentUser?.isEmailVerified == true
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun currentUid(): String? = auth.currentUser?.uid

    override suspend fun currentEmail(): String? = auth.currentUser?.email
    override suspend fun currentDisplayName(): String? = auth.currentUser?.displayName
    override suspend fun isAnonymous(): Boolean = auth.currentUser?.isAnonymous == true

    override suspend fun validateCollegeDomain(email: String): Result<Boolean> = runCatching {
        val domain = BuildConfig.COLLEGE_EMAIL_DOMAIN.trim().lowercase()
        val normalized = email.trim().lowercase()
        if (domain.isBlank()) return@runCatching false
        val emailDomain = normalized.substringAfter('@', missingDelimiterValue = "")
        emailDomain == domain || emailDomain.endsWith(".$domain")
    }
}
