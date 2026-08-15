package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ExpenseViewModel
import com.example.ui.components.AddTransactionSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.CardSurface
import com.example.ui.theme.LavenderBackground
import com.example.ui.theme.PurpleLightContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.StudentExpenseTrackerTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    TRANSACTIONS("Transactions", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "tab_transactions"),
    BUDGETS("Budgets", Icons.Filled.Tune, Icons.Outlined.Tune, "tab_budgets"),
    ANALYTICS("Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics, "tab_analytics"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
}

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StudentExpenseTrackerTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var currentTab by remember { mutableStateOf(AppTab.HOME) }
                var showAddSheet by remember { mutableStateOf(false) }
                var initialAddIsExpense by remember { mutableStateOf(true) }

                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val scope = rememberCoroutineScope()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = LavenderBackground,
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                initialAddIsExpense = true
                                showAddSheet = true
                            },
                            containerColor = PurplePrimary,
                            contentColor = TextOnDark,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                            modifier = Modifier.testTag("fab_add_transaction")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Transaction",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = CardSurface,
                            tonalElevation = 6.dp
                        ) {
                            AppTab.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PurplePrimary,
                                        selectedTextColor = PurplePrimary,
                                        indicatorColor = PurpleLightContainer,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tab_switch_animation"
                        ) { targetTab ->
                            when (targetTab) {
                                AppTab.HOME -> HomeScreen(
                                    uiState = uiState,
                                    onToggleHideSensitive = { viewModel.toggleHideSensitiveNumbers() },
                                    onOpenAddTransaction = { isExpense ->
                                        initialAddIsExpense = isExpense
                                        showAddSheet = true
                                    },
                                    onNavigateToTransactions = { currentTab = AppTab.TRANSACTIONS },
                                    onNavigateToBudgets = { currentTab = AppTab.BUDGETS },
                                    onNavigateToAnalytics = { currentTab = AppTab.ANALYTICS },
                                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                                )

                                AppTab.TRANSACTIONS -> TransactionsScreen(
                                    uiState = uiState,
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    onFilterTypeChange = { viewModel.setFilterType(it) },
                                    onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                                )

                                AppTab.BUDGETS -> BudgetsScreen(
                                    uiState = uiState,
                                    onUpdateBudget = { cat, limit -> viewModel.updateBudgetLimit(cat, limit) },
                                    onUpdateOverallBudget = { limit -> viewModel.updateOverallBudget(limit) }
                                )

                                AppTab.ANALYTICS -> AnalyticsScreen(
                                    uiState = uiState,
                                    onTimeframeChange = { viewModel.setAnalyticsTimeframe(it) }
                                )

                                AppTab.PROFILE -> ProfileScreen(
                                    uiState = uiState,
                                    onUpdateProfile = { name, role, reminder, enabled ->
                                        viewModel.updateProfile(name, role, reminder, enabled)
                                    },
                                    onUpdateCurrency = { symbol, code ->
                                        viewModel.updateCurrency(symbol, code)
                                    },
                                    onToggleHideSensitive = { viewModel.toggleHideSensitiveNumbers() },
                                    onResetSampleData = { viewModel.resetToSampleData() },
                                    onClearAllData = { viewModel.clearAllData() }
                                )
                            }
                        }
                    }
                }

                // Add Transaction Modal Sheet
                if (showAddSheet) {
                    AddTransactionSheet(
                        sheetState = sheetState,
                        currencySymbol = uiState.preferences.currencySymbol,
                        onDismiss = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showAddSheet = false
                            }
                        },
                        onAddTransaction = { title, amount, type, category, dateMillis, note ->
                            viewModel.addTransaction(title, amount, type, category, dateMillis, note)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showAddSheet = false
                            }
                        }
                    )
                }
            }
        }
    }
}
