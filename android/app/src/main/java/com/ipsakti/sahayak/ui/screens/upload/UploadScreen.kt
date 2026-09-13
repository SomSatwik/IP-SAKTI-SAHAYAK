package com.ipsakti.sahayak.ui.screens.upload

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UploadScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Document Intelligence",
                subtitle = "Ingest, Process & Index Authoritative Sources",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upload Drop Zone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = Slate300,
                            cornerRadius = CornerRadius(12f, 12f),
                            style = Stroke(
                                width = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                            )
                        )
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate100),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = RoyalBlue800,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Upload Legal / Regulatory Document",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                    )
                    Text(
                        text = "PDF documents (statutes, acts, rules, guidelines)",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate500)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { /* Document picker intent in production */ },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Document", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Processing Pipeline Visualization
            Text(
                text = "DOCUMENT PROCESSING PIPELINE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 1.sp
                )
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PipelineStep(step = 1, label = "Upload", status = "idle", description = "Select and upload the document")
                    PipelineStep(step = 2, label = "Extract", status = "idle", description = "Extract text from PDF pages")
                    PipelineStep(step = 3, label = "Clean", status = "idle", description = "Fix broken words, normalize whitespace")
                    PipelineStep(step = 4, label = "Legal-Aware Chunking", status = "idle", description = "Split by Section, Rule, Article boundaries")
                    PipelineStep(step = 5, label = "Embedding", status = "idle", description = "Generate BGE-M3 multilingual embeddings")
                    PipelineStep(step = 6, label = "Indexing", status = "idle", description = "Insert into FAISS vector store")
                    PipelineStep(step = 7, label = "Ready", status = "idle", description = "Available for investigation queries")
                }
            }

            // Supported Document Types
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SUPPORTED DOCUMENT TYPES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val types = listOf(
                        "Indian Patents Act", "Trademarks Act", "Copyright Act",
                        "Biological Diversity Act", "TKDL Documentation",
                        "Geographical Indications Act", "Designs Act",
                        "TRIPS Agreement", "Paris Convention"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        types.forEach { type ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CardBackground,
                                border = BorderStroke(1.dp, Slate200)
                            ) {
                                Text(
                                    text = type,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Navy800,
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

@Composable
private fun PipelineStep(
    step: Int,
    label: String,
    status: String,
    description: String
) {
    val (color, icon) = when (status) {
        "completed" -> Color(0xFF16A34A) to Icons.Default.CheckCircle
        "processing" -> Gold600 to Icons.Default.Pending
        "error" -> RiskHigh to Icons.Default.Error
        else -> Slate400 to Icons.Default.RadioButtonUnchecked
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$step",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (status == "idle") Slate500 else Navy900
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Slate400,
                    fontSize = 11.sp
                )
            )
        }
    }
}
