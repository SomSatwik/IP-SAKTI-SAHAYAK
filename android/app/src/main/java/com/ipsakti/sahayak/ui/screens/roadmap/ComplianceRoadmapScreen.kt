package com.ipsakti.sahayak.ui.screens.roadmap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.ipsakti.sahayak.data.model.ComplianceReportResponse
import com.ipsakti.sahayak.data.model.RoadmapStep
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.screens.chat.ChatContextHelper
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ComplianceRoadmapScreen(
    investigationId: String,
    onNavigateBack: () -> Unit,
    onAskAyurBot: ((String) -> Unit)? = null
) {
    val repository = remember { IpSaktiRepository() }
    val demoInv = remember { repository.getFallbackDemoInvestigation() }
    val steps = remember { demoInv.roadmap.steps }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showReportDialog by remember { mutableStateOf(false) }
    var isGeneratingReport by remember { mutableStateOf(false) }
    var complianceReport by remember { mutableStateOf<ComplianceReportResponse?>(null) }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Compliance Roadmap",
                subtitle = "Actionable Investigation Steps",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
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
                            tint = RoyalBlue800,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "This roadmap is generated from authoritative statutory & regulatory evidence. It provides investigative guidance — not definitive legal advice.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Slate600,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            itemsIndexed(steps) { index, step ->
                RoadmapStepItem(
                    stepNumber = index + 1,
                    step = step,
                    isLast = index == steps.lastIndex,
                    onAskAyurBot = onAskAyurBot
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy900)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "FINAL STEP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Gold600,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Engage qualified Indian Patent Agent and regulatory counsel for formal evaluation before filing.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Generate Compliance Report Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isGeneratingReport = true
                        coroutineScope.launch {
                            val res = repository.getComplianceReport(investigationId)
                            complianceReport = res.getOrElse { repository.getFallbackComplianceReport(investigationId) }
                            isGeneratingReport = false
                            showReportDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold600),
                    enabled = !isGeneratingReport
                ) {
                    if (isGeneratingReport) {
                        CircularProgressIndicator(
                            color = Navy900,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Navy900,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate Compliance Report",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Navy900,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showReportDialog && complianceReport != null) {
        val rep = complianceReport!!
        val shareableText = buildString {
            appendLine("=== IP-SAKTI SAHAYAK: OFFICIAL COMPLIANCE AUDIT DOSSIER ===")
            appendLine("Date: 2026-09-17 | Case Ref: $investigationId")
            appendLine()
            appendLine("--- MANDATORY STATUTORY FILINGS ---")
            rep.mandatoryForms.forEach { appendLine("• $it") }
            appendLine()
            appendLine("--- COMPLIANCE SUMMARY ---")
            rep.complianceSummary.forEach { appendLine("• $it") }
            appendLine()
            appendLine("--- ACTIONABLE ROADMAP STEPS ---")
            steps.forEachIndexed { i, s ->
                appendLine("Step ${i + 1}: ${s.title} [Status: ${s.status}]")
                appendLine("  ${s.description}")
            }
            appendLine()
            appendLine("--- STATUTORY DISCLAIMER ---")
            appendLine("Generated by IP-SAKTI Sahayak (SIH PS26045). Informational dossier for regulatory planning.")
        }

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Gold600,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Compliance Audit Dossier",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF16A34A).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ready for Export & Statutory Submission",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF15803D),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "MANDATORY FILINGS IDENTIFIED:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        rep.mandatoryForms.forEach { form ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Slate100,
                                border = BorderStroke(1.dp, Slate300),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "📋 $form",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Navy900
                                    )
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "ROADMAP AUDIT SUMMARY:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        rep.complianceSummary.forEach { item ->
                            Text(
                                text = "• $item",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "IP-SAKTI Official Compliance Report")
                            putExtra(Intent.EXTRA_TEXT, shareableText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Compliance Dossier"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Dossier")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Close", color = Slate500)
                }
            }
        )
    }
}

@Composable
private fun RoadmapStepItem(
    stepNumber: Int,
    step: RoadmapStep,
    isLast: Boolean,
    onAskAyurBot: ((String) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }

    val (statusColor, statusIcon) = when (step.status) {
        "completed" -> Color(0xFF16A34A) to Icons.Default.CheckCircle
        "in_progress" -> Gold600 to Icons.Default.Pending
        else -> Slate400 to Icons.Default.RadioButtonUnchecked
    }

    val (priorityLabel, priorityColor) = when {
        step.status == "completed" -> "DONE" to Color(0xFF16A34A)
        stepNumber <= 2 -> "HIGH" to RiskHigh
        stepNumber <= 4 -> "MEDIUM" to RiskMedium
        else -> "RECOMMENDED" to RoyalBlue800
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(statusColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(if (isExpanded) 140.dp else 80.dp)
                        .background(Slate300)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Step content card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp)
                .clickable { isExpanded = !isExpanded },
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
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = priorityColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = priorityLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = priorityColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Slate600),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2
                )

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    step.duration?.let { dur ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Estimated: $dur",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Status: ${step.status.replace("_", " ").replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    if (onAskAyurBot != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                val prompt = ChatContextHelper.forComplianceStep(step.title, step.description)
                                onAskAyurBot(prompt)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, RoyalBlue800.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalBlue800)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Gold700,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ask AyurBot for Step Guidance",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlue800
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
