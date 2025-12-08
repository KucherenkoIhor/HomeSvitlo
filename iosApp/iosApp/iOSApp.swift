import SwiftUI
import BackgroundTasks
import WidgetKit

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase
    
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
        .onChange(of: scenePhase) { oldPhase, newPhase in
            if newPhase == .active {
                // Refresh data and widget when app becomes active
                print("📱 App became active, refreshing...")
                BackgroundTaskManager.shared.fetchInverterStatus()
            }
        }
    }
}
