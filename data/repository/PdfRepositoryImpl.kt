package com.itpdf.app.data.repository

import android.content.Context
import com.itpdf.app.data.local.dao.PdfDao
import com.itpdf.app.data.local.entities.PdfEntity
import com.itpdf.app.domain.model.PdfDocument
import com.itpdf.app.domain.repository.PdfRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfRepositoryImpl @Inject constructor(
    private val pdfDao: PdfDao,
    @ApplicationContext private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PdfRepository {

    override fun getAllPdfs(): Flow<List<PdfDocument>> {
        return pdfDao.getAllPdfs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPdfById(id: Long): Flow<PdfDocument?> {
        return pdfDao.getPdfById(id).map { it?.toDomain() }
    }

    override fun searchPdfs(query: String): Flow<List<PdfDocument>> {
        return pdfDao.searchPdfs("%$query%").map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun savePdf(
        pdfDocument: PdfDocument,
        pdfBytes: ByteArray
    ): Result<Long> = withContext(ioDispatcher) {
        try {
            val directory = File(context.getExternalFilesDir(null), "IT_PDF_Documents")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val sanitizedBaseName = pdfDocument.fileName.substringBeforeLast(".").ifEmpty { "document" }
            val extension = pdfDocument.fileName.substringAfterLast(".", "pdf")
            
            var finalFile = File(directory, "$sanitizedBaseName.$extension")
            var counter = 1
            while (finalFile.exists()) {
                finalFile = File(directory, "${sanitizedBaseName}_$counter.$extension")
                counter++
            }

            FileOutputStream(finalFile).use { output ->
                output.write(pdfBytes)
                output.flush()
            }

            val entity = PdfEntity(
                title = pdfDocument.title,
                fileName = finalFile.name,
                filePath = finalFile.absolutePath,
                fileSize = finalFile.length(),
                createdAt = System.currentTimeMillis(),
                isFavorite = pdfDocument.isFavorite,
                category = pdfDocument.category
            )

            val id = pdfDao.insertPdf(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePdfMetadata(pdfDocument: PdfDocument): Result<Unit> = withContext(ioDispatcher) {
        try {
            val existingEntity = pdfDao.getPdfByIdSync(pdfDocument.id)
                ?: return@withContext Result.failure(Exception("Document not found"))

            var currentPath = existingEntity.filePath
            var currentFileName = existingEntity.fileName

            if (existingEntity.title != pdfDocument.title) {
                val renameResult = renamePhysicalFile(existingEntity, pdfDocument.title)
                if (renameResult.isSuccess) {
                    val newFile = File(renameResult.getOrThrow())
                    currentPath = newFile.absolutePath
                    currentFileName = newFile.name
                }
            }

            pdfDao.updatePdf(
                existingEntity.copy(
                    title = pdfDocument.title,
                    fileName = currentFileName,
                    filePath = currentPath,
                    isFavorite = pdfDocument.isFavorite,
                    category = pdfDocument.category
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePdf(pdfDocument: PdfDocument): Result<Unit> = withContext(ioDispatcher) {
        try {
            val file = File(pdfDocument.filePath)
            if (file.exists() && !file.delete()) {
                return@withContext Result.failure(IOException("Failed to delete physical file"))
            }
            pdfDao.deletePdfById(pdfDocument.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun renamePhysicalFile(entity: PdfEntity, newTitle: String): Result<String> {
        val oldFile = File(entity.filePath)
        if (!oldFile.exists()) return Result.failure(IOException("Source file not found"))

        val parentDir = oldFile.parentFile
        val extension = oldFile.extension.ifEmpty { "pdf" }
        val sanitizedTitle = newTitle.replace(Regex("[^a-zA-Z0-9.-]"), "_").ifEmpty { "untitled" }
        
        var newFile = File(parentDir, "$sanitizedTitle.$extension")
        var counter = 1
        while (newFile.exists() && newFile.absolutePath != oldFile.absolutePath) {
            newFile = File(parentDir, "${sanitizedTitle}_$counter.$extension")
            counter++
        }

        return if (oldFile.renameTo(newFile)) {
            Result.success(newFile.absolutePath)
        } else {
            Result.failure(IOException("Rename operation failed"))
        }
    }

    private fun PdfEntity.toDomain(): PdfDocument {
        return PdfDocument(
            id = this.id,
            title = this.title,
            fileName = this.fileName,
            filePath = this.filePath,
            fileSize = this.fileSize,
            createdAt = this.createdAt,
            isFavorite = this.isFavorite,
            category = this.category
        )
    }
}