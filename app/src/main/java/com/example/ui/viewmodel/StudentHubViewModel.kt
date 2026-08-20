package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StudentDatabase
import com.example.data.model.AttendanceCourse
import com.example.data.model.AttendanceRecord
import com.example.data.model.BudgetSetting
import com.example.data.model.ExpenseItem
import com.example.data.model.StudentEssential
import com.example.data.model.TimetableItem
import com.example.data.repository.StudentHubRepository
import com.example.notification.ClassNotificationScheduler
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppTab(val title: String) {
    OVERVIEW("Overview"),
    TIMETABLE("Timetable"),
    ATTENDANCE("Attendance"),
    SPENDING("Spending"),
    ESSENTIALS("Essentials")
}

enum class GraphPeriod {
    THIS_WEEK,
    THIS_MONTH
}

enum class SpendingTimeframe(val label: String) {
    ALL("All Time"),
    THIS_MONTH("This Month"),
    THIS_WEEK("This Week"),
    TODAY("Today")
}

data class DailySpending(
    val dayLabel: String,
    val dayNumber: Int,
    val amount: Double,
    val timestamp: Long
)

data class CategorySpending(
    val category: String,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int
)

data class PaymentModeSpending(
    val mode: String,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int
)

data class StudentHubUiState(
    val currentTab: AppTab = AppTab.OVERVIEW,
    val selectedDayOfWeek: Int = currentDayOfWeekIndex(),
    val timetableList: List<TimetableItem> = emptyList(),
    val attendanceCourses: List<AttendanceCourse> = emptyList(),
    val recentAttendanceLogs: List<AttendanceRecord> = emptyList(),
    val expenses: List<ExpenseItem> = emptyList(),
    val budgetSetting: BudgetSetting = BudgetSetting(),
    val essentials: List<StudentEssential> = emptyList(),
    val graphPeriod: GraphPeriod = GraphPeriod.THIS_WEEK,
    val spendingTimeframe: SpendingTimeframe = SpendingTimeframe.THIS_MONTH,
    val selectedExpenseCategory: String? = null,
    val selectedGraphBarIndex: Int? = null,
    val userSearchQuery: String = "",
    val classRemindersEnabled: Boolean = true,
    val isNotificationBannerDismissed: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
) {
    // Quick Calculated Stats for Overview & Summaries
    val todaysClasses: List<TimetableItem>
        get() = timetableList.filter { it.dayOfWeek == selectedDayOfWeek }

    val overallAttendancePercentage: Float
        get() {
            val totalAttended = attendanceCourses.sumOf { it.attendedClasses }
            val totalConducted = attendanceCourses.sumOf { it.totalClasses }
            return if (totalConducted > 0) (totalAttended.toFloat() / totalConducted) * 100f else 100f
        }

    val coursesNeedingAttentionCount: Int
        get() = attendanceCourses.count { it.percentage < it.targetPercentage }

    // Money Spent Calculations
    val todayTotalSpending: Double
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = cal.timeInMillis
            return expenses.filter { it.dateMillis >= startOfDay }.sumOf { it.amount }
        }

    val weeklyTotalSpending: Double
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfWeek = cal.timeInMillis
            return expenses.filter { it.dateMillis >= startOfWeek }.sumOf { it.amount }
        }

    val monthlyTotalSpending: Double
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val monthStart = cal.timeInMillis
            return expenses.filter { it.dateMillis >= monthStart }.sumOf { it.amount }
        }

    val allTimeTotalSpending: Double
        get() = expenses.sumOf { it.amount }

    val dailyAverageSpending: Double
        get() {
            val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            return if (dayOfMonth > 0) monthlyTotalSpending / dayOfMonth else 0.0
        }

    val dailySafeAllowance: Double
        get() {
            val cal = Calendar.getInstance()
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val remainingDays = (maxDays - dayOfMonth + 1).coerceAtLeast(1)
            val remainingBudget = budgetSetting.monthlyBudget - monthlyTotalSpending
            return if (remainingBudget > 0) remainingBudget / remainingDays else 0.0
        }

    val highestExpenseCategory: CategorySpending?
        get() = categoryBreakdown.firstOrNull()

    val pendingTasksCount: Int
        get() = essentials.count { it.type == "TASK" && !it.isCompleted }

    val upcomingExams: List<StudentEssential>
        get() = essentials.filter { it.type == "EXAM" && !it.isCompleted }
            .sortedBy { it.dueOrEventDateMillis }

    val weeklyDailySpendings: List<DailySpending>
        get() {
            val list = mutableListOf<DailySpending>()
            val cal = Calendar.getInstance()
            // Set to beginning of current week (Monday)
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            for (i in 0..6) {
                val start = cal.timeInMillis
                val end = start + 86400000L
                val total = expenses.filter { it.dateMillis in start until end }.sumOf { it.amount }
                list.add(DailySpending(dayLabel = days[i], dayNumber = i + 1, amount = total, timestamp = start))
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
            return list
        }

    val monthlyDailySpendings: List<DailySpending>
        get() {
            val list = mutableListOf<DailySpending>()
            val cal = Calendar.getInstance()
            val currentMonth = cal.get(Calendar.MONTH)
            val currentYear = cal.get(Calendar.YEAR)
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            for (d in 1..maxDay) {
                val c = Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear)
                    set(Calendar.MONTH, currentMonth)
                    set(Calendar.DAY_OF_MONTH, d)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val start = c.timeInMillis
                val end = start + 86400000L
                val total = expenses.filter { it.dateMillis in start until end }.sumOf { it.amount }
                list.add(DailySpending(dayLabel = "$d", dayNumber = d, amount = total, timestamp = start))
            }
            return list
        }

    val categoryBreakdown: List<CategorySpending>
        get() {
            val totalAll = expenses.sumOf { it.amount }
            if (totalAll <= 0.0) return emptyList()
            return expenses.groupBy { it.category }
                .map { (cat, items) ->
                    val sum = items.sumOf { it.amount }
                    CategorySpending(
                        category = cat,
                        totalAmount = sum,
                        percentage = ((sum / totalAll) * 100f).toFloat(),
                        count = items.size
                    )
                }.sortedByDescending { it.totalAmount }
        }

    val paymentModeBreakdown: List<PaymentModeSpending>
        get() {
            val totalAll = expenses.sumOf { it.amount }
            if (totalAll <= 0.0) return emptyList()
            return expenses.groupBy { it.paymentMode }
                .map { (mode, items) ->
                    val sum = items.sumOf { it.amount }
                    PaymentModeSpending(
                        mode = if (mode.isNotBlank()) mode else "Other",
                        totalAmount = sum,
                        percentage = ((sum / totalAll) * 100f).toFloat(),
                        count = items.size
                    )
                }.sortedByDescending { it.totalAmount }
        }

    val filteredExpenses: List<ExpenseItem>
        get() {
            val startOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val startOfWeek = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val startOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            return expenses.filter { exp ->
                val matchesCategory = selectedExpenseCategory == null || exp.category == selectedExpenseCategory
                val matchesTimeframe = when (spendingTimeframe) {
                    SpendingTimeframe.ALL -> true
                    SpendingTimeframe.THIS_MONTH -> exp.dateMillis >= startOfMonth
                    SpendingTimeframe.THIS_WEEK -> exp.dateMillis >= startOfWeek
                    SpendingTimeframe.TODAY -> exp.dateMillis >= startOfToday
                }
                val matchesSearch = userSearchQuery.isBlank() ||
                        exp.title.contains(userSearchQuery, ignoreCase = true) ||
                        exp.category.contains(userSearchQuery, ignoreCase = true) ||
                        exp.note.contains(userSearchQuery, ignoreCase = true)

                matchesCategory && matchesTimeframe && matchesSearch
            }.sortedByDescending { it.dateMillis }
        }
}

