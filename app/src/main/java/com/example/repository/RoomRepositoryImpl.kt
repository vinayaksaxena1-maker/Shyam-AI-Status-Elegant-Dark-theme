package com.example.repository

import com.example.dao.PreferenceDao
import com.example.dao.PromptHistoryDao
import com.example.dao.SavedProjectDao
import com.example.entity.PromptHistoryEntity
import com.example.entity.SavedProjectEntity
import com.example.entity.UserPreferenceEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RoomRepositoryImpl(
    private val savedProjectDao: SavedProjectDao,
    private val promptHistoryDao: PromptHistoryDao,
    private val preferenceDao: PreferenceDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RoomRepository {

    override suspend fun insertProject(project: SavedProjectEntity): Long = withContext(ioDispatcher) {
        savedProjectDao.insert(project)
    }

    override suspend fun updateProject(project: SavedProjectEntity) = withContext(ioDispatcher) {
        savedProjectDao.update(project)
    }

    override suspend fun deleteProject(project: SavedProjectEntity) = withContext(ioDispatcher) {
        savedProjectDao.delete(project)
    }

    override suspend fun deleteProjectById(id: Int) = withContext(ioDispatcher) {
        savedProjectDao.deleteById(id)
    }

    override suspend fun getAllProjects(): List<SavedProjectEntity> = withContext(ioDispatcher) {
        savedProjectDao.getAll()
    }

    override fun getFavoriteProjects(): Flow<List<SavedProjectEntity>> {
        return savedProjectDao.getFavorites()
    }

    override fun searchProjects(query: String): Flow<List<SavedProjectEntity>> {
        return savedProjectDao.searchByTitle(query)
    }

    override suspend fun getProjectById(id: Int): SavedProjectEntity? = withContext(ioDispatcher) {
        savedProjectDao.getById(id)
    }

    override fun observeAllProjects(): Flow<List<SavedProjectEntity>> {
        return savedProjectDao.observeAll()
    }

    override suspend fun insertPromptHistory(promptHistory: PromptHistoryEntity): Long = withContext(ioDispatcher) {
        promptHistoryDao.insert(promptHistory)
    }

    override suspend fun deletePromptHistory(promptHistory: PromptHistoryEntity) = withContext(ioDispatcher) {
        promptHistoryDao.delete(promptHistory)
    }

    override suspend fun deleteAllPromptHistory() = withContext(ioDispatcher) {
        promptHistoryDao.deleteAll()
    }

    override suspend fun getRecentPromptHistory(): List<PromptHistoryEntity> = withContext(ioDispatcher) {
        promptHistoryDao.getRecent()
    }

    override fun observeRecentPromptHistory(): Flow<List<PromptHistoryEntity>> {
        return promptHistoryDao.observeRecent()
    }

    override suspend fun savePreferences(preference: UserPreferenceEntity) = withContext(ioDispatcher) {
        preferenceDao.save(preference)
    }

    override suspend fun updatePreferences(preference: UserPreferenceEntity) = withContext(ioDispatcher) {
        preferenceDao.update(preference)
    }

    override fun observePreferences(): Flow<UserPreferenceEntity?> {
        return preferenceDao.observe()
    }

    override suspend fun getPreferences(): UserPreferenceEntity? = withContext(ioDispatcher) {
        preferenceDao.getPreferences()
    }
}
