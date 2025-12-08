package com.home.svitlo

import com.home.svitlo.config.AppConfig
import com.home.svitlo.di.NetworkModule
import com.home.svitlo.domain.model.InverterData
import com.home.svitlo.domain.model.RateLimitException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: InverterData) : HomeUiState
    data object RateLimited : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val getInverterDataUseCase = NetworkModule.getInverterDataUseCase

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchData()
    }

    fun refresh() {
        _isRefreshing.value = true
        fetchData()
    }

    private fun fetchData() {
        scope.launch {
            val result = getInverterDataUseCase(
                wifiSn = AppConfig.wifiSn,
                tokenId = AppConfig.tokenId
            )

            result.onSuccess { data ->
                _uiState.value = HomeUiState.Success(data)
            }.onFailure { error ->
                _uiState.value = when (error) {
                    is RateLimitException -> HomeUiState.RateLimited
                    else -> HomeUiState.Error(error.message ?: "Невідома помилка")
                }
            }

            _isRefreshing.value = false
        }
    }
}

