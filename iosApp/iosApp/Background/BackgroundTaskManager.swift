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
    
    private init() {}
    
    func registerBackgroundTask() {
        // Register refresh task
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: Self.refreshTaskIdentifier,
            using: nil
        ) { task in
            self.handleRefreshTask(task: task as! BGAppRefreshTask)
        }
        
        // Register processing task (more reliable for periodic work)
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: Self.processingTaskIdentifier,
            using: nil
        ) { task in
            self.handleProcessingTask(task: task as! BGProcessingTask)
        }
        
        print("✅ Background tasks registered")
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
            print("✅ Refresh task scheduled for ~15 min")
        } catch {
            print("❌ Failed to schedule refresh task: \(error)")
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
            print("✅ Processing task scheduled for ~15 min")
        } catch {
            print("❌ Failed to schedule processing task: \(error)")
        }
    }
    
    private func handleRefreshTask(task: BGAppRefreshTask) {
        print("🔄 Refresh task started at \(Date())")
        scheduleRefreshTask()
        
        task.expirationHandler = {
            print("⚠️ Refresh task expired")
            task.setTaskCompleted(success: false)
        }
        
        fetchInverterStatus { success in
            print("✅ Refresh task completed: \(success)")
            task.setTaskCompleted(success: success)
        }
    }
    
    private func handleProcessingTask(task: BGProcessingTask) {
        print("🔄 Processing task started at \(Date())")
        scheduleProcessingTask()
        
        task.expirationHandler = {
            print("⚠️ Processing task expired")
            task.setTaskCompleted(success: false)
        }
        
        fetchInverterStatus { success in
            print("✅ Processing task completed: \(success)")
            task.setTaskCompleted(success: success)
        }
    }
    
    private func handleBackgroundTask(task: BGAppRefreshTask) {
        print("🔄 Background task started")
        
        // Schedule the next background task
        scheduleBackgroundTask()
        
        task.expirationHandler = {
            print("⚠️ Background task expired")
            task.setTaskCompleted(success: false)
        }
        
        fetchInverterStatus { success in
            print("✅ Background task completed: \(success)")
            task.setTaskCompleted(success: success)
        }
    }
    
    func fetchInverterStatus(completion: ((Bool) -> Void)? = nil) {
        let previousStatusCode = storage.getPreviousStatusCode()
        print("📡 Fetching inverter status... Previous: \(previousStatusCode ?? "none")")
        
        apiClient.fetchInverterStatus { [weak self] result in
            guard let self = self else { return }
            
            switch result {
            case .success(let data):
                print("✅ API Response: status=\(data.statusCode), battery=\(data.batteryCharge)")
                
                // Save to storage
                self.storage.saveStatus(
                    statusCode: data.statusCode,
                    batteryCharge: data.batteryCharge
                )
                
                // Check if status changed and show notification
                if let previous = previousStatusCode, previous != data.statusCode {
                    print("🔔 Status changed from \(previous) to \(data.statusCode), showing notification")
                    self.notificationHelper.showStatusChangeNotification(
                        statusCode: data.statusCode,
                        batteryCharge: data.batteryCharge
                    )
                }
                
                // Update widget with specific kind for better reliability
                WidgetCenter.shared.reloadTimelines(ofKind: "InverterWidget")
                print("🔄 Widget timeline reloaded for InverterWidget")
                
                // Also reload all as backup
                WidgetCenter.shared.reloadAllTimelines()
                
                completion?(true)
                
            case .failure(let error):
                print("❌ Failed to fetch status: \(error.localizedDescription)")
                completion?(false)
            }
        }
    }
}
