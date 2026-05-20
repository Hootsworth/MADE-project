package com.afterlight.madeproject.domain.repository

import com.afterlight.madeproject.domain.model.AiSettings
import com.afterlight.madeproject.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun onboardingDone(): Flow<Boolean>
    suspend fun setOnboardingDone(done: Boolean)
    fun themeMode(): Flow<ThemeMode>
    suspend fun getThemeMode(): ThemeMode
    suspend fun setThemeMode(mode: ThemeMode)
    fun aiSettings(): Flow<AiSettings>
    suspend fun getAiSettings(): AiSettings
    suspend fun saveAiSettings(settings: AiSettings)
}
