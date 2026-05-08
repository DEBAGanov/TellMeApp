/**
 * @file: ActivateSubscriptionUseCase.kt
 * @description: Use case for activating subscription via link
 * @dependencies: SubscriptionRepository
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.domain.usecase

import com.TellMeUp.tellmeapp.domain.model.Subscription
import com.TellMeUp.tellmeapp.domain.repository.SubscriptionRepository
import javax.inject.Inject

class ActivateSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(link: String): Result<Subscription> {
        return subscriptionRepository.activate(link)
    }
}
