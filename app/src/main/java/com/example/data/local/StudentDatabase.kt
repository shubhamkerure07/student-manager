package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AttendanceCourse
import com.example.data.model.AttendanceRecord
import com.example.data.model.BudgetSetting
import com.example.data.model.ExpenseItem
import com.example.data.model.StudentEssential
import com.example.data.model.TimetableItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TimetableItem::class,
        AttendanceCourse::class,
        AttendanceRecord::class,
        ExpenseItem::class,
        StudentEssential::class,
        BudgetSetting::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudentDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun essentialDao(): EssentialDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: StudentDatabase? = null

        fun getDatabase(context: Context): StudentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentDatabase::class.java,
                    "student_hub_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: StudentDatabase) {
            // Default Budget
            db.budgetDao().setBudgetSetting(
                BudgetSetting(
                    id = 1,
                    monthlyBudget = 450.0,
                    currencySymbol = "$"
                )
            )

            // Initial Timetable (Mon-Fri)
            val timetableList = listOf(
                TimetableItem(
                    subject = "Data Structures & Algorithms",
                    courseCode = "CS-301",
                    professor = "Dr. Alan Vance",
                    room = "Hall B-204",
                    dayOfWeek = 1, // Monday
                    startTime = "09:00",
                    endTime = "10:30",
                    colorHex = "#4338CA",
                    notes = "Bring laptop with CLion/VSCode"
                ),
                TimetableItem(
                    subject = "Database Management Systems",
                    courseCode = "CS-304",
                    professor = "Prof. Sarah Miller",
                    room = "Lab 3",
                    dayOfWeek = 1, // Monday
                    startTime = "11:00",
                    endTime = "12:30",
                    colorHex = "#0D9488",
                    notes = "SQL project review"
                ),
                TimetableItem(
                    subject = "Computer Networks",
                    courseCode = "CS-308",
                    professor = "Dr. Kevin Wright",
                    room = "Room 105",
                    dayOfWeek = 1, // Monday
                    startTime = "14:00",
                    endTime = "15:30",
                    colorHex = "#D97706",
                    notes = "OSI layer test next week"
                ),
                TimetableItem(
                    subject = "Software Engineering",
                    courseCode = "CS-310",
                    professor = "Prof. Elena Gomez",
                    room = "Hall A-101",
                    dayOfWeek = 2, // Tuesday
                    startTime = "09:30",
                    endTime = "11:00",
                    colorHex = "#7C3AED",
                    notes = "Agile sprint demo"
                ),
                TimetableItem(
                    subject = "Operating Systems",
                    courseCode = "CS-302",
                    professor = "Dr. David Kim",
                    room = "Lab 1",
                    dayOfWeek = 2, // Tuesday
                    startTime = "11:30",
                    endTime = "13:00",
                    colorHex = "#059669",
                    notes = "Process synchronization"
                ),
                TimetableItem(
                    subject = "Data Structures & Algorithms",
                    courseCode = "CS-301",
                    professor = "Dr. Alan Vance",
                    room = "Hall B-204",
                    dayOfWeek = 3, // Wednesday
                    startTime = "09:00",
                    endTime = "10:30",
                    colorHex = "#4338CA"
                ),
                TimetableItem(
                    subject = "Web Development Lab",
                    courseCode = "CS-312",
                    professor = "Prof. Lisa Chen",
                    room = "Lab 4",
                    dayOfWeek = 3, // Wednesday
                    startTime = "14:00",
                    endTime = "16:00",
                    colorHex = "#E11D48"
                ),
                TimetableItem(
                    subject = "Software Engineering",
                    courseCode = "CS-310",
                    professor = "Prof. Elena Gomez",
                    room = "Hall A-101",
                    dayOfWeek = 4, // Thursday
                    startTime = "10:00",
                    endTime = "11:30",
                    colorHex = "#7C3AED"
                ),
                TimetableItem(
                    subject = "Computer Networks",
                    courseCode = "CS-308",
                    professor = "Dr. Kevin Wright",
                    room = "Room 105",
                    dayOfWeek = 4, // Thursday
                    startTime = "13:00",
                    endTime = "14:30",
                    colorHex = "#D97706"
                ),
                TimetableItem(
                    subject = "Operating Systems Lab",
                    courseCode = "CS-302",
                    professor = "Dr. David Kim",
                    room = "Lab 1",
                    dayOfWeek = 5, // Friday
                    startTime = "09:30",
                    endTime = "12:00",
                    colorHex = "#059669"
                ),
                TimetableItem(
                    subject = "AI & Machine Learning Seminar",
                    courseCode = "CS-401",
                    professor = "Dr. R. Patel",
                    room = "Auditorium 2",
                    dayOfWeek = 5, // Friday
                    startTime = "14:00",
                    endTime = "15:30",
                    colorHex = "#2563EB"
                )
            )
            for (item in timetableList) {
                db.timetableDao().insertTimetableItem(item)
            }

            // Initial Attendance Courses
            val course1Id = db.attendanceDao().insertCourse(
                AttendanceCourse(
                    name = "Data Structures & Algorithms",
                    code = "CS-301",
                    professor = "Dr. Alan Vance",
                    attendedClasses = 24,
                    totalClasses = 28, // 85.7%
                    targetPercentage = 75,
                    colorHex = "#4338CA"
                )
            ).toInt()

            val course2Id = db.attendanceDao().insertCourse(
                AttendanceCourse(
                    name = "Database Management Systems",
                    code = "CS-304",
                    professor = "Prof. Sarah Miller",
                    attendedClasses = 19,
                    totalClasses = 24, // 79.1%
                    targetPercentage = 75,
                    colorHex = "#0D9488"
                )
            ).toInt()

            val course3Id = db.attendanceDao().insertCourse(
                AttendanceCourse(
                    name = "Computer Networks",
                    code = "CS-308",
                    professor = "Dr. Kevin Wright",
                    attendedClasses = 16,
                    totalClasses = 22, // 72.7% (Low Attendance Alert)
                    targetPercentage = 75,
                    colorHex = "#D97706"
                )
            ).toInt()

            val course4Id = db.attendanceDao().insertCourse(
                AttendanceCourse(
                    name = "Operating Systems",
                    code = "CS-302",
                    professor = "Dr. David Kim",
                    attendedClasses = 25,
                    totalClasses = 26, // 96.1%
                    targetPercentage = 75,
                    colorHex = "#059669"
                )
            ).toInt()

            val course5Id = db.attendanceDao().insertCourse(
                AttendanceCourse(
                    name = "Software Engineering",
                    code = "CS-310",
                    professor = "Prof. Elena Gomez",
                    attendedClasses = 18,
                    totalClasses = 20, // 90.0%
                    targetPercentage = 80,
                    colorHex = "#7C3AED"
                )
            ).toInt()

            // Attendance Records log
            val now = System.currentTimeMillis()
            val oneDay = 86400000L
            db.attendanceDao().insertRecord(AttendanceRecord(courseId = course1Id, dateMillis = now - oneDay, status = "PRESENT", note = "Binary Trees"))
            db.attendanceDao().insertRecord(AttendanceRecord(courseId = course2Id, dateMillis = now - oneDay * 2, status = "PRESENT", note = "Normalization"))
            db.attendanceDao().insertRecord(AttendanceRecord(courseId = course3Id, dateMillis = now - oneDay * 3, status = "ABSENT", note = "Sick leave"))
            db.attendanceDao().insertRecord(AttendanceRecord(courseId = course4Id, dateMillis = now - oneDay * 4, status = "PRESENT", note = "Semaphores"))

            // Sample Expenses across days for spending graph
            val expenses = listOf(
                ExpenseItem(title = "Campus Canteen Lunch", amount = 8.50, category = "Food & Canteen", dateMillis = now - (oneDay * 0), paymentMode = "UPI/Online"),
                ExpenseItem(title = "Bus Card Recharge", amount = 25.00, category = "Transport", dateMillis = now - (oneDay * 1), paymentMode = "Card"),
                ExpenseItem(title = "Algorithms Reference Book", amount = 42.00, category = "Books & Study", dateMillis = now - (oneDay * 2), paymentMode = "UPI/Online"),
                ExpenseItem(title = "Afternoon Iced Coffee & Bagel", amount = 6.80, category = "Snacks & Drinks", dateMillis = now - (oneDay * 2), paymentMode = "UPI/Online"),
                ExpenseItem(title = "Spotify Student Subscription", amount = 5.99, category = "Bills & Fees", dateMillis = now - (oneDay * 3), paymentMode = "Card"),
                ExpenseItem(title = "Weekend Movie & Popcorn", amount = 16.50, category = "Entertainment", dateMillis = now - (oneDay * 4), paymentMode = "Cash"),
                ExpenseItem(title = "Groceries & Hostel Supplies", amount = 34.20, category = "Food & Canteen", dateMillis = now - (oneDay * 5), paymentMode = "UPI/Online"),
                ExpenseItem(title = "Stationery Notebooks & Pens", amount = 12.00, category = "Books & Study", dateMillis = now - (oneDay * 6), paymentMode = "Cash"),
                ExpenseItem(title = "Hostel Laundry Tokens", amount = 8.00, category = "Other", dateMillis = now - (oneDay * 7), paymentMode = "Cash"),
                ExpenseItem(title = "Dinner with Project Team", amount = 22.50, category = "Food & Canteen", dateMillis = now - (oneDay * 8), paymentMode = "UPI/Online")
            )
            for (exp in expenses) {
                db.expenseDao().insertExpense(exp)
            }

            // Student Essentials (Tasks, Exams, Key Info)
            val essentials = listOf(
                StudentEssential(
                    type = "EXAM",
                    title = "Midterm: Computer Networks",
                    detail = "Covers Chapters 1-4, Room 105, 10:00 AM",
                    dueOrEventDateMillis = now + (oneDay * 4),
                    priority = "HIGH",
                    extraTag = "Exam Hall 105"
                ),
                StudentEssential(
                    type = "TASK",
                    title = "Submit Database Mini-Project",
                    detail = "ER diagram, Schema SQL, and normal forms documentation",
                    dueOrEventDateMillis = now + (oneDay * 2),
                    priority = "HIGH",
                    extraTag = "Portal Upload"
                ),
                StudentEssential(
                    type = "TASK",
                    title = "Solve LeetCode Graph problems",
                    detail = "BFS/DFS 5 questions for DSA lab prep",
                    dueOrEventDateMillis = now + (oneDay * 3),
                    priority = "MEDIUM",
                    extraTag = "Lab Prep"
                ),
                StudentEssential(
                    type = "KEY_INFO",
                    title = "Campus Library & Wi-Fi Details",
                    detail = "Wi-Fi: EduRoam-Campus (Login: Roll#), Library Card: LIB-2026-9482",
                    dueOrEventDateMillis = now,
                    priority = "LOW",
                    extraTag = "Student ID: CS-2026-088"
                ),
                StudentEssential(
                    type = "NOTE",
                    title = "Prof. Vance Office Hours",
                    detail = "Tuesdays & Thursdays 3:30 PM - 5:00 PM in Faculty Room 210",
                    dueOrEventDateMillis = now,
                    priority = "LOW",
                    extraTag = "Office 210"
                )
            )
            for (ess in essentials) {
                db.essentialDao().insertEssential(ess)
            }
        }
    }
}
