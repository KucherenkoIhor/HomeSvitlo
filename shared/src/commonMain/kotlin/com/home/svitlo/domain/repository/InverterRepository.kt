package com.home.svitlo.domain.repository

import com.home.svitlo.domain.model.InverterStatus

interface InverterRepository {
    suspend fun getInverterStatus(wifiSn: String, tokenId: String): Result<InverterStatus>
}

