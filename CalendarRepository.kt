package com.example.calendarwidget.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarRepository(private val context: Context) {

    private val projection = arrayOf(
        CalendarContract.Instances.EVENT_ID,       // 0
        CalendarContract.Instances.TITLE,          // 1
        CalendarContract.Instances.BEGIN,          // 2
        CalendarContract.Instances.END,            // 3
        CalendarContract.Instances.ALL_DAY,        // 4
        CalendarContract.Instances.DISPLAY_COLOR,  // 5
        CalendarContract.Instances.CALENDAR_COLOR, // 6
        CalendarContract.Instances.VISIBLE         // 7
    )

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    suspend fun getCurrentWeekSchedule(): WeekSchedule = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance(Locale.getDefault()).apply {
            firstDayOfWeek = Calendar.getInstance().firstDayOfWeek
        }

        val todayYear = calendar.get(Calendar.YEAR)
        val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfWeekMs = calendar.timeInMillis
        val monthYearTitle = monthYearFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_WEEK, 7)
        val endOfWeekMs = calendar.timeInMillis - 1

        calendar.timeInMillis = startOfWeekMs

        val dayBuckets = ArrayList<DayScheduleData>(7)
        val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayAbbrFormat = SimpleDateFormat("EEEEE", Locale.getDefault())

        for (i in 0 until 7) {
            val date = calendar.time
            val isToday = (calendar.get(Calendar.YEAR) == todayYear &&
                    calendar.get(Calendar.DAY_OF_YEAR) == todayDayOfYear)

            dayBuckets.add(
                DayScheduleData(
                    dayIndex = i,
                    dateKey = dayKeyFormat.format(date),
                    dayAbbr = dayAbbrFormat.format(date),
                    dayNumber = calendar.get(Calendar.DAY_OF_MONTH),
                    isToday = isToday,
                    allDayEvents = mutableListOf(),
                    timedEvents = mutableListOf()
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Query CalendarContract.Instances to properly expand recurring events
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startOfWeekMs)
        ContentUris.appendId(builder, endOfWeekMs)

        val instancesUri: Uri = builder.build()
        val selection = "${CalendarContract.Instances.VISIBLE} = 1"
        val sortOrder = "${CalendarContract.Instances.BEGIN} ASC"

        var totalEventsFound = 0

        try {
            val cursor: Cursor? = context.contentResolver.query(
                instancesUri,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val idIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                val titleIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val beginIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val endIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.END)
                val allDayIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
                val displayColorIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.DISPLAY_COLOR)
                val calColorIdx = c.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_COLOR)

                val eventCal = Calendar.getInstance()

                while (c.moveToNext()) {
                    val id = c.getLong(idIdx)
                    val title = c.getString(titleIdx) ?: "(No title)"
                    val begin = c.getLong(beginIdx)
                    val end = c.getLong(endIdx)
                    val isAllDay = c.getInt(allDayIdx) == 1

                    var color = c.getInt(displayColorIdx)
                    if (color == 0) color = c.getInt(calColorIdx)
                    if (color == 0) color = 0xFF4285F4.toInt()

                    eventCal.timeInMillis = begin
                    val eventDateKey = dayKeyFormat.format(eventCal.time)
                    val startTimeStr = if (isAllDay) "" else timeFormat.format(eventCal.time)

                    val eventModel = CalendarEventModel(
                        id = id,
                        title = title,
                        startTimeMs = begin,
                        endTimeMs = end,
                        startTimeString = startTimeStr,
                        isAllDay = isAllDay,
                        displayColor = color
                    )

                    val targetDay = dayBuckets.find { it.dateKey == eventDateKey }
                    if (targetDay != null) {
                        if (isAllDay) {
                            (targetDay.allDayEvents as MutableList).add(eventModel)
                        } else {
                            (targetDay.timedEvents as MutableList).add(eventModel)
                        }
                        totalEventsFound++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        WeekSchedule(
            monthYearTitle = monthYearTitle,
            days = dayBuckets,
            isEmptyWeek = totalEventsFound == 0
        )
    }
}