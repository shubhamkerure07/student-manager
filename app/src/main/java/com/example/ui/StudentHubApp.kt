package com.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.AppThemeMode
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.StudentHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHubApp(
    viewModel: StudentHubViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()

    var showAddClassDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddEssentialDialog by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    val isDarkEffective = when (uiState.themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (uiState.currentTab) {
                                AppTab.OVERVIEW -> "Student Hub"
                                AppTab.TIMETABLE -> "Class Schedule & Timetable"
                                AppTab.ATTENDANCE -> "Attendance Manager"
                                AppTab.SPENDING -> "Student Expenses & Budget"
                                AppTab.ESSENTIALS -> "Student Essentials & Tasks"
                            },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = when (uiState.currentTab) {
                                AppTab.OVERVIEW -> "All-in-one student workspace"
                                AppTab.TIMETABLE -> "Weekly schedule & room allocation"
                                AppTab.ATTENDANCE -> "Safe bunks & criteria tracker"
                                AppTab.SPENDING -> "Interactive graphs & category stats"
                                AppTab.ESSENTIALS -> "Assignments, exams & notes"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Theme Switcher Quick Toggle & Dropdown Menu
                    Box {
                        IconButton(
                            onClick = { viewModel.toggleThemeMode(isSystemDark) },
                            modifier = Modifier.testTag("btn_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDarkEffective) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkEffective) "Switch to Light Mode" else "Switch to Dark / Study Mode",
                                tint = if (isDarkEffective) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showThemeMenu = true },
                            modifier = Modifier.testTag("btn_theme_menu")
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Theme and display options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("Late-Night Study (Dark)", fontWeight = if (uiState.themeMode == AppThemeMode.DARK) FontWeight.Bold else FontWeight.Normal)
                                        Text("Eye-friendly dark theme for late sessions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = if (uiState.themeMode == AppThemeMode.DARK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (uiState.themeMode == AppThemeMode.DARK) {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    viewModel.setThemeMode(AppThemeMode.DARK)
                                    showThemeMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("Daytime Focus (Light)", fontWeight = if (uiState.themeMode == AppThemeMode.LIGHT) FontWeight.Bold else FontWeight.Normal)
                                        Text("Bright, crisp layout for day study", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = if (uiState.themeMode == AppThemeMode.LIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (uiState.themeMode == AppThemeMode.LIGHT) {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    viewModel.setThemeMode(AppThemeMode.LIGHT)
                                    showThemeMenu = false
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("System Default", fontWeight = if (uiState.themeMode == AppThemeMode.SYSTEM) FontWeight.Bold else FontWeight.Normal)
                                        Text("Follow device system theme automatically", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.BrightnessAuto,
                                        contentDescription = null,
                                        tint = if (uiState.themeMode == AppThemeMode.SYSTEM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (uiState.themeMode == AppThemeMode.SYSTEM) {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    viewModel.setThemeMode(AppThemeMode.SYSTEM)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == AppTab.OVERVIEW,
                    onClick = { viewModel.selectTab(AppTab.OVERVIEW) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                    label = { Text("Overview") },
                    modifier = Modifier.testTag("nav_overview")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == AppTab.TIMETABLE,
                    onClick = { viewModel.selectTab(AppTab.TIMETABLE) },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Timetable") },
                    label = { Text("Timetable") },
                    modifier = Modifier.testTag("nav_timetable")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == AppTab.ATTENDANCE,
                    onClick = { viewModel.selectTab(AppTab.ATTENDANCE) },
                    icon = { Icon(Icons.Default.FactCheck, contentDescription = "Attendance") },
                    label = { Text("Attendance") },
                    modifier = Modifier.testTag("nav_attendance")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == AppTab.SPENDING,
                    onClick = { viewModel.selectTab(AppTab.SPENDING) },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Spending") },
                    label = { Text("Spending") },
                    modifier = Modifier.testTag("nav_spending")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == AppTab.ESSENTIALS,
                    onClick = { viewModel.selectTab(AppTab.ESSENTIALS) },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = "Essentials") },
                    label = { Text("Essentials") },
                    modifier = Modifier.testTag("nav_essentials")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                AppTab.OVERVIEW -> DashboardScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onNavigateToTab = { viewModel.selectTab(it) },
                    onOpenAddClass = { showAddClassDialog = true },
                    onOpenAddExpense = { showAddExpenseDialog = true },
                    onOpenAddCourse = { showAddCourseDialog = true },
                    onOpenAddEssential = { showAddEssentialDialog = true }
                )

                AppTab.TIMETABLE -> TimetableScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )

                AppTab.ATTENDANCE -> AttendanceScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )

                AppTab.SPENDING -> SpendingScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )

                AppTab.ESSENTIALS -> EssentialsScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }

    // Global dialogs accessible from quick actions
    if (showAddClassDialog) {
        AddEditTimetableDialog(
            defaultDayOfWeek = uiState.selectedDayOfWeek,
            onDismiss = { showAddClassDialog = false },
            onConfirm = { newItem ->
                viewModel.addTimetableItem(newItem)
                showAddClassDialog = false
            }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            currencySymbol = uiState.budgetSetting.currencySymbol,
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { newExpense ->
                viewModel.addExpense(newExpense)
                showAddExpenseDialog = false
            }
        )
    }

    if (showAddCourseDialog) {
        AddEditCourseDialog(
            onDismiss = { showAddCourseDialog = false },
            onConfirm = { newCourse ->
                viewModel.addAttendanceCourse(newCourse)
                showAddCourseDialog = false
            }
        )
    }

    if (showAddEssentialDialog) {
        AddEssentialDialog(
            onDismiss = { showAddEssentialDialog = false },
            onConfirm = { newEssential ->
                viewModel.addEssential(newEssential)
                showAddEssentialDialog = false
            }
        )
    }
}
