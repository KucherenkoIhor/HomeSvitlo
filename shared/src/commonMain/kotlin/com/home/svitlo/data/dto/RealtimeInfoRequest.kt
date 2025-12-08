package com.home.svitlo.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeInfoRequest(
    val wifiSn: String
)

