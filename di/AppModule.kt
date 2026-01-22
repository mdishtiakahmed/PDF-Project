package com.itpdf.app.di

import android.content.Context
import androidx.room.Room
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.itpdf.app.data.local.AppDatabase
import com.itpdf.app.data.local.PdfDao
import com.itpdf.app.data.repository.AiRepositoryImpl
import com.itpdf.app.data.repository.PdfRepositoryImpl
import com.itpdf.app.data.repository.SettingsRepositoryImpl
import com.itpdf.app.domain.repository.AiRepository
import com.itpdf.app.domain.repository.PdfRepository
import com.itpdf.app.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for providing application-level singletons.
 * Optimized for production with Gemini 1.5 Flash for improved latency and token efficiency.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val DATABASE_NAME = "it_pdf_db"
    private const val GEMINI_API_KEY = "AIzaSyClB3oy5L_gkjJI0s6_ky12QjDBrnPcCmY"
    private const val MODEL_NAME = "gemini-1.5-flash"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun providePdfDao(database: AppDatabase): PdfDao {
        return database.pdfDao()
    }

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        val config = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 4096
        }

        return GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = GEMINI_API_KEY,
            generationConfig = config
        )
    }

    @Provides
    @Singleton
    fun providePdfRepository(
        pdfDao: PdfDao,
        @ApplicationContext context: Context
    ): PdfRepository {
        return PdfRepositoryImpl(pdfDao, context)
    }

    @Provides
    @Singleton
    fun provideAiRepository(
        generativeModel: GenerativeModel
    ): AiRepository {
        return AiRepositoryImpl(generativeModel)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }
}