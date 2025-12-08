package com.home.svitlo.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.home.svitlo.config.AppConfig
import com.home.svitlo.data.InverterStatusStorage
import com.home.svitlo.di.NetworkModule
import com.home.svitlo.domain.model.InverterStatus
import com.home.svitlo.notification.NotificationHelper
import com.home.svitlo.widget.InverterWidget
import java.util.concurrent.TimeUnit

class InverterStatusWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val storage = InverterStatusStorage(applicationContext)
    private val notificationHelper = NotificationHelper(applicationContext)
    private val getInverterDataUseCase = NetworkModule.getInverterDataUseCase

    override suspend fun doWork(): Result {
        return try {
            // Get previous status before fetching new one
            val previousStatusCode = storage.getPreviousStatusCode()
            
            // Fetch new status
            val result = getInverterDataUseCase(
                wifiSn = AppConfig.wifiSn,
                tokenId = AppConfig.tokenId
            )
            
            result.onSuccess { data ->
                val newStatusCode = data.status.code
                val batteryCharge = data.batteryCharge ?: 0.0
                
                // Save to storage
                storage.saveStatus(newStatusCode, batteryCharge)
                
                // Check if status changed and show notification
                if (previousStatusCode != null && previousStatusCode != newStatusCode) {
                    notificationHelper.showStatusChangeNotification(data.status, batteryCharge)
                }
                
                // Update widget
                InverterWidget().updateAll(applicationContext)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "inverter_status_sync"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<InverterStatusWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun scheduleImmediate(context: Context) {
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<InverterStatusWorker>()
                .build()
            
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}

