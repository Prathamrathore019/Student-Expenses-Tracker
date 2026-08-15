package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "expense" or "income"
    val category: String, // "Food", "Transport", "Study", "Entertainment", "Shopping", "Others"
    val dateMillis: Long,
    val note: String = ""
)
