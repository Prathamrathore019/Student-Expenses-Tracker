package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.BarChartItem
import com.example.ui.CategoryRegistry
import com.example.ui.DonutSliceItem
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.IndigoDeep
import com.example.ui.theme.IndigoMedium
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.PurpleLightContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun CategoryIconBadge(
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    bgColor: Color? = null,
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor ?: color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun CircularBudgetRing(
    progress: Float,
    percentUsed: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = Color(0xFFF1F5F9),
    primaryColor: Color = PurplePrimary,
    warningColor: Color = WarningAmber,
    dangerColor: Color = ExpenseRed
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val activeColor = when {
        percentUsed >= 100 -> dangerColor
        percentUsed >= 70 -> warningColor
        else -> primaryColor
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - stroke, size.toPx() - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress Arc
            if (animatedProgress.value > 0f) {
                drawArc(
                    color = activeColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress.value * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$percentUsed%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = TextPrimary
            )
            Text(
                text = "used",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp
                ),
                color = TextMuted
            )
        }
    }
}

@Composable
fun CustomBarChart(
    items: List<BarChartItem>,
    modifier: Modifier = Modifier,
    barColor: Color = PurplePrimary,
    maxBarColor: Color = IndigoMedium
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No expense records in this timeframe", color = TextMuted)
        }
        return
    }

    var selectedItemIndex by remember { mutableStateOf<Int?>(null) }
    val maxVal = remember(items) {
        val highest = items.maxOfOrNull { it.amount } ?: 1.0
        if (highest <= 0.0) 1.0 else highest
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Active selection indicator
        selectedItemIndex?.let { index ->
            if (index in items.indices) {
                val item = items[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PurpleLightContainer,
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = "${item.dateLabel.ifBlank { item.label }}: ${item.formattedAmount}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = PurplePrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Bars Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEachIndexed { index, item ->
                val ratio = (item.amount / maxVal).toFloat().coerceIn(0.04f, 1f)
                val isSelected = selectedItemIndex == index
                val isHighest = item.isMax

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp)
                        .clickable {
                            selectedItemIndex = if (selectedItemIndex == index) null else index
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Value on bar if highest or selected
                    if (isHighest || isSelected) {
                        Text(
                            text = item.formattedAmount,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isSelected) PurplePrimary else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Bar Pillar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (items.size > 8) 0.8f else 0.55f)
                            .fillMaxHeight(ratio)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                brush = if (isSelected || isHighest) {
                                    Brush.verticalGradient(
                                        listOf(PurplePrimary, IndigoMedium)
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            barColor.copy(alpha = 0.85f),
                                            barColor.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // X-Axis Label
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = if (items.size > 8) 9.sp else 11.sp
                        ),
                        color = if (isSelected) PurplePrimary else TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CustomDonutChart(
    slices: List<DonutSliceItem>,
    currencySymbol: String,
    totalSpent: Double,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    strokeWidth: Dp = 26.dp
) {
    if (slices.isEmpty() || totalSpent <= 0.0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No expense breakdown available", color = TextMuted)
        }
        return
    }

    var activeSlice by remember { mutableStateOf<DonutSliceItem?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(slices) {
                        detectTapGestures { offset ->
                            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                            val touchVec = offset - center
                            var angle = Math.toDegrees(
                                Math.atan2(touchVec.y.toDouble(), touchVec.x.toDouble())
                            ).toFloat()
                            if (angle < 0) angle += 360f

                            // Slices start at -90 degrees (top)
                            var adjustedAngle = angle + 90f
                            if (adjustedAngle >= 360f) adjustedAngle -= 360f

                            var accumulated = 0f
                            var found: DonutSliceItem? = null
                            for (slice in slices) {
                                val sweep = (slice.percentage / 100f) * 360f
                                if (adjustedAngle in accumulated..(accumulated + sweep)) {
                                    found = slice
                                    break
                                }
                                accumulated += sweep
                            }
                            activeSlice = if (activeSlice == found) null else found
                        }
                    }
            ) {
                val stroke = strokeWidth.toPx()
                val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)

                var currentAngle = -90f

                slices.forEach { slice ->
                    val sweepAngle = (slice.percentage / 100f) * 360f
                    val isHighlighted = activeSlice == slice
                    val sliceStroke = if (isHighlighted) stroke * 1.25f else stroke

                    drawArc(
                        color = slice.color,
                        startAngle = currentAngle + 1f, // small gap
                        sweepAngle = (sweepAngle - 2f).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = if (isHighlighted) Offset(
                            (this.size.width - arcSize.width) / 2,
                            (this.size.height - arcSize.height) / 2
                        ) else topLeft,
                        size = arcSize,
                        style = Stroke(
                            width = sliceStroke,
                            cap = StrokeCap.Round
                        )
                    )
                    currentAngle += sweepAngle
                }
            }

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (activeSlice != null) {
                    Text(
                        text = activeSlice!!.category,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = activeSlice!!.color
                    )
                    Text(
                        text = ExpenseViewModel.formatCurrency(activeSlice!!.amount, currencySymbol),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "${"%.1f".format(activeSlice!!.percentage)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                } else {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = ExpenseViewModel.formatCurrency(totalSpent, currencySymbol),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "${slices.size} Categories",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slices.chunked(2).forEach { rowSlices ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowSlices.forEach { slice ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (activeSlice == slice) slice.color.copy(alpha = 0.12f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    activeSlice = if (activeSlice == slice) null else slice
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(slice.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = slice.category,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${"%.0f".format(slice.percentage)}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                        }
                    }
                    if (rowSlices.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    currencySymbol: String,
    hideSensitive: Boolean,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val meta = remember(transaction.category) {
        CategoryRegistry.getCategoryMeta(transaction.category)
    }
    val isIncome = transaction.type.equals("income", ignoreCase = true)
    val amountPrefix = if (isIncome) "+ " else "- "
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, Color(0xFFEEF2FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge with themed background
            CategoryIconBadge(
                icon = meta.icon,
                color = meta.color,
                bgColor = meta.bgColor,
                size = 40.dp,
                iconSize = 20.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ExpenseViewModel.formatShortDate(transaction.dateMillis),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp
                        ),
                        color = TextMuted
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextMuted
                    )
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        color = TextSecondary
                    )
                    if (transaction.note.isNotBlank()) {
                        Text(
                            text = " • ${transaction.note}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount with subtle icon
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (hideSensitive) "$currencySymbol••••"
                    else "$amountPrefix${ExpenseViewModel.formatCurrency(transaction.amount, currencySymbol)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = amountColor
                )

                IconButton(
                    onClick = { onDelete(transaction.id) },
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("delete_tx_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete transaction",
                        tint = TextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
