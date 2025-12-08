import Foundation
import BackgroundTasks
import ComposeApp
import WidgetKit

class BackgroundTaskManager {
    static let shared = BackgroundTaskManager()
    static let taskIdentifier = "com.home.svitlo.refresh"
    
    private let storage = InverterStatusStorage.shared
    private let notificationHelper = NotificationHelper.shared
    private let inverterService = InverterService.shared
    
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
            print("Background task scheduled")
        } catch {
            print("Failed to schedule background task: \(error.localizedDescription)")
        }
    }
    
    private func handleBackgroundTask(task: BGAppRefreshTask) {
        // Schedule the next background task
        scheduleBackgroundTask()
        
        task.expirationHandler = {
            task.setTaskCompleted(success: false)
        }
        
        fetchInverterStatus { success in
            task.setTaskCompleted(success: success)
        }
    }
    
    func fetchInverterStatus(completion: ((Bool) -> Void)? = nil) {
        let previousStatusCode = storage.getPreviousStatusCode()
        
        inverterService.fetchStatus { result in
            if result.isSuccess {
                // Save to storage
                self.storage.saveStatus(
                    statusCode: result.statusCode,
                    batteryCharge: result.batteryCharge
                )
                
                // Check if status changed and show notification
                if let previous = previousStatusCode, previous != result.statusCode {
                    self.notificationHelper.showStatusChangeNotification(
                        statusCode: result.statusCode,
                        batteryCharge: result.batteryCharge
                    )
                }
                
                // Update widget
                WidgetCenter.shared.reloadAllTimelines()
                
                completion?(true)
            } else {
                print("Failed to fetch status: \(result.errorMessage ?? "Unknown error")")
                completion?(false)
            }
        }
    }
}

