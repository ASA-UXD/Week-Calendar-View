package com.example.calendarwidget.data

import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.CalendarContract
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.example.calendarwidget.widget.CalendarWeekWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CalendarObserverService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var calendarObserver: ContentObserver? = null

    override fun onCreate() {
        super.onCreate()
        registerCalendarObserver()
    }

    private fun registerCalendarObserver() {
        calendarObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                serviceScope.launch {
                    val manager = GlanceAppWidgetManager(applicationContext)
                    val glanceIds = manager.getGlanceIds(CalendarWeekWidget::class.java)
                    val widget = CalendarWeekWidget()
                    glanceIds.forEach { id ->
                        widget.update(applicationContext, id)
                    }
                }
            }
        }

        contentResolver.registerContentObserver(
            CalendarContract.Events.CONTENT_URI,
            true,
            calendarObserver!!
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        calendarObserver?.let { contentResolver.unregisterContentObserver(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun startObserving(context: Context) {
            context.startService(Intent(context, CalendarObserverService::class.java))
        }

        fun stopObserving(context: Context) {
            context.stopService(Intent(context, CalendarObserverService::class.java))
        }
    }
}