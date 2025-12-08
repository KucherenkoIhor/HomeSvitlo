package com.home.svitlo.data.repository

import com.home.svitlo.data.network.SolaxCloudApi
import com.home.svitlo.domain.model.InverterData
import com.home.svitlo.domain.model.InverterStatus
import com.home.svitlo.domain.model.RateLimitException
import com.home.svitlo.domain.repository.InverterRepository

class InverterRepositoryImpl(
    private val api: SolaxCloudApi
) : InverterRepository {

    override suspend fun getInverterData(wifiSn: String, tokenId: String): Result<InverterData> {
        return try {
            val response = api.getRealtimeInfo(wifiSn = wifiSn, tokenId = tokenId)
            
            when {
                response.isRateLimited -> {
                    Result.failure(RateLimitException())
                }
                response.success && response.result != null -> {
                    val data = InverterData(
                        status = InverterStatus.fromCode(response.result.inverterStatus),
                        acPower = response.result.acpower,
                        batteryCharge = response.result.soc
                    )
                    Result.success(data)
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

