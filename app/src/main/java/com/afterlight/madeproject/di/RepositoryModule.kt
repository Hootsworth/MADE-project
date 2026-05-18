package com.afterlight.madeproject.di

import com.afterlight.madeproject.data.local.DataStoreSettingsRepository
import com.afterlight.madeproject.data.repository.FirebaseAuthRepository
import com.afterlight.madeproject.data.repository.FirebaseEventRepository
import com.afterlight.madeproject.data.repository.FirebaseUserRepository
import com.afterlight.madeproject.data.repository.RemoteAiAssistRepository
import com.afterlight.madeproject.domain.repository.AiAssistRepository
import com.afterlight.madeproject.domain.repository.AuthRepository
import com.afterlight.madeproject.domain.repository.EventRepository
import com.afterlight.madeproject.domain.repository.SettingsRepository
import com.afterlight.madeproject.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(repository: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(repository: FirebaseUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(repository: FirebaseEventRepository): EventRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(repository: DataStoreSettingsRepository): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAiAssistRepository(repository: RemoteAiAssistRepository): AiAssistRepository
}
