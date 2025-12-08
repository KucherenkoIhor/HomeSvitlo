package com.home.svitlo.ios

import com.home.svitlo.config.AppConfig
import com.home.svitlo.di.NetworkModule
import com.home.svitlo.domain.model.RateLimitException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Result class for inverter status fetch operation.
 * Exposed to Swift via Kotlin/Native.
 */
data class InverterStatusResult(
    val statusCode: String,
    val statusDescription: String,
    val batteryCharge: Double,
    val isSuccess: Boolean,
    val errorMessage: String?
)

/**
 * Service for fetching inverter status from the API.
 * This is exposed to Swift for use in background tasks.
 */
class InverterService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val getInverterDataUseCase = NetworkModule.getInverterDataUseCase
    
    /**
     * Fetch the current inverter status.
     * @param completion Callback with the result
     */
    fun fetchStatus(completion: (InverterStatusResult) -> Unit) {
        scope.launch {
            val result = getInverterDataUseCase(
                wifiSn = AppConfig.wifiSn,
                tokenId = AppConfig.tokenId
            )
            
            result.onSuccess { data ->
                completion(
                    InverterStatusResult(
                        statusCode = data.status.code,
                        statusDescription = data.status.description,
                        batteryCharge = data.batteryCharge ?: 0.0,
                        isSuccess = true,
                        errorMessage = null
                    )
                )
            }.onFailure { error ->
                completion(
                    InverterStatusResult(
                        statusCode = "",
                        statusDescription = "",
                        batteryCharge = 0.0,
                        isSuccess = false,
                        errorMessage = when (error) {
                            is RateLimitException -> "Rate limit exceeded"
                            else -> error.message ?: "Unknown error"
                        }
                    )
                )
            }
        }
    }
    
    companion object {
        val shared = InverterService()
    }
}

