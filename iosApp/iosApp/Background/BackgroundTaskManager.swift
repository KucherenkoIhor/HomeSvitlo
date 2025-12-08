import Foundation
import BackgroundTasks
import WidgetKit

class BackgroundTaskManager {
    static let shared = BackgroundTaskManager()
    static let taskIdentifier = "com.home.svitlo.refresh"
    
    private let storage = InverterStatusStorage.shared
    private let notificationHelper = NotificationHelper.shared
    private let apiClient = SolaxApiClient.shared
    
    private init() {}
    
    func registerBackgroundTask() {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: Self.taskIdentifier,
            using: nil
        ) { task in
            self.handleBackgroundTask(task: task as! BGAppRefreshTask)
        }
    }
    
    func scheduleBackgroundTask() {
        let request = BGAppRefreshTaskRequest(identifier: Self.taskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60) // 15 minutes
        
        do {
            try BGTaskScheduler.shared.submit(request)
            print("✅ Background task scheduled for 15 minutes")
        } catch {
            print("❌ Failed to schedule background task: \(error.localizedDescription)")
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
