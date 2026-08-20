package com.example.data.repository

import com.example.data.local.StudentDatabase
import com.example.data.model.AttendanceCourse
import com.example.data.model.AttendanceRecord
import com.example.data.model.BudgetSetting
import com.example.data.model.ExpenseItem
import com.example.data.model.StudentEssential
import com.example.data.model.TimetableItem
import kotlinx.coroutines.flow.Flow

class StudentHubRepository(private val database: StudentDatabase) {

    // Timetable
    val allTimetableItems: Flow<List<TimetableItem>> = database.timetableDao().getAllTimetableItems()

    fun getTimetableForDay(dayOfWeek: Int): Flow<List<TimetableItem>> =
        database.timetableDao().getTimetableForDay(dayOfWeek)

    suspend fun insertTimetableItem(item: TimetableItem) =
        database.timetableDao().insertTimetableItem(item)

    suspend fun updateTimetableItem(item: TimetableItem) =
        database.timetableDao().updateTimetableItem(item)

    suspend fun deleteTimetableItem(item: TimetableItem) =
        database.timetableDao().deleteTimetableItem(item)

    suspend fun deleteTimetableItemById(id: Int) =
        database.timetableDao().deleteById(id)

    // Attendance
    val allCourses: Flow<List<AttendanceCourse>> = database.attendanceDao().getAllCourses()
    val recentAttendanceRecords: Flow<List<AttendanceRecord>> = database.attendanceDao().getRecentRecords()

    fun getRecordsForCourse(courseId: Int): Flow<List<AttendanceRecord>> =
        database.attendanceDao().getRecordsForCourse(courseId)

    suspend fun insertCourse(course: AttendanceCourse) =
        database.attendanceDao().insertCourse(course)

    suspend fun updateCourse(course: AttendanceCourse) =
        database.attendanceDao().updateCourse(course)

    suspend fun deleteCourse(course: AttendanceCourse) =
        database.attendanceDao().deleteCourse(course)

    suspend fun markAttendance(courseId: Int, status: String, note: String = "") {
        val course = database.attendanceDao().getCourseById(courseId) ?: return
        when (status) {
            "PRESENT" -> {
                val updated = course.copy(
                    attendedClasses = course.attendedClasses + 1,
                    totalClasses = course.totalClasses + 1
                )
                database.attendanceDao().updateCourse(updated)
            }
            "ABSENT" -> {
                val updated = course.copy(
                    totalClasses = course.totalClasses + 1
                )
                database.attendanceDao().updateCourse(updated)
            }
            "CANCELLED" -> {
                // Total and attended remain unchanged
            }
        }
        database.attendanceDao().insertRecord(
            AttendanceRecord(
                courseId = courseId,
                status = status,
                note = note,
                dateMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateCourseCounts(courseId: Int, attended: Int, total: Int) {
        val course = database.attendanceDao().getCourseById(courseId) ?: return
        database.attendanceDao().updateCourse(
            course.copy(
                attendedClasses = attended.coerceAtLeast(0),
                totalClasses = total.coerceAtLeast(attended)
            )
        )
    }

    // Expenses
    val allExpenses: Flow<List<ExpenseItem>> = database.expenseDao().getAllExpenses()

    fun getExpensesSince(sinceMillis: Long): Flow<List<ExpenseItem>> =
        database.expenseDao().getExpensesSince(sinceMillis)

    suspend fun insertExpense(expense: ExpenseItem) =
        database.expenseDao().insertExpense(expense)

    suspend fun updateExpense(expense: ExpenseItem) =
        database.expenseDao().updateExpense(expense)

    suspend fun deleteExpense(expense: ExpenseItem) =
        database.expenseDao().deleteExpense(expense)

    suspend fun deleteExpenseById(id: Int) =
        database.expenseDao().deleteExpenseById(id)

    // Budget
    val budgetSetting: Flow<BudgetSetting?> = database.budgetDao().getBudgetSetting()

    suspend fun setBudget(budget: Double, currency: String) =
        database.budgetDao().setBudgetSetting(
            BudgetSetting(id = 1, monthlyBudget = budget, currencySymbol = currency)
        )

    // Essentials (Tasks, Exams, Notes)
    val allEssentials: Flow<List<StudentEssential>> = database.essentialDao().getAllEssentials()

    fun getEssentialsByType(type: String): Flow<List<StudentEssential>> =
        database.essentialDao().getEssentialsByType(type)

    suspend fun insertEssential(essential: StudentEssential) =
        database.essentialDao().insertEssential(essential)

    suspend fun updateEssential(essential: StudentEssential) =
        database.essentialDao().updateEssential(essential)

    suspend fun deleteEssential(essential: StudentEssential) =
        database.essentialDao().deleteEssential(essential)

    suspend fun toggleTaskCompleted(id: Int, isCompleted: Boolean) =
        database.essentialDao().toggleCompleted(id, isCompleted)
}
