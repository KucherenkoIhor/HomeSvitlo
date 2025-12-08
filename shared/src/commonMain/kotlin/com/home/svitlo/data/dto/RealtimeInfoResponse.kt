package com.home.svitlo.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RealtimeInfoResponse(
    val success: Boolean,
    val exception: String?,
    val result: RealtimeInfoResult?,
    val code: Int
)

@Serializable
data class RealtimeInfoResult(
    val inverterSN: String?,
    val sn: String?,
    val acpower: Double?,
    val yieldtoday: Double?,
    val yieldtotal: Double?,
    val feedinpower: Double?,
    val feedinenergy: Double?,
    val consumeenergy: Double?,
    val feedinpowerM2: Double?,
    val soc: Double?,
    val peps1: Double?,
    val peps2: Double?,
    val peps3: Double?,
    val inverterType: String?,
    val inverterStatus: String?,
    val uploadTime: String?,
    val batPower: Double?,
    val powerdc1: Double?,
    val powerdc2: Double?,
    val powerdc3: Double?,
    val powerdc4: Double?,
    val batStatus: String?,
    val utcDateTime: String?
)

