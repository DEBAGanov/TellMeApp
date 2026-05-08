/**
 * @file: RepositoryModule.kt
 * @description: Hilt module binding repository interfaces to implementations
 * @dependencies: SpeechRepository, SubscriptionRepository
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.di

import com.TellMeUp.tellmeapp.data.repository.SpeechRepositoryImpl
import com.TellMeUp.tellmeapp.data.repository.SubscriptionRepositoryImpl
import com.TellMeUp.tellmeapp.domain.repository.SpeechRepository
import com.TellMeUp.tellmeapp.domain.repository.SubscriptionRepository
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
    abstract fun bindSpeechRepository(
        impl: SpeechRepositoryImpl
    ): SpeechRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        impl: SubscriptionRepositoryImpl
    ): SubscriptionRepository
}
