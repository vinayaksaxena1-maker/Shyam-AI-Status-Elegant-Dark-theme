package com.example.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompt_history")
data class PromptHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
)
