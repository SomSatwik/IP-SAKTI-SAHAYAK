package com.ipsakti.sahayak.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ipsakti.sahayak.ui.screens.evidence.EvidenceDetailScreen
import com.ipsakti.sahayak.ui.screens.evidence.EvidenceListScreen
import com.ipsakti.sahayak.ui.screens.graph.EvidenceGraphScreen
import com.ipsakti.sahayak.ui.screens.home.HomeScreen
import com.ipsakti.sahayak.ui.screens.investigate.InvestigateScreen
import com.ipsakti.sahayak.ui.screens.result.ResultScreen
import com.ipsakti.sahayak.ui.screens.roadmap.ComplianceRoadmapScreen
import com.ipsakti.sahayak.ui.screens.saved.SavedScreen
import com.ipsakti.sahayak.ui.screens.settings.SettingsScreen
import com.ipsakti.sahayak.ui.screens.upload.UploadScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Bottom Nav Screens
        composable(Screen.Home.route) {
            HomeScreen(
                onStartInvestigation = {
                    navController.navigate(Screen.Investigate.route)
                },
                onOpenInvestigation = { id ->
                    navController.navigate(Screen.Result.createRoute(id))
                },
                onLoadDemoCase = {
                    navController.navigate(Screen.Result.createRoute("demo_ayurvedic_01"))
                },
                onOpenUpload = {
                    navController.navigate(Screen.DocumentUpload.route)
                }
            )
        }

        composable(Screen.Investigate.route) {
            InvestigateScreen(
                onInvestigationCompleted = { investigationId ->
                    navController.navigate(Screen.Result.createRoute(investigationId)) {
                        popUpTo(Screen.Investigate.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EvidenceList.route) {
            EvidenceListScreen(
                onViewEvidence = { evidenceId ->
                    navController.navigate(Screen.EvidenceDetail.createRoute(evidenceId))
                }
            )
        }

        composable(Screen.Saved.route) {
            SavedScreen(
                onOpenInvestigation = { id ->
                    navController.navigate(Screen.Result.createRoute(id))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        // Sub-screens
        composable(
            route = Screen.Result.route,
            arguments = listOf(navArgument("investigationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val investigationId = backStackEntry.arguments?.getString("investigationId") ?: ""
            ResultScreen(
                investigationId = investigationId,
                onNavigateBack = { navController.popBackStack() },
                onViewEvidence = { evidenceId ->
                    navController.navigate(Screen.EvidenceDetail.createRoute(evidenceId))
                },
                onViewGraph = { id ->
                    navController.navigate(Screen.EvidenceGraph.createRoute(id))
                },
                onViewRoadmap = { id ->
                    navController.navigate(Screen.ComplianceRoadmap.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.EvidenceDetail.route,
            arguments = listOf(navArgument("evidenceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val evidenceId = backStackEntry.arguments?.getString("evidenceId") ?: ""
            EvidenceDetailScreen(
                evidenceId = evidenceId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EvidenceGraph.route,
            arguments = listOf(navArgument("investigationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val investigationId = backStackEntry.arguments?.getString("investigationId") ?: ""
            EvidenceGraphScreen(
                investigationId = investigationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ComplianceRoadmap.route,
            arguments = listOf(navArgument("investigationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val investigationId = backStackEntry.arguments?.getString("investigationId") ?: ""
            ComplianceRoadmapScreen(
                investigationId = investigationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DocumentUpload.route) {
            UploadScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
