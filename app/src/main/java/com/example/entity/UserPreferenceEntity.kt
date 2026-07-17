package com.example.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey val id: Int = 1, // Single row for preferences
    val selectedTheme: String,
    val lastStyle: String,
    val language: String,
    val notificationsEnabled: Boolean
)
