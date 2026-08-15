package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ExpenseUiState
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CircularBudgetRing
import com.example.ui.components.TransactionRowItem
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.IncomeGreenLight
import com.example.ui.theme.IndigoDeep
import com.example.ui.theme.IndigoMedium
import com.example.ui.theme.LavenderBackground
import com.example.ui.theme.PurpleLightContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnDark
import com.example.ui.theme.TextOnDarkSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun HomeScreen(
    uiState: ExpenseUiState,
    onToggleHideSensitive: () -> Unit,
    onOpenAddTransaction: (isExpense: Boolean) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = uiState.preferences.currencySymbol
    val hideSensitive = uiState.preferences.hideSensitiveNumbers

    val studentInitials = remember(uiState.preferences.userName) {
        val parts = uiState.preferences.userName.trim().split(" ")
        if (parts.size >= 2) {
            "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        } else if (parts.isNotEmpty() && parts[0].isNotEmpty()) {
            parts[0].take(2).uppercase()
        } else {
            "ST"
        }
    }

    val firstName = remember(uiState.preferences.userName) {
        uiState.preferences.userName.split(" ").firstOrNull()?.ifBlank { "Student" } ?: "Student"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LavenderBackground),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sleek Top Header (Deep Indigo #3B2A6B with rounded bottom 32.dp)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(IndigoDeep)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar Badge with 2dp border
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PurplePrimary)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(PurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = studentInitials,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = TextOnDark
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "GOOD DAY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp
                                ),
                                color = TextOnDark.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Hi, $firstName",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = TextOnDark
                            )
                        }
                    }

                    // Eye visibility button in glassmorphic container
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(
                            onClick = onToggleHideSensitive,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("toggle_sensitive_visibility")
                        ) {
                            Icon(
                                imageVector = if (hideSensitive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (hideSensitive) "Show numbers" else "Hide numbers",
                                tint = TextOnDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Main Content Area inside horizontal padding
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2. Total Balance Hero Card (Gradient from #4A3AA0 to #3B2A6B with decorative circle)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("balance_card"),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoDeep),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(IndigoMedium, IndigoDeep)
                                )
                            )
                            .padding(22.dp)
                    ) {
                        // Decorative glassmorphic circle in top-right
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .offset(x = 50.dp, y = (-40).dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                                .align(Alignment.TopEnd)
                        )

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Balance",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = TextOnDark.copy(alpha = 0.8f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = uiState.preferences.currencyCode,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = TextOnDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = ExpenseViewModel.formatCurrency(uiState.totalBalance, currency, hideSensitive),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 30.sp
                                ),
                                color = TextOnDark
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Divided Income and Expense Footer
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Income column
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "INCOME",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = TextOnDark.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "+${ExpenseViewModel.formatCurrency(uiState.totalIncome, currency, hideSensitive)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            ),
                                            color = IncomeGreenLight
                                        )
                                    }

                                    // Vertical divider
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(30.dp)
                                            .background(Color.White.copy(alpha = 0.15f))
                                    )

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // Expense column
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "EXPENSES",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = TextOnDark.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "-${ExpenseViewModel.formatCurrency(uiState.totalExpenses, currency, hideSensitive)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            ),
                                            color = ExpenseRedLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onOpenAddTransaction(true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_add_expense_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurplePrimary,
                            contentColor = TextOnDark
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Expense", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { onOpenAddTransaction(false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_add_income_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IncomeGreen,
                            contentColor = TextOnDark
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Income", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, Color(0xFFEEF2FF)),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNavigateToBudgets() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Budgets", tint = PurplePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Budgets", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        }
                    }
                }

                // 4. Budget Overview Card (Sleek White Card with #3B2A6B header and gauge)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBudgets() }
                        .testTag("home_budget_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, Color(0xFFEEF2FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Budget Overview",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = IndigoDeep
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PurpleLightContainer
                            ) {
                                Text(
                                    text = "Monthly",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = PurplePrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Gauge
                            CircularBudgetRing(
                                progress = uiState.overallBudgetProgress,
                                percentUsed = uiState.overallBudgetUsedPercent,
                                size = 80.dp,
                                strokeWidth = 8.dp,
                                primaryColor = PurplePrimary
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Textual Explanation
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "You've spent ${ExpenseViewModel.formatCurrency(uiState.currentMonthExpenses, currency, hideSensitive)} of your ${ExpenseViewModel.formatCurrency(uiState.overallMonthlyBudget, currency, hideSensitive)} monthly limit.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    ),
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        uiState.overallBudgetUsedPercent >= 100 -> ExpenseRedBg
                                        uiState.overallBudgetUsedPercent >= 70 -> WarningAmber.copy(alpha = 0.12f)
                                        else -> IncomeGreenBg
                                    }
                                ) {
                                    Text(
                                        text = when {
                                            uiState.overallBudgetUsedPercent >= 100 -> "⚠️ Over budget cap"
                                            uiState.overallBudgetUsedPercent >= 70 -> "⚡ Approaching limit"
                                            else -> "✨ Spending on track"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        ),
                                        color = when {
                                            uiState.overallBudgetUsedPercent >= 100 -> ExpenseRed
                                            uiState.overallBudgetUsedPercent >= 70 -> WarningAmber
                                            else -> IncomeGreen
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Recent Transactions Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = IndigoDeep
                    )
                    TextButton(
                        onClick = onNavigateToTransactions,
                        modifier = Modifier.testTag("view_all_transactions_button")
                    ) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = PurplePrimary
                        )
                    }
                }
            }
        }

        // Recent items list
        if (uiState.recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, Color(0xFFEEF2FF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent transactions. Tap + to record an expense or income!",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(uiState.recentTransactions, key = { it.id }) { tx ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TransactionRowItem(
                        transaction = tx,
                        currencySymbol = currency,
                        hideSensitive = hideSensitive,
                        onDelete = onDeleteTransaction
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
