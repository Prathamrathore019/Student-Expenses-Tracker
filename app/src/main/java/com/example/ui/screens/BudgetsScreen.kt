package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CategoryBudgetItem
import com.example.ui.ExpenseUiState
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.EditBudgetDialog
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.IndigoDeep
import com.example.ui.theme.IndigoMedium
import com.example.ui.theme.LavenderBackground
import com.example.ui.theme.PurpleLightContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleVibrant
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnDark
import com.example.ui.theme.TextOnDarkSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BudgetsScreen(
    uiState: ExpenseUiState,
    onUpdateBudget: (category: String, limit: Double) -> Unit,
    onUpdateOverallBudget: (limit: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = uiState.preferences.currencySymbol
    val hideSensitive = uiState.preferences.hideSensitiveNumbers
    val currentMonthYear = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    var editingCategory by remember { mutableStateOf<Pair<String, Double>?>(null) }
    var editingOverall by remember { mutableStateOf(false) }

    editingCategory?.let { (catName, curLimit) ->
        EditBudgetDialog(
            categoryName = catName,
            currentLimit = curLimit,
            currencySymbol = currency,
            onDismiss = { editingCategory = null },
            onConfirm = { newLimit ->
                onUpdateBudget(catName, newLimit)
                editingCategory = null
            }
        )
    }

    if (editingOverall) {
        EditBudgetDialog(
            categoryName = "Overall",
            currentLimit = uiState.overallMonthlyBudget,
            currencySymbol = currency,
            onDismiss = { editingOverall = false },
            onConfirm = { newLimit ->
                onUpdateOverallBudget(newLimit)
                editingOverall = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LavenderBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header with Month/Year
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Overview",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentMonthYear,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = PurplePrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardSurface,
                    shadowElevation = 1.dp
                ) {
                    TextButton(onClick = { editingOverall = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Total", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PurplePrimary)
                    }
                }
            }
        }

        // 2. Overall Budget Highlight Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoDeep),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(IndigoDeep, IndigoMedium, PurpleVibrant)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Savings,
                                        contentDescription = null,
                                        tint = TextOnDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Total Monthly Budget",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextOnDark
                                    )
                                    Text(
                                        text = "Semester target cap",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextOnDarkSecondary
                                    )
                                }
                            }

                            // % used badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    uiState.overallBudgetUsedPercent >= 100 -> ExpenseRed
                                    uiState.overallBudgetUsedPercent >= 70 -> WarningAmber
                                    else -> IncomeGreen
                                }
                            ) {
                                Text(
                                    text = "${uiState.overallBudgetUsedPercent}% Used",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextOnDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Amount Spent / Limit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("Spent so far", style = MaterialTheme.typography.labelSmall, color = TextOnDarkSecondary)
                                Text(
                                    text = ExpenseViewModel.formatCurrency(uiState.currentMonthExpenses, currency, hideSensitive),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = TextOnDark
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Budget Limit", style = MaterialTheme.typography.labelSmall, color = TextOnDarkSecondary)
                                Text(
                                    text = ExpenseViewModel.formatCurrency(uiState.overallMonthlyBudget, currency, hideSensitive),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextOnDarkSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Animated Horizontal Progress Bar
                        val animOverallProgress by animateFloatAsState(
                            targetValue = uiState.overallBudgetProgress.coerceIn(0f, 1f),
                            animationSpec = tween(700),
                            label = "overall_progress"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animOverallProgress)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            uiState.overallBudgetUsedPercent >= 100 -> ExpenseRed
                                            uiState.overallBudgetUsedPercent >= 70 -> WarningAmber
                                            else -> IncomeGreen
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }

        // 3. Category Budgets Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Budget Limits",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Tap card to edit limit",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        // 4. Category Budget Cards List
        items(uiState.categoryBudgets, key = { it.category }) { budgetItem ->
            CategoryBudgetCard(
                item = budgetItem,
                currencySymbol = currency,
                hideSensitive = hideSensitive,
                onClick = { editingCategory = Pair(budgetItem.category, budgetItem.limit) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CategoryBudgetCard(
    item: CategoryBudgetItem,
    currencySymbol: String,
    hideSensitive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = (item.spent / if (item.limit > 0) item.limit else 1.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "cat_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("budget_card_${item.category}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, Color(0xFFEEF2FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        icon = item.icon,
                        color = item.categoryColor,
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${item.percentUsed}% of limit",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = item.progressColor
                            )
                        )
                    }
                }

                // Spent / Limit text & Edit Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = ExpenseViewModel.formatCurrency(item.spent, currencySymbol, hideSensitive),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.percentUsed >= 100) ExpenseRed else TextPrimary
                                )
                            )
                            Text(
                                text = " / " + ExpenseViewModel.formatCurrency(item.limit, currencySymbol, hideSensitive),
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                modifier = Modifier.padding(bottom = 1.dp)
                            )
                        }

                        val remaining = (item.limit - item.spent).coerceAtLeast(0.0)
                        Text(
                            text = if (item.spent > item.limit) "Over by ${ExpenseViewModel.formatCurrency(item.spent - item.limit, currencySymbol, hideSensitive)}"
                            else "Left: ${ExpenseViewModel.formatCurrency(remaining, currencySymbol, hideSensitive)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.spent > item.limit) ExpenseRed else TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit ${item.category} budget",
                            tint = PurplePrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Progress Bar with dynamic color (green -> amber -> red)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(SurfaceSubtle)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(item.progressColor)
                )
            }
        }
    }
}
