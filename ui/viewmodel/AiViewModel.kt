package com.itpdf.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itpdf.app.data.repository.GeminiRepository
import com.itpdf.app.domain.model.AiPromptType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * UI State for AI Operations
 * Representing the immutable state of the AI generation screen.
 */
data class AiUiState(
    val isLoading: Boolean = false,
    val generatedText: String = "",
    val error: String? = null,
    val remainingDailyLimit: Int = 5,
    val isProUser: Boolean = false
)

/**
 * AiViewModel handles the logic for interacting with the Gemini AI API.
 * Adheres to Clean Architecture and MVVM principles.
 */
class AiViewModel(
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    private var lastRequestTimestamp: Long = 0
    private var dailyRequestCount = 0
    private val maxFreeDailyRequests = 5

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        // Implementation would interface with Billing/Preferences Repository
        // For now, initializing the UI state with default limits
        _uiState.update { 
            it.copy(
                isProUser = false, 
                remainingDailyLimit = maxFreeDailyRequests 
            ) 
        }
    }

    /**
     * Executes AI generation based on user input and selected prompt type.
     * Includes validation, rate-limiting, and state management.
     */
    fun processAiAction(input: String, type: AiPromptType) {
        val currentInput = input.trim()
        
        if (currentInput.isEmpty()) {
            _uiState.update { it.copy(error = "Input text cannot be empty.") }
            return
        }

        if (!canMakeRequest()) {
            _uiState.update { it.copy(error = "Daily limit reached. Upgrade to Pro for unlimited AI.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val prompt = buildPrompt(currentInput, type)
                val response = geminiRepository.generateContent(prompt)
                
                if (!response.isNullOrBlank()) {
                    dailyRequestCount++
                    lastRequestTimestamp = System.currentTimeMillis()
                    
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false, 
                            generatedText = response,
                            remainingDailyLimit = if (state.isProUser) 999 else (maxFreeDailyRequests - dailyRequestCount).coerceAtLeast(0)
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "The AI returned an empty response. Please try rephrasing." 
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.localizedMessage ?: "A network error occurred. Please check your connection." 
                    ) 
                }
            }
        }
    }

    /**
     * Constructs optimized prompts for Gemini to ensure high-quality output.
     * Designed to support professional CV generation and multi-language support (Bangla/English).
     */
    private fun buildPrompt(input: String, type: AiPromptType): String {
        return when (type) {
            AiPromptType.IMPROVE -> 
                "Act as a professional editor. Improve the following text for clarity, grammar, and professional flow. Maintain the original language (English or Bangla): \n\n$input"
            
            AiPromptType.PROFESSIONAL -> 
                "Rewrite the following content to be highly professional, formal, and sophisticated. Suitable for executive documents: \n\n$input"
            
            AiPromptType.CV_GENERATION -> 
                "Convert the following information into a structured professional CV. Use clear headings: SUMMARY, EXPERIENCE, EDUCATION, and SKILLS. Ensure the tone is recruiter-friendly. If the input is in Bangla, generate the CV in Bangla: \n\n$input"
            
            AiPromptType.TRANSLATE_TO_EN -> 
                "Translate the following text into fluent, natural English, ensuring professional terminology is used: \n\n$input"
            
            AiPromptType.TRANSLATE_TO_BN -> 
                "Translate the following text into standard, natural Bangla (Bengali): \n\n$input"
            
            AiPromptType.SUMMARIZE -> 
                "Provide a concise summary of the following text using professional bullet points: \n\n$input"
        }
    }

    /**
     * Validates if the user is eligible to make an API request based on tier and daily usage.
     */
    private fun canMakeRequest(): Boolean {
        if (_uiState.value.isProUser) return true
        
        val now = Calendar.getInstance()
        val lastReq = Calendar.getInstance().apply { timeInMillis = lastRequestTimestamp }
        
        // Reset counter if the current request is on a different day than the last successful request
        if (lastRequestTimestamp != 0L && 
            (now.get(Calendar.DAY_OF_YEAR) != lastReq.get(Calendar.DAY_OF_YEAR) || 
             now.get(Calendar.YEAR) != lastReq.get(Calendar.YEAR))) {
            dailyRequestCount = 0
            _uiState.update { it.copy(remainingDailyLimit = maxFreeDailyRequests) }
        }
        
        return dailyRequestCount < maxFreeDailyRequests
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetGeneratedContent() {
        _uiState.update { it.copy(generatedText = "") }
    }

    fun updateGeneratedText(newText: String) {
        _uiState.update { it.copy(generatedText = newText) }
    }
}