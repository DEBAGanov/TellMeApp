/**
 * @file: PreferencesStore.kt
 * @description: Local storage for subscription data and app settings via DataStore
 * @dependencies: DataStore Preferences
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.TellMeUp.tellmeapp.domain.model.Subscription
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tellmeapp_prefs")

@Singleton
class PreferencesStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACTIVATION_LINK = stringPreferencesKey("activation_link")
        private val KEY_SUBSCRIPTION_STATUS = stringPreferencesKey("subscription_status")
        private val KEY_EXPIRY_DATE = longPreferencesKey("expiry_date")
        private val KEY_TARIFF_TYPE = stringPreferencesKey("tariff_type")
        private val KEY_API_KEY = stringPreferencesKey("api_key")
    }

    val subscription: Flow<Subscription?> = context.dataStore.data.map { prefs ->
        val status = prefs[KEY_SUBSCRIPTION_STATUS]
        if (status == null) {
            null
        } else {
            Subscription(
                isActive = status == "active",
                expiryDate = prefs[KEY_EXPIRY_DATE],
                tariffType = prefs[KEY_TARIFF_TYPE]
            )
        }
    }

    val apiKey: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_API_KEY]
    }

    suspend fun saveSubscription(link: String, subscription: Subscription) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACTIVATION_LINK] = link
            prefs[KEY_SUBSCRIPTION_STATUS] = if (subscription.isActive) "active" else "inactive"
            subscription.expiryDate?.let { prefs[KEY_EXPIRY_DATE] = it }
            subscription.tariffType?.let { prefs[KEY_TARIFF_TYPE] = it }
        }
    }

    suspend fun clearSubscription() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACTIVATION_LINK)
            prefs.remove(KEY_SUBSCRIPTION_STATUS)
            prefs.remove(KEY_EXPIRY_DATE)
            prefs.remove(KEY_TARIFF_TYPE)
        }
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_API_KEY] = key
        }
    }
}
