package com.home.svitlo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform