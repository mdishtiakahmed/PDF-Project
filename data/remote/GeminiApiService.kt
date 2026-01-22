package com.itpdf.app.data.remote

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Result Wrapper for API responses to handle UI state in a Clean Architecture flow.
 */
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * GeminiApiService handles communication with Google's Generative AI SDK.
 * Refined for production use with strict prompt engineering and error handling.
 */
class GeminiApiService {

    companion object {
        private const val TAG = "GeminiApiService"
        private const val API_KEY = "AIzaSyClB3oy5L_gkjJI0s6_ky12QjDBrnPcCmY"
        private const val MODEL_NAME = "gemini-1.5-flash"
    }

    private val config = generationConfig {
        temperature = 0.4f // Lowered for more consistent, professional output
        topK = 32
        topP = 0.95f
        maxOutputTokens = 4096
    }

    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
    )

    private val generativeModel = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = API_KEY,
        generationConfig = config,
        safetySettings = safetySettings
    )

    /**
     * Core function to generate content. 
     * Includes logic to strip Markdown artifacts for clean PDF rendering.
     */
    private suspend fun generateContent(prompt: String): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text?.replace("```markdown", "")?.replace("```", "")?.trim()
            
            if (!responseText.isNullOrBlank()) {
                ApiResult.Success(responseText)
            } else {
                ApiResult.Error("AI returned an empty response. Please try again.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API Error: ${e.message}", e)
            ApiResult.Error(e.localizedMessage ?: "An unexpected error occurred", e)
        }
    }

    /**
     * Refines text for professional use. Supports Bangla and English.
     */
    suspend fun improveText(inputText: String, isBangla: Boolean): ApiResult<String> {
        val prompt = if (isBangla) {
            "নিচের টেক্সটটিকে আরও সুন্দর, মার্জিত এবং পেশাদার করে দিন। কোনো বাড়তি কথা লিখবেন না, শুধুমাত্র সংশোধিত টেক্সটটুকু দিন:\n\n$inputText"
        } else {
            "Refine the following text to be professional and grammatically perfect. Provide ONLY the corrected text without any introductory remarks:\n\n$inputText"
        }
        return generateContent(prompt)
    }

    /**
     * Generates structured CV content.
     */
    suspend fun generateCvContent(rawDetails: String, language: String = "English"): ApiResult<String> {
        val prompt = """
            Act as a professional CV writer. Create a high-quality CV based on the details provided.
            Use these exact sections: PROFESSIONAL SUMMARY, EXPERIENCE, EDUCATION, and SKILLS.
            Language: $language
            Details: $rawDetails
            Instruction: Provide only the formatted text. Do not include chat-like responses or markdown formatting blocks.
        """.trimIndent()
        return generateContent(prompt)
    }

    /**
     * Translates content while maintaining professional context.
     */
    suspend fun translateText(text: String, targetLanguage: String): ApiResult<String> {
        val prompt = "Translate the following text to $targetLanguage. Keep the tone professional and maintain the original formatting:\n\n$text"
        return generateContent(prompt)
    }

    /**
     * Provides career objectives or skill lists based on job title.
     */
    suspend fun getSuggestions(title: String, type: SuggestionType): ApiResult<String> {
        val prompt = when (type) {
            SuggestionType.SKILLS -> "List 10 highly relevant professional skills for a $title as a comma-separated list. No preamble."
            SuggestionType.OBJECTIVE -> "Provide 3 high-impact professional career objectives for a $title. Separate them with new lines."
        }
        return generateContent(prompt)
    }

    enum class SuggestionType {
        SKILLS, OBJECTIVE
    }
}