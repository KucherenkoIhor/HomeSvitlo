import WidgetKit
import SwiftUI

struct InverterEntry: TimelineEntry {
    let date: Date
    let statusEmoji: String
    let statusText: String
    let batteryCharge: Int
    let backgroundColor: Color
}

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> InverterEntry {
        InverterEntry(
            date: Date(),
            statusEmoji: "⏳",
            statusText: "Завантаження...",
            batteryCharge: 0,
            backgroundColor: .purple
        )
    }

    func getSnapshot(in context: Context, completion: @escaping (InverterEntry) -> ()) {
        completion(createEntry())
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<InverterEntry>) -> ()) {
        let entry = createEntry()
        let nextUpdate = Calendar.current.date(byAdding: .minute, value: 15, to: Date())!
        completion(Timeline(entries: [entry], policy: .after(nextUpdate)))
    }
    
    private func createEntry() -> InverterEntry {
        guard let userDefaults = UserDefaults(suiteName: "group.com.home.svitlo"),
              let data = userDefaults.data(forKey: "inverter_status"),
              let status = try? JSONDecoder().decode(StoredStatus.self, from: data) else {
            return InverterEntry(
                date: Date(),
                statusEmoji: "❓",
                statusText: "Немає даних",
                batteryCharge: 0,
                backgroundColor: .gray
            )
        }
        
        let (emoji, text, color) = getStatusDisplay(statusCode: status.statusCode)
        return InverterEntry(
            date: Date(),
            statusEmoji: emoji,
            statusText: text,
            batteryCharge: Int(status.batteryCharge),
            backgroundColor: color
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
    
    var body: some View {
        ZStack {
            entry.backgroundColor
            
            VStack(spacing: 8) {
                Text(entry.statusEmoji)
                    .font(.system(size: 40))
                
                Text(entry.statusText)
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                
                HStack(spacing: 4) {
                    Text("🔋")
                        .font(.system(size: 18))
                    Text("\(entry.batteryCharge)%")
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundColor(.white)
                }
            }
            .padding()
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
    InverterEntry(date: .now, statusEmoji: "☀️", statusText: "Світло є!", batteryCharge: 85, backgroundColor: Color(red: 0.2, green: 0.7, blue: 0.3))
    InverterEntry(date: .now, statusEmoji: "🔌", statusText: "Світла немає!", batteryCharge: 45, backgroundColor: Color(red: 0.9, green: 0.2, blue: 0.2))
}
