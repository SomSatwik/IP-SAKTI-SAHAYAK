package com.ipsakti.sahayak.ui.screens.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipsakti.sahayak.ui.components.*
import com.ipsakti.sahayak.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    investigationId: String,
    onNavigateBack: () -> Unit,
    onViewEvidence: (String) -> Unit,
    onViewGraph: (String) -> Unit,
    onViewRoadmap: (String) -> Unit,
    onViewVersionHistory: (String) -> Unit = {},
    viewModel: ResultViewModel = viewModel()
) {
    LaunchedEffect(investigationId) {
        viewModel.loadInvestigation(investigationId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Investigation Report",
                subtitle = "Grounded Intelligence Analysis",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = RoyalBlue800)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading investigation report...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            uiState.investigation == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Investigation not found",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Slate500)
                    )
                }
            }

            else -> {
                val inv = uiState.investigation!!
                val resp = inv.response

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Query Summary Header
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Navy900)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "INVESTIGATION",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Gold600,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = inv.query,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = RoyalBlue800
                                    ) {
                                        Text(
                                            text = inv.status,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Navy700
                                    ) {
                                        Text(
                                            text = inv.domain,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Slate300
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Confidence Meter
                    item {
                        ConfidenceMeter(
                            confidence = resp.confidence,
                            sourceCount = resp.evidence.size
                        )
                    }

                    // Safe Abstention (shown only if abstained)
                    if (resp.abstained) {
                        item {
                            AbstentionCard(
                                message = "",
                                evidenceFoundCount = resp.evidence.size
                            )
                        }
                    }

                    // Domains Detected
                    if (resp.domains.isNotEmpty()) {
                        item {
                            Text(
                                text = "DOMAINS DETECTED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                resp.domains.forEach { domain ->
                                    DomainChip(name = domain, isSelected = true)
                                }
                            }
                        }
                    }

                    // Key Findings (Answer)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = com.ipsakti.sahayak.data.manager.LanguageManager.getString("key_findings"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate500,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                MarkdownText(
                                    text = resp.answer,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Navy900,
                                        lineHeight = 24.sp
                                    )
                                )
                            }
                        }
                    }

                    // Risks Section
                    if (resp.risks.isNotEmpty()) {
                        item {
                            Text(
                                text = com.ipsakti.sahayak.data.manager.LanguageManager.getString("identified_risks"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        items(resp.risks) { risk ->
                            RiskBadge(riskText = risk)
                        }
                    }

                    // Evidence Section
                    if (resp.evidence.isNotEmpty()) {
                        item {
                            Text(
                                text = "${com.ipsakti.sahayak.data.manager.LanguageManager.getString("authoritative_evidence")} (${resp.evidence.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        items(resp.evidence) { ev ->
                            EvidenceCard(
                                evidence = ev,
                                onClick = { onViewEvidence(ev.id) },
                                onViewVersionHistory = onViewVersionHistory
                            )
                        }
                    }

                    // Action Buttons (Graph + Roadmap)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onViewGraph(investigationId) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountTree,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(com.ipsakti.sahayak.data.manager.LanguageManager.getString("view_graph"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Button(
                                onClick = { onViewRoadmap(investigationId) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Gold700),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(com.ipsakti.sahayak.data.manager.LanguageManager.getString("view_roadmap"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Recommended Actions
                    if (resp.actions.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = com.ipsakti.sahayak.data.manager.LanguageManager.getString("recommended_actions"),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate500,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    resp.actions.forEachIndexed { index, action ->
                                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Text(
                                                text = "${index + 1}.",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = RoyalBlue800
                                                ),
                                                modifier = Modifier.width(22.dp)
                                            )
                                            Text(
                                                text = action,
                                                style = MaterialTheme.typography.bodyMedium.copy(color = Navy900)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Legal Disclaimer
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate100),
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = resp.disclaimer,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Slate500,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
