package com.itpdf.app.ui.screens.cv

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.itpdf.app.domain.model.cv.*

/**
 * CvWizardScreen: A multi-step form for building resumes with integrated AI suggestions.
 * Follows Clean Architecture and Material 3 design principles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvWizardScreen(
    navController: NavController,
    viewModel: CvWizardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "CV Builder Wizard",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            WizardBottomBar(
                currentStep = uiState.currentStep,
                totalSteps = 6,
                onPrevious = { viewModel.moveToPreviousStep() },
                onNext = { 
                    if (uiState.currentStep == 5) {
                        viewModel.finalizeCv { cvId ->
                            navController.navigate("cv_editor/$cvId")
                        }
                    } else {
                        viewModel.moveToNextStep() 
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            StepIndicator(
                currentStep = uiState.currentStep,
                totalSteps = 6
            )

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = uiState.currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                        }.using(SizeTransform(clip = false))
                    },
                    label = "StepTransition"
                ) { step ->
                    when (step) {
                        0 -> PersonalInfoStep(uiState.personalInfo, viewModel::updatePersonalInfo)
                        1 -> ObjectiveStep(
                            objective = uiState.objective,
                            onUpdate = viewModel::updateObjective,
                            onGenerateAi = { viewModel.generateAiObjective() },
                            isAiLoading = uiState.isAiLoading
                        )
                        2 -> EducationStep(uiState.educationList, viewModel::addEducation, viewModel::removeEducation)
                        3 -> ExperienceStep(uiState.experienceList, viewModel::addExperience, viewModel::removeExperience)
                        4 -> SkillsStep(uiState.skills, viewModel::updateSkills)
                        5 -> ProjectsStep(uiState.projects, viewModel::addProject, viewModel::removeProject)
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    val steps = listOf("Info", "Goal", "Edu", "Work", "Skills", "Projects")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, title ->
            val isActive = index <= currentStep
            val isCurrent = index == currentStep
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < currentStep) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text(
                            text = (index + 1).toString(),
                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (index < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .offset(y = (-10).dp),
                    thickness = 2.dp,
                    color = if (index < currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
fun PersonalInfoStep(data: PersonalInfo, onUpdate: (PersonalInfo) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle(title = "Personal Details", icon = Icons.Default.Person)
        
        CvTextField(value = data.fullName, label = "Full Name", onValueChange = { onUpdate(data.copy(fullName = it)) })
        CvTextField(value = data.email, label = "Email Address", onValueChange = { onUpdate(data.copy(email = it)) })
        CvTextField(value = data.phone, label = "Phone Number", onValueChange = { onUpdate(data.copy(phone = it)) })
        CvTextField(value = data.address, label = "Address", onValueChange = { onUpdate(data.copy(address = it)) })
        CvTextField(value = data.linkedIn, label = "LinkedIn Profile (Optional)", onValueChange = { onUpdate(data.copy(linkedIn = it)) })
        CvTextField(value = data.website, label = "Portfolio/Website (Optional)", onValueChange = { onUpdate(data.copy(website = it)) })
    }
}

@Composable
fun ObjectiveStep(
    objective: String,
    onUpdate: (String) -> Unit,
    onGenerateAi: () -> Unit,
    isAiLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle(title = "Career Objective", icon = Icons.Default.Info)
        
        Text(
            text = "Describe your professional goals or use AI to generate a summary based on your details.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = objective,
            onValueChange = onUpdate,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            label = { Text("Professional Summary") },
            placeholder = { Text("Example: Experienced developer specialized in mobile architecture...") }
        )

        Button(
            onClick = onGenerateAi,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAiLoading,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            if (isAiLoading) {
                CircularProgressIndicator(size = 20.dp, color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("AI is generating...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generate with AI")
            }
        }
    }
}

@Composable
fun EducationStep(
    educationList: List<Education>,
    onAdd: (Education) -> Unit,
    onRemove: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle(title = "Education", icon = Icons.Default.School)
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(educationList) { index, edu ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(edu.degree, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(edu.institution, style = MaterialTheme.typography.bodySmall)
                            Text("${edu.startYear} - ${edu.endYear}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onRemove(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { onAdd(Education("New Degree", "Institution", "2020", "2024")) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Add Education")
        }
    }
}

@Composable
fun ExperienceStep(
    experienceList: List<Experience>,
    onAdd: (Experience) -> Unit,
    onRemove: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle(title = "Work Experience", icon = Icons.Default.Work)
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(experienceList) { index, exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exp.jobTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(exp.company, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onRemove(index) }) {
                            Icon(Icons.Default.Delete, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { onAdd(Experience("Job Title", "Company", "2021", "Present", "")) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(4.dp))
            Text("Add Experience")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsStep(skills: List<String>, onUpdate: (List<String>) -> Unit) {
    var skillInput by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle(title = "Key Skills", icon = Icons.Default.Build)
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = skillInput,
                onValueChange = { skillInput = it },
                modifier = Modifier.weight(1f),
                label = { Text("Skill (e.g. Kotlin, UI Design)") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = {
                    if (skillInput.isNotBlank()) {
                        onUpdate(skills + skillInput.trim())
                        skillInput = ""
                    }
                },
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp
        ) {
            skills.forEach { skill ->
                InputChip(
                    selected = true,
                    onClick = { onUpdate(skills - skill) },
                    label = { Text(skill) },
                    trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) },
                    shape = CircleShape
                )
            }
        }
    }
}

@Composable
fun ProjectsStep(
    projects: List<Project>,
    onAdd: (Project) -> Unit,
    onRemove: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionTitle(title = "Projects", icon = Icons.Default.Code)
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(projects) { index, project ->
                ListItem(
                    headlineContent = { Text(project.title, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(project.description) },
                    trailingContent = {
                        IconButton(onClick = { onRemove(index) }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                )
            }
        }

        OutlinedButton(
            onClick = { onAdd(Project("New Project", "Description", "https://github.com/...")) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(4.dp))
            Text("Add Project")
        }
    }
}

@Composable
fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CvTextField(value: String, label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun WizardBottomBar(
    currentStep: Int,
    totalSteps: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onPrevious,
                enabled = currentStep > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text("Previous")
            }

            Spacer(Modifier.width(16.dp))

            Button(
                onClick = onNext,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.5f)
            ) {
                Text(if (currentStep == totalSteps - 1) "Finish & Preview" else "Continue")
                if (currentStep < totalSteps - 1) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: Dp = 0.dp,
    crossAxisSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        val mainSpacingPx = mainAxisSpacing.roundToPx()
        val crossSpacingPx = crossAxisSpacing.roundToPx()

        placeables.forEach { placeable ->
            if (currentRowWidth + placeable.width + mainSpacingPx > layoutWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + mainSpacingPx
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow)

        val layoutHeight = rows.sumOf { row -> row.maxOf { it.height } } + (rows.size - 1).coerceAtLeast(0) * crossSpacingPx
        
        layout(layoutWidth, layoutHeight) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val rowHeight = row.maxOf { it.height }
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + mainSpacingPx
                }
                y += rowHeight + crossSpacingPx
            }
        }
    }
}