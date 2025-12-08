package com.home.svitlo.data.repository

import com.home.svitlo.data.network.SolaxCloudApi
import com.home.svitlo.domain.model.InverterStatus
import com.home.svitlo.domain.model.RateLimitException
import com.home.svitlo.domain.repository.InverterRepository

class InverterRepositoryImpl(
    private val api: SolaxCloudApi
) : InverterRepository {

    override suspend fun getInverterStatus(wifiSn: String, tokenId: String): Result<InverterStatus> {
        return try {
            val response = api.getRealtimeInfo(wifiSn = wifiSn, tokenId = tokenId)
            
            when {
                response.isRateLimited -> {
                    Result.failure(RateLimitException())
                }
                response.success && response.result != null -> {
                    val status = InverterStatus.fromCode(response.result.inverterStatus)
                    Result.success(status)
                }
                else -> {
                    Result.failure(
                        Exception(response.exception ?: "Unknown error occurred")
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

