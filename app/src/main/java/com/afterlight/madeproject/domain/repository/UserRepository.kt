package com.afterlight.madeproject.domain.repository

import com.afterlight.madeproject.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveProfile(profile: UserProfile): Result<Unit>
    fun observeCurrentUser(): Flow<UserProfile?>
    suspend fun getCurrentUser(): UserProfile?
}
