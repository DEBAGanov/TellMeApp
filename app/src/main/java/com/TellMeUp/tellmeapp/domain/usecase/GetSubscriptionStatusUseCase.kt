/**
 * @file: GetSubscriptionStatusUseCase.kt
 * @description: Use case for observing subscription status
 * @dependencies: SubscriptionRepository
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.domain.usecase

import com.TellMeUp.tellmeapp.domain.model.Subscription
import com.TellMeUp.tellmeapp.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSubscriptionStatusUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(): Flow<Subscription?> {
        return subscriptionRepository.subscription
    }
}
