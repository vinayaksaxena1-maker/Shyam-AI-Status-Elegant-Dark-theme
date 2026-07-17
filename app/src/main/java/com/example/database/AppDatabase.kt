package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.Project
import com.example.data.ProjectDao
import com.example.dao.PreferenceDao
import com.example.dao.PromptHistoryDao
import com.example.dao.SavedProjectDao
import com.example.entity.PromptHistoryEntity
import com.example.entity.SavedProjectEntity
import com.example.entity.UserPreferenceEntity

@Database(
    entities = [
        Project::class,
        SavedProjectEntity::class,
        PromptHistoryEntity::class,
        UserPreferenceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun projectDao(): ProjectDao
    abstract fun savedProjectDao(): SavedProjectDao
    abstract fun promptHistoryDao(): PromptHistoryDao
    abstract fun preferenceDao(): PreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration from version 1 to version 2.
         * Creates 'saved_projects', 'prompt_history', and 'user_preferences' tables
         * while keeping the existing 'projects' table intact.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `saved_projects` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `prompt` TEXT NOT NULL, 
                        `enhancedPrompt` TEXT NOT NULL, 
                        `imagePath` TEXT NOT NULL, 
                        `thumbnailPath` TEXT NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        `updatedAt` INTEGER NOT NULL, 
                        `favorite` INTEGER NOT NULL, 
                        `style` TEXT NOT NULL, 
                        `aspectRatio` TEXT NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `prompt_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `prompt` TEXT NOT NULL, 
                        `response` TEXT NOT NULL, 
                        `timestamp` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_preferences` (
                        `id` INTEGER NOT NULL, 
                        `selectedTheme` TEXT NOT NULL, 
                        `lastStyle` TEXT NOT NULL, 
                        `language` TEXT NOT NULL, 
                        `notificationsEnabled` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shyam_ai_status_db"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
