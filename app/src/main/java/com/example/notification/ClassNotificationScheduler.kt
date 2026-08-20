package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.TimetableItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ClassNotificationScheduler {

    private const val TAG = "ClassReminderScheduler"
    private const val PREFS_NAME = "class_reminder_prefs"
    private const val KEY_REMINDERS_ENABLED = "reminders_enabled"

    fun isRemindersEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_REMINDERS_ENABLED, true)
    }

    fun setRemindersEnabled(context: Context, enabled: Boolean, items: List<TimetableItem>? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply()

        if (items != null) {
            if (enabled) {
                scheduleAll(context, items)
            } else {
                cancelAll(context, items)
            }
        }
    }

    /**
     * Schedules a notification precisely 15 minutes before the class startTime.
     * Uses AlarmManager with exact alarm capabilities to wake up even in Doze mode.
     */
    fun scheduleClassReminder(context: Context, item: TimetableItem) {
        if (!isRemindersEnabled(context)) {
            Log.d(TAG, "Reminders are disabled in settings; skipping schedule for ${item.subject}")
            return
        }

        if (item.id <= 0 || item.startTime.isBlank()) return

        val triggerMillis = calculateNextTriggerMillis(item.dayOfWeek, item.startTime)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            action = ClassReminderReceiver.ACTION_CLASS_REMINDER
            putExtra(ClassReminderReceiver.EXTRA_CLASS_ID, item.id)
            putExtra(ClassReminderReceiver.EXTRA_SUBJECT, item.subject)
            putExtra(ClassReminderReceiver.EXTRA_COURSE_CODE, item.courseCode)
            putExtra(ClassReminderReceiver.EXTRA_PROFESSOR, item.professor)
            putExtra(ClassReminderReceiver.EXTRA_ROOM, item.room)
            putExtra(ClassReminderReceiver.EXTRA_START_TIME, item.startTime)
            putExtra(ClassReminderReceiver.EXTRA_END_TIME, item.endTime)
            putExtra(ClassReminderReceiver.EXTRA_NOTES, item.notes)
            putExtra(ClassReminderReceiver.EXTRA_DAY_OF_WEEK, item.dayOfWeek)
            putExtra(ClassReminderReceiver.EXTRA_COLOR_HEX, item.colorHex)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
            val formatted = SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault()).format(Date(triggerMillis))
            Log.d(TAG, "Scheduled 15-min reminder for ${item.subject} on $formatted")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for ${item.subject}", e)
        }
    }

    /**
     * Cancels an existing scheduled class reminder by item ID.
     */
    fun cancelClassReminder(context: Context, itemId: Int) {
        if (itemId <= 0) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            action = ClassReminderReceiver.ACTION_CLASS_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            itemId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled class reminder for ID $itemId")
        }
    }

    /**
     * Schedules reminders for all classes in the timetable.
     */
    fun scheduleAll(context: Context, items: List<TimetableItem>) {
        if (!isRemindersEnabled(context)) return
        for (item in items) {
            scheduleClassReminder(context, item)
        }
    }

    /**
     * Cancels all scheduled class reminders.
     */
    fun cancelAll(context: Context, items: List<TimetableItem>) {
        for (item in items) {
            cancelClassReminder(context, item.id)
        }
    }

    /**
     * Sends an immediate test notification simulating a 15-minute advance reminder
     */
    fun sendTestNotification(context: Context, sampleItem: TimetableItem? = null) {
        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            action = ClassReminderReceiver.ACTION_CLASS_REMINDER
            putExtra(ClassReminderReceiver.EXTRA_CLASS_ID, sampleItem?.id ?: 9999)
            putExtra(ClassReminderReceiver.EXTRA_SUBJECT, sampleItem?.subject ?: "Data Structures & Algorithms")
            putExtra(ClassReminderReceiver.EXTRA_COURSE_CODE, sampleItem?.courseCode ?: "CS-301")
            putExtra(ClassReminderReceiver.EXTRA_PROFESSOR, sampleItem?.professor ?: "Dr. Alan Vance")
            putExtra(ClassReminderReceiver.EXTRA_ROOM, sampleItem?.room ?: "Hall B-204")
            putExtra(ClassReminderReceiver.EXTRA_START_TIME, sampleItem?.startTime ?: "09:00")
            putExtra(ClassReminderReceiver.EXTRA_END_TIME, sampleItem?.endTime ?: "10:30")
            putExtra(ClassReminderReceiver.EXTRA_NOTES, sampleItem?.notes ?: "Bring laptop with CLion/VSCode")
            putExtra(ClassReminderReceiver.EXTRA_DAY_OF_WEEK, sampleItem?.dayOfWeek ?: 1)
        }
        context.sendBroadcast(intent)
    }

    /**
     * Calculates the exact epoch millis for 15 minutes before the class startTime on its scheduled day.
     */
    fun calculateNextTriggerMillis(dayOfWeek: Int, startTime: String): Long {
        val parts = startTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        // Calculate reminder time: 15 minutes before class starts
        var reminderHour = hour
        var reminderMinute = minute - 15
        var dayAdjustment = 0

        if (reminderMinute < 0) {
            reminderMinute += 60
            reminderHour -= 1
            if (reminderHour < 0) {
                reminderHour += 24
                dayAdjustment = -1 // 15 minutes before midnight belongs to previous day
            }
        }

        val targetDayOfWeek = when (dayOfWeek) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            7 -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }

        val now = Calendar.getInstance()
        val targetCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, targetDayOfWeek)
            set(Calendar.HOUR_OF_DAY, reminderHour)
            set(Calendar.MINUTE, reminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (dayAdjustment != 0) {
                add(Calendar.DAY_OF_YEAR, dayAdjustment)
            }
        }

        // If the target timestamp is in the past, schedule for the next week
        if (targetCal.timeInMillis <= now.timeInMillis) {
            targetCal.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return targetCal.timeInMillis
    }

    /**
     * Formats next trigger time for human-readable UI badges
     */
    fun getNextTriggerDescription(dayOfWeek: Int, startTime: String): String {
        val triggerMillis = calculateNextTriggerMillis(dayOfWeek, startTime)
        val format = SimpleDateFormat("EEE, h:mm a", Locale.getDefault())
        return format.format(Date(triggerMillis))
    }
}
