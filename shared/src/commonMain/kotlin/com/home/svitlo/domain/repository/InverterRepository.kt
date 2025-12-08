package com.home.svitlo.domain.repository

import com.home.svitlo.domain.model.InverterData

interface InverterRepository {
    suspend fun getInverterData(wifiSn: String, tokenId: String): Result<InverterData>
}

