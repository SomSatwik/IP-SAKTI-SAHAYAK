package com.ipsakti.sahayak.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Bottom Bar items
    object Home : Screen("home", "Home", Icons.Default.Dashboard)
    object Investigate : Screen("investigate", "Investigate", Icons.Default.Search)
    object EvidenceList : Screen("evidence_list", "Evidence", Icons.Default.MenuBook)
    object Saved : Screen("saved", "Saved", Icons.Default.Folder)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    // Sub-screens
    object Result : Screen("result/{investigationId}", "Investigation Report") {
        fun createRoute(investigationId: String) = "result/$investigationId"
    }

    object EvidenceDetail : Screen("evidence_detail/{evidenceId}", "Authoritative Evidence") {
        fun createRoute(evidenceId: String) = "evidence_detail/$evidenceId"
    }

    object EvidenceGraph : Screen("graph/{investigationId}", "Evidence Graph") {
        fun createRoute(investigationId: String) = "graph/$investigationId"
    }

    object ComplianceRoadmap : Screen("roadmap/{investigationId}", "Compliance Roadmap") {
        fun createRoute(investigationId: String) = "roadmap/$investigationId"
    }

    object DocumentUpload : Screen("document_upload", "Ingest Document")

    object Chat : Screen("chat", "AyurBot", Icons.Default.AutoAwesome)

    object RegulationTimeline : Screen("timeline/{sourceId}", "Regulation Time Machine") {
        fun createRoute(sourceId: String) = "timeline/$sourceId"
    }

    object PriorArtSearch : Screen("prior_art", "Prior Art Search", Icons.Default.Policy)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Investigate,
    Screen.EvidenceList,
    Screen.Saved,
    Screen.Settings
)
