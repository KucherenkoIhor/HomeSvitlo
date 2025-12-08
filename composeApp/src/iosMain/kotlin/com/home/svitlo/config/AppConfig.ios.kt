package com.home.svitlo.config

actual object AppConfig {
    actual val wifiSn: String = SecretConfig.wifiSn
    actual val tokenId: String = SecretConfig.tokenId
}

