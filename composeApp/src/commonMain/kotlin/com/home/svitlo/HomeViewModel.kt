package com.home.svitlo

import com.home.svitlo.di.NetworkModule
import com.home.svitlo.domain.model.InverterStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val status: InverterStatus) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val getInverterStatusUseCase = NetworkModule.getInverterStatusUseCase

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchStatus()
    }

    fun refresh() {
        _isRefreshing.value = true
        fetchStatus()
    }

    private fun fetchStatus() {
        scope.launch {
            val result = getInverterStatusUseCase(
                wifiSn = WIFI_SN,
                tokenId = TOKEN_ID
            )

            result.onSuccess { status ->
                _uiState.value = HomeUiState.Success(status)
            }.onFailure { error ->
                _uiState.value = HomeUiState.Error(
                    error.message ?: "Невідома помилка"
                )
            }

            _isRefreshing.value = false
        }
    }

    companion object {
        // TODO: Move these to a secure configuration
        private const val WIFI_SN = "SN6MBN9GUZ"
        private const val TOKEN_ID = "20251208145659068702972"
    }
}

