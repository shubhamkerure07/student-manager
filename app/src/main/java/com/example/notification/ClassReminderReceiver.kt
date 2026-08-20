package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.TimetableItem

class ClassReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val classId = intent.getIntExtra(EXTRA_CLASS_ID, -1)
        val subject = intent.getStringExtra(EXTRA_SUBJECT) ?: "Class"
        val courseCode = intent.getStringExtra(EXTRA_COURSE_CODE) ?: ""
        val professor = intent.getStringExtra(EXTRA_PROFESSOR) ?: ""
        val room = intent.getStringExtra(EXTRA_ROOM) ?: ""
        val startTime = intent.getStringExtra(EXTRA_START_TIME) ?: ""
        val endTime = intent.getStringExtra(EXTRA_END_TIME) ?: ""
        val notes = intent.getStringExtra(EXTRA_NOTES) ?: ""
        val dayOfWeek = intent.getIntExtra(EXTRA_DAY_OF_WEEK, 1)
        val colorHex = intent.getStringExtra(EXTRA_COLOR_HEX) ?: "#4338CA"

        // Show push notification
        showClassNotification(
            context = context,
            classId = classId,
            subject = subject,
            courseCode = courseCode,
            professor = professor,
            room = room,
            startTime = startTime,
            endTime = endTime,
            notes = notes
        )

        // Reschedule for next week's occurrence automatically
        if (classId > 0 && startTime.isNotBlank()) {
            val item = TimetableItem(
                id = classId,
                subject = subject,
                courseCode = courseCode,
                professor = professor,
                room = room,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                colorHex = colorHex,
                notes = notes
            )
            ClassNotificationScheduler.scheduleClassReminder(context, item)
        }
    }

    private fun showClassNotification(
        context: Context,
        classId: Int,
        subject: String,
        courseCode: String,
        professor: String,
        room: String,
        startTime: String,
        endTime: String,
        notes: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel for Android 8.0+ (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timetable class reminders scheduled 15 minutes before class start time"
                enableLights(true)
                lightColor = Color.parseColor("#4338CA")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 150, 350)
                setShowBadge(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap action -> Open Timetable in MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TAB, "TIMETABLE")
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            if (classId > 0) classId else (System.currentTimeMillis() % 10000).toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val headerText = if (courseCode.isNotBlank()) "[$courseCode] $subject" else subject
        val timeRoomLine = buildString {
            if (startTime.isNotBlank()) append("🕒 $startTime - $endTime")
            if (room.isNotBlank()) {
                if (isNotEmpty()) append("  •  ")
                append("📍 Room $room")
            }
        }

        val bigText = buildString {
            append("🔔 Starts in 15 minutes!\n")
            if (timeRoomLine.isNotBlank()) append("$timeRoomLine\n")
            if (professor.isNotBlank()) append("👨‍🏫 Instructor: $professor\n")
            if (notes.isNotBlank()) append("📝 Note: $notes\n")
            append("Tap to open your timetable and log attendance.")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Class in 15 Mins: $headerText")
            .setContentText(timeRoomLine.ifBlank { "Starts in 15 minutes" })
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("Class in 15 Mins: $headerText")
                    .bigText(bigText)
                    .setSummaryText("Timetable Reminder")
            )
            .setColor(0xFF4338CA.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .addAction(
                R.mipmap.ic_launcher,
                "View Schedule",
                contentPendingIntent
            )

        val notifId = if (classId > 0) classId else 1001
        notificationManager.notify(notifId, builder.build())
    }

    companion object {
        const val ACTION_CLASS_REMINDER = "com.example.action.CLASS_REMINDER"
        const val CHANNEL_ID = "class_schedule_reminders"
        const val CHANNEL_NAME = "Class Schedule Reminders"

        const val EXTRA_CLASS_ID = "extra_class_id"
        const val EXTRA_SUBJECT = "extra_subject"
        const val EXTRA_COURSE_CODE = "extra_course_code"
        const val EXTRA_PROFESSOR = "extra_professor"
        const val EXTRA_ROOM = "extra_room"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_END_TIME = "extra_end_time"
        const val EXTRA_NOTES = "extra_notes"
        const val EXTRA_DAY_OF_WEEK = "extra_day_of_week"
        const val EXTRA_COLOR_HEX = "extra_color_hex"
        const val EXTRA_NAVIGATE_TAB = "extra_navigate_tab"
    }
}
