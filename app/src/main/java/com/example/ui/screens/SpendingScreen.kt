package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.ExpenseItem
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.EditBudgetDialog
import com.example.ui.components.ExpenseCategories
import com.example.ui.components.InteractiveSpendingChart
import com.example.ui.viewmodel.CategorySpending
import com.example.ui.viewmodel.GraphPeriod
import com.example.ui.viewmodel.PaymentModeSpending
import com.example.ui.viewmodel.SpendingTimeframe
import com.example.ui.viewmodel.StudentHubUiState
import com.example.ui.viewmodel.StudentHubViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpendingScreen(
    uiState: StudentHubUiState,
    viewModel: StudentHubViewModel,
    modifier: Modifier = Modifier
) {
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showEditBudgetDialog by remember { mutableStateOf(false) }

    val currency = uiState.budgetSetting.currencySymbol
    val budget = uiState.budgetSetting.monthlyBudget
    val spent = uiState.monthlyTotalSpending
    val remaining = budget - spent
    val budgetFraction = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 1f

    val displayedExpenses = uiState.filteredExpenses
    val currentDataPoints = if (uiState.graphPeriod == GraphPeriod.THIS_WEEK) {
        uiState.weeklyDailySpendings
    } else {
        uiState.monthlyDailySpendings
    }

    Box(modifier = modifier.fillMaxSize().testTag("spending_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Money Spent Summary Card & Budget Overview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Monthly Budget & Spent",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Current Month Financial Health",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalButton(
                                onClick = { showEditBudgetDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Budget", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("Total Spent This Month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$currency${String.format(Locale.getDefault(), "%.2f", spent)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (spent > budget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Monthly Budget", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$currency${String.format(Locale.getDefault(), "%.2f", budget)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Budget Progress Bar
                        LinearProgressIndicator(
                            progress = { budgetFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (spent > budget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (remaining >= 0) "Remaining: $currency${String.format(Locale.getDefault(), "%.2f", remaining)}"
                                else "Over budget by: $currency${String.format(Locale.getDefault(), "%.2f", -remaining)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                            )

                            Text(
                                text = "${String.format(Locale.getDefault(), "%.0f", budgetFraction * 100)}% used",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Money Spent Breakdown Across Periods
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Money Spent Breakdown",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MoneySpentMetricTile(
                                title = "Today",
                                amount = "$currency${String.format(Locale.getDefault(), "%.2f", uiState.todayTotalSpending)}",
                                modifier = Modifier.weight(1f)
                            )
                            MoneySpentMetricTile(
                                title = "This Week",
                                amount = "$currency${String.format(Locale.getDefault(), "%.2f", uiState.weeklyTotalSpending)}",
                                modifier = Modifier.weight(1f)
                            )
                            MoneySpentMetricTile(
                                title = "All Time",
                                amount = "$currency${String.format(Locale.getDefault(), "%.2f", uiState.allTimeTotalSpending)}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Safe Daily Spending Allowance Insight
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Daily Safe Allowance",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$currency${String.format(Locale.getDefault(), "%.2f", uiState.dailySafeAllowance)} / day",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.dailySafeAllowance > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Daily Average",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$currency${String.format(Locale.getDefault(), "%.2f", uiState.dailyAverageSpending)} / day",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick 1-Tap Money Spent Logger
            item {
                Column {
                    Text(
                        text = "Quick Spend Log",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            QuickSpendChip(
                                icon = Icons.Default.Fastfood,
                                title = "Meal",
                                price = "$currency 3.50",
                                onClick = { viewModel.quickAddExpense("Canteen Meal", 3.50, "Food & Canteen") }
                            )
                        }
                        item {
                            QuickSpendChip(
                                icon = Icons.Default.LocalCafe,
                                title = "Coffee",
                                price = "$currency 1.50",
                                onClick = { viewModel.quickAddExpense("Coffee / Tea", 1.50, "Snacks & Drinks") }
                            )
                        }
                        item {
                            QuickSpendChip(
                                icon = Icons.Default.DirectionsBus,
                                title = "Transit",
                                price = "$currency 2.00",
                                onClick = { viewModel.quickAddExpense("Bus / Transit Fare", 2.00, "Transport") }
                            )
                        }
                        item {
                            QuickSpendChip(
                                icon = Icons.Default.Print,
                                title = "Printouts",
                                price = "$currency 1.00",
                                onClick = { viewModel.quickAddExpense("Photocopy / Printouts", 1.00, "Books & Study") }
                            )
                        }
                        item {
                            QuickSpendChip(
                                icon = Icons.Default.Cookie,
                                title = "Snack",
                                price = "$currency 2.50",
                                onClick = { viewModel.quickAddExpense("Snack Bar", 2.50, "Snacks & Drinks") }
                            )
                        }
                    }
                }
            }

            // Interactive Spending Graph
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spending Timeline Chart",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = uiState.graphPeriod == GraphPeriod.THIS_WEEK,
                            onClick = { viewModel.setGraphPeriod(GraphPeriod.THIS_WEEK) },
                            label = { Text("Week", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = uiState.graphPeriod == GraphPeriod.THIS_MONTH,
                            onClick = { viewModel.setGraphPeriod(GraphPeriod.THIS_MONTH) },
                            label = { Text("Month", fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Custom Interactive Spending Canvas Chart
            item {
                InteractiveSpendingChart(
                    dataPoints = currentDataPoints,
                    currencySymbol = currency,
                    selectedIndex = uiState.selectedGraphBarIndex,
                    onBarSelected = { viewModel.selectGraphBar(it) }
                )
            }

            // Category Breakdown Section
            val categories = uiState.categoryBreakdown
            if (categories.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Category-wise Expense Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            categories.forEach { cat ->
                                CategoryProgressRow(
                                    item = cat,
                                    currency = currency,
                                    onCategoryClick = {
                                        if (uiState.selectedExpenseCategory == cat.category) {
                                            viewModel.selectExpenseCategory(null)
                                        } else {
                                            viewModel.selectExpenseCategory(cat.category)
                                        }
                                    },
                                    isSelected = uiState.selectedExpenseCategory == cat.category
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Payment Mode Breakdown
            val paymentModes = uiState.paymentModeBreakdown
            if (paymentModes.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Payment Method Breakdown",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                paymentModes.forEach { mode ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = mode.mode,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "$currency${String.format(Locale.getDefault(), "%.0f", mode.totalAmount)}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${String.format(Locale.getDefault(), "%.0f", mode.percentage)}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expense Transactions Section with Timeframe & Category Filters
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Money Spent Records",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.selectedExpenseCategory != null) {
                            TextButton(onClick = { viewModel.selectExpenseCategory(null) }) {
                                Text("Clear Category", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Timeframe filter row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SpendingTimeframe.entries.forEach { tf ->
                            FilterChip(
                                selected = uiState.spendingTimeframe == tf,
                                onClick = { viewModel.setSpendingTimeframe(tf) },
                                label = { Text(tf.label, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Category Filter chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedExpenseCategory == null,
                                onClick = { viewModel.selectExpenseCategory(null) },
                                label = { Text("All Categories") }
                            )
                        }
                        items(ExpenseCategories) { cat ->
                            FilterChip(
                                selected = uiState.selectedExpenseCategory == cat,
                                onClick = { viewModel.selectExpenseCategory(cat) },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            }

            if (displayedExpenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No spending transactions found for selected timeframe",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(displayedExpenses, key = { it.id }) { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        currency = currency,
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Add Expense FAB
        FloatingActionButton(
            onClick = { showAddExpenseDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_expense_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense")
        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            currencySymbol = currency,
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { newExpense ->
                viewModel.addExpense(newExpense)
                showAddExpenseDialog = false
            }
        )
    }

    if (showEditBudgetDialog) {
        EditBudgetDialog(
            currentBudget = budget,
            currentCurrency = currency,
            onDismiss = { showEditBudgetDialog = false },
            onConfirm = { newBudget, newCurrency ->
                viewModel.updateBudget(newBudget, newCurrency)
                showEditBudgetDialog = false
            }
        )
    }
}

@Composable
private fun MoneySpentMetricTile(
    title: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QuickSpendChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    price: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = price,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryProgressRow(
    item: CategorySpending,
    currency: String,
    onCategoryClick: () -> Unit,
    isSelected: Boolean
) {
    val categoryIcon = when (item.category) {
        "Food & Canteen" -> Icons.Default.Fastfood
        "Books & Study" -> Icons.Default.MenuBook
        "Transport" -> Icons.Default.DirectionsBus
        "Bills & Fees" -> Icons.Default.ReceiptLong
        "Snacks & Drinks" -> Icons.Default.LocalCafe
        "Entertainment" -> Icons.Default.TheaterComedy
        else -> Icons.Default.ShoppingCart
    }

    val categoryColor = when (item.category) {
        "Food & Canteen" -> Color(0xFFE11D48)
        "Books & Study" -> Color(0xFF4338CA)
        "Transport" -> Color(0xFF0D9488)
        "Bills & Fees" -> Color(0xFFD97706)
        "Snacks & Drinks" -> Color(0xFFEC4899)
        "Entertainment" -> Color(0xFF8B5CF6)
        else -> Color(0xFF64748B)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCategoryClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent
    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        categoryIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${item.count})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$currency${String.format(Locale.getDefault(), "%.2f", item.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.0f", item.percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = categoryColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun ExpenseItemCard(
    expense: ExpenseItem,
    currency: String,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val dateStr = remember(expense.dateMillis) { dateFormat.format(Date(expense.dateMillis)) }

    val categoryIcon = when (expense.category) {
        "Food & Canteen" -> Icons.Default.Fastfood
        "Books & Study" -> Icons.Default.MenuBook
        "Transport" -> Icons.Default.DirectionsBus
        "Bills & Fees" -> Icons.Default.ReceiptLong
        "Snacks & Drinks" -> Icons.Default.LocalCafe
        "Entertainment" -> Icons.Default.TheaterComedy
        else -> Icons.Default.ShoppingCart
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_card_${expense.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        categoryIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = expense.paymentMode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (expense.note.isNotBlank()) {
                    Text(
                        text = expense.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currency${String.format(Locale.getDefault(), "%.2f", expense.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete Expense",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
