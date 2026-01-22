package com.itpdf.app.engine

import android.content.Context
import android.util.Log
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.io.font.FontProgramFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * FontManager Utility
 *
 * Handles font embedding for PDF generation with a specific focus on
 * Bangla Unicode support using iText 7. This ensures that complex
 * script rendering (ligatures, glyph positioning) is handled correctly.
 */
class FontManager private constructor(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        private const val TAG = "FontManager"
        
        const val FONT_BANGLA_REGULAR = "hind_siliguri_regular.ttf"
        const val FONT_BANGLA_BOLD = "hind_siliguri_bold.ttf"
        const val FONT_ENGLISH_REGULAR = "roboto_regular.ttf"
        const val FONT_ENGLISH_BOLD = "roboto_bold.ttf"
        
        @Volatile
        private var instance: FontManager? = null

        fun getInstance(context: Context): FontManager {
            return instance ?: synchronized(this) {
                instance ?: FontManager(context).also { instance = it }
            }
        }
    }

    private val fontCache = ConcurrentHashMap<String, PdfFont>()

    /**
     * Retrieves a PdfFont instance for Bangla text.
     */
    fun getBanglaFont(isBold: Boolean = false): PdfFont {
        val fontName = if (isBold) FONT_BANGLA_BOLD else FONT_BANGLA_REGULAR
        return getFont(fontName)
    }

    /**
     * Retrieves a PdfFont instance for English text.
     */
    fun getEnglishFont(isBold: Boolean = false): PdfFont {
        val fontName = if (isBold) FONT_ENGLISH_BOLD else FONT_ENGLISH_REGULAR
        return getFont(fontName)
    }

    /**
     * Loads a font from assets, caches it, and returns the PdfFont.
     * Identity-H encoding is mandatory for Bangla to ensure glyphs render correctly.
     */
    fun getFont(fontFileName: String): PdfFont {
        return fontCache.getOrPut(fontFileName) {
            try {
                val fontPath = getFontPath(fontFileName)
                PdfFontFactory.createFont(
                    fontPath,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading font $fontFileName: ${e.message}")
                // Fallback to internal font to prevent crash, though Bangla will not render correctly
                PdfFontFactory.createFont()
            }
        }
    }

    /**
     * iText requires a physical file path. This helper copies the font from assets 
     * to internal storage if not already present. Optimized with synchronization 
     * for thread safety during file I/O.
     */
    private fun getFontPath(fontFileName: String): String {
        val fontFolder = File(appContext.cacheDir, "fonts")
        if (!fontFolder.exists()) fontFolder.mkdirs()
        
        val fontFile = File(fontFolder, fontFileName)
        if (!fontFile.exists()) {
            synchronized(this) {
                if (!fontFile.exists()) {
                    try {
                        appContext.assets.open("fonts/$fontFileName").use { inputStream ->
                            fontFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Failed to copy font asset: $fontFileName", e)
                    }
                }
            }
        }
        return fontFile.absolutePath
    }

    /**
     * Checks if a string contains Bangla characters to determine which font to apply.
     */
    fun containsBangla(text: String?): Boolean {
        if (text == null) return false
        return text.any { Character.UnicodeBlock.of(it) == Character.UnicodeBlock.BENGALI }
    }

    /**
     * Clear cache to free up memory when PDF generation session is finished.
     */
    fun clearCache() {
        fontCache.clear()
    }
    
    /**
     * Returns a FontProgram for advanced layout rendering via iText Calligraph/Typography.
     */
    fun getFontProgram(fontFileName: String) = FontProgramFactory.createFont(getFontPath(fontFileName))
}