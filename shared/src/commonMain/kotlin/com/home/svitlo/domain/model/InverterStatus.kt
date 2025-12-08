package com.home.svitlo.domain.model

enum class InverterStatus(val code: String, val description: String) {
    WAITING_FOR_OPERATION("100", "Waiting for operation"),
    SELF_TEST("101", "Self-test"),
    NORMAL("102", "Normal"),
    RECOVERABLE_FAULT("103", "Recoverable fault"),
    PERMANENT_FAULT("104", "Permanent fault"),
    FIRMWARE_UPGRADE("105", "Firmware upgrade"),
    EPS_DETECTION("106", "EPS detection"),
    OFF_GRID("107", "Off-grid"),
    SELF_TEST_MODE_ITALIAN("108", "Self-test mode (Italian safety regulations)"),
    SLEEP_MODE("109", "Sleep mode"),
    STANDBY_MODE("110", "Standby mode"),
    PV_WAKE_UP_BATTERY_MODE("111", "Photovoltaic wake-up battery mode"),
    GENERATOR_DETECTION_MODE("112", "Generator detection mode"),
    GENERATOR_MODE("113", "Generator mode"),
    FAST_SHUTDOWN_STANDBY_MODE("114", "Fast shutdown standby mode"),
    VPP_MODE("130", "VPP mode"),
    TOU_SELF_USE("131", "TOU-Self use"),
    TOU_CHARGING("132", "TOU-Charging"),
    TOU_DISCHARGING("133", "TOU-Discharging"),
    UNKNOWN("", "Unknown status");

    companion object {
        fun fromCode(code: String?): InverterStatus {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}

