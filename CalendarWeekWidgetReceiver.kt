package com.example.calendarwidget.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.example.calendarwidget.data.CalendarObserverService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Bridges standard AppWidget broadcasts with Glance's lifecycle.
 * Manages ContentObserver lifecycle and triggers widget refreshes.
 */
class CalendarWeekWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CalendarWeekWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        CalendarObserverService.startObserving(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        CalendarObserverService.stopObserving(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH_WIDGET,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                MainScope().launch {
                    val manager = GlanceAppWidgetManager(context)
                    val glanceIds = manager.getGlanceIds(CalendarWeekWidget::class.java)
                    glanceIds.forEach { glanceId ->
                        glanceAppWidget.update(context, glanceId)
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.calendarwidget.ACTION_REFRESH_WIDGET"
    }
}