package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceCourse
import com.example.data.model.TimetableItem
import com.example.ui.components.InteractiveSpendingChart
import com.example.ui.components.parseHexColor
import com.example.ui.theme.AppThemeMode
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.StudentHubUiState
import com.example.ui.viewmodel.StudentHubViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: StudentHubUiState,
    viewModel: StudentHubViewModel,
    onNavigateToTab: (AppTab) -> Unit,
    onOpenAddClass: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenAddCourse: () -> Unit,
    onOpenAddEssential: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()) }
    val todayDateStr = remember { dateFormat.format(Date()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Hero Card with Student Academic Greeting
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Student Hub",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = todayDateStr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = "Student Icon",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Summary Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickMetricBadge(
                            label = "Attendance",
                            value = "${String.format(Locale.getDefault(), "%.0f", uiState.overallAttendancePercentage)}%",
                            isAlert = uiState.coursesNeedingAttentionCount > 0,
                            modifier = Modifier.weight(1f)
                        )
                        QuickMetricBadge(
                            label = "Today's Classes",
                            value = "${uiState.todaysClasses.size} Active",
                            isAlert = false,
                            modifier = Modifier.weight(1f)
                        )
                        QuickMetricBadge(
                            label = "Month Spent",
                            value = "${uiState.budgetSetting.currencySymbol}${String.format(Locale.getDefault(), "%.0f", uiState.monthlyTotalSpending)}",
                            isAlert = uiState.monthlyTotalSpending > uiState.budgetSetting.monthlyBudget,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "+ Class",
                    onClick = onOpenAddClass,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.AttachMoney,
                    label = "+ Expense",
                    onClick = onOpenAddExpense,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.HowToReg,
                    label = "+ Course",
                    onClick = onOpenAddCourse,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.NoteAdd,
                    label = "+ Task",
                    onClick = onOpenAddEssential,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Late-Night Study Mode Quick Toggle Bar
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_study_theme_mode")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (uiState.themeMode == AppThemeMode.DARK)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (uiState.themeMode == AppThemeMode.DARK) Icons.Default.Bedtime else Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = if (uiState.themeMode == AppThemeMode.DARK) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Late-Night Study Mode",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.themeMode == AppThemeMode.DARK)
                                    "Dark mode active — reduced glare & eye strain"
                                else
                                    "Switch to dark mode for late study sessions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.padding(start = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.themeMode == AppThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                            label = { Text("Light", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(13.dp))
                            },
                            modifier = Modifier.testTag("chip_theme_light")
                        )
                        FilterChip(
                            selected = uiState.themeMode == AppThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                            label = { Text("Dark", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(13.dp))
                            },
                            modifier = Modifier.testTag("chip_theme_dark")
                        )
                    }
                }
            }
        }

        // Today's Classes & Live Attendance Marker Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Class Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateToTab(AppTab.TIMETABLE) }) {
                    Text("View Full Timetable", fontSize = 13.sp)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (uiState.todaysClasses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Weekend,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("No classes scheduled for today!", fontWeight = FontWeight.SemiBold)
                            Text("Take some time to revise or relax.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else {
            items(uiState.todaysClasses) { classItem ->
                TodayClassCard(
                    item = classItem,
                    onMarkAttendance = { status ->
                        viewModel.markAttendanceForSubject(classItem.subject, status)
                    }
                )
            }
        }

        // Spending Summary & Interactive Graph Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending & Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateToTab(AppTab.SPENDING) }) {
                    Text("Full Analytics", fontSize = 13.sp)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        item {
            InteractiveSpendingChart(
                dataPoints = uiState.weeklyDailySpendings,
                currencySymbol = uiState.budgetSetting.currencySymbol,
                selectedIndex = uiState.selectedGraphBarIndex,
                onBarSelected = { viewModel.selectGraphBar(it) }
            )
        }

        // Attendance Quick Health Check
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance Health Check",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateToTab(AppTab.ATTENDANCE) }) {
                    Text("Manage All", fontSize = 13.sp)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (uiState.attendanceCourses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        "No courses added yet. Tap '+ Course' to start tracking attendance!",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(uiState.attendanceCourses.take(3)) { course ->
                AttendanceGlanceCard(
                    course = course,
                    onQuickMark = { status -> viewModel.markAttendance(course.id, status) }
                )
            }
        }

        // Urgent Essentials (Exams & High Priority Tasks)
        val urgentItems = uiState.essentials.filter { !it.isCompleted }.take(3)
        if (urgentItems.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Essentials & Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { onNavigateToTab(AppTab.ESSENTIALS) }) {
                        Text("View All", fontSize = 13.sp)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            items(urgentItems) { essential ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (essential.type == "TASK") {
                            Checkbox(
                                checked = essential.isCompleted,
                                onCheckedChange = { viewModel.toggleTaskCompleted(essential.id, it) }
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = if (essential.type == "EXAM") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (essential.type == "EXAM") Icons.Default.EventNote else Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (essential.type == "EXAM") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = essential.title,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (essential.detail.isNotBlank()) {
                                Text(
                                    text = essential.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (essential.extraTag.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = essential.extraTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun QuickMetricBadge(
    label: String,
    value: String,
    isAlert: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = if (isAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (isAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun TodayClassCard(
    item: TimetableItem,
    onMarkAttendance: (status: String) -> Unit
) {
    val barColor = parseHexColor(item.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_class_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator pill
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.subject,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.courseCode.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = item.courseCode,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.startTime} - ${item.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.room.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Room,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = item.room,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = "15m Alert Active",
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Quick Mark Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalIconButton(
                    onClick = { onMarkAttendance("PRESENT") },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Mark Present", modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = { onMarkAttendance("ABSENT") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Mark Absent",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceGlanceCard(
    course: AttendanceCourse,
    onQuickMark: (status: String) -> Unit
) {
    val percent = course.percentage
    val isGood = percent >= course.targetPercentage
    val barColor = parseHexColor(course.colorHex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isGood) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${String.format(Locale.getDefault(), "%.0f", percent)}%",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = if (isGood) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(course.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = "${course.attendedClasses}/${course.totalClasses} Classes • Target: ${course.targetPercentage}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalButton(
                    onClick = { onQuickMark("PRESENT") },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+1 Pres", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
