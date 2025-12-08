package com.home.svitlo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.home.svitlo.MainActivity
import com.home.svitlo.data.InverterStatusStorage
import com.home.svitlo.domain.model.InverterStatus

class InverterWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load data from storage
        val storage = InverterStatusStorage(context)
        val storedStatus = storage.getStatus()
        
        // Update widget state
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
            prefs.toMutablePreferences().apply {
                this[STATUS_CODE_KEY] = storedStatus?.statusCode ?: ""
                this[BATTERY_KEY] = storedStatus?.batteryCharge ?: 0.0
            }
        }

        provideContent {
            GlanceTheme {
                InverterWidgetContent()
            }
        }
    }

    @Composable
    private fun InverterWidgetContent() {
        val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
        val statusCode = prefs[STATUS_CODE_KEY] ?: ""
        val batteryCharge = prefs[BATTERY_KEY] ?: 0.0
        
        val status = InverterStatus.fromCode(statusCode)
        
        val (backgroundColor, emoji, statusText) = when (status) {
            InverterStatus.NORMAL -> Triple(
                ColorProvider(android.graphics.Color.parseColor("#6EC6FF")),
                "☀️",
                "Світло є!"
            )
            InverterStatus.OFF_GRID -> Triple(
                ColorProvider(android.graphics.Color.parseColor("#E53935")),
                "🔌",
                "Світла немає!"
            )
            else -> Triple(
                ColorProvider(android.graphics.Color.parseColor("#7C4DFF")),
                "🔄",
                status.description.ifEmpty { "Завантаження..." }
            )
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .clickable(actionStartActivity<MainActivity>())
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                Text(
                    text = emoji,
                    style = TextStyle(fontSize = 40.sp)
                )
                
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                Text(
                    text = statusText,
                    style = TextStyle(
                        color = ColorProvider(android.graphics.Color.WHITE),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔋",
                        style = TextStyle(fontSize = 18.sp)
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Text(
                        text = "${batteryCharge.toInt()}%",
                        style = TextStyle(
                            color = ColorProvider(android.graphics.Color.WHITE),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }

    companion object {
        val STATUS_CODE_KEY = stringPreferencesKey("widget_status_code")
        val BATTERY_KEY = doublePreferencesKey("widget_battery")
    }
}

class InverterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = InverterWidget()
}

