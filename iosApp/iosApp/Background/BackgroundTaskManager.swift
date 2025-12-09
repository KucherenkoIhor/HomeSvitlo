import Foundation
import BackgroundTasks
import WidgetKit

class BackgroundTaskManager {
    static let shared = BackgroundTaskManager()
    static let refreshTaskIdentifier = "com.home.svitlo.refresh"
    static let processingTaskIdentifier = "com.home.svitlo.processing"
    
    private let storage = InverterStatusStorage.shared
    private let notificationHelper = NotificationHelper.shared
    private let apiClient = SolaxApiClient.shared
    private let logger = DebugLogger.shared
    
    private init() {}
    
    func registerBackgroundTask() {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: Self.refreshTaskIdentifier,
            using: nil
        ) { task in
            self.handleRefreshTask(task: task as! BGAppRefreshTask)
        }
        
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: Self.processingTaskIdentifier,
            using: nil
        ) { task in
            self.handleProcessingTask(task: task as! BGProcessingTask)
        }
        
        logger.log("✅ Background tasks registered")
    }
    
    func scheduleBackgroundTask() {
        scheduleRefreshTask()
        scheduleProcessingTask()
    }
    
    private func scheduleRefreshTask() {
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: Self.refreshTaskIdentifier)
        
        let request = BGAppRefreshTaskRequest(identifier: Self.refreshTaskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)
        
        do {
            try BGTaskScheduler.shared.submit(request)
            logger.log("📅 Refresh task scheduled")
        } catch {
            logger.log("❌ Refresh schedule failed: \(error.localizedDescription)")
        }
    }
    
    private func scheduleProcessingTask() {
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: Self.processingTaskIdentifier)
        
        let request = BGProcessingTaskRequest(identifier: Self.processingTaskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)
        request.requiresNetworkConnectivity = true
        request.requiresExternalPower = false
        
        do {
            try BGTaskScheduler.shared.submit(request)
            logger.log("📅 Processing task scheduled")
        } catch {
            logger.log("❌ Processing schedule failed: \(error.localizedDescription)")
        }
    }
    
    private func handleRefreshTask(task: BGAppRefreshTask) {
        logger.log("🔄 REFRESH TASK STARTED")
        scheduleRefreshTask()
        
        task.expirationHandler = {
            self.logger.log("⚠️ Refresh task expired")
            task.setTaskCompleted(success: false)
        }
        
        fetchInverterStatus { success in
            self.logger.log("✅ Refresh completed: \(success)")
            task.setTaskCompleted(success: success)
        }
    }
    
    private func handleProcessingTask(task: BGProcessingTask) {
        logger.log("🔄 PROCESSING TASK STARTED")
        scheduleProcessingTask()
        
        task.expirationHandler = {
            self.logger.log("⚠️ Processing task expired")
            task.setTaskCompleted(success: false)
        }
        
        fetchInverterStatus { success in
            self.logger.log("✅ Processing completed: \(success)")
            task.setTaskCompleted(success: success)
        }
    }
    
    func fetchInverterStatus(completion: ((Bool) -> Void)? = nil) {
        let previousStatusCode = storage.getPreviousStatusCode()
        logger.log("📡 Fetching... prev: \(previousStatusCode ?? "none")")
        
        apiClient.fetchInverterStatus { [weak self] result in
            guard let self = self else { return }
            
            switch result {
            case .success(let data):
                self.logger.log("✅ API: status=\(data.statusCode), bat=\(Int(data.batteryCharge))%")
                
                // Save to storage
                self.storage.saveStatus(
                    statusCode: data.statusCode,
                    batteryCharge: data.batteryCharge
                )
                
                // Check if status changed
                if let previous = previousStatusCode, previous != data.statusCode {
                    self.logger.log("🔔 Status changed! \(previous) → \(data.statusCode)")
                    self.notificationHelper.showStatusChangeNotification(
                        statusCode: data.statusCode,
                        batteryCharge: data.batteryCharge
                    )
                }
                
                // Force sync
                UserDefaults(suiteName: "group.com.home.svitlo")?.synchronize()
                self.logger.log("💾 Data saved & synced")
                
                // Reload widget after delay
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    WidgetCenter.shared.reloadTimelines(ofKind: "InverterWidget")
                    WidgetCenter.shared.reloadAllTimelines()
                    self.logger.log("🔄 Widget reload requested")
                }
                
                completion?(true)
                
            case .failure(let error):
                self.logger.log("❌ API error: \(error.localizedDescription)")
                completion?(false)
            }
        }
    }
}
