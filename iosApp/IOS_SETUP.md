# iOS Widget & Background Tasks Setup

## Required Xcode Configuration

### 1. Add App Group Capability (Required for Widget)

1. Open `iosApp.xcodeproj` in Xcode
2. Select the **iosApp** target
3. Go to **Signing & Capabilities** tab
4. Click **+ Capability**
5. Add **App Groups**
6. Add a new group: `group.com.home.svitlo`

### 2. Add Background Modes Capability

1. Still in **Signing & Capabilities**
2. Click **+ Capability**
3. Add **Background Modes**
4. Check:
   - ☑️ Background fetch
   - ☑️ Background processing

### 3. Add Widget Extension Target

1. In Xcode, go to **File → New → Target**
2. Search for **Widget Extension**
3. Click **Next**
4. Configure:
   - Product Name: `InverterWidget`
   - Bundle Identifier: `com.home.svitlo.InverterWidget`
   - **Uncheck** "Include Configuration Intent" (we use static configuration)
5. Click **Finish**
6. When prompted, **DO NOT activate** the new scheme (click Cancel)
7. Delete the auto-generated files in the new target
8. Add these files from `iosApp/InverterWidget/`:
   - `InverterWidget.swift`
   - `InverterWidgetBundle.swift`
   - `Info.plist`

### 4. Add App Group to Widget Extension

1. Select the **InverterWidget** target
2. Go to **Signing & Capabilities**
3. Add **App Groups** capability
4. Add the same group: `group.com.home.svitlo`

### 5. Embed Widget Extension in Main App

The widget extension should be automatically embedded. Verify:
1. Select the **iosApp** target
2. Go to **General** tab
3. Scroll to **Frameworks, Libraries, and Embedded Content**
4. Verify `InverterWidgetExtension.appex` is listed

## Project Structure After Setup

```
iosApp/
├── iosApp/
│   ├── Background/
│   │   └── BackgroundTaskManager.swift
│   ├── Notifications/
│   │   └── NotificationHelper.swift
│   ├── Storage/
│   │   └── InverterStatusStorage.swift
│   ├── ContentView.swift
│   ├── iOSApp.swift
│   └── Info.plist
└── InverterWidget/
    ├── InverterWidget.swift
    ├── InverterWidgetBundle.swift
    └── Info.plist
```

## Testing

### Test Background Tasks (Debug)
In Xcode debug console, use:
```
e -l objc -- (void)[[BGTaskScheduler sharedScheduler] _simulateLaunchForTaskWithIdentifier:@"com.home.svitlo.refresh"]
```

### Test Widget
1. Build and run the app
2. Long-press on home screen
3. Tap **+** to add widgets
4. Search for "Статус світла"
5. Add the widget

## Troubleshooting

### Widget shows "Немає даних"
- Ensure App Group is configured correctly on both targets
- Run the main app first to fetch initial data

### Background tasks not running
- Background tasks are managed by iOS and may be delayed
- Ensure the device is not in Low Power Mode
- iOS prioritizes based on usage patterns

### Notifications not appearing
- Check notification permissions in Settings
- Ensure the app was run at least once

