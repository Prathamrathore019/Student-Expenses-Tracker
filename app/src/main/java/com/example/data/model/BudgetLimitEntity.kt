package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_limits")
data class BudgetLimitEntity(
    @PrimaryKey
    val category: String, // "Food", "Transport", "Study", "Entertainment", "Shopping", "Others", "Overall"
    val monthlyLimit: Double
)
