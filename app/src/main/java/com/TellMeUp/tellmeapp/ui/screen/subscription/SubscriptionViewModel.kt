/**
 * @file: SubscriptionViewModel.kt
 * @description: ViewModel for subscription screen managing activation and status
 * @dependencies: Hilt, ActivateSubscriptionUseCase, GetSubscriptionStatusUseCase
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.screen.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.TellMeUp.tellmeapp.domain.model.Subscription
import com.TellMeUp.tellmeapp.domain.usecase.ActivateSubscriptionUseCase
import com.TellMeUp.tellmeapp.domain.usecase.GetSubscriptionStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val subscription: Subscription? = null,
    val isLoading: Boolean = false,
    val activationLink: String = "",
    val error: String? = null,
    val activationSuccess: Boolean = false
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val activateSubscriptionUseCase: ActivateSubscriptionUseCase,
    private val getSubscriptionStatusUseCase: GetSubscriptionStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getSubscriptionStatusUseCase().collect { subscription ->
                _uiState.value = _uiState.value.copy(
                    subscription = subscription,
                    activationSuccess = false
                )
            }
        }
    }

    fun onLinkChanged(link: String) {
        _uiState.value = _uiState.value.copy(
            activationLink = link,
            error = null
        )
    }

    fun activate() {
        val link = _uiState.value.activationLink.trim()
        if (link.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Введите ссылку активации")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = activateSubscriptionUseCase(link)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                activationSuccess = result.isSuccess,
                error = if (result.isFailure) result.exceptionOrNull()?.message else null,
                activationLink = if (result.isSuccess) "" else _uiState.value.activationLink
            )
        }
    }
}
