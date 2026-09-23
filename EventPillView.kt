package com.example.calendarwidget.widget.ui

import android.content.ContentUris
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.calendarwidget.data.CalendarEventModel

@Composable
fun EventPillView(
    event: CalendarEventModel,
    isAllDay: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    val viewEventIntent = Intent(Intent.ACTION_VIEW).apply {
        data = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    // Dynamic contrast luminance calculation
    val luminance = ColorUtils.calculateLuminance(event.displayColor)
    val textColor = if (luminance > 0.45) Color(0xFF1F1F1F) else Color(0xFFFFFFFF)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .cornerRadius(4.dp)
            .background(ColorProvider(Color(event.displayColor)))
            .clickable(actionStartActivity(viewEventIntent))
            .padding(horizontal = 3.dp, vertical = 2.dp)
    ) {
        Text(
            text = event.title,
            maxLines = 1,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        )

        if (!isAllDay && event.startTimeString.isNotEmpty()) {
            Text(
                text = event.startTimeString,
                maxLines = 1,
                style = TextStyle(
                    color = ColorProvider(textColor.copy(alpha = 0.85f)),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}