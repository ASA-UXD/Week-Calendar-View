package com.example.calendarwidget.widget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.example.calendarwidget.data.CalendarRepository
import com.example.calendarwidget.data.WeekSchedule
import com.example.calendarwidget.widget.ui.PermissionPromptView
import com.example.calendarwidget.widget.ui.WeekWidgetContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CalendarWeekWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(250.dp, 120.dp), // 4x2
            DpSize(320.dp, 200.dp), // 4x3 standard
            DpSize(400.dp, 280.dp)  // 5x4 / foldables
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        val repository = CalendarRepository(context)
        val weekSchedule: WeekSchedule? = if (hasPermission) {
            withContext(Dispatchers.IO) {
                repository.getCurrentWeekSchedule()
            }
        } else {
            null
        }

        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .padding(8.dp)
                ) {
                    if (!hasPermission) {
                        PermissionPromptView()
                    } else if (weekSchedule != null) {
                        WeekWidgetContent(weekSchedule = weekSchedule)
                    }
                }
            }
        }
    }
}