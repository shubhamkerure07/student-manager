package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.ui.components.AddEditCourseDialog
import com.example.ui.components.EditAttendanceCountsDialog
import com.example.ui.components.parseHexColor
import com.example.ui.viewmodel.StudentHubUiState
import com.example.ui.viewmodel.StudentHubViewModel
import java.util.Locale

@Composable
fun AttendanceScreen(
    uiState: StudentHubUiState,
    viewModel: StudentHubViewModel,
    modifier: Modifier = Modifier
) {
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var editingCourse by remember { mutableStateOf<AttendanceCourse?>(null) }
    var countEditingCourse by remember { mutableStateOf<AttendanceCourse?>(null) }

    Box(modifier = modifier.fillMaxSize().testTag("attendance_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Attendance Summary Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular Gauge
                        val animatedPercent by animateFloatAsState(
                            targetValue = (uiState.overallAttendancePercentage / 100f).coerceIn(0f, 1f),
                            label = "overallGauge"
                        )

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                strokeWidth = 8.dp
                            )
                            CircularProgressIndicator(
                                progress = { animatedPercent },
                                modifier = Modifier.fillMaxSize(),
                                color = if (uiState.overallAttendancePercentage >= 75f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                strokeWidth = 8.dp
                            )
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.0f", uiState.overallAttendancePercentage)}%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Text(
                                text = "Overall Attendance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val totalAttended = uiState.attendanceCourses.sumOf { it.attendedClasses }
                            val totalConducted = uiState.attendanceCourses.sumOf { it.totalClasses }
                            Text(
                                text = "$totalAttended attended out of $totalConducted total classes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val warningCount = uiState.coursesNeedingAttentionCount
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (warningCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = if (warningCount > 0) "⚠️ $warningCount course(s) below target" else "✓ All courses in safe zone",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Subject Cards List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Course Attendance Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.attendanceCourses.size} Enrolled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState.attendanceCourses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.FactCheck,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Courses Added Yet", fontWeight = FontWeight.Bold)
                            Text(
                                "Add your subjects with required attendance criteria to track safe bunks and alerts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(uiState.attendanceCourses, key = { it.id }) { course ->
                    AttendanceCourseCard(
                        course = course,
                        onMarkPresent = { viewModel.markAttendance(course.id, "PRESENT") },
                        onMarkAbsent = { viewModel.markAttendance(course.id, "ABSENT") },
                        onMarkCancelled = { viewModel.markAttendance(course.id, "CANCELLED") },
                        onEditCounts = { countEditingCourse = course },
                        onEditCourse = { editingCourse = course },
                        onDeleteCourse = { viewModel.deleteAttendanceCourse(course) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Add Course FAB
        FloatingActionButton(
            onClick = { showAddCourseDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_course_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Course")
        }
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

    if (editingCourse != null) {
        AddEditCourseDialog(
            initialCourse = editingCourse,
            onDismiss = { editingCourse = null },
            onConfirm = { updated ->
                viewModel.updateAttendanceCourse(updated)
                editingCourse = null
            }
        )
    }

    if (countEditingCourse != null) {
        EditAttendanceCountsDialog(
            course = countEditingCourse!!,
            onDismiss = { countEditingCourse = null },
            onSave = { attended, total ->
                viewModel.updateAttendanceManualCounts(countEditingCourse!!.id, attended, total)
                countEditingCourse = null
            }
        )
    }
}

@Composable
private fun AttendanceCourseCard(
    course: AttendanceCourse,
    onMarkPresent: () -> Unit,
    onMarkAbsent: () -> Unit,
    onMarkCancelled: () -> Unit,
    onEditCounts: () -> Unit,
    onEditCourse: () -> Unit,
    onDeleteCourse: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val percentage = course.percentage
    val isSafe = percentage >= course.targetPercentage
    val barColor = parseHexColor(course.colorHex)

    val progressFraction = if (course.totalClasses > 0) (course.attendedClasses.toFloat() / course.totalClasses).coerceIn(0f, 1f) else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_course_${course.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(barColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (course.code.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = course.code,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit Count Numbers") },
                            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                            onClick = { showMenu = false; onEditCounts() }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Course Details") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = { showMenu = false; onEditCourse() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Course", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDeleteCourse() }
                        )
                    }
                }
            }

            if (course.professor.isNotBlank()) {
                Text(
                    text = "Instructor: ${course.professor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Percentage & Progress Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", percentage)}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSafe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Target: ${course.targetPercentage}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${course.attendedClasses} / ${course.totalClasses} Attended",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isSafe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Safe Bunks vs Attendance Advice Alert
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSafe) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isSafe) Icons.Default.CheckCircleOutline else Icons.Default.WarningAmber,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSafe) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSafe) {
                            val safe = course.safeBunks
                            if (safe > 0) "Safe zone: You can skip next $safe class(es) & stay above ${course.targetPercentage}%"
                            else "On edge! Next class attendance is critical to stay above ${course.targetPercentage}%"
                        } else {
                            val needed = course.neededToReachTarget
                            "Shortage: Attend next $needed consecutive class(es) to reach ${course.targetPercentage}%"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSafe) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Log Actions Row (+ Present, + Absent, + Cancelled)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onMarkPresent,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Present", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onMarkAbsent,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Absent", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onMarkCancelled,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("Cancelled", fontSize = 11.sp)
                }
            }
        }
    }
}
