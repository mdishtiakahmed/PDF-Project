package com.itpdf.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.itpdf.app.navigation.NavRoutes
import com.itpdf.app.navigation.BottomNavigationBar
import com.itpdf.app.ui.screens.splash.SplashScreen
import com.itpdf.app.ui.screens.home.HomeScreen
import com.itpdf.app.ui.screens.tools.ToolsScreen
import com.itpdf.app.ui.screens.files.FilesScreen
import com.itpdf.app.ui.screens.profile.ProfileScreen
import com.itpdf.app.ui.screens.editor.EditorScreen
import com.itpdf.app.ui.screens.cv.CVWizardScreen
import com.itpdf.app.ui.screens.pro.ProUpgradeScreen
import com.itpdf.app.ui.screens.viewer.PdfViewerScreen
import com.itpdf.app.ui.theme.ITPDFTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        // Initialize AdMob SDK. It handles its own threading.
        MobileAds.initialize(this) {}

        setContent {
            ITPDFTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = remember(currentRoute) {
                    currentRoute in listOf(
                        NavRoutes.Home.route,
                        NavRoutes.Tools.route,
                        NavRoutes.Files.route,
                        NavRoutes.Profile.route
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.navigationBars,
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route,
        modifier = modifier
    ) {
        composable(NavRoutes.Splash.route) {
            SplashScreen(
                onNavigationComplete = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Home.route) {
            HomeScreen(
                onNavigateToEditor = { docId ->
                    navController.navigate(NavRoutes.Editor.createRoute(docId))
                },
                onNavigateToCVWizard = {
                    navController.navigate(NavRoutes.CVWizard.route)
                },
                onNavigateToPro = {
                    navController.navigate(NavRoutes.ProUpgrade.route)
                }
            )
        }

        composable(NavRoutes.Tools.route) {
            ToolsScreen(
                onToolClick = { toolType ->
                    // Logic for specific PDF tools navigation handled in ViewModel
                }
            )
        }

        composable(NavRoutes.Files.route) {
            FilesScreen(
                onFileClick = { filePath ->
                    navController.navigate(NavRoutes.PdfViewer.createRoute(filePath))
                }
            )
        }

        composable(NavRoutes.Profile.route) {
            ProfileScreen(
                onNavigateToPro = {
                    navController.navigate(NavRoutes.ProUpgrade.route)
                },
                onAboutDeveloper = {
                    // Navigate to Dev Info screen implementation
                }
            )
        }

        composable(
            route = NavRoutes.Editor.route,
            arguments = NavRoutes.Editor.arguments
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId")
            EditorScreen(
                documentId = documentId,
                onBack = { navController.popBackStack() },
                onExportSuccess = { pdfPath ->
                    navController.navigate(NavRoutes.PdfViewer.createRoute(pdfPath)) {
                        popUpTo(NavRoutes.Editor.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.CVWizard.route) {
            CVWizardScreen(
                onBack = { navController.popBackStack() },
                onComplete = { cvData ->
                    navController.navigate(NavRoutes.Editor.createRoute("new_cv"))
                }
            )
        }

        composable(
            route = NavRoutes.PdfViewer.route,
            arguments = NavRoutes.PdfViewer.arguments
        ) { backStackEntry ->
            val filePath = backStackEntry.arguments?.getString("filePath") ?: ""
            PdfViewerScreen(
                filePath = filePath,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ProUpgrade.route) {
            ProUpgradeScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}