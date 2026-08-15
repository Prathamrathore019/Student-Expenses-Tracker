package com.example.data

import com.example.data.dao.BudgetDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserPreferencesDao
import com.example.data.model.BudgetLimitEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val userPreferencesDao: UserPreferencesDao,
    private val database: AppDatabase
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<BudgetLimitEntity>> = budgetDao.getAllBudgets()
    val userPreferences: Flow<UserPreferencesEntity?> = userPreferencesDao.getUserPreferences()

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun setBudget(category: String, limit: Double) {
        budgetDao.insertOrUpdateBudget(BudgetLimitEntity(category, limit))
    }

    suspend fun updatePreferences(preferences: UserPreferencesEntity) {
        userPreferencesDao.insertOrUpdatePreferences(preferences)
    }

    suspend fun resetToSampleData() {
        transactionDao.deleteAllTransactions()
        budgetDao.deleteAllBudgets()
        AppDatabase.populateInitialData(database)
    }

    suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
    }
}