private fun currentDayOfWeekIndex(): Int {
    val cal = Calendar.getInstance()
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 1
    }
}

class StudentHubViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudentHubRepository

    private val _currentTab = MutableStateFlow(AppTab.OVERVIEW)
    private val _selectedDayOfWeek = MutableStateFlow(currentDayOfWeekIndex())
    private val _graphPeriod = MutableStateFlow(GraphPeriod.THIS_WEEK)
    private val _spendingTimeframe = MutableStateFlow(SpendingTimeframe.THIS_MONTH)
    private val _selectedExpenseCategory = MutableStateFlow<String?>(null)
    private val _selectedGraphBarIndex = MutableStateFlow<Int?>(null)
    private val _userSearchQuery = MutableStateFlow("")
    private val _classRemindersEnabled = MutableStateFlow(ClassNotificationScheduler.isRemindersEnabled(application))
    private val _notificationBannerDismissed = MutableStateFlow(false)

    private val themePrefs = application.getSharedPreferences("student_hub_theme_prefs", android.content.Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(themePrefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    )

    init {
        val database = StudentDatabase.getDatabase(application)
        repository = StudentHubRepository(database)

        // Automatically observe timetable items and sync 15-minute advance class push notifications
        viewModelScope.launch {
            repository.allTimetableItems.collect { items ->
                if (_classRemindersEnabled.value) {
                    ClassNotificationScheduler.scheduleAll(getApplication(), items)
                }
            }
        }
    }

    private data class AcademicData(
        val timetable: List<TimetableItem>,
        val courses: List<AttendanceCourse>,
        val records: List<AttendanceRecord>,
        val essentials: List<StudentEssential>
    )

    private data class FinanceData(
        val expenses: List<ExpenseItem>,
        val budget: BudgetSetting?
    )

    private val academicFlow = combine(
        repository.allTimetableItems,
        repository.allCourses,
        repository.recentAttendanceRecords,
        repository.allEssentials
    ) { timetable, courses, records, essentials ->
        AcademicData(timetable, courses, records, essentials)
    }

    private val financeFlow = combine(
        repository.allExpenses,
        repository.budgetSetting
    ) { expenses, budget ->
        FinanceData(expenses, budget)
    }

    private data class UiControls(
        val tab: AppTab,
        val dayOfWeek: Int,
        val graphPeriod: GraphPeriod,
        val spendingTimeframe: SpendingTimeframe,
        val catFilter: String?,
        val barIdx: Int?,
        val remindersEnabled: Boolean,
        val bannerDismissed: Boolean,
        val themeMode: AppThemeMode
    )

    private val uiControlsFlow = combine(
        _currentTab,
        _selectedDayOfWeek,
        _graphPeriod,
        _spendingTimeframe,
        _selectedExpenseCategory
    ) { tab, day, period, timeframe, catFilter ->
        Triple(tab, day, period) to (timeframe to catFilter)
    }.combine(_selectedGraphBarIndex) { (pair1, pair2), barIdx ->
        Triple(pair1, pair2, barIdx)
    }.combine(combine(_classRemindersEnabled, _notificationBannerDismissed, _themeMode) { rem, ban, theme -> Triple(rem, ban, theme) }) { (pair1, pair2, barIdx), (rem, ban, theme) ->
        val (tab, day, period) = pair1
        val (timeframe, catFilter) = pair2
        UiControls(tab, day, period, timeframe, catFilter, barIdx, rem, ban, theme)
    }

    val uiState: StateFlow<StudentHubUiState> = combine(
        academicFlow,
        financeFlow,
        uiControlsFlow,
        _userSearchQuery
    ) { academic, finance, controls, search ->
        StudentHubUiState(
            currentTab = controls.tab,
            selectedDayOfWeek = controls.dayOfWeek,
            timetableList = academic.timetable,
            attendanceCourses = academic.courses,
            recentAttendanceLogs = academic.records,
            expenses = finance.expenses,
            budgetSetting = finance.budget ?: BudgetSetting(),
            essentials = academic.essentials,
            graphPeriod = controls.graphPeriod,
            spendingTimeframe = controls.spendingTimeframe,
            selectedExpenseCategory = controls.catFilter,
            selectedGraphBarIndex = controls.barIdx,
            userSearchQuery = search,
            classRemindersEnabled = controls.remindersEnabled,
            isNotificationBannerDismissed = controls.bannerDismissed,
            themeMode = controls.themeMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudentHubUiState()
    )

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectDayOfWeek(day: Int) {
        _selectedDayOfWeek.value = day
    }

    fun setGraphPeriod(period: GraphPeriod) {
        _graphPeriod.value = period
        _selectedGraphBarIndex.value = null
    }

    fun setSpendingTimeframe(timeframe: SpendingTimeframe) {
        _spendingTimeframe.value = timeframe
    }

    fun selectExpenseCategory(category: String?) {
        _selectedExpenseCategory.value = category
    }

    fun selectGraphBar(index: Int?) {
        _selectedGraphBarIndex.value = index
    }

    fun setSearchQuery(query: String) {
        _userSearchQuery.value = query
    }

    // Theme Mode Switching
    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        themePrefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun toggleThemeMode(isCurrentSystemDark: Boolean = false) {
        val current = _themeMode.value
        val next = when (current) {
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.DARK -> AppThemeMode.LIGHT
            AppThemeMode.SYSTEM -> if (isCurrentSystemDark) AppThemeMode.LIGHT else AppThemeMode.DARK
        }
        setThemeMode(next)
    }

    // Notification Reminders Controls
    fun setClassRemindersEnabled(enabled: Boolean) {
        _classRemindersEnabled.value = enabled
        ClassNotificationScheduler.setRemindersEnabled(getApplication(), enabled, uiState.value.timetableList)
    }

    fun dismissNotificationBanner() {
        _notificationBannerDismissed.value = true
    }

    fun sendTestClassReminder(item: TimetableItem? = null) {
        val sample = item ?: uiState.value.todaysClasses.firstOrNull() ?: uiState.value.timetableList.firstOrNull()
        ClassNotificationScheduler.sendTestNotification(getApplication(), sample)
    }

    // Quick Expense Logging
    fun quickAddExpense(title: String, amount: Double, category: String, paymentMode: String = "UPI/Online") {
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseItem(
                    title = title,
                    amount = amount,
                    category = category,
                    dateMillis = System.currentTimeMillis(),
                    paymentMode = paymentMode,
                    note = "Quick logged"
                )
            )
        }
    }

    // Timetable Actions
    fun addTimetableItem(item: TimetableItem) {
        viewModelScope.launch {
            val insertedId = repository.insertTimetableItem(item)
            if (_classRemindersEnabled.value) {
                ClassNotificationScheduler.scheduleClassReminder(
                    getApplication(),
                    item.copy(id = insertedId.toInt())
                )
            }
        }
    }

    fun updateTimetableItem(item: TimetableItem) {
        viewModelScope.launch {
            repository.updateTimetableItem(item)
            if (_classRemindersEnabled.value) {
                ClassNotificationScheduler.scheduleClassReminder(getApplication(), item)
            }
        }
    }

    fun deleteTimetableItem(item: TimetableItem) {
        viewModelScope.launch {
            repository.deleteTimetableItem(item)
            ClassNotificationScheduler.cancelClassReminder(getApplication(), item.id)
        }
    }

    // Attendance Actions
    fun addAttendanceCourse(course: AttendanceCourse) {
        viewModelScope.launch {
            repository.insertCourse(course)
        }
    }

    fun updateAttendanceCourse(course: AttendanceCourse) {
        viewModelScope.launch {
            repository.updateCourse(course)
        }
    }

    fun deleteAttendanceCourse(course: AttendanceCourse) {
        viewModelScope.launch {
            repository.deleteCourse(course)
        }
    }

    fun markAttendance(courseId: Int, status: String, note: String = "") {
        viewModelScope.launch {
            repository.markAttendance(courseId, status, note)
        }
    }

    fun updateAttendanceManualCounts(courseId: Int, attended: Int, total: Int) {
        viewModelScope.launch {
            repository.updateCourseCounts(courseId, attended, total)
        }
    }

    // Direct attendance mark for subject name matching timetable
    fun markAttendanceForSubject(subjectName: String, status: String) {
        viewModelScope.launch {
            val matchingCourse = uiState.value.attendanceCourses.firstOrNull {
                it.name.equals(subjectName, ignoreCase = true) ||
                (it.code.isNotBlank() && subjectName.contains(it.code, ignoreCase = true))
            }
            if (matchingCourse != null) {
                repository.markAttendance(matchingCourse.id, status, "Logged from Schedule")
            }
        }
    }

    // Expense Actions
    fun addExpense(expense: ExpenseItem) {
        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }

    fun updateExpense(expense: ExpenseItem) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseItem) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun updateBudget(budgetAmount: Double, currency: String) {
        viewModelScope.launch {
            repository.setBudget(budgetAmount, currency)
        }
    }

    // Essentials Actions
    fun addEssential(essential: StudentEssential) {
        viewModelScope.launch {
            repository.insertEssential(essential)
        }
    }

    fun updateEssential(essential: StudentEssential) {
        viewModelScope.launch {
            repository.updateEssential(essential)
        }
    }

    fun toggleTaskCompleted(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(id, isCompleted)
        }
    }

    fun deleteEssential(essential: StudentEssential) {
        viewModelScope.launch {
            repository.deleteEssential(essential)
        }
    }
}
