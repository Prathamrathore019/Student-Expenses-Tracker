package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.AppDatabase
import com.example.data.ExpenseRepository
import com.example.data.model.BudgetLimitEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserPreferencesEntity
import com.example.ui.theme.CategoryEntertainment
import com.example.ui.theme.CategoryEntertainmentBg
import com.example.ui.theme.CategoryFood
import com.example.ui.theme.CategoryFoodBg
import com.example.ui.theme.CategoryOthers
import com.example.ui.theme.CategoryOthersBg
import com.example.ui.theme.CategoryShopping
import com.example.ui.theme.CategoryShoppingBg
import com.example.ui.theme.CategoryStudy
import com.example.ui.theme.CategoryStudyBg
import com.example.ui.theme.CategoryTransport
import com.example.ui.theme.CategoryTransportBg
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TransactionFilterType {
    ALL, EXPENSE, INCOME
}

enum class AnalyticsTimeframe {
    WEEK, MONTH, YEAR
}

data class CategoryMeta(
    val name: String,
    val color: Color,
    val bgColor: Color,
    val icon: ImageVector
)

object CategoryRegistry {
    val categories = listOf(
        CategoryMeta("Food", CategoryFood, CategoryFoodBg, Icons.Default.Fastfood),
        CategoryMeta("Transport", CategoryTransport, CategoryTransportBg, Icons.Default.DirectionsBus),
        CategoryMeta("Study", CategoryStudy, CategoryStudyBg, Icons.Default.MenuBook),
        CategoryMeta("Entertainment", CategoryEntertainment, CategoryEntertainmentBg, Icons.Default.Movie),
        CategoryMeta("Shopping", CategoryShopping, CategoryShoppingBg, Icons.Default.ShoppingBag),
        CategoryMeta("Others", CategoryOthers, CategoryOthersBg, Icons.Default.Category)
    )

    fun getCategoryMeta(name: String): CategoryMeta {
        return categories.find { it.name.equals(name, ignoreCase = true) }
            ?: CategoryMeta("Others", CategoryOthers, CategoryOthersBg, Icons.Default.Category)
    }
}

data class CategoryBudgetItem(
    val category: String,
    val spent: Double,
    val limit: Double,
    val progress: Float, // 0.0f to 1.0f+
    val percentUsed: Int,
    val progressColor: Color,
    val icon: ImageVector,
    val categoryColor: Color
)

data class BarChartItem(
    val label: String,
    val amount: Double,
    val formattedAmount: String,
    val isMax: Boolean = false,
    val dateLabel: String = ""
)

data class DonutSliceItem(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val color: Color,
    val icon: ImageVector
)

data class AnalyticsState(
    val timeframe: AnalyticsTimeframe = AnalyticsTimeframe.MONTH,
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val percentChangeVsPrevious: Double = 0.0,
    val barChartData: List<BarChartItem> = emptyList(),
    val donutSlices: List<DonutSliceItem> = emptyList(),
    val highestCategory: String = "None",
    val mentorTips: List<String> = emptyList()
)

data class ExpenseUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val currentMonthExpenses: Double = 0.0,
    val currentMonthIncome: Double = 0.0,
    val overallMonthlyBudget: Double = 11500.0,
    val overallBudgetUsedPercent: Int = 0,
    val overallBudgetProgress: Float = 0f,
    val transactions: List<TransactionEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val groupedTransactions: Map<String, List<TransactionEntity>> = emptyMap(),
    val categoryBudgets: List<CategoryBudgetItem> = emptyList(),
    val analytics: AnalyticsState = AnalyticsState(),
    val preferences: UserPreferencesEntity = UserPreferencesEntity(),
    val activeFilterType: TransactionFilterType = TransactionFilterType.ALL,
    val searchQuery: String = "",
    val activeCategoryFilter: String = "All",
    val analyticsTimeframe: AnalyticsTimeframe = AnalyticsTimeframe.MONTH
)

private data class DataBundle(
    val transactions: List<TransactionEntity>,
    val budgets: List<BudgetLimitEntity>,
    val preferences: UserPreferencesEntity
)

