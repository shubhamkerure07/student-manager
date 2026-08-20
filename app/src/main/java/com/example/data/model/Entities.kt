package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_items")
data class TimetableItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val courseCode: String = "",
    val professor: String = "",
    val room: String = "",
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ..., 7 = Sunday
    val startTime: String, // e.g. "09:00"
    val endTime: String,   // e.g. "10:30"
    val colorHex: String = "#4338CA",
    val notes: String = ""
)

@Entity(tableName = "attendance_courses")
data class AttendanceCourse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val code: String = "",
    val professor: String = "",
    val attendedClasses: Int = 0,
    val totalClasses: Int = 0,
    val targetPercentage: Int = 75,
    val colorHex: String = "#4338CA"
) {
    val percentage: Float
        get() = if (totalClasses > 0) (attendedClasses.toFloat() / totalClasses) * 100f else 100f

    val safeBunks: Int
        get() {
            if (totalClasses == 0) return 0
            val target = targetPercentage / 100f
            // (attended) / (total + x) >= target  => attended >= target*(total + x) => x <= (attended - target*total)/target
            val allowed = ((attendedClasses - target * totalClasses) / target).toInt()
            return if (allowed > 0) allowed else 0
        }

    val neededToReachTarget: Int
        get() {
            if (totalClasses == 0) return 0
            val target = targetPercentage / 100f
            if (percentage >= targetPercentage) return 0
            // (attended + y) / (total + y) >= target => attended + y >= target*total + target*y => y*(1-target) >= target*total - attended
            val needed = kotlin.math.ceil((target * totalClasses - attendedClasses) / (1f - target)).toInt()
            return if (needed > 0) needed else 1
        }
}

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val dateMillis: Long = System.currentTimeMillis(),
    val status: String, // "PRESENT", "ABSENT", "CANCELLED"
    val note: String = ""
)

@Entity(tableName = "expenses")
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String, // "Food & Canteen", "Books & Study", "Transport", "Bills & Fees", "Snacks & Drinks", "Entertainment", "Other"
    val dateMillis: Long = System.currentTimeMillis(),
    val paymentMode: String = "UPI/Online", // "UPI/Online", "Cash", "Card"
    val note: String = ""
)

@Entity(tableName = "student_essentials")
data class StudentEssential(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "TASK", "EXAM", "NOTE", "KEY_INFO"
    val title: String,
    val detail: String = "",
    val dueOrEventDateMillis: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val extraTag: String = "" // e.g. "Room 402", "Hall Ticket", "WiFi"
)

@Entity(tableName = "budget_settings")
data class BudgetSetting(
    @PrimaryKey val id: Int = 1,
    val monthlyBudget: Double = 500.0,
    val currencySymbol: String = "$"
)
