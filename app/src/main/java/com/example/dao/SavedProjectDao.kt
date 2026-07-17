package com.example.dao

import androidx.room.*
import com.example.entity.SavedProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: SavedProjectEntity): Long

    @Update
    suspend fun update(project: SavedProjectEntity)

    @Delete
    suspend fun delete(project: SavedProjectEntity)

    @Query("DELETE FROM saved_projects WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM saved_projects ORDER BY updatedAt DESC")
    suspend fun getAll(): List<SavedProjectEntity>

    @Query("SELECT * FROM saved_projects WHERE favorite = 1 ORDER BY updatedAt DESC")
    fun getFavorites(): Flow<List<SavedProjectEntity>>

    @Query("SELECT * FROM saved_projects WHERE title LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchByTitle(query: String): Flow<List<SavedProjectEntity>>

    @Query("SELECT * FROM saved_projects WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): SavedProjectEntity?

    @Query("SELECT * FROM saved_projects ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SavedProjectEntity>>
}
