package com.example.repository

import com.example.entity.PromptHistoryEntity
import com.example.entity.SavedProjectEntity
import com.example.entity.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    // Saved Projects
    suspend fun insertProject(project: SavedProjectEntity): Long
    suspend fun updateProject(project: SavedProjectEntity)
    suspend fun deleteProject(project: SavedProjectEntity)
    suspend fun deleteProjectById(id: Int)
    suspend fun getAllProjects(): List<SavedProjectEntity>
    fun getFavoriteProjects(): Flow<List<SavedProjectEntity>>
    fun searchProjects(query: String): Flow<List<SavedProjectEntity>>
    suspend fun getProjectById(id: Int): SavedProjectEntity?
    fun observeAllProjects(): Flow<List<SavedProjectEntity>>

    // Prompt History
    suspend fun insertPromptHistory(promptHistory: PromptHistoryEntity): Long
    suspend fun deletePromptHistory(promptHistory: PromptHistoryEntity)
    suspend fun deleteAllPromptHistory()
    suspend fun getRecentPromptHistory(): List<PromptHistoryEntity>
    fun observeRecentPromptHistory(): Flow<List<PromptHistoryEntity>>

    // User Preferences
    suspend fun savePreferences(preference: UserPreferenceEntity)
    suspend fun updatePreferences(preference: UserPreferenceEntity)
    fun observePreferences(): Flow<UserPreferenceEntity?>
    suspend fun getPreferences(): UserPreferenceEntity?
}
