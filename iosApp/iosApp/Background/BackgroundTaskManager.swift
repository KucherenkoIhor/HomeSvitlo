import Foundation
import BackgroundTasks
import WidgetKit
import ComposeApp

class BackgroundTaskManager {
    static let shared = BackgroundTaskManager()
    static let refreshTaskIdentifier = "com.home.svitlo.refresh"
    static let processingTaskIdentifier = "com.home.svitlo.processing"
    
    private let storage = InverterStatusStorage.shared
    private let notificationHelper = NotificationHelper.shared
    private let inverterService = InverterServiceProvider.shared.service
    
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
        } catch {
            print("Failed to schedule refresh task: \(error)")
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
        } catch {
            print("Failed to schedule processing task: \(error)")
        }
    }
    
    private func handleRefreshTask(task: BGAppRefreshTask) {
        scheduleRefreshTask()
        
        task.expirationHandler = {
            task.setTaskCompleted(success: false)
        }
        
        fetchInverterStatus { success in
            task.setTaskCompleted(success: success)
        }
    }
    
    private func handleProcessingTask(task: BGProcessingTask) {
        scheduleProcessingTask()
        
        task.expirationHandler = {
            task.setTaskCompleted(success: false)
        }
        
        fetchInverterStatus { success in
            task.setTaskCompleted(success: success)
        }
    }
    
    func fetchInverterStatus(completion: ((Bool) -> Void)? = nil) {
        let previousStatusCode = storage.getPreviousStatusCode()
        
        inverterService.fetchStatus { [weak self] result in
            guard let self = self else { return }
            
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
                
                // Force sync and reload widget
                UserDefaults(suiteName: "group.com.home.svitlo")?.synchronize()
                
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    WidgetCenter.shared.reloadTimelines(ofKind: "InverterWidget")
                    WidgetCenter.shared.reloadAllTimelines()
                }
                
                completion?(true)
            } else {
                completion?(false)
            }
        }
    }
}
