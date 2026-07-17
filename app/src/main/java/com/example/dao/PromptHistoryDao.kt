package com.example.dao

import androidx.room.*
import com.example.entity.PromptHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(promptHistory: PromptHistoryEntity): Long

    @Delete
    suspend fun delete(promptHistory: PromptHistoryEntity)

    @Query("DELETE FROM prompt_history")
    suspend fun deleteAll()

    @Query("SELECT * FROM prompt_history ORDER BY timestamp DESC")
    suspend fun getRecent(): List<PromptHistoryEntity>

    @Query("SELECT * FROM prompt_history ORDER BY timestamp DESC")
    fun observeRecent(): Flow<List<PromptHistoryEntity>>
}
