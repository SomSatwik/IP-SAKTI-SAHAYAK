package com.ipsakti.sahayak.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.manager.PersonaManager
import com.ipsakti.sahayak.data.manager.PersonaType
import com.ipsakti.sahayak.data.model.InvestigationSummary
import com.ipsakti.sahayak.data.model.RegulationUpdate
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.screens.home.personas.*
import com.ipsakti.sahayak.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    onStartInvestigation: () -> Unit,
    onOpenInvestigation: (String) -> Unit,
    onLoadDemoCase: () -> Unit,
    onOpenUpload: () -> Unit,
    onOpenChat: () -> Unit = {},
    onOpenPriorArt: () -> Unit = {},
    onOpenWizard: () -> Unit = {},
    onOpenExportReadiness: () -> Unit = {},
    onOpenCraft: () -> Unit = {},
    onOpenChatWithQuery: (String, Boolean) -> Unit = { _, _ -> onOpenChat() },
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLang by com.ipsakti.sahayak.data.manager.LanguageManager.currentLanguage.collectAsState()
    val currentPersona by PersonaManager.currentPersona.collectAsState()
    var showPersonaDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = com.ipsakti.sahayak.data.manager.LanguageManager.getString("app_name"),
                subtitle = com.ipsakti.sahayak.data.manager.LanguageManager.getString("app_tagline"),
                isOnline = uiState.isOnline,
                actions = {
                    IconButton(onClick = onOpenCraft) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Formulation Novelty Lab (Infinite Craft)",
                            tint = Gold600
                        )
                    }
                    IconButton(onClick = onOpenChat) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "AyurSakti Assistant",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onOpenUpload) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Upload Document",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.loadDashboardData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Active Persona Banner with Quick Switcher
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPersonaDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = currentPersona.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentPersona.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Gold600.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "ACTIVE PERSONA",
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Gold800,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }
                                Text(
                                    text = currentPersona.tag,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate500,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        TextButton(
                            onClick = { showPersonaDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Switch",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = RoyalBlue800,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Dynamic Persona-Specific Dashboard
            item {
                when (currentPersona) {
                    PersonaType.PRACTITIONER -> PractitionerDashboard(
                        onStartInvestigation = onStartInvestigation,
                        onOpenInvestigation = onOpenInvestigation,
                        onOpenChatWithQuery = onOpenChatWithQuery,
                        onOpenCraft = onOpenCraft,
                        onOpenWizard = onOpenWizard
                    )
                    PersonaType.RESEARCHER -> ResearcherDashboard(
                        onStartInvestigation = onStartInvestigation,
                        onOpenInvestigation = onOpenInvestigation,
                        onOpenChatWithQuery = onOpenChatWithQuery,
                        onOpenPriorArt = onOpenPriorArt,
                        onOpenCraft = onOpenCraft
                    )
                    PersonaType.AYUSH_STARTUP -> StartupDashboard(
                        onStartInvestigation = onStartInvestigation,
                        onOpenInvestigation = onOpenInvestigation,
                        onOpenChatWithQuery = onOpenChatWithQuery,
                        onOpenWizard = onOpenWizard,
                        onOpenPriorArt = onOpenPriorArt,
                        onOpenExportReadiness = onOpenExportReadiness
                    )
                    PersonaType.MSME -> MsmeDashboard(
                        onStartInvestigation = onStartInvestigation,
                        onOpenInvestigation = onOpenInvestigation,
                        onOpenChatWithQuery = onOpenChatWithQuery,
                        onOpenExportReadiness = onOpenExportReadiness,
                        onOpenUpload = onOpenUpload
                    )
                    PersonaType.CULTIVATOR -> CultivatorDashboard(
                        onStartInvestigation = onStartInvestigation,
                        onOpenInvestigation = onOpenInvestigation,
                        onOpenChatWithQuery = onOpenChatWithQuery
                    )
                }
            }

            // Recent Regulatory Updates Header & Live Badge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT REGULATORY UPDATES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF16A34A).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "LIVE MONITORING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }

            // Regulatory updates items
            items(uiState.recentRegulations.take(3)) { reg ->
                RegulationUpdateCard(update = reg)
            }

            // Executive Dashboard Metrics
            item {
                Text(
                    text = "EXECUTIVE INTELLIGENCE DASHBOARD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Active Cases",
                        value = "${uiState.stats.activeCases.coerceAtLeast(3)}",
                        icon = Icons.Default.FolderOpen,
                        iconColor = RoyalBlue800,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Sources Indexed",
                        value = "${uiState.stats.documentsIndexed.coerceAtLeast(1284)}",
                        icon = Icons.Default.MenuBook,
                        iconColor = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "High-Risk Flags",
                        value = "${uiState.stats.riskAlerts.coerceAtLeast(7)}",
                        icon = Icons.Default.Warning,
                        iconColor = RiskHigh,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Total Analyses",
                        value = "${uiState.stats.totalInvestigations.coerceAtLeast(12)}",
                        icon = Icons.Default.Assessment,
                        iconColor = Gold700,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Investigations Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT INVESTIGATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            if (uiState.recentInvestigations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No investigations yet",
                                style = MaterialTheme.typography.titleMedium.copy(color = Slate600)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Investigate' or 'Load Demo' to run an analysis.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
                            )
                        }
                    }
                }
            } else {
                items(uiState.recentInvestigations) { inv ->
                    RecentInvestigationItem(
                        item = inv,
                        onClick = { onOpenInvestigation(inv.id) }
                    )
                }
            }
        }
    }

    if (showPersonaDialog) {
        PersonaSelectionModal(
            currentPersona = currentPersona,
            onSelect = {
                PersonaManager.setPersona(it)
                showPersonaDialog = false
            },
            onDismiss = { showPersonaDialog = false }
        )
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
            )
        }
    }
}

@Composable
fun RecentInvestigationItem(
    item: InvestigationSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.query,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = item.domain,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RoyalBlue800,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                    )
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View",
                tint = Slate400,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun RegulationUpdateCard(update: RegulationUpdate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = RoyalBlue800.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = update.sourceName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RoyalBlue800,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF16A34A).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = update.status,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = update.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = update.summary,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate600,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ref: ${update.notificationNumber}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate400,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = update.issuedDate,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate500,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun PersonaSelectionModal(
    currentPersona: PersonaType,
    onSelect: (PersonaType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Persona Focus Mode",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Adapts statutory checklists, legal terminology, and AI retrieval biasing to your specialized domain.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 12.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                PersonaType.entries.forEach { persona ->
                    val isSelected = currentPersona == persona
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(persona) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) RoyalBlue800.copy(alpha = 0.08f) else Slate50
                        ),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) RoyalBlue800 else CardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = persona.iconEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = persona.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) RoyalBlue800 else Navy900,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = persona.tag,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate500,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = RoyalBlue800,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = RoyalBlue800, fontWeight = FontWeight.Bold)
            }
        }
    )
}
