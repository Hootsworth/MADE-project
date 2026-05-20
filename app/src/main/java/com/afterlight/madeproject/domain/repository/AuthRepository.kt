package com.afterlight.madeproject.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(name: String, email: String, password: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit>
    fun authStateChanges(): Flow<String?>
    suspend fun signInAnonymously(): Result<Unit>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun isEmailVerified(): Boolean
    suspend fun signOut()
    suspend fun currentUid(): String?
    suspend fun currentEmail(): String?
    suspend fun currentDisplayName(): String?
    suspend fun isAnonymous(): Boolean
    suspend fun validateCollegeDomain(email: String): Result<Boolean>
}
