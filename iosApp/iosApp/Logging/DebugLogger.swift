import Foundation

class DebugLogger {
    static let shared = DebugLogger()
    
    private let suiteName = "group.com.home.svitlo"
    private let logKey = "debug_logs"
    private let maxLogs = 50
    
    private var userDefaults: UserDefaults? {
        UserDefaults(suiteName: suiteName)
    }
    
    private init() {}
    
    func log(_ message: String) {
        let timestamp = DateFormatter.localizedString(from: Date(), dateStyle: .none, timeStyle: .medium)
        let logEntry = "[\(timestamp)] \(message)"
        
        print(logEntry)
        
        // Also save to UserDefaults so widget can read it
        var logs = getLogs()
        logs.append(logEntry)
        
        // Keep only last N logs
        if logs.count > maxLogs {
            logs = Array(logs.suffix(maxLogs))
        }
        
        userDefaults?.set(logs, forKey: logKey)
        userDefaults?.synchronize()
    }
    
    func getLogs() -> [String] {
        return userDefaults?.stringArray(forKey: logKey) ?? []
    }
    
    func clearLogs() {
        userDefaults?.removeObject(forKey: logKey)
        userDefaults?.synchronize()
    }
    
    func getLogsText() -> String {
        return getLogs().joined(separator: "\n")
    }
}

