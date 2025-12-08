package com.home.svitlo.data.network

import com.home.svitlo.data.dto.RealtimeInfoRequest
import com.home.svitlo.data.dto.RealtimeInfoResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SolaxCloudApi(
    private val httpClient: HttpClient
) {
    suspend fun getRealtimeInfo(
        wifiSn: String,
        tokenId: String
    ): RealtimeInfoResponse {
        return httpClient.post(BASE_URL + REALTIME_INFO_ENDPOINT) {
            contentType(ContentType.Application.Json)
            header(HEADER_TOKEN_ID, tokenId)
            setBody(RealtimeInfoRequest(wifiSn = wifiSn))
        }.body()
    }

    companion object {
        private const val BASE_URL = "https://global.solaxcloud.com/api/v2"
        private const val REALTIME_INFO_ENDPOINT = "/dataAccess/realtimeInfo/get"
        private const val HEADER_TOKEN_ID = "tokenId"
    }
}

