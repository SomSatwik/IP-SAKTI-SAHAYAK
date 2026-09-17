package com.ipsakti.sahayak.ui.screens.home.personas

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.ui.screens.chat.ChatContextHelper
import com.ipsakti.sahayak.ui.theme.*

@Composable
fun StartupDashboard(
    onStartInvestigation: () -> Unit,
    onOpenInvestigation: (String) -> Unit,
    onOpenChatWithQuery: (String, Boolean) -> Unit,
    onOpenWizard: () -> Unit,
    onOpenPriorArt: () -> Unit,
    onOpenExportReadiness: () -> Unit
) {
    val context = LocalContext.current

    // NBA/ABS Checklist state with interactive checkboxes & progress bar
    val checklistItems = remember {
        mutableStateListOf(
            ChecklistItem("Determine Biological Resource Origin (Indian bio-resources)", true),
            ChecklistItem("Verify Normally Traded Commodities (NTC) exemption list", true),
            ChecklistItem("National Biodiversity Authority Form III application (prior to patent)", false),
            ChecklistItem("Form 18A expedited examination request under Startup India (DPIIT)", false),
            ChecklistItem("Executed Access & Benefit Sharing (ABS) agreement with SBB/NBA", false)
        )
    }
    val completedCount = checklistItems.count { it.isChecked }
    val progress = completedCount.toFloat() / checklistItems.size.toFloat()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner: Startup Founder Focus
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Navy900),
            border = BorderStroke(1.dp, Navy700)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🚀", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AYUSH STARTUP FOUNDER DASHBOARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Gold600,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Expedited Filing & Commercialization",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Accelerate patent prosecution via Form 18A, complete 90-day NBA clearances, protect institutional valuation, and export investor IP summaries.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate300)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onOpenWizard,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold600),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "🧭 Triage Wizard", color = Navy900, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { onOpenInvestigation("demo_ayurvedic_01") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text(text = "🗺️ Compliance Plan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Widget 1: Patentability Triage Wizard CTA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenWizard() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D9488).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🧭", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Patentability Triage Wizard",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF0D9488).copy(alpha = 0.15f)) {
                            Text(
                                text = "DECISION TREE",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF0D9488), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "5-minute guided interactive assessment: Route product across AYUSH, CDSCO & FSSAI before committing capital.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                    )
                }
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(16.dp))
            }
        }

        // Widget 2: NBA / ABS Compliance Checklist with Progress Tracking
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📋", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NBA / ABS Startup Clearance Checklist",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                        )
                    }
                    Text(
                        text = "${(progress * 100).toInt()}% Done",
                        style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlue800, fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF16A34A),
                    trackColor = Slate200
                )
                Spacer(modifier = Modifier.height(10.dp))

                checklistItems.forEachIndexed { idx, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { checklistItems[idx] = item.copy(isChecked = !item.isChecked) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { isChecked ->
                                checklistItems[idx] = item.copy(isChecked = isChecked)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = RoyalBlue800)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (item.isChecked) Slate500 else Navy900,
                                fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            val prompt = ChatContextHelper.forAbsBenefitSharing("AYUSH commercial startup formulation", "Form III approval prior to patent grant")
                            onOpenChatWithQuery(prompt, true)
                        }
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ask AyurBot for NBA Clearance Guidance", style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlue800, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Widget 3: Investor-Facing IP Summary Export
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💼", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Investor-Facing IP Summary Export",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                        )
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = Gold600.copy(alpha = 0.2f)) {
                        Text(
                            text = "PITCH DECK READY",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = Gold800, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Generate a concise 1-page IP barrier defense summary for venture pitch decks, showcasing Section 3(p) clearance and DPIIT valuation security.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val investorPitch = buildString {
                            appendLine("=== IP-SAKTI SAHAYAK: INVESTOR IP MOAT & COMPLIANCE SUMMARY ===")
                            appendLine("Target: Series A / Seed Pitch Deck Due Diligence")
                            appendLine("Date: 2026-09-17")
                            appendLine()
                            appendLine("1. INTELLECTUAL PROPERTY MOAT")
                            appendLine("• Statutory Freedom-to-Operate: Overcame Section 3(p) traditional knowledge bar via novel synergistic extraction.")
                            appendLine("• Synergistic Combination Index: CI < 0.82 documented under Section 3(e).")
                            appendLine("• Fast-Track Filing: Form 18A expedited examination active under DPIIT Startup India Scheme.")
                            appendLine()
                            appendLine("2. REGULATORY & BIODIVERSITY DE-RISKING")
                            appendLine("• Biological Diversity Act: NBA Form III clearance pathway identified; 90-day fast-track processing.")
                            appendLine("• Manufacturing Licensing: Rule 158B proof of safety compliant.")
                            appendLine()
                            appendLine("Generated by IP-SAKTI Sahayak (SIH PS26045).")
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "IP-SAKTI Investor IP Moat Summary")
                            putExtra(Intent.EXTRA_TEXT, investorPitch)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Investor IP Summary"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export & Share Investor IP Summary", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Widget 4: Competitor & Prior-Art Quick Search
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate100),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔎 Competitor & Prior-Art Quick Screen",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Screen similar Ayurvedic formulations registered at IPO, USPTO, and WIPO before filing:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpenPriorArt,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CardBackground, contentColor = RoyalBlue800),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Prior-Art Patent Registry", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

private data class ChecklistItem(val title: String, var isChecked: Boolean)