private data class FilterBundle(
    val query: String,
    val filterType: TransactionFilterType,
    val categoryFilter: String,
    val timeframe: AnalyticsTimeframe
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow(TransactionFilterType.ALL)
    private val _categoryFilter = MutableStateFlow("All")
    private val _analyticsTimeframe = MutableStateFlow(AnalyticsTimeframe.MONTH)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(
            transactionDao = db.transactionDao(),
            budgetDao = db.budgetDao(),
            userPreferencesDao = db.userPreferencesDao(),
            database = db
        )
    }

    private val dataFlow = combine(
        repository.allTransactions,
        repository.allBudgets,
        repository.userPreferences
    ) { txs, budgets, prefs ->
        DataBundle(
            transactions = txs,
            budgets = budgets,
            preferences = prefs ?: UserPreferencesEntity()
        )
    }

    private val filterFlow = combine(
        _searchQuery,
        _filterType,
        _categoryFilter,
        _analyticsTimeframe
    ) { q, ft, cf, tf ->
        FilterBundle(
            query = q,
            filterType = ft,
            categoryFilter = cf,
            timeframe = tf
        )
    }

    val uiState: StateFlow<ExpenseUiState> = combine(
        dataFlow,
        filterFlow
    ) { data, filters ->
        val allTransactions = data.transactions
        val allBudgets = data.budgets
        val userPrefs = data.preferences

        val query = filters.query
        val filterType = filters.filterType
        val catFilter = filters.categoryFilter
        val timeframe = filters.timeframe

        // 1. Calculate All-time & Month-specific values
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        var totalIncomeAll = 0.0
        var totalExpenseAll = 0.0
        var thisMonthExpense = 0.0
        var thisMonthIncome = 0.0

        val categorySpentThisMonth = mutableMapOf<String, Double>()
        CategoryRegistry.categories.forEach {
            categorySpentThisMonth[it.name] = 0.0
        }

        allTransactions.forEach { tx ->
            if (tx.type.equals("income", ignoreCase = true)) {
                totalIncomeAll += tx.amount
            } else {
                totalExpenseAll += tx.amount
            }

            val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
            if (txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear) {
                if (tx.type.equals("income", ignoreCase = true)) {
                    thisMonthIncome += tx.amount
                } else {
                    thisMonthExpense += tx.amount
                    val cat = tx.category
                    categorySpentThisMonth[cat] = (categorySpentThisMonth[cat] ?: 0.0) + tx.amount
                }
            }
        }

        val totalBalance = totalIncomeAll - totalExpenseAll

        // 2. Budget limits & Progress
        val budgetMap = allBudgets.associate { it.category to it.monthlyLimit }
        val overallBudget = if (userPrefs.overallMonthlyBudget > 0) userPrefs.overallMonthlyBudget else 11500.0

        val categoryBudgetList = CategoryRegistry.categories.map { meta ->
            val spent = categorySpentThisMonth[meta.name] ?: 0.0
            val limit = budgetMap[meta.name] ?: when (meta.name) {
                "Food" -> 3500.0
                "Transport" -> 1500.0
                "Study" -> 2000.0
                "Entertainment" -> 1500.0
                "Shopping" -> 1800.0
                else -> 1200.0
            }
            val progress = if (limit > 0) (spent / limit).toFloat() else 0f
            val percentUsed = (progress * 100).toInt()

            val progressColor = when {
                progress >= 1.0f -> ExpenseRed
                progress >= 0.70f -> WarningAmber
                else -> IncomeGreen
            }

            CategoryBudgetItem(
                category = meta.name,
                spent = spent,
                limit = limit,
                progress = progress.coerceAtMost(1.5f),
                percentUsed = percentUsed,
                progressColor = progressColor,
                icon = meta.icon,
                categoryColor = meta.color
            )
        }

        val overallProgress = if (overallBudget > 0) (thisMonthExpense / overallBudget).toFloat() else 0f
        val overallPercentUsed = (overallProgress * 100).toInt()

        // 3. Filtered & Grouped Transactions
        val filteredList = allTransactions.filter { tx ->
            val matchesType = when (filterType) {
                TransactionFilterType.ALL -> true
                TransactionFilterType.EXPENSE -> tx.type.equals("expense", ignoreCase = true)
                TransactionFilterType.INCOME -> tx.type.equals("income", ignoreCase = true)
            }
            val matchesCategory = (catFilter == "All") || tx.category.equals(catFilter, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    tx.title.contains(query, ignoreCase = true) ||
                    tx.category.contains(query, ignoreCase = true) ||
                    tx.note.contains(query, ignoreCase = true) ||
                    tx.amount.toString().contains(query)

            matchesType && matchesCategory && matchesQuery
        }

        // Group by Month/Year header
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val grouped = filteredList.groupBy { tx ->
            monthYearFormat.format(Date(tx.dateMillis))
        }

        val recentTxs = allTransactions.take(4)

        // 4. Analytics Calculations
        val analyticsState = calculateAnalytics(
            allTransactions = allTransactions,
            timeframe = timeframe,
            userPrefs = userPrefs
        )

        ExpenseUiState(
            totalBalance = totalBalance,
            totalIncome = totalIncomeAll,
            totalExpenses = totalExpenseAll,
            currentMonthExpenses = thisMonthExpense,
            currentMonthIncome = thisMonthIncome,
            overallMonthlyBudget = overallBudget,
            overallBudgetUsedPercent = overallPercentUsed,
            overallBudgetProgress = overallProgress.coerceAtMost(1.0f),
            transactions = allTransactions,
            recentTransactions = recentTxs,
            filteredTransactions = filteredList,
            groupedTransactions = grouped,
            categoryBudgets = categoryBudgetList,
            analytics = analyticsState,
            preferences = userPrefs,
            activeFilterType = filterType,
            searchQuery = query,
            activeCategoryFilter = catFilter,
            analyticsTimeframe = timeframe
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseUiState()
    )

    private fun calculateAnalytics(
        allTransactions: List<TransactionEntity>,
        timeframe: AnalyticsTimeframe,
        userPrefs: UserPreferencesEntity
    ): AnalyticsState {
        val now = Calendar.getInstance()
        val curMonth = now.get(Calendar.MONTH)
        val curYear = now.get(Calendar.YEAR)

        var totalSpentInPeriod = 0.0
        var totalIncomeInPeriod = 0.0
        var prevPeriodSpent = 0.0

        val categorySpent = mutableMapOf<String, Double>()
        CategoryRegistry.categories.forEach { categorySpent[it.name] = 0.0 }

        val barItems = mutableListOf<BarChartItem>()

        when (timeframe) {
            AnalyticsTimeframe.WEEK -> {
                // Past 7 days
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val fullDateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
                val dayTotals = mutableMapOf<Int, Double>()

                for (i in 6 downTo 0) {
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                    val dKey = cal.get(Calendar.DAY_OF_YEAR)
                    dayTotals[dKey] = 0.0
                }

                allTransactions.forEach { tx ->
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
                    val diffDays = (now.timeInMillis - tx.dateMillis) / (24 * 60 * 60 * 1000L)
                    if (diffDays in 0..6) {
                        if (tx.type.equals("expense", ignoreCase = true)) {
                            totalSpentInPeriod += tx.amount
                            val dKey = txCal.get(Calendar.DAY_OF_YEAR)
                            dayTotals[dKey] = (dayTotals[dKey] ?: 0.0) + tx.amount
                            categorySpent[tx.category] = (categorySpent[tx.category] ?: 0.0) + tx.amount
                        } else {
                            totalIncomeInPeriod += tx.amount
                        }
                    } else if (diffDays in 7..13) {
                        if (tx.type.equals("expense", ignoreCase = true)) {
                            prevPeriodSpent += tx.amount
                        }
                    }
                }

                var maxVal = 0.0
                dayTotals.values.forEach { if (it > maxVal) maxVal = it }

                for (i in 6 downTo 0) {
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                    val dKey = cal.get(Calendar.DAY_OF_YEAR)
                    val amt = dayTotals[dKey] ?: 0.0
                    barItems.add(
                        BarChartItem(
                            label = dayFormat.format(cal.time),
                            amount = amt,
                            formattedAmount = formatCurrency(amt, userPrefs.currencySymbol),
                            isMax = amt > 0 && amt == maxVal,
                            dateLabel = fullDateFormat.format(cal.time)
                        )
                    )
                }
            }
            AnalyticsTimeframe.MONTH -> {
                // 4 Weeks of current month
                val weekTotals = mutableMapOf<Int, Double>(1 to 0.0, 2 to 0.0, 3 to 0.0, 4 to 0.0)

                allTransactions.forEach { tx ->
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
                    if (txCal.get(Calendar.YEAR) == curYear && txCal.get(Calendar.MONTH) == curMonth) {
                        if (tx.type.equals("expense", ignoreCase = true)) {
                            totalSpentInPeriod += tx.amount
                            val dayOfMonth = txCal.get(Calendar.DAY_OF_MONTH)
                            val weekBucket = when {
                                dayOfMonth <= 7 -> 1
                                dayOfMonth <= 14 -> 2
                                dayOfMonth <= 21 -> 3
                                else -> 4
                            }
                            weekTotals[weekBucket] = (weekTotals[weekBucket] ?: 0.0) + tx.amount
                            categorySpent[tx.category] = (categorySpent[tx.category] ?: 0.0) + tx.amount
                        } else {
                            totalIncomeInPeriod += tx.amount
                        }
                    } else {
                        // Check previous month
                        val prevMonth = if (curMonth == 0) 11 else curMonth - 1
                        val prevYear = if (curMonth == 0) curYear - 1 else curYear
                        if (txCal.get(Calendar.YEAR) == prevYear && txCal.get(Calendar.MONTH) == prevMonth) {
                            if (tx.type.equals("expense", ignoreCase = true)) {
                                prevPeriodSpent += tx.amount
                            }
                        }
                    }
                }

                var maxVal = 0.0
                weekTotals.values.forEach { if (it > maxVal) maxVal = it }

                listOf(1, 2, 3, 4).forEach { w ->
                    val amt = weekTotals[w] ?: 0.0
                    barItems.add(
                        BarChartItem(
                            label = "W$w",
                            amount = amt,
                            formattedAmount = formatCurrency(amt, userPrefs.currencySymbol),
                            isMax = amt > 0 && amt == maxVal,
                            dateLabel = "Week $w"
                        )
                    )
                }
            }
            AnalyticsTimeframe.YEAR -> {
                // Months of year
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                val monthTotals = mutableMapOf<Int, Double>()
                for (m in 0..11) monthTotals[m] = 0.0

                allTransactions.forEach { tx ->
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
                    if (txCal.get(Calendar.YEAR) == curYear) {
                        if (tx.type.equals("expense", ignoreCase = true)) {
                            totalSpentInPeriod += tx.amount
                            val m = txCal.get(Calendar.MONTH)
                            monthTotals[m] = (monthTotals[m] ?: 0.0) + tx.amount
                            categorySpent[tx.category] = (categorySpent[tx.category] ?: 0.0) + tx.amount
                        } else {
                            totalIncomeInPeriod += tx.amount
                        }
                    } else if (txCal.get(Calendar.YEAR) == curYear - 1) {
                        if (tx.type.equals("expense", ignoreCase = true)) {
                            prevPeriodSpent += tx.amount
                        }
                    }
                }

                var maxVal = 0.0
                monthTotals.values.forEach { if (it > maxVal) maxVal = it }

                for (m in 0..11) {
                    val cal = Calendar.getInstance().apply { set(Calendar.MONTH, m) }
                    val amt = monthTotals[m] ?: 0.0
                    barItems.add(
                        BarChartItem(
                            label = monthFormat.format(cal.time),
                            amount = amt,
                            formattedAmount = formatCurrency(amt, userPrefs.currencySymbol),
                            isMax = amt > 0 && amt == maxVal,
                            dateLabel = monthFormat.format(cal.time)
                        )
                    )
                }
            }
        }

        // Percentage change vs previous period
        val pctChange = if (prevPeriodSpent > 0) {
            ((totalSpentInPeriod - prevPeriodSpent) / prevPeriodSpent) * 100.0
        } else 0.0

        // Donut Slices
        val slices = mutableListOf<DonutSliceItem>()
        var highestCat = "None"
        var highestCatAmt = 0.0

        categorySpent.forEach { (catName, amt) ->
            if (amt > highestCatAmt) {
                highestCatAmt = amt
                highestCat = catName
            }
            if (amt > 0) {
                val pct = if (totalSpentInPeriod > 0) ((amt / totalSpentInPeriod) * 100f).toFloat() else 0f
                val meta = CategoryRegistry.getCategoryMeta(catName)
                slices.add(
                    DonutSliceItem(
                        category = catName,
                        amount = amt,
                        percentage = pct,
                        color = meta.color,
                        icon = meta.icon
                    )
                )
            }
        }

        // Student Money Mentor Tips
        val tips = mutableListOf<String>()
        if (totalSpentInPeriod > 0) {
            if (highestCat != "None") {
                val catPct = ((highestCatAmt / totalSpentInPeriod) * 100).toInt()
                tips.add("💡 $highestCat is your biggest expense category at $catPct% of total spending.")
            }
            if (pctChange < 0) {
                tips.add("🎉 Excellent discipline! You spent ${"%.1f".format(Math.abs(pctChange))}% less than the previous period.")
            } else if (pctChange > 15) {
                tips.add("⚠️ Spending is up by ${"%.1f".format(pctChange)}% compared to last period. Watch out for weekend impulse expenses.")
            }
            tips.add("🎓 Student Tip: Take advantage of semester student travel cards and campus library digital copies to keep expenses low.")
        } else {
            tips.add("💡 Start logging your daily canteen and travel expenses to get customized student financial insights.")
        }

        return AnalyticsState(
            timeframe = timeframe,
            totalSpent = totalSpentInPeriod,
            totalIncome = totalIncomeInPeriod,
            percentChangeVsPrevious = pctChange,
            barChartData = barItems,
            donutSlices = slices.sortedByDescending { it.amount },
            highestCategory = highestCat,
            mentorTips = tips
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(filterType: TransactionFilterType) {
        _filterType.value = filterType
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun setAnalyticsTimeframe(timeframe: AnalyticsTimeframe) {
        _analyticsTimeframe.value = timeframe
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        dateMillis: Long,
        note: String
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    title = title.trim(),
                    amount = amount,
                    type = type,
                    category = category,
                    dateMillis = dateMillis,
                    note = note.trim()
                )
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun updateBudgetLimit(category: String, limit: Double) {
        viewModelScope.launch {
            repository.setBudget(category, limit)
        }
    }

    fun updateOverallBudget(limit: Double) {
        viewModelScope.launch {
            val current = uiState.value.preferences
            repository.updatePreferences(current.copy(overallMonthlyBudget = limit))
        }
    }

    fun toggleHideSensitiveNumbers() {
        viewModelScope.launch {
            val current = uiState.value.preferences
            repository.updatePreferences(
                current.copy(hideSensitiveNumbers = !current.hideSensitiveNumbers)
            )
        }
    }

    fun updateCurrency(symbol: String, code: String) {
        viewModelScope.launch {
            val current = uiState.value.preferences
            repository.updatePreferences(
                current.copy(currencySymbol = symbol, currencyCode = code)
            )
        }
    }

    fun updateProfile(name: String, role: String, reminderTime: String, reminderEnabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.preferences
            repository.updatePreferences(
                current.copy(
                    userName = name.trim(),
                    studentRole = role.trim(),
                    reminderTime = reminderTime,
                    reminderEnabled = reminderEnabled
                )
            )
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.resetToSampleData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    companion object {
        fun formatCurrency(amount: Double, symbol: String, hide: Boolean = false): String {
            if (hide) return "$symbol•••••"
            val nf = NumberFormat.getNumberInstance(Locale.getDefault())
            nf.minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            nf.maximumFractionDigits = 2
            return "$symbol${nf.format(amount)}"
        }

        fun formatDate(millis: Long): String {
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            return sdf.format(Date(millis))
        }

        fun formatShortDate(millis: Long): String {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            return sdf.format(Date(millis))
        }
    }
}
