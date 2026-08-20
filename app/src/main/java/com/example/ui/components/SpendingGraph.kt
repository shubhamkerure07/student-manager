package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.DailySpending
import java.util.Locale

@Composable
fun InteractiveSpendingChart(
    dataPoints: List<DailySpending>,
    currencySymbol: String,
    selectedIndex: Int?,
    onBarSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No spending recorded in this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val maxAmount = remember(dataPoints) {
        val max = dataPoints.maxOfOrNull { it.amount } ?: 1.0
        if (max <= 0.0) 10.0 else max * 1.25 // Add 25% top headroom for labels
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Animation progress for chart loading
    var animationPlayed by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "chartAnimation"
    )
    LaunchedEffect(dataPoints) {
        animationPlayed = true
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_spending_chart"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Selected info or summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spending Trend & Volume",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (selectedIndex != null && selectedIndex in dataPoints.indices) {
                            val sel = dataPoints[selectedIndex]
                            "Selected: ${sel.dayLabel} • $currencySymbol${String.format(Locale.getDefault(), "%.2f", sel.amount)}"
                        } else {
                            "Tap any bar to inspect daily total"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedIndex != null) MaterialTheme.colorScheme.primary else onSurfaceVariantColor,
                        fontWeight = if (selectedIndex != null) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                if (selectedIndex != null) {
                    TextButton(onClick = { onBarSelected(null) }) {
                        Text("Reset", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dataPoints) {
                            detectTapGestures { offset ->
                                val count = dataPoints.size
                                val barSpacing = size.width / count
                                val tappedIndex = (offset.x / barSpacing).toInt().coerceIn(0, count - 1)
                                if (selectedIndex == tappedIndex) {
                                    onBarSelected(null)
                                } else {
                                    onBarSelected(tappedIndex)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 32.dp.toPx()
                    val topPadding = 20.dp.toPx()
                    val chartHeight = height - bottomPadding - topPadding
                    val count = dataPoints.size
                    val slotWidth = width / count
                    val barWidth = (slotWidth * 0.48f).coerceAtMost(28.dp.toPx())

                    // Draw 3 horizontal guideline dashes
                    val gridLines = 3
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    for (i in 1..gridLines) {
                        val y = topPadding + (chartHeight * (i.toFloat() / gridLines))
                        drawLine(
                            color = onSurfaceVariantColor.copy(alpha = 0.18f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashEffect
                        )
                    }

                    // Calculate bar heights and curve points
                    val points = mutableListOf<Offset>()

                    dataPoints.forEachIndexed { index, dp ->
                        val centerX = (index * slotWidth) + (slotWidth / 2f)
                        val barHeightFraction = (dp.amount / maxAmount).toFloat().coerceIn(0f, 1f) * progress
                        val barH = (barHeightFraction * chartHeight).coerceAtLeast(4.dp.toPx())
                        val barTop = height - bottomPadding - barH
                        val isSelected = selectedIndex == index

                        points.add(Offset(centerX, barTop))

                        // Draw Bar Gradient
                        val gradientBrush = if (isSelected) {
                            Brush.verticalGradient(
                                colors = listOf(secondaryColor, primaryColor),
                                startY = barTop,
                                endY = height - bottomPadding
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.85f),
                                    primaryColor.copy(alpha = 0.35f)
                                ),
                                startY = barTop,
                                endY = height - bottomPadding
                            )
                        }

                        // Draw Bar
                        drawRoundRect(
                            brush = gradientBrush,
                            topLeft = Offset(centerX - (barWidth / 2f), barTop),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Highlight border if selected
                        if (isSelected) {
                            drawRoundRect(
                                color = secondaryColor,
                                topLeft = Offset(centerX - (barWidth / 2f) - 2.dp.toPx(), barTop - 2.dp.toPx()),
                                size = Size(barWidth + 4.dp.toPx(), barH + 4.dp.toPx()),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        // X-axis label using native android Canvas text
                        val textPaint = android.graphics.Paint().apply {
                            color = if (isSelected) primaryColor.hashCode() else onSurfaceVariantColor.hashCode()
                            textSize = if (count > 15) 9.sp.toPx() else 11.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                            isFakeBoldText = isSelected
                        }

                        // For monthly view (many items), skip some labels for clean look
                        val shouldDrawLabel = when {
                            count <= 7 -> true
                            count <= 15 -> index % 2 == 0 || index == count - 1
                            else -> index % 4 == 0 || index == count - 1
                        }

                        if (shouldDrawLabel) {
                            drawContext.canvas.nativeCanvas.drawText(
                                dp.dayLabel,
                                centerX,
                                height - 8.dp.toPx(),
                                textPaint
                            )
                        }
                    }

                    // Draw smooth connecting line trend
                    if (points.size > 1 && count <= 14) {
                        val path = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                val controlX1 = (prev.x + curr.x) / 2f
                                cubicTo(controlX1, prev.y, controlX1, curr.y, curr.x, curr.y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = secondaryColor.copy(alpha = 0.5f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // Interactive Tooltip Info Box when a day is selected
            AnimatedVisibility(
                visible = selectedIndex != null && selectedIndex in dataPoints.indices,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (selectedIndex != null && selectedIndex in dataPoints.indices) {
                    val sel = dataPoints[selectedIndex]
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Day: ${sel.dayLabel}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "$currencySymbol${String.format(Locale.getDefault(), "%.2f", sel.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
