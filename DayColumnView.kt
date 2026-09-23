package com.example.calendarwidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.calendarwidget.data.DayScheduleData

@Composable
fun DayColumnView(
    dayData: DayScheduleData,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier.fillMaxHeight().padding(horizontal = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Weekday initial (M, T, W, T, F, S, S)
        Text(
            text = dayData.dayAbbr,
            style = TextStyle(
                color = if (dayData.isToday) GlanceTheme.colors.primary else GlanceTheme.colors.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = if (dayData.isToday) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = GlanceModifier.height(2.dp))

        // Date circle - highlighted with primary color if Today
        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .cornerRadius(12.dp)
                .background(
                    if (dayData.isToday) GlanceTheme.colors.primary else GlanceTheme.colors.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayData.dayNumber.toString(),
                style = TextStyle(
                    color = if (dayData.isToday) GlanceTheme.colors.onPrimary else GlanceTheme.colors.onSurface,
                    fontSize = 12.sp,
                    fontWeight = if (dayData.isToday) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Stacked events: All-day pinned first, then timed events
        Column(
            modifier = GlanceModifier.fillMaxWidth().fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            dayData.allDayEvents.take(2).forEach { event ->
                EventPillView(event = event, isAllDay = true)
                Spacer(modifier = GlanceModifier.height(2.dp))
            }

            dayData.timedEvents.take(3).forEach { event ->
                EventPillView(event = event, isAllDay = false)
                Spacer(modifier = GlanceModifier.height(2.dp))
            }

            val overflow = (dayData.allDayEvents.size + dayData.timedEvents.size) - 5
            if (overflow > 0) {
                Text(
                    text = "+$overflow more",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.padding(top = 1.dp)
                )
            }
        }
    }
}