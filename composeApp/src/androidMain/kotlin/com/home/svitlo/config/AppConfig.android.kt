package com.home.svitlo.config

import com.home.svitlo.BuildConfig

actual object AppConfig {
    actual val wifiSn: String = BuildConfig.SOLAX_WIFI_SN
    actual val tokenId: String = BuildConfig.SOLAX_TOKEN_ID
}

