package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BudgetDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserPreferencesDao
import com.example.data.model.BudgetLimitEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserPreferencesEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        BudgetLimitEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_expense_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val now = System.currentTimeMillis()
            val oneDay = 24 * 60 * 60 * 1000L

            // 1. Initial User Preferences
            database.userPreferencesDao().insertOrUpdatePreferences(
                UserPreferencesEntity(
                    id = 1,
                    userName = "Alex Sharma",
                    studentRole = "University Student • CS",
                    currencySymbol = "₹",
                    currencyCode = "INR",
                    hideSensitiveNumbers = false,
                    reminderTime = "08:00 PM",
                    reminderEnabled = true,
                    overallMonthlyBudget = 11500.0
                )
            )

            // 2. Initial Budgets
            val defaultBudgets = listOf(
                BudgetLimitEntity("Food", 3500.0),
                BudgetLimitEntity("Transport", 1500.0),
                BudgetLimitEntity("Study", 2000.0),
                BudgetLimitEntity("Entertainment", 1500.0),
                BudgetLimitEntity("Shopping", 1800.0),
                BudgetLimitEntity("Others", 1200.0)
            )
            database.budgetDao().insertBudgets(defaultBudgets)

            // 3. Initial Sample Transactions
            val sampleTransactions = listOf(
                TransactionEntity(
                    title = "Campus Cafeteria Lunch",
                    amount = 180.0,
                    type = "expense",
                    category = "Food",
                    dateMillis = now - (2 * 60 * 60 * 1000L),
                    note = "Thali with dessert"
                ),
                TransactionEntity(
                    title = "Semester Lab Manuals & Pens",
                    amount = 650.0,
                    type = "expense",
                    category = "Study",
                    dateMillis = now - (oneDay),
                    note = "Physics & CS manuals"
                ),
                TransactionEntity(
                    title = "Monthly Student Bus Pass",
                    amount = 450.0,
                    type = "expense",
                    category = "Transport",
                    dateMillis = now - (2 * oneDay),
                    note = "Discounted university route pass"
                ),
                TransactionEntity(
                    title = "Python Tutoring Batch",
                    amount = 2500.0,
                    type = "income",
                    category = "Others",
                    dateMillis = now - (3 * oneDay),
                    note = "Weekly junior student tutoring"
                ),
                TransactionEntity(
                    title = "Hostel Wi-Fi & Electric Share",
                    amount = 800.0,
                    type = "expense",
                    category = "Others",
                    dateMillis = now - (4 * oneDay),
                    note = "Room bill split"
                ),
                TransactionEntity(
                    title = "Weekend Movie & Popcorn",
                    amount = 420.0,
                    type = "expense",
                    category = "Entertainment",
                    dateMillis = now - (5 * oneDay),
                    note = "Campus batchmates movie night"
                ),
                TransactionEntity(
                    title = "Campus Cafe & Cold Coffee",
                    amount = 120.0,
                    type = "expense",
                    category = "Food",
                    dateMillis = now - (6 * oneDay),
                    note = "Late night project discussion"
                ),
                TransactionEntity(
                    title = "Algorithms Subscription",
                    amount = 499.0,
                    type = "expense",
                    category = "Study",
                    dateMillis = now - (8 * oneDay),
                    note = "Practice platform"
                ),
                TransactionEntity(
                    title = "Dorm Supplies & Groceries",
                    amount = 950.0,
                    type = "expense",
                    category = "Shopping",
                    dateMillis = now - (10 * oneDay),
                    note = "Detergent, oats, healthy snacks"
                ),
                TransactionEntity(
                    title = "College Academic Merit Stipend",
                    amount = 8000.0,
                    type = "income",
                    category = "Others",
                    dateMillis = now - (14 * oneDay),
                    note = "Direct bank transfer"
                ),
                TransactionEntity(
                    title = "Metro Card Top-Up",
                    amount = 300.0,
                    type = "expense",
                    category = "Transport",
                    dateMillis = now - (16 * oneDay),
                    note = "Commute to library"
                ),
                TransactionEntity(
                    title = "Spotify Student Plan",
                    amount = 59.0,
                    type = "expense",
                    category = "Entertainment",
                    dateMillis = now - (18 * oneDay),
                    note = "Monthly student discount"
                ),
                TransactionEntity(
                    title = "Campus Canteen Breakfast",
                    amount = 90.0,
                    type = "expense",
                    category = "Food",
                    dateMillis = now - (21 * oneDay),
                    note = "Poha & tea"
                ),
                TransactionEntity(
                    title = "College Fest Hoodie",
                    amount = 600.0,
                    type = "expense",
                    category = "Shopping",
                    dateMillis = now - (24 * oneDay),
                    note = "Annual tech fest merch"
                )
            )
            database.transactionDao().insertTransactions(sampleTransactions)
        }
    }
}
