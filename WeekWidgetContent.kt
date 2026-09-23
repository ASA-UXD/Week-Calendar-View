package com.example.calendarwidget.widget.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.calendarwidget.data.WeekSchedule

@Composable
fun WeekWidgetContent(
    weekSchedule: WeekSchedule,
    modifier: GlanceModifier = GlanceModifier
) {
    val openCalendarIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("content://com.android.calendar/time/${System.currentTimeMillis()}")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .cornerRadius(24.dp)
    ) {
        // --- 1. Month Header & Today Jump ---
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = weekSchedule.monthYearTitle,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.clickable(actionStartActivity(openCalendarIntent))
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            Row(
                modifier = GlanceModifier
                    .cornerRadius(12.dp)
                    .clickable(actionStartActivity(openCalendarIntent))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        // --- 2. 7-Day Columns Body ---
        if (weekSchedule.isEmptyWeek) {
            EmptyWeekView(onOpenCalendar = openCalendarIntent)
        } else {
            Row(
                modifier = GlanceModifier.fillMaxWidth().fillMaxHeight(),
                verticalAlignment = Alignment.Top
            ) {
                weekSchedule.days.forEachIndexed { index, dayData ->
                    DayColumnView(
                        dayData = dayData,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    if (index < weekSchedule.days.size - 1) {
                        Spacer(modifier = GlanceModifier.width(2.dp))
                    }
                }
            }
        }
    }
}