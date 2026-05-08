/**
 * @file: SubscriptionRepositoryImpl.kt
 * @description: Implementation of SubscriptionRepository using PreferencesStore
 * @dependencies: PreferencesStore, SubscriptionRepository
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.data.repository

import com.TellMeUp.tellmeapp.data.local.PreferencesStore
import com.TellMeUp.tellmeapp.domain.model.Subscription
import com.TellMeUp.tellmeapp.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val preferencesStore: PreferencesStore
) : SubscriptionRepository {

    override val subscription: Flow<Subscription?> = preferencesStore.subscription

    override suspend fun activate(link: String): Result<Subscription> {
        return try {
            if (link.isBlank()) {
                return Result.failure(IllegalArgumentException("Link is empty"))
            }

            // Mock activation: will be replaced with real API call later
            val mockSubscription = Subscription(
                isActive = true,
                expiryDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
                tariffType = "Pro"
            )

            preferencesStore.saveSubscription(link, mockSubscription)
            Result.success(mockSubscription)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deactivate() {
        preferencesStore.clearSubscription()
    }
}
