import WidgetKit
import SwiftUI

struct InverterEntry: TimelineEntry {
    let date: Date
    let statusEmoji: String
    let statusText: String
    let batteryCharge: Int
    let backgroundColor: Color
    let lastUpdated: Date?
}

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> InverterEntry {
        InverterEntry(
            date: Date(),
            statusEmoji: "⏳",
            statusText: "Завантаження...",
            batteryCharge: 0,
            backgroundColor: .purple,
            lastUpdated: nil
        )
    }

    func getSnapshot(in context: Context, completion: @escaping (InverterEntry) -> ()) {
        completion(createEntry(for: Date()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<InverterEntry>) -> ()) {
        let currentDate = Date()
        var entries: [InverterEntry] = []
        
        // Log widget refresh to shared storage
        logWidgetRefresh()
        
        // Create entries for the next 60 minutes (one per minute)
        // This makes the "time ago" label update every minute
        for minuteOffset in 0..<60 {
            let entryDate = Calendar.current.date(byAdding: .minute, value: minuteOffset, to: currentDate)!
            let entry = createEntry(for: entryDate)
            entries.append(entry)
        }
        
        // Request refresh after 15 minutes (iOS may delay this)
        let nextUpdate = Calendar.current.date(byAdding: .minute, value: 15, to: currentDate)!
        completion(Timeline(entries: entries, policy: .after(nextUpdate)))
    }
    
    private func logWidgetRefresh() {
        guard let userDefaults = UserDefaults(suiteName: "group.com.home.svitlo") else { return }
        
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm:ss"
        let timeStr = formatter.string(from: Date())
        
        var logs = userDefaults.stringArray(forKey: "debug_logs") ?? []
        logs.append("[\(timeStr)] 📱 WIDGET getTimeline called")
        
        // Log what data widget is reading
        if let data = userDefaults.data(forKey: "inverter_status"),
           let status = try? JSONDecoder().decode(StoredStatus.self, from: data) {
            let updatedTimeStr = formatter.string(from: status.lastUpdated)
            logs.append("[\(timeStr)] 📱 WIDGET reads: bat=\(Int(status.batteryCharge))%, updated=\(updatedTimeStr)")
        } else {
            logs.append("[\(timeStr)] 📱 WIDGET: no data found!")
        }
        
        // Keep last 50 logs
        if logs.count > 50 {
            logs = Array(logs.suffix(50))
        }
        
        userDefaults.set(logs, forKey: "debug_logs")
        userDefaults.synchronize()
    }
    
    private func createEntry(for date: Date) -> InverterEntry {
        guard let userDefaults = UserDefaults(suiteName: "group.com.home.svitlo"),
              let data = userDefaults.data(forKey: "inverter_status"),
              let status = try? JSONDecoder().decode(StoredStatus.self, from: data) else {
            return InverterEntry(
                date: date,
                statusEmoji: "❓",
                statusText: "Немає даних",
                batteryCharge: 0,
                backgroundColor: .gray,
                lastUpdated: nil
            )
        }
        
        let (emoji, text, color) = getStatusDisplay(statusCode: status.statusCode)
        return InverterEntry(
            date: date,
            statusEmoji: emoji,
            statusText: text,
            batteryCharge: Int(status.batteryCharge),
            backgroundColor: color,
            lastUpdated: status.lastUpdated
        )
    }
    
    private func getStatusDisplay(statusCode: String) -> (String, String, Color) {
        switch statusCode {
        case "102": // NORMAL
            return ("☀️", "Світло є!", Color(red: 0.2, green: 0.7, blue: 0.3))
        case "107": // OFF_GRID
            return ("🔌", "Світла немає!", Color(red: 0.9, green: 0.2, blue: 0.2))
        default:
            return ("🔄", "Обробка...", Color(red: 0.5, green: 0.3, blue: 1.0))
        }
    }
}

struct StoredStatus: Codable {
    let statusCode: String
    let batteryCharge: Double
    let lastUpdated: Date
}

struct InverterWidgetEntryView: View {
    var entry: Provider.Entry
    @Environment(\.widgetFamily) var family
    
    var body: some View {
        VStack(spacing: 6) {
            Text(entry.statusEmoji)
                .font(.system(size: family == .systemSmall ? 36 : 44))
            
            Text(entry.statusText)
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
            
            HStack(spacing: 4) {
                Text("🔋")
                    .font(.system(size: 16))
                Text("\(entry.batteryCharge)%")
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.white)
            }
            
            if let lastUpdated = entry.lastUpdated {
                Text(timeAgo(from: lastUpdated, relativeTo: entry.date))
                    .font(.caption2)
                    .foregroundColor(.white.opacity(0.7))
            }
        }
        .padding()
        .containerBackground(entry.backgroundColor, for: .widget)
    }
    
    private func timeAgo(from date: Date, relativeTo now: Date) -> String {
        let minutes = Int(now.timeIntervalSince(date) / 60)
        if minutes < 1 {
            return "щойно"
        } else if minutes < 60 {
            return "\(minutes) хв тому"
        } else {
            let hours = minutes / 60
            return "\(hours) год тому"
        }
    }
}

struct InverterWidget: Widget {
    let kind: String = "InverterWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            InverterWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("Статус світла")
        .description("Показує статус електроенергії та заряд батареї")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

#Preview(as: .systemSmall) {
    InverterWidget()
} timeline: {
    InverterEntry(date: .now, statusEmoji: "☀️", statusText: "Світло є!", batteryCharge: 85, backgroundColor: Color(red: 0.2, green: 0.7, blue: 0.3), lastUpdated: Date())
    InverterEntry(date: .now, statusEmoji: "🔌", statusText: "Світла немає!", batteryCharge: 45, backgroundColor: Color(red: 0.9, green: 0.2, blue: 0.2), lastUpdated: Date().addingTimeInterval(-300))
}
