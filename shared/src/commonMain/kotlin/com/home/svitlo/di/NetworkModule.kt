package com.home.svitlo.di

import com.home.svitlo.data.network.SolaxCloudApi
import com.home.svitlo.data.network.createHttpClient
import com.home.svitlo.data.repository.InverterRepositoryImpl
import com.home.svitlo.domain.repository.InverterRepository
import com.home.svitlo.domain.usecase.GetInverterDataUseCase

/**
 * Simple dependency provider for network and domain layer components.
 * In a larger project, consider using a DI framework like Koin or Kodein.
 */
object NetworkModule {
    
    private val httpClient by lazy { createHttpClient() }
    
    private val solaxCloudApi by lazy { SolaxCloudApi(httpClient) }
    
    val inverterRepository: InverterRepository by lazy {
        InverterRepositoryImpl(solaxCloudApi)
    }
    
    val getInverterDataUseCase by lazy {
        GetInverterDataUseCase(inverterRepository)
    }
}

