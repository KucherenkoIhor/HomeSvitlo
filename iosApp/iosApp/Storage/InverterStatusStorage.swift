import Foundation

struct StoredInverterStatus: Codable {
    let statusCode: String
    let batteryCharge: Double
    let lastUpdated: Date
}

class InverterStatusStorage {
    static let shared = InverterStatusStorage()
    
    // Use App Group for sharing data with widget
    private let suiteName = "group.com.home.svitlo"
    private let statusKey = "inverter_status"
    
    private var userDefaults: UserDefaults? {
        UserDefaults(suiteName: suiteName)
    }
    
    private init() {}
    
    func saveStatus(statusCode: String, batteryCharge: Double) {
        let status = StoredInverterStatus(
            statusCode: statusCode,
            batteryCharge: batteryCharge,
            lastUpdated: Date()
        )
        
        if let encoded = try? JSONEncoder().encode(status) {
            userDefaults?.set(encoded, forKey: statusKey)
        }
    }
    
    func getStatus() -> StoredInverterStatus? {
        guard let data = userDefaults?.data(forKey: statusKey),
              let status = try? JSONDecoder().decode(StoredInverterStatus.self, from: data) else {
            return nil
        }
        return status
    }
    
    func getPreviousStatusCode() -> String? {
        return getStatus()?.statusCode
    }
}

