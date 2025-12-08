import Foundation
import UserNotifications

class NotificationHelper {
    static let shared = NotificationHelper()
    
    private init() {}
    
    func requestPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, error in
            if granted {
                print("Notification permission granted")
            } else if let error = error {
                print("Notification permission error: \(error.localizedDescription)")
            }
        }
    }
    
    func showStatusChangeNotification(statusCode: String, batteryCharge: Double) {
        let content = UNMutableNotificationContent()
        
        switch statusCode {
        case "102": // NORMAL
            content.title = "☀️ Світло є!"
            content.body = "Електроенергія відновлена. Батарея: \(Int(batteryCharge))%"
        case "107": // OFF_GRID
            content.title = "🔌 Світла немає!"
            content.body = "Працює автономний режим. Батарея: \(Int(batteryCharge))%"
        default:
            content.title = "🔄 Статус змінився"
            content.body = "Новий статус. Батарея: \(Int(batteryCharge))%"
        }
        
        content.sound = .default
        
        let request = UNNotificationRequest(
            identifier: UUID().uuidString,
            content: content,
            trigger: nil // Deliver immediately
        )
        
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("Failed to show notification: \(error.localizedDescription)")
            }
        }
    }
}

