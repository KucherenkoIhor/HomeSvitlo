package com.home.svitlo.domain.model

data class InverterData(
    val status: InverterStatus,
    val acPower: Double?,
    val batteryCharge: Double?
)

