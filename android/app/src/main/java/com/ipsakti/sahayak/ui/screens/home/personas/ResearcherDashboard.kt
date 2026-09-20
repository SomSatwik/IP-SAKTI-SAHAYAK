package com.ipsakti.sahayak.ui.screens.home.personas

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.ui.screens.chat.ChatContextHelper
import com.ipsakti.sahayak.ui.theme.*

@Composable
fun ResearcherDashboard(
    onStartInvestigation: () -> Unit,
    onOpenInvestigation: (String) -> Unit,
    onOpenChatWithQuery: (String, Boolean) -> Unit,
    onOpenPriorArt: () -> Unit,
    onOpenCraft: () -> Unit
) {
    val context = LocalContext.current

    // Filter state for Literature Search
    var selectedActFilter by remember { mutableStateOf("Patent Act (Sec 3)") }
    val actFilters = listOf("Patent Act (Sec 3)", "Biodiversity Act", "AYUSH Rule 158B", "Drugs & Cosmetics")

    // Saved Queries with tags
    val savedQueries = listOf(
        Pair("Synergistic Combination Index (CI < 1) in Polyherbal Extracts", listOf("Synergy", "Sec 3(e)", "Pharmacology")),
        Pair("Standardized Withanolide Extraction Novelty vs TKDL Prior Art", listOf("Extraction", "TKDL", "Novelty")),
        Pair("Curcuminoid Bioavailability Enhancement via Piperine Nanoparticles", listOf("Nanotech", "Drug Delivery", "Patentable"))
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner: Researcher Focus
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Navy900,
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, Navy700)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔬", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ACADEMIC & R&D RESEARCHER DASHBOARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Gold600,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Novelty Discovery & Statutory Benchmarks",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Explore indexed patent case law, cross-reference TKDL prior art, evaluate Section 3(e) synergy, and export academic research briefs.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate300)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onOpenPriorArt,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBlue800,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Search Prior Art",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenCraft,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "⚗️ Novelty Lab",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Widget 1: Literature & Citation Search with Multi-Filter
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
                        Text(text = "📑", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Literature & Statutory Filter Search",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = RoyalBlue800.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "INDEXED CORPUS",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = RoyalBlue800,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Filter legal precedents, IPO Controller decisions, and gazette notifications:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(actFilters) { filter ->
                        val isSelected = selectedActFilter == filter
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) RoyalBlue800 else Slate100,
                            modifier = Modifier.clickable { selectedActFilter = filter }
                        ) {
                            Text(
                                text = filter,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color.White else Navy900,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate50,
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Active Scope: $selectedActFilter (Judgments & Patents 2012–2026)",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Controller of Patents vs. CSIR / TKDL landmark Section 3(p) objections and IPAB reversals regarding standardized synergistic botanical extracts.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Navy900, fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    val prompt = "Retrieve all indexed judgments and controller decisions under $selectedActFilter regarding Ayurvedic polyherbal synergy and traditional knowledge preclusion."
                                    onOpenChatWithQuery(prompt, true)
                                }
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ask AyurBot to Synthesize Citations", style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlue800, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        // Widget 2: TKDL Cross-Reference Tool
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
                        Text(text = "🏛️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TKDL Cross-Reference Tool",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Gold600.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "PRE-GRANT OPPOSITION",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Gold800,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Verify whether your experimental formulation overlaps with CSIR Traditional Knowledge Digital Library prior art:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TkdlQuickBadge(code = "TKDL-AY-0482", herb = "Ashwagandha + Brahmi", context = "Barred under Sec 3(p)")
                    TkdlQuickBadge(code = "TKDL-SD-1102", herb = "Curcumin + Piperine", context = "Synergy claim required")
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val prompt = "Cross-reference my polyherbal research with TKDL database citations and Section 3(p) prior art defenses. What evidence is required to overcome a TKDL-based pre-grant opposition?"
                        onOpenChatWithQuery(prompt, true)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate100, contentColor = RoyalBlue800),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cross-Reference with AyurBot", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Widget 3: Legal Corpus & Benchmark Explorer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate100),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📊 Legal Corpus & Benchmark Explorer",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Authoritative statutory sources indexed in IP-SAKTI knowledge graph:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CorpusMetricChip(label = "IPO Judgments", count = "1,420+", icon = "⚖️", modifier = Modifier.weight(1f))
                    CorpusMetricChip(label = "TKDL Monos", count = "2,500+", icon = "📜", modifier = Modifier.weight(1f))
                    CorpusMetricChip(label = "ABS Notifications", count = "340+", icon = "🌿", modifier = Modifier.weight(1f))
                }
            }
        }

        // Widget 4: Research Brief Dossier Export
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
                        Text(text = "📥", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Research Brief Export",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF16A34A).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "SHAREABLE BRIEF",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Generate and share an academic IP brief summarizing Section 3(p) prior art, Combination Index methodology, and regulatory statutory references.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val briefText = buildString {
                            appendLine("=== IP-SAKTI SAHAYAK: ACADEMIC RESEARCH IP BRIEF ===")
                            appendLine("Date: 2026-09-17 | Prepared for Academic Researcher")
                            appendLine()
                            appendLine("1. STATUTORY BENCHMARKS")
                            appendLine("• Section 3(p) Indian Patent Act 1970: Bars direct traditional formulations.")
                            appendLine("• Section 3(e) Synergistic Combination: Requires Combination Index (CI < 1.0).")
                            appendLine("• Biological Diversity Act 2002: Form III clearance needed before patent grant.")
                            appendLine()
                            appendLine("2. EXPERIMENTAL METHODOLOGY")
                            appendLine("• Chou-Talalay Combination Index method for polyherbal interaction.")
                            appendLine("• Standardized marker quantification (HPLC/HPTLC) vs Ayurvedic Pharmacopoeia.")
                            appendLine()
                            appendLine("Generated by IP-SAKTI Sahayak (SIH PS26045).")
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "IP-SAKTI Academic Research IP Brief")
                            putExtra(Intent.EXTRA_TEXT, briefText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Academic Research Brief"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue800,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export & Share Research Brief (Markdown/Text)",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Widget 5: Saved Scientific Query History with Tags
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🏷️ Saved Query History",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quickly re-analyze prior research investigations with AyurBot:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                savedQueries.forEach { (query, tags) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate50,
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onOpenChatWithQuery(query, true)
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = query,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Navy900),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Run Query",
                                    tint = RoyalBlue800,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                tags.forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = RoyalBlue800.copy(alpha = 0.08f)
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = RoyalBlue800,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
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
    }
}

@Composable
private fun TkdlQuickBadge(code: String, herb: String, context: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Slate100,
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = code, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RoyalBlue800))
            Text(text = herb, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Navy900))
            Text(text = context, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Slate500))
        }
    }
}

@Composable
private fun CorpusMetricChip(label: String, count: String, icon: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900))
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Slate500), maxLines = 1)
        }
    }
}
