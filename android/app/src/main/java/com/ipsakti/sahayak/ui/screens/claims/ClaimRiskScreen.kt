package com.ipsakti.sahayak.ui.screens.claims

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.model.ClaimRiskItem
import com.ipsakti.sahayak.data.model.ClaimRiskResponse
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ClaimRiskScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { IpSaktiRepository() }

    var claimsInput by remember {
        mutableStateOf(
            "Cures diabetes and controls chronic blood sugar spikes within 3 weeks.\n" +
            "Clinically proven remedy to prevent arthritis and eliminate joint inflammation.\n" +
            "Supports natural digestion and helps maintain metabolic wellness."
        )
    }
    var productType by remember { mutableStateOf("Ayurvedic Proprietary Medicine") }
    var targetMarket by remember { mutableStateOf("India") }

    var isLoading by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<ClaimRiskResponse?>(null) }

    // Run initial scan
    LaunchedEffect(Unit) {
        isLoading = true
        repository.analyzeClaimRisks(claimsInput, productType, targetMarket).onSuccess {
            analysisResult = it
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Claim Risk Detector",
                subtitle = "Drugs & Magic Remedies Act 1954 & AYUSH Rule 170 Scanner",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header & Presets Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Navy900,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Navy700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚠️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MARKETING CLAIM SCANNER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Gold600,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "Statutory Bar & Penal Risk Detection",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Screens promotional copy, package text, and social ads against DMR Act 1954 (54 scheduled diseases), Rule 170, and CCPA Misleading Advertisement guidelines.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate300)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "QUICK LOAD TEST CASES:",
                            style = MaterialTheme.typography.labelSmall.copy(color = Gold600, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    claimsInput = "Guaranteed cure for chronic diabetes and permanent blood pressure control. Magic herbal formulation eliminates insulin dependence."
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("🚨 Prohibited Curative", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            FilledTonalButton(
                                onClick = {
                                    claimsInput = "Supports healthy digestion and promotes natural vitality. Traditionally documented in Bhavaprakasha Nighantu for digestive fire (Agni)."
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("✅ Compliant Wellness", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Input Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PROPOSED ADVERTISING OR PACKAGING CLAIMS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = claimsInput,
                            onValueChange = { claimsInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            placeholder = { Text("Paste each claim or packaging statement on a new line...") }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    repository.analyzeClaimRisks(claimsInput, productType, targetMarket).onSuccess {
                                        analysisResult = it
                                        isLoading = false
                                    }.onFailure {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalBlue800,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isLoading && claimsInput.isNotBlank()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Screening Claims Against DMR Act...", color = Color.White)
                            } else {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Claims for Legal Risk", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Analysis Results
            analysisResult?.let { result ->
                // Overall Risk Banner
                item {
                    val (riskBg, riskText, riskBadge) = when (result.overallRiskLevel) {
                        "CRITICAL" -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), "CRITICAL STATUTORY BAR")
                        "HIGH" -> Triple(Color(0xFFFFF7ED), Color(0xFFEA580C), "HIGH RISK — CLINICAL TRIAL REQUIRED")
                        "MODERATE" -> Triple(Color(0xFFFFFBEB), Gold800, "MODERATE RISK")
                        else -> Triple(Color(0xFFF0FDF4), Color(0xFF16A34A), "LOW RISK — STATUTORILY COMPLIANT")
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = riskBg),
                        border = BorderStroke(1.dp, riskText.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = riskText
                                ) {
                                    Text(
                                        text = riskBadge,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                                Text(
                                    text = "${result.totalClaimsAnalyzed} Claims Analyzed",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate600,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = result.generalAdvisory,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Navy900,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }

                // Analyzed Claims List Header
                item {
                    Text(
                        text = "EVALUATION PER INDIVIDUAL CLAIM STATEMENT (${result.items.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                }

                // Claim Items
                items(result.items) { item ->
                    ClaimDetailCard(item = item)
                }

                // Share & Export Actions
                item {
                    Button(
                        onClick = {
                            val report = buildString {
                                appendLine("=== IP-SAKTI SAHAYAK: MARKETING CLAIM RISK AUDIT ===")
                                appendLine("Overall Risk Level: ${result.overallRiskLevel}")
                                appendLine("Total Claims Scanned: ${result.totalClaimsAnalyzed}")
                                appendLine("\nAdvisory: ${result.generalAdvisory}")
                                appendLine("\n--- CLAIM BY CLAIM AUDIT ---")
                                result.items.forEachIndexed { idx, itm ->
                                    appendLine("\nClaim #${idx + 1}: \"${itm.claimText}\"")
                                    appendLine("Category: ${itm.detectedCategory}")
                                    appendLine("Risk Level: ${itm.riskLevel}")
                                    if (itm.flaggedPhrases.isNotEmpty()) {
                                        appendLine("Flagged Phrases: ${itm.flaggedPhrases.joinToString(", ")}")
                                    }
                                    appendLine("Statutory Bar: ${itm.statutoryBar}")
                                    appendLine("Evidence Reasoning: ${itm.evidenceReasoning}")
                                    appendLine("Evidence Needed: ${itm.evidenceNeededToSupport}")
                                    appendLine("Recommended Action: ${itm.recommendedNextAction}")
                                }
                                appendLine("\nGenerated by IP-SAKTI Sahayak (SIH PS26045).")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Marketing Claim Risk Audit")
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Claim Risk Audit"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBlue800,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Claim Risk Report", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ClaimDetailCard(item: ClaimRiskItem) {
    val (statusColor, containerBg) = when (item.riskLevel) {
        "CRITICAL" -> Pair(Color(0xFFDC2626), Color(0xFFFEF2F2))
        "HIGH" -> Pair(Color(0xFFEA580C), Color(0xFFFFF7ED))
        "MODERATE" -> Pair(Gold800, Color(0xFFFFFBEB))
        else -> Pair(Color(0xFF16A34A), Color(0xFFF0FDF4))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = item.riskLevel,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
                Text(
                    text = item.detectedCategory,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate600,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Slate100
            ) {
                Text(
                    text = "\"${item.claimText}\"",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Navy900
                    )
                )
            }

            if (item.flaggedPhrases.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Flagged:",
                        style = MaterialTheme.typography.labelSmall.copy(color = statusColor, fontWeight = FontWeight.Bold)
                    )
                    item.flaggedPhrases.forEach { phrase ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = statusColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = phrase,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column {
                Text(
                    text = "STATUTORY BAR:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = item.statutoryBar,
                    style = MaterialTheme.typography.bodySmall.copy(color = RoyalBlue800, fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = "LEGAL REASONING:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = item.evidenceReasoning,
                    style = MaterialTheme.typography.bodySmall.copy(color = Navy900, lineHeight = 16.sp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "RECOMMENDED ACTION / ALLOWABLE REWORDING:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.recommendedNextAction,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Navy900,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}
