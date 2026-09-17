package com.ipsakti.sahayak.ui.screens.investigate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipsakti.sahayak.ui.components.DomainChip
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*

private val AVAILABLE_DOMAINS = listOf(
    "Patent",
    "Traditional Knowledge",
    "Ayurveda",
    "Biodiversity / ABS",
    "Trademark",
    "Copyright",
    "Geographical Indication",
    "Design",
    "International IP"
)

private val SAMPLE_PROMPTS = listOf(
    "We developed an Ayurvedic formulation using a traditional medicinal plant. What IP and compliance issues should we investigate?",
    "Can we trademark and register GI for an indigenous handloom textile from Odisha?",
    "Is our AI-based medical diagnostic algorithm patentable under Section 3(k) of Indian Patents Act?",
    "What are the Access & Benefit Sharing (ABS) mandates for exporting biological specimens from Western Ghats?"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InvestigateScreen(
    onInvestigationCompleted: (String) -> Unit,
    onNavigateToPriorArt: (() -> Unit)? = null,
    onOpenWizard: (() -> Unit)? = null,
    viewModel: InvestigateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "IP Investigation Workspace",
                subtitle = "Multi-Domain Legal & Regulatory Discovery"
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
            // Classification Wizard Shortcut Banner
            if (onOpenWizard != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenWizard() },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🧭", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Product Classification Decision Tree",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                )
                                Text(
                                    text = "Determine if formulation routes to AYUSH, FSSAI, or CDSCO",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = RoyalBlue800,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Investigation Mode Selector (Quick vs Deep)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "INVESTIGATION RIGOR MODE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = uiState.isDeepAnalysis,
                                onClick = { viewModel.setMode(true) },
                                label = { Text(com.ipsakti.sahayak.data.manager.LanguageManager.getString("deep_analysis")) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalBlue800,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Gold600
                                )
                            )
                            FilterChip(
                                selected = !uiState.isDeepAnalysis,
                                onClick = { viewModel.setMode(false) },
                                label = { Text(com.ipsakti.sahayak.data.manager.LanguageManager.getString("quick_answer")) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalBlue800,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Domain Multi-Select Section
            item {
                Text(
                    text = com.ipsakti.sahayak.data.manager.LanguageManager.getString("target_domains"),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AVAILABLE_DOMAINS.forEach { domain ->
                        DomainChip(
                            name = domain,
                            isSelected = uiState.selectedDomains.contains(domain),
                            onClick = { viewModel.toggleDomain(domain) }
                        )
                    }
                }
            }

            // Case Description Input Area
            item {
                Text(
                    text = com.ipsakti.sahayak.data.manager.LanguageManager.getString("describe_case"),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.queryText,
                    onValueChange = { viewModel.onQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp),
                    placeholder = {
                        Text(
                            text = "Example: We developed a novel poly-herbal formulation using Ashwagandha and Turmeric for arthritis. Can we patent the extraction process or formulation, and what National Biodiversity Authority approvals are required?",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedBorderColor = RoyalBlue800,
                        unfocusedBorderColor = CardBorder
                    )
                )
            }

            // Sample Queries
            item {
                Text(
                    text = "EXAMPLE HACKATHON CASE STUDIES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SAMPLE_PROMPTS.forEach { prompt ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardBackground,
                            border = BorderStroke(1.dp, CardBorder),
                            onClick = { viewModel.applySampleQuery(prompt) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = RoyalBlue800,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        color = Navy800
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Loading state indicator
            if (uiState.isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = RoyalBlue800,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = uiState.loadingStep,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Navy900
                                )
                            )
                        }
                    }
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = { viewModel.analyze(onComplete = onInvestigationCompleted) },
                    enabled = uiState.queryText.trim().isNotEmpty() && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue800,
                        disabledContainerColor = Slate300
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isLoading) com.ipsakti.sahayak.data.manager.LanguageManager.getString("analyzing") else com.ipsakti.sahayak.data.manager.LanguageManager.getString("run_analysis"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            if (onNavigateToPriorArt != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPriorArt() },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Policy,
                                contentDescription = null,
                                tint = RoyalBlue800,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Prior-Art Patent Search Registry",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                )
                                Text(
                                    text = "Search whether a formulation or herb has existing granted/revoked patents",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = RoyalBlue800,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
