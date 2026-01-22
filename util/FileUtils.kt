package com.itpdf.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * FileUtils.kt
 * 
 * Lead QA Refined: Utility for handling PDF file operations, Uri parsing, 
 * and system integration for the IT PDF application.
 */
object FileUtils {

    private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"
    private const val DEFAULT_PDF_NAME = "IT_PDF_Document.pdf"
    private const val DOCUMENTS_FOLDER = "IT_PDF"

    /**
     * Formats a long byte size into a human-readable string.
     * Logic refined for bounds safety and precision.
     */
    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt().coerceAtMost(units.size - 1)
        return try {
            val value = size / 1024.0.pow(digitGroups.toDouble())
            DecimalFormat("#,##0.#").format(value) + " " + units[digitGroups]
        } catch (e: Exception) {
            "Unknown Size"
        }
    }

    /**
     * Extracts the display name from a Uri.
     * Supports content providers and file paths.
     */
    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: DEFAULT_PDF_NAME
    }

    /**
     * Returns the app-specific directory for PDFs.
     */
    fun getAppPdfDirectory(context: Context): File {
        val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), DOCUMENTS_FOLDER)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }

    /**
     * Persists an InputStream to app storage. 
     * Refined with robust stream handling.
     */
    fun copyFileToAppStorage(context: Context, inputStream: InputStream, fileName: String): File? {
        return try {
            val targetFile = File(getAppPdfDirectory(context), fileName)
            inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            targetFile
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Launches the system share sheet.
     */
    fun sharePdf(context: Context, file: File) {
        if (!file.exists()) return

        try {
            val uri = getUriForFile(context, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Opens a PDF in an external viewer.
     */
    fun openPdf(context: Context, file: File) {
        if (!file.exists()) return

        try {
            val uri = getUriForFile(context, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // No handler for PDF - common in clean installs
        }
    }

    /**
     * Safe file deletion.
     */
    fun deleteFile(file: File): Boolean {
        return try {
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generates a unique filename. 
     * Supports Unicode (Bangla) by stripping only illegal filesystem characters.
     */
    fun generateUniqueFileName(prefix: String = "IT_PDF"): String {
        val timeStamp = System.currentTimeMillis()
        val illegalChars = Regex("[\\\\/:*?\"<>|]")
        val cleanedPrefix = prefix.replace(illegalChars, "_")
        return "${cleanedPrefix}_$timeStamp.pdf"
    }

    /**
     * Generates a FileProvider URI for the given file.
     */
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}$FILE_PROVIDER_AUTHORITY_SUFFIX",
            file
        )
    }

    /**
     * Efficiently retrieves file size via ContentResolver or File API.
     */
    fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (index != -1) return cursor.getLong(index)
                }
            }
        } else if (uri.scheme == "file") {
            uri.path?.let { return File(it).length() }
        }
        return 0L
    }

    /**
     * Gets file extension from Uri.
     */
    fun getFileExtension(context: Context, uri: Uri): String? {
        return if (uri.scheme == "content") {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        }
    }
}