package com.itpdf.app.domain.model

import java.util.UUID

/**
 * Represents the complete data structure for a CV/Resume.
 * Designed for Clean Architecture, ensuring immutability and thread-safety.
 */
data class CvData(
    val id: String = UUID.randomUUID().toString(),
    val templateId: String = "modern_01",
    val themeColor: String = "#2196F3",
    val personalInfo: PersonalInfo = PersonalInfo(),
    val educationList: List<Education> = emptyList(),
    val experienceList: List<Experience> = emptyList(),
    val skills: List<String> = emptyList(),
    val projects: List<Project> = emptyList(),
    val languages: List<LanguageEntry> = emptyList(),
    val lastModified: Long = System.currentTimeMillis()
)

data class PersonalInfo(
    val firstName: String = "",
    val lastName: String = "",
    val jobTitle: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val portfolioUrl: String = "",
    val linkedinUrl: String = "",
    val summary: String = "",
    val profileImageUri: String? = null
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

data class Education(
    val id: String = UUID.randomUUID().toString(),
    val schoolName: String = "",
    val degree: String = "",
    val fieldOfStudy: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val grade: String = "",
    val description: String = ""
)

data class Experience(
    val id: String = UUID.randomUUID().toString(),
    val companyName: String = "",
    val jobTitle: String = "",
    val location: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val description: String = "",
    val isCurrentRole: Boolean = false
)

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val link: String = "",
    val technologiesUsed: List<String> = emptyList()
)

data class LanguageEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val proficiency: String = "" // e.g., Native, Fluent, Intermediate
)

/**
 * Extension functions for immutable data manipulation within the Domain layer.
 */

fun CvData.updateEducation(updated: Education): CvData {
    val newList = educationList.map { if (it.id == updated.id) updated else it }
    return this.copy(educationList = if (educationList.any { it.id == updated.id }) newList else educationList + updated, lastModified = System.currentTimeMillis())
}

fun CvData.removeEducation(id: String): CvData {
    return this.copy(educationList = educationList.filterNot { it.id == id }, lastModified = System.currentTimeMillis())
}

fun CvData.updateExperience(updated: Experience): CvData {
    val newList = experienceList.map { if (it.id == updated.id) updated else it }
    return this.copy(experienceList = if (experienceList.any { it.id == updated.id }) newList else experienceList + updated, lastModified = System.currentTimeMillis())
}

fun CvData.removeExperience(id: String): CvData {
    return this.copy(experienceList = experienceList.filterNot { it.id == id }, lastModified = System.currentTimeMillis())
}

fun CvData.updateProject(updated: Project): CvData {
    val newList = projects.map { if (it.id == updated.id) updated else it }
    return this.copy(projects = if (projects.any { it.id == updated.id }) newList else projects + updated, lastModified = System.currentTimeMillis())
}

fun CvData.removeProject(id: String): CvData {
    return this.copy(projects = projects.filterNot { it.id == id }, lastModified = System.currentTimeMillis())
}

fun CvData.updateLanguage(updated: LanguageEntry): CvData {
    val newList = languages.map { if (it.id == updated.id) updated else it }
    return this.copy(languages = if (languages.any { it.id == updated.id }) newList else languages + updated, lastModified = System.currentTimeMillis())
}

fun CvData.removeLanguage(id: String): CvData {
    return this.copy(languages = languages.filterNot { it.id == id }, lastModified = System.currentTimeMillis())
}