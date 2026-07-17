package com.example.dao

import androidx.room.*
import com.example.entity.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(preference: UserPreferenceEntity)

    @Update
    suspend fun update(preference: UserPreferenceEntity)

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun observe(): Flow<UserPreferenceEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getPreferences(): UserPreferenceEntity?
}
