package com.itpdf.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.itpdf.app.data.local.dao.RecentFileDao
import com.itpdf.app.data.local.dao.UserProfileDao
import com.itpdf.app.data.local.entity.RecentFileEntity
import com.itpdf.app.data.local.entity.UserProfileEntity

/**
 * PdfDatabase serves as the primary database controller for IT PDF.
 * It encapsulates the Room database and provides access to local persisted data.
 *
 * Clean Architecture: Data Layer (Local Persistence)
 */
@Database(
    entities = [
        RecentFileEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PdfDatabase : RoomDatabase() {

    /**
     * Data Access Object for PDF metadata and history management.
     */
    abstract fun recentFileDao(): RecentFileDao

    /**
     * Data Access Object for user profile and CV-specific personal data.
     */
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: PdfDatabase? = null

        private const val DATABASE_NAME = "it_pdf_database"

        /**
         * Returns the thread-safe singleton instance of PdfDatabase.
         *
         * @param context The application context to prevent memory leaks.
         * @return The initialized Room database instance.
         */
        fun getDatabase(context: Context): PdfDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PdfDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}