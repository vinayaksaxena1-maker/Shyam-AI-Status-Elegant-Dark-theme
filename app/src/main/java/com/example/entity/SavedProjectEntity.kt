package com.example.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_projects")
data class SavedProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val prompt: String,
    val enhancedPrompt: String,
    val imagePath: String,
    val thumbnailPath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val favorite: Boolean = false,
    val style: String,
    val aspectRatio: String
)
