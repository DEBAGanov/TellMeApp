/**
 * @file: Subscription.kt
 * @description: Domain model for subscription data
 * @dependencies: None
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.domain.model

data class Subscription(
    val isActive: Boolean,
    val expiryDate: Long?,
    val tariffType: String?
)
