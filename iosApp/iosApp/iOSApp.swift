import SwiftUI
import BackgroundTasks

@main
struct iOSApp: App {
    
    init() {
        // Register background task
        BackgroundTaskManager.shared.registerBackgroundTask()
        
        // Request notification permission
        NotificationHelper.shared.requestPermission()
        
        // Schedule initial background task
        BackgroundTaskManager.shared.scheduleBackgroundTask()
        
        // Fetch initial status
        BackgroundTaskManager.shared.fetchInverterStatus()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
