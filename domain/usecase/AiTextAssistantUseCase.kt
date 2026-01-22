package com.itpdf.app.domain.usecase

import com.itpdf.app.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * AiTextAssistantUseCase
 *
 * This UseCase handles business logic for AI-powered text manipulations.
 * It interacts with the AiRepository to process prompts using the Gemini API.
 */
class AiTextAssistantUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {

    /**
     * Enhances the input text for better grammar, clarity, and flow.
     */
    fun improveText(text: String): Flow<Result<String>> = flow {
        if (text.isBlank()) {
            emit(Result.failure(IllegalArgumentException("Input text cannot be empty")))
            return@flow
        }

        val prompt = """
            Instruction: Improve the following text for clarity, grammar, and natural flow.
            Rules:
            1. Keep the original language (Bangla or English).
            2. Maintain the original meaning and intent.
            3. Fix any spelling or punctuation errors.
            
            Text to improve:
            ---
            $text
            ---
        """.trimIndent()

        emit(aiRepository.generateContent(prompt))
    }

    /**
     * Converts the input text into a formal, professional business tone.
     */
    fun makeProfessional(text: String): Flow<Result<String>> = flow {
        if (text.isBlank()) {
            emit(Result.failure(IllegalArgumentException("Input text cannot be empty")))
            return@flow
        }

        val prompt = """
            Instruction: Rewrite the following text in a professional, formal, and sophisticated tone.
            Context: Corporate communication, CV, or official document.
            Rules:
            1. Maintain the language of the input (Bangla or English).
            2. Use high-quality vocabulary.
            
            Text to transform:
            ---
            $text
            ---
        """.trimIndent()

        emit(aiRepository.generateContent(prompt))
    }

    /**
     * Translates text between Bangla and English.
     */
    fun translate(text: String, targetLanguage: String): Flow<Result<String>> = flow {
        if (text.isBlank()) {
            emit(Result.failure(IllegalArgumentException("Input text cannot be empty")))
            return@flow
        }

        val prompt = """
            Instruction: Translate the following text into $targetLanguage.
            Rules:
            1. Ensure the translation sounds natural to a native speaker.
            2. Maintain the original professional context and formatting.
            3. If the input contains technical terms, provide the most appropriate equivalent.
            
            Text:
            ---
            $text
            ---
        """.trimIndent()

        emit(aiRepository.generateContent(prompt))
    }

    /**
     * Generates structured CV content based on user input.
     */
    fun generateCvContent(userInput: String, sectionType: CvSectionType): Flow<Result<String>> = flow {
        if (userInput.isBlank()) {
            emit(Result.failure(IllegalArgumentException("Input cannot be empty")))
            return@flow
        }

        val prompt = when (sectionType) {
            CvSectionType.PROFESSIONAL_SUMMARY -> """
                Instruction: Generate a professional CV summary (3-4 impactful sentences) based on the provided details.
                Details: $userInput
                Focus: Core competencies, years of experience, and key achievements.
            """.trimIndent()
            
            CvSectionType.WORK_EXPERIENCE -> """
                Instruction: Transform the following work experience notes into professional, achievement-oriented bullet points.
                Rules: 
                1. Use strong action verbs (e.g., Spearheaded, Optimized, Orchestrated).
                2. Quantify achievements where possible.
                Notes: $userInput
            """.trimIndent()
            
            CvSectionType.SKILLS_SUGGESTION -> """
                Instruction: Based on the following job role or field, list the top 10 most relevant technical and soft skills.
                Role: $userInput
                Format: Clean, comma-separated list or bullet points.
            """.trimIndent()
        }

        emit(aiRepository.generateContent(prompt))
    }

    /**
     * Generic execution for custom user prompts within the AI Assistant.
     */
    fun processCustomPrompt(userPrompt: String): Flow<Result<String>> = flow {
        if (userPrompt.isBlank()) {
            emit(Result.failure(IllegalArgumentException("Prompt cannot be empty")))
            return@flow
        }
        emit(aiRepository.generateContent(userPrompt))
    }
}

/**
 * Defines specific sections of a CV for targeted AI generation.
 */
enum class CvSectionType {
    PROFESSIONAL_SUMMARY,
    WORK_EXPERIENCE,
    SKILLS_SUGGESTION
}