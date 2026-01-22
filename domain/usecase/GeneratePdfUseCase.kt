package com.itpdf.app.domain.usecase

import com.itpdf.app.domain.model.PdfDocumentConfig
import com.itpdf.app.domain.repository.PdfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * GeneratePdfUseCase
 *
 * Orchestrates the business logic for converting document configurations,
 * UI states, or CV data into a physical PDF file.
 */
class GeneratePdfUseCase @Inject constructor(
    private val pdfRepository: PdfRepository
) {

    /**
     * Executes the PDF generation process.
     *
     * @param config The domain model containing rendering instructions.
     * @return Result<File> containing the successfully created file or an error.
     */
    suspend operator fun invoke(config: PdfDocumentConfig): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. Validation Logic
            validateConfig(config).getOrThrow()

            // 2. Data Sanitization
            val sanitizedConfig = sanitizeConfig(config)

            // 3. Orchestrate Generation via Repository
            val file = pdfRepository.generatePdf(sanitizedConfig)

            // 4. Final verification of output
            if (!file.exists() || file.length() == 0L) {
                throw IllegalStateException("PDF engine failed to write data: ${file.absolutePath}")
            }
            
            file
        }
    }

    /**
     * Internal validation to ensure the request is logical.
     */
    private fun validateConfig(config: PdfDocumentConfig): Result<Unit> {
        return when {
            config.pages.isEmpty() -> {
                Result.failure(IllegalArgumentException("Cannot generate a document with zero pages."))
            }
            config.fileName.isBlank() -> {
                Result.failure(IllegalArgumentException("File name cannot be empty."))
            }
            else -> Result.success(Unit)
        }
    }

    /**
     * Sanitizes data to prevent file system issues or metadata corruption.
     */
    private fun sanitizeConfig(config: PdfDocumentConfig): PdfDocumentConfig {
        val cleanName = config.fileName
            .trim()
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .let { name ->
                if (name.endsWith(".pdf", ignoreCase = true)) name else "$name.pdf"
            }

        return config.copy(
            fileName = cleanName,
            author = config.author.takeIf { it.isNotBlank() } ?: "IT PDF"
        )
    }
}