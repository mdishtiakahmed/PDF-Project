package com.itpdf.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for IT PDF application.
 */
sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Tools : Screen

    @Serializable
    data object Files : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object ProUpgrade : Screen

    @Serializable
    data object AiGenerator : Screen

    @Serializable
    data class Editor(
        val documentId: String? = null,
        val initialContent: String? = null
    ) : Screen

    @Serializable
    data object CvBuilder : Screen

    @Serializable
    data class PdfPreview(val filePath: String) : Screen

    @Serializable
    data object ImageToPdf : Screen

    @Serializable
    data object AboutDeveloper : Screen
}

/**
 * Centralized Navigation Graph for the application.
 * Optimized with modern slide transitions and type-safe argument handling.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable<Screen.Splash>(
            enterTransition = { fadeIn(tween(500)) },
            exitTransition = { fadeOut(tween(500)) }
        ) {
            // SplashScreen(onTimeout = { 
            //    navController.navigate(Screen.Home) { popUpTo(Screen.Splash) { inclusive = true } } 
            // })
        }

        composable<Screen.Home> {
            // HomeScreen(
            //    onNavigateToTools = { navController.navigate(Screen.Tools) },
            //    onNavigateToFiles = { navController.navigate(Screen.Files) },
            //    onNavigateToEditor = { navController.navigate(Screen.Editor()) },
            //    onNavigateToCvBuilder = { navController.navigate(Screen.CvBuilder) },
            //    onNavigateToSettings = { navController.navigate(Screen.Settings) },
            //    onNavigateToPro = { navController.navigate(Screen.ProUpgrade) }
            // )
        }

        composable<Screen.Tools> {
            // ToolsScreen(
            //    onNavigateBack = { navController.popBackStack() },
            //    onToolSelected = { /* Handle tool logic */ }
            // )
        }

        composable<Screen.Files> {
            // FilesScreen(
            //    onFileClick = { path -> navController.navigate(Screen.PdfPreview(path)) },
            //    onNavigateBack = { navController.popBackStack() }
            // )
        }

        composable<Screen.Settings> {
            // SettingsScreen(
            //    onNavigateToAbout = { navController.navigate(Screen.AboutDeveloper) },
            //    onNavigateToPro = { navController.navigate(Screen.ProUpgrade) },
            //    onNavigateBack = { navController.popBackStack() }
            // )
        }

        composable<Screen.ProUpgrade> {
            // ProUpgradeScreen(onDismiss = { navController.popBackStack() })
        }

        composable<Screen.AiGenerator> {
            // AiGeneratorScreen(
            //    onResultGenerated = { content -> navController.navigate(Screen.Editor(initialContent = content)) },
            //    onNavigateBack = { navController.popBackStack() }
            // )
        }

        composable<Screen.Editor> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Editor>()
            // EditorScreen(
            //    documentId = args.documentId,
            //    initialContent = args.initialContent,
            //    onExportSuccess = { path -> navController.navigate(Screen.PdfPreview(path)) },
            //    onNavigateBack = { navController.popBackStack() }
            // )
        }

        composable<Screen.CvBuilder> {
            // CvBuilderScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<Screen.PdfPreview> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.PdfPreview>()
            // PdfPreviewScreen(
            //    filePath = args.filePath,
            //    onNavigateBack = { navController.popBackStack() }
            // )
        }

        composable<Screen.ImageToPdf> {
            // ImageToPdfScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<Screen.AboutDeveloper> {
            // AboutDeveloperScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

/**
 * Extension for Bottom Navigation tab switching.
 */
fun NavHostController.navigateTab(screen: Screen) {
    navigate(screen) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Clean navigation actions for ViewModels or Screen level logic.
 */
class AppNavigationActions(private val navController: NavHostController) {
    fun navigateToHome() {
        navController.navigate(Screen.Home) {
            popUpTo(Screen.Splash) { inclusive = true }
        }
    }

    fun navigateToEditor(content: String? = null) {
        navController.navigate(Screen.Editor(initialContent = content))
    }

    fun navigateToPreview(path: String) {
        navController.navigate(Screen.PdfPreview(filePath = path))
    }

    fun safeBack() {
        if (navController.currentBackStackEntry != null) {
            navController.popBackStack()
        }
    }
}