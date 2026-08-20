package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AttendanceCourse
import com.example.data.model.ExpenseItem
import com.example.data.model.StudentEssential
import com.example.data.model.TimetableItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val SubjectColorPalette = listOf(
    "#4338CA", // Indigo
    "#0D9488", // Teal
    "#D97706", // Amber
    "#059669", // Emerald
    "#7C3AED", // Violet
    "#E11D48", // Rose
    "#2563EB", // Blue
    "#DB2777"  // Pink
)

fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF4338CA)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        defaultColor
    }
}

// ------------------- TIMETABLE DIALOG -------------------
@Composable
fun AddEditTimetableDialog(
    initialItem: TimetableItem? = null,
    defaultDayOfWeek: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (TimetableItem) -> Unit
) {
    var subject by remember { mutableStateOf(initialItem?.subject ?: "") }
    var courseCode by remember { mutableStateOf(initialItem?.courseCode ?: "") }
    var professor by remember { mutableStateOf(initialItem?.professor ?: "") }
    var room by remember { mutableStateOf(initialItem?.room ?: "") }
    var dayOfWeek by remember { mutableIntStateOf(initialItem?.dayOfWeek ?: defaultDayOfWeek) }
    var startTime by remember { mutableStateOf(initialItem?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(initialItem?.endTime ?: "10:30") }
    var colorHex by remember { mutableStateOf(initialItem?.colorHex ?: SubjectColorPalette[0]) }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }

    val days = listOf("Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4, "Fri" to 5, "Sat" to 6, "Sun" to 7)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialItem == null) "Add Class to Schedule" else "Edit Class",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject Name *") },
                    placeholder = { Text("e.g. Data Structures") },
                    modifier = Modifier.fillMaxWidth().testTag("timetable_subject_input"),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Course Code") },
                        placeholder = { Text("CS-301") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room / Hall") },
                        placeholder = { Text("Room 204") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = professor,
                    onValueChange = { professor = it },
                    label = { Text("Professor Name") },
                    placeholder = { Text("Dr. Alan Vance") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Day of Week", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(days) { (name, dayIdx) ->
                        FilterChip(
                            selected = dayOfWeek == dayIdx,
                            onClick = { dayOfWeek = dayIdx },
                            label = { Text(name) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        placeholder = { Text("09:00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        placeholder = { Text("10:30") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text("Color Accent", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SubjectColorPalette.forEach { hex ->
                        val isSelected = colorHex == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .clickable { colorHex = hex }
                                .then(
                                    if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected color",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Preparation") },
                    placeholder = { Text("e.g. Bring lab manual") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank()) {
                        val item = initialItem?.copy(
                            subject = subject.trim(),
                            courseCode = courseCode.trim(),
                            professor = professor.trim(),
                            room = room.trim(),
                            dayOfWeek = dayOfWeek,
                            startTime = startTime.trim(),
                            endTime = endTime.trim(),
                            colorHex = colorHex,
                            notes = notes.trim()
                        ) ?: TimetableItem(
                            subject = subject.trim(),
                            courseCode = courseCode.trim(),
                            professor = professor.trim(),
                            room = room.trim(),
                            dayOfWeek = dayOfWeek,
                            startTime = startTime.trim(),
                            endTime = endTime.trim(),
                            colorHex = colorHex,
                            notes = notes.trim()
                        )
                        onConfirm(item)
                    }
                },
                modifier = Modifier.testTag("save_timetable_button")
            ) {
                Text(if (initialItem == null) "Add Class" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ------------------- ATTENDANCE COURSE DIALOG -------------------
@Composable
fun AddEditCourseDialog(
    initialCourse: AttendanceCourse? = null,
    onDismiss: () -> Unit,
    onConfirm: (AttendanceCourse) -> Unit
) {
    var name by remember { mutableStateOf(initialCourse?.name ?: "") }
    var code by remember { mutableStateOf(initialCourse?.code ?: "") }
    var professor by remember { mutableStateOf(initialCourse?.professor ?: "") }
    var attended by remember { mutableIntStateOf(initialCourse?.attendedClasses ?: 0) }
    var total by remember { mutableIntStateOf(initialCourse?.totalClasses ?: 0) }
    var target by remember { mutableIntStateOf(initialCourse?.targetPercentage ?: 75) }
    var colorHex by remember { mutableStateOf(initialCourse?.colorHex ?: SubjectColorPalette[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialCourse == null) "Add Course Attendance" else "Edit Course Attendance",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course Name *") },
                    placeholder = { Text("e.g. Operating Systems") },
                    modifier = Modifier.fillMaxWidth().testTag("course_name_input"),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Course Code") },
                        placeholder = { Text("CS-302") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = professor,
                        onValueChange = { professor = it },
                        label = { Text("Professor") },
                        placeholder = { Text("Dr. Kim") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text("Starting Record (Optional)", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = attended.toString(),
                        onValueChange = { attended = it.toIntOrNull() ?: 0 },
                        label = { Text("Attended") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = total.toString(),
                        onValueChange = { total = it.toIntOrNull() ?: 0 },
                        label = { Text("Total Conducted") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    "Target Attendance Criteria: $target%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = target.toFloat(),
                    onValueChange = { target = it.toInt() },
                    valueRange = 50f..95f,
                    steps = 8
                )

                Text("Subject Color", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SubjectColorPalette.forEach { hex ->
                        val isSelected = colorHex == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .clickable { colorHex = hex }
                                .then(
                                    if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected color",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val validTotal = total.coerceAtLeast(attended)
                        val course = initialCourse?.copy(
                            name = name.trim(),
                            code = code.trim(),
                            professor = professor.trim(),
                            attendedClasses = attended,
                            totalClasses = validTotal,
                            targetPercentage = target,
                            colorHex = colorHex
                        ) ?: AttendanceCourse(
                            name = name.trim(),
                            code = code.trim(),
                            professor = professor.trim(),
                            attendedClasses = attended,
                            totalClasses = validTotal,
                            targetPercentage = target,
                            colorHex = colorHex
                        )
                        onConfirm(course)
                    }
                },
                modifier = Modifier.testTag("save_course_button")
            ) {
                Text(if (initialCourse == null) "Add Course" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ------------------- MANUAL ATTENDANCE COUNTS EDIT -------------------
@Composable
fun EditAttendanceCountsDialog(
    course: AttendanceCourse,
    onDismiss: () -> Unit,
    onSave: (attended: Int, total: Int) -> Unit
) {
    var attended by remember { mutableIntStateOf(course.attendedClasses) }
    var total by remember { mutableIntStateOf(course.totalClasses) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Class Counts - ${course.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Attended Classes", fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (attended > 0) attended-- }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Minus")
                        }
                        Text("$attended", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { attended++; if (total < attended) total = attended }) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Plus")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Conducted", fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (total > attended) total-- }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Minus")
                        }
                        Text("$total", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { total++ }) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Plus")
                        }
                    }
                }

                val currentPercent = if (total > 0) (attended.toFloat() / total * 100f) else 100f
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Calculated Attendance: ${String.format(Locale.getDefault(), "%.1f", currentPercent)}% (Target: ${course.targetPercentage}%)",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(attended, total) }) {
                Text("Save Counts")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ------------------- ADD EXPENSE DIALOG -------------------
val ExpenseCategories = listOf(
    "Food & Canteen",
    "Books & Study",
    "Transport",
    "Bills & Fees",
    "Snacks & Drinks",
    "Entertainment",
    "Other"
)

val PaymentModes = listOf("UPI/Online", "Cash", "Card")

@Composable
fun AddExpenseDialog(
    currencySymbol: String = "$",
    onDismiss: () -> Unit,
    onConfirm: (ExpenseItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategories[0]) }
    var selectedPaymentMode by remember { mutableStateOf(PaymentModes[0]) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Student Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($currencySymbol) *") },
                    placeholder = { Text("15.50") },
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
                    singleLine = true,
                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Description / Item *") },
                    placeholder = { Text("e.g. Canteen Lunch, Metro Pass") },
                    modifier = Modifier.fillMaxWidth().testTag("expense_title_input"),
                    singleLine = true
                )

                Text("Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ExpenseCategories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Text("Payment Mode", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentModes.forEach { mode ->
                        FilterChip(
                            selected = selectedPaymentMode == mode,
                            onClick = { selectedPaymentMode = mode },
                            label = { Text(mode) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Optional Note") },
                    placeholder = { Text("e.g. Shared with roommates") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0 && title.isNotBlank()) {
                        onConfirm(
                            ExpenseItem(
                                title = title.trim(),
                                amount = amount,
                                category = selectedCategory,
                                paymentMode = selectedPaymentMode,
                                note = note.trim(),
                                dateMillis = System.currentTimeMillis()
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_expense_button")
            ) {
                Text("Add Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ------------------- EDIT BUDGET DIALOG -------------------
@Composable
fun EditBudgetDialog(
    currentBudget: Double,
    currentCurrency: String,
    onDismiss: () -> Unit,
    onConfirm: (budget: Double, currency: String) -> Unit
) {
    var budgetStr by remember { mutableStateOf(currentBudget.toString()) }
    var currency by remember { mutableStateOf(currentCurrency) }

    val currencies = listOf("$", "₹", "€", "£", "¥", "C$")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Monthly Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Currency", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    currencies.forEach { cur ->
                        FilterChip(
                            selected = currency == cur,
                            onClick = { currency = cur },
                            label = { Text(cur, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    label = { Text("Monthly Budget Limit") },
                    placeholder = { Text("500") },
                    modifier = Modifier.fillMaxWidth().testTag("budget_amount_input"),
                    singleLine = true,
                    leadingIcon = { Text(currency, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val budget = budgetStr.toDoubleOrNull() ?: currentBudget
                onConfirm(budget, currency)
            }) {
                Text("Save Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ------------------- ADD ESSENTIAL DIALOG -------------------
@Composable
fun AddEssentialDialog(
    onDismiss: () -> Unit,
    onConfirm: (StudentEssential) -> Unit
) {
    var type by remember { mutableStateOf("TASK") } // "TASK", "EXAM", "NOTE", "KEY_INFO"
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var tag by remember { mutableStateOf("") }

    val types = listOf("TASK" to "Task", "EXAM" to "Exam", "NOTE" to "Note", "KEY_INFO" to "Cheat Sheet Info")
    val priorities = listOf("HIGH" to "High", "MEDIUM" to "Medium", "LOW" to "Low")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Student Essential", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Item Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.forEach { (tKey, tLabel) ->
                        FilterChip(
                            selected = type == tKey,
                            onClick = { type = tKey },
                            label = { Text(tLabel) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (type == "EXAM") "Exam Subject *" else "Title *") },
                    placeholder = {
                        Text(
                            when (type) {
                                "EXAM" -> "e.g. Midterm: Computer Networks"
                                "TASK" -> "e.g. Submit DB Lab Assignment"
                                "NOTE" -> "e.g. Professor Office Hours"
                                else -> "e.g. Wi-Fi Password / Roll Number"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("essential_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Details & Description") },
                    placeholder = { Text("e.g. Chapters 1-4, Room 105, 10 AM") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                if (type == "TASK" || type == "EXAM") {
                    Text("Priority", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        priorities.forEach { (pKey, pLabel) ->
                            FilterChip(
                                selected = priority == pKey,
                                onClick = { priority = pKey },
                                label = { Text(pLabel) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Tag / Location / Badge") },
                    placeholder = { Text("e.g. Lab 4, Hall Ticket, WiFi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            StudentEssential(
                                type = type,
                                title = title.trim(),
                                detail = detail.trim(),
                                priority = priority,
                                extraTag = tag.trim(),
                                dueOrEventDateMillis = System.currentTimeMillis() + (86400000L * 3)
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_essential_button")
            ) {
                Text("Add Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
