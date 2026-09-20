package com.ipsakti.sahayak.ui.screens.evidence

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.ipsakti.sahayak.data.model.EvidenceItem
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*

@Composable
fun EvidenceDetailScreen(
    evidenceId: String,
    onNavigateBack: () -> Unit
) {
    // Get evidence from the demo investigation (in production, fetch from API)
    val repository = remember { IpSaktiRepository() }
    val demoInv = remember { repository.getFallbackDemoInvestigation() }
    val evidence = remember(evidenceId) {
        demoInv.response.evidence.find { it.id == evidenceId }
    }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Authoritative Evidence",
                subtitle = "Source Verification & Passage Inspection",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        if (evidence == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Evidence not found", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        val src = evidence.source

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Document Title & Relevance
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Navy900,
                    contentColor = Color.White
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SOURCE DOCUMENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Gold600,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = evidence.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = RoyalBlue800
                    ) {
                        Text(
                            text = "Relevance ${(evidence.relevanceScore * 100).toInt()}%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = androidx.compose.ui.graphics.Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Source Metadata Grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SOURCE VERIFICATION METADATA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    MetadataRow("Document", src.documentName)
                    src.authority?.let { MetadataRow("Authority", it) }
                    src.jurisdiction?.let { MetadataRow("Jurisdiction", it) }
                    src.section?.let { MetadataRow("Section", it) }
                    src.page?.let { MetadataRow("Page", it) }
                    src.version?.let { MetadataRow("Version", it) }
                    src.effectiveDate?.let { MetadataRow("Effective Date", it) }
                    src.sourceUrl?.let { MetadataRow("Source URL", it) }
                }
            }

            // Evidence Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EVIDENCE SUMMARY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = evidence.summary,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Navy900,
                            lineHeight = 24.sp
                        )
                    )
                }
            }

            // Supporting Passage (exact content from source)
            src.content?.let { content ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = RoyalBlue800.copy(alpha = 0.04f)),
                    border = BorderStroke(1.dp, RoyalBlue800.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = RoyalBlue800,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EXACT SUPPORTING PASSAGE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlue800,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Navy900,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Slate500
            ),
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(color = Navy900),
            modifier = Modifier.weight(1f)
        )
    }
    HorizontalDivider(color = Slate200.copy(alpha = 0.5f))
}
