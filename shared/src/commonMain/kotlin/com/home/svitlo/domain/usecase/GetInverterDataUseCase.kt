package com.home.svitlo.domain.usecase

import com.home.svitlo.domain.model.InverterData
import com.home.svitlo.domain.repository.InverterRepository

class GetInverterDataUseCase(
    private val repository: InverterRepository
) {
    suspend operator fun invoke(wifiSn: String, tokenId: String): Result<InverterData> {
        return repository.getInverterData(wifiSn = wifiSn, tokenId = tokenId)
    }
}

