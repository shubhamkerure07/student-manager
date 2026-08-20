package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceCourse
import com.example.data.model.AttendanceRecord
import com.example.data.model.BudgetSetting
import com.example.data.model.ExpenseItem
import com.example.data.model.StudentEssential
import com.example.data.model.TimetableItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_items ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllTimetableItems(): Flow<List<TimetableItem>>

    @Query("SELECT * FROM timetable_items ORDER BY dayOfWeek ASC, startTime ASC")
    suspend fun getAllTimetableItemsList(): List<TimetableItem>

    @Query("SELECT * FROM timetable_items WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getTimetableForDay(dayOfWeek: Int): Flow<List<TimetableItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableItem(item: TimetableItem): Long

    @Update
    suspend fun updateTimetableItem(item: TimetableItem)

    @Delete
    suspend fun deleteTimetableItem(item: TimetableItem)

    @Query("DELETE FROM timetable_items WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_courses ORDER BY name ASC")
    fun getAllCourses(): Flow<List<AttendanceCourse>>

    @Query("SELECT * FROM attendance_courses WHERE id = :id")
    suspend fun getCourseById(id: Int): AttendanceCourse?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: AttendanceCourse): Long

    @Update
    suspend fun updateCourse(course: AttendanceCourse)

    @Delete
    suspend fun deleteCourse(course: AttendanceCourse)

    @Query("DELETE FROM attendance_courses WHERE id = :id")
    suspend fun deleteCourseById(id: Int)

    // Attendance Records
    @Query("SELECT * FROM attendance_records WHERE courseId = :courseId ORDER BY dateMillis DESC")
    fun getRecordsForCourse(courseId: Int): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY dateMillis DESC LIMIT 50")
    fun getRecentRecords(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord): Long

    @Delete
    suspend fun deleteRecord(record: AttendanceRecord)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<ExpenseItem>>

    @Query("SELECT * FROM expenses WHERE dateMillis >= :sinceMillis ORDER BY dateMillis DESC")
    fun getExpensesSince(sinceMillis: Long): Flow<List<ExpenseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseItem): Long

    @Update
    suspend fun updateExpense(expense: ExpenseItem)

    @Delete
    suspend fun deleteExpense(expense: ExpenseItem)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Int)
}

@Dao
interface EssentialDao {
    @Query("SELECT * FROM student_essentials ORDER BY isCompleted ASC, dueOrEventDateMillis ASC")
    fun getAllEssentials(): Flow<List<StudentEssential>>

    @Query("SELECT * FROM student_essentials WHERE type = :type ORDER BY isCompleted ASC, dueOrEventDateMillis ASC")
    fun getEssentialsByType(type: String): Flow<List<StudentEssential>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssential(essential: StudentEssential): Long

    @Update
    suspend fun updateEssential(essential: StudentEssential)

    @Delete
    suspend fun deleteEssential(essential: StudentEssential)

    @Query("UPDATE student_essentials SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun toggleCompleted(id: Int, isCompleted: Boolean)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_settings WHERE id = 1")
    fun getBudgetSetting(): Flow<BudgetSetting?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudgetSetting(setting: BudgetSetting)
}
