/**
 * @file: SubscriptionRepository.kt
 * @description: Domain interface for subscription management
 * @dependencies: Subscription model
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.domain.repository

import com.TellMeUp.tellmeapp.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    val subscription: Flow<Subscription?>
    suspend fun activate(link: String): Result<Subscription>
    suspend fun deactivate()
}
