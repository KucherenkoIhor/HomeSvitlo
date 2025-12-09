import SwiftUI
import WidgetKit

struct DebugView: View {
    @State private var logs: [String] = []
    @State private var autoRefresh = true
    let timer = Timer.publish(every: 2, on: .main, in: .common).autoconnect()
    
    var body: some View {
        NavigationView {
            VStack {
                // Status
                HStack {
                    if let status = InverterStatusStorage.shared.getStatus() {
                        VStack(alignment: .leading) {
                            Text("Status: \(status.statusCode)")
                            Text("Battery: \(Int(status.batteryCharge))%")
                            Text("Updated: \(timeAgo(from: status.lastUpdated))")
                        }
                        .font(.caption)
                    } else {
                        Text("No data stored")
                    }
                    Spacer()
                }
                .padding()
                .background(Color.gray.opacity(0.1))
                .cornerRadius(8)
                .padding(.horizontal)
                
                // Logs
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 4) {
                        ForEach(logs.reversed(), id: \.self) { log in
                            Text(log)
                                .font(.system(size: 11, design: .monospaced))
                                .foregroundColor(.primary)
                        }
                    }
                    .padding()
                }
                
                // Actions
                HStack(spacing: 12) {
                    Button("Refresh") {
                        loadLogs()
                    }
                    
                    Button("Fetch Now") {
                        BackgroundTaskManager.shared.fetchInverterStatus()
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                            loadLogs()
                        }
                    }
                    
                    Button("Reload Widget") {
                        WidgetCenter.shared.reloadAllTimelines()
                        DebugLogger.shared.log("🔄 Manual widget reload")
                        loadLogs()
                    }
                    
                    Button("Clear") {
                        DebugLogger.shared.clearLogs()
                        loadLogs()
                    }
                }
                .padding()
            }
            .navigationTitle("Debug Logs")
            .onAppear { loadLogs() }
            .onReceive(timer) { _ in
                if autoRefresh {
                    loadLogs()
                }
            }
        }
    }
    
    private func loadLogs() {
        logs = DebugLogger.shared.getLogs()
    }
    
    private func timeAgo(from date: Date) -> String {
        let minutes = Int(-date.timeIntervalSinceNow / 60)
        if minutes < 1 { return "just now" }
        if minutes < 60 { return "\(minutes) min ago" }
        return "\(minutes / 60) hr ago"
    }
}

