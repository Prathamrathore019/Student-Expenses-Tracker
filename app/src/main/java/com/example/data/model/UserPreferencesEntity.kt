package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Alex Sharma",
    val studentRole: String = "University Student",
    val currencySymbol: String = "₹",
    val currencyCode: String = "INR",
    val hideSensitiveNumbers: Boolean = false,
    val reminderTime: String = "08:00 PM",
    val reminderEnabled: Boolean = true,
    val overallMonthlyBudget: Double = 12000.0
)
