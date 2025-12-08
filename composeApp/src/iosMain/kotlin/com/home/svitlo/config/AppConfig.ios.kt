package com.home.svitlo.config

actual object AppConfig {
    // For iOS, these values need to be set during build or from Info.plist
    // For now, using the same values - in production, use proper iOS configuration
    actual val wifiSn: String = "SN6MBN9GUZ"
    actual val tokenId: String = "20251208145659068702972"
}

