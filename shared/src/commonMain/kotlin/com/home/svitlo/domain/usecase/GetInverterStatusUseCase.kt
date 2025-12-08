package com.home.svitlo.domain.usecase

import com.home.svitlo.domain.model.InverterStatus
import com.home.svitlo.domain.repository.InverterRepository

class GetInverterStatusUseCase(
    private val repository: InverterRepository
) {
    suspend operator fun invoke(wifiSn: String, tokenId: String): Result<InverterStatus> {
        return repository.getInverterStatus(wifiSn = wifiSn, tokenId = tokenId)
    }
}

