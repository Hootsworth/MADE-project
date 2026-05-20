package com.afterlight.madeproject.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.afterlight.madeproject.domain.model.AiProvider
import com.afterlight.madeproject.domain.model.AiSettings
import com.afterlight.madeproject.domain.model.ThemeMode
import com.afterlight.madeproject.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("paperlike_settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val onboardingKey = booleanPreferencesKey("onboarding_done")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val aiProviderKey = stringPreferencesKey("ai_provider")
    private val openAiKey = stringPreferencesKey("openai_api_key")
    private val geminiKey = stringPreferencesKey("gemini_api_key")
    private val aiModelKey = stringPreferencesKey("ai_model")

    override fun onboardingDone(): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[onboardingKey] ?: false
    }

    override suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[onboardingKey] = done
        }
    }

    override fun themeMode(): Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        runCatching {
            ThemeMode.valueOf(prefs[themeModeKey] ?: ThemeMode.SYSTEM.name)
        }.getOrDefault(ThemeMode.SYSTEM)
    }

    override suspend fun getThemeMode(): ThemeMode = themeMode().first()

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    override fun aiSettings(): Flow<AiSettings> = context.dataStore.data.map(::mapAiSettings)

    override suspend fun getAiSettings(): AiSettings = mapAiSettings(context.dataStore.data.first())

    override suspend fun saveAiSettings(settings: AiSettings) {
        context.dataStore.edit { prefs ->
            prefs[aiProviderKey] = settings.provider.name
            prefs[openAiKey] = settings.openAiApiKey
            prefs[geminiKey] = settings.geminiApiKey
            prefs[aiModelKey] = settings.model
        }
    }

    private fun mapAiSettings(prefs: Preferences): AiSettings {
        val provider = runCatching {
            AiProvider.valueOf(prefs[aiProviderKey] ?: AiProvider.OPENAI.name)
        }.getOrDefault(AiProvider.OPENAI)
        return AiSettings(
            provider = provider,
            openAiApiKey = prefs[openAiKey].orEmpty(),
            geminiApiKey = prefs[geminiKey].orEmpty(),
            model = prefs[aiModelKey].orEmpty().ifBlank {
                if (provider == AiProvider.OPENAI) "gpt-4.1-mini" else "gemini-1.5-flash"
            }
        )
    }
}
