package com.ipsakti.sahayak.ui.screens.home.personas

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.ui.screens.chat.ChatContextHelper
import com.ipsakti.sahayak.ui.theme.*

@Composable
fun PractitionerDashboard(
    onStartInvestigation: () -> Unit,
    onOpenInvestigation: (String) -> Unit,
    onOpenChatWithQuery: (String, Boolean) -> Unit,
    onOpenCraft: () -> Unit,
    onOpenWizard: () -> Unit
) {
    // 1. Formulation Lookup state
    var selectedLookupIndex by remember { mutableIntStateOf(0) }
    val lookupItems = listOf(
        Triple("Chyawanprash Awaleha", "Barred under Sec 3(p)", "Classical formulation listed in Charaka Samhita Chikitsa Sthana; not patentable as composition. Eligible for novel bioavailability carrier or cold-fill packaging patent."),
        Triple("Triphala Micronized Extract", "Eligible with Synergy (Sec 3(e))", "Standardized gallic acid + chebulagic acid with demonstrated combination index (CI < 0.8) and modified-release matrix."),
        Triple("Mahasudarshan Kwatha Tablet", "Barred under Sec 3(p)", "Direct solid dose conversion of classical 54-herb decoction. Precluded unless unexpected therapeutic synergistic efficacy is established.")
    )

    // 2. Schedule E(1) Botanical safety checker state
    var selectedPoisonIndex by remember { mutableIntStateOf(0) }
    val poisonousHerbs = listOf(
        ScheduleE1Herb("Vatsanabha", "Aconitum ferox", "High (Schedule E(1))", "Mandatory Gomutra / Godugdha Shodhana (7 cycles). Max permissible clinical dose 16 mg. High cardiotoxic alkaloid (aconitine)."),
        ScheduleE1Herb("Gunja", "Abrus precatorius", "Severe (Schedule E(1))", "Swedana in Kanji/Cow milk for 3 hours. Contains abrin lectin. Strict external / purified internal usage only."),
        ScheduleE1Herb("Dhatura", "Datura metel", "High (Schedule E(1))", "Dolayantra Shodhana in cow milk. Tropane alkaloid (scopolamine) limits strictly audited under Schedule T GMP."),
        ScheduleE1Herb("Kuchla", "Strychnos nux-vomica", "High (Schedule E(1))", "Purification in Ghee / Kanji. Strychnine content must be monitored by SLA batch test before release.")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner: Vaidya & Clinician Focus
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
                    Text(text = "🩺", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AYURVEDIC CLINICIAN DASHBOARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Gold600,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Grounded Formulations, Safety & Samhitas",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Screen polyherbal remedies against Section 3(p) patent bars, verify Schedule E(1) purification rules, and query classical texts.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate300)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onStartInvestigation,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBlue800,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Formulation Check",
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
                            text = "⚗️ Novelty Cauldron",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Widget 1: Classical Formulation Lookup (Section 3(p) Bar vs Eligible)
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
                        Text(text = "⚖️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Formulation 3(p) Status Lookup",
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
                            text = "STATUTORY TRIAGE",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = RoyalBlue800,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap a formulation archetype to inspect Section 3(p) traditional knowledge preclusion versus patentable synergy:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    lookupItems.forEachIndexed { idx, item ->
                        val isSel = selectedLookupIndex == idx
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) RoyalBlue800 else Slate100,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedLookupIndex = idx }
                        ) {
                            Text(
                                text = item.first.split(" ").take(2).joinToString(" "),
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSel) Color.White else Navy900,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                val currentLookup = lookupItems[selectedLookupIndex]
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (currentLookup.second.contains("Barred")) RiskHigh.copy(alpha = 0.1f) else Color(0xFF16A34A).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, if (currentLookup.second.contains("Barred")) RiskHigh.copy(alpha = 0.4f) else Color(0xFF16A34A).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentLookup.first,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Navy900)
                            )
                            Text(
                                text = currentLookup.second,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentLookup.second.contains("Barred")) RiskHigh else Color(0xFF15803D)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentLookup.third,
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700, fontSize = 11.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            val prompt = ChatContextHelper.forPatentTriage(currentLookup.first, "Classical/Modified Formulation")
                            onOpenChatWithQuery(prompt, true)
                        }
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ask AyurBot for Detailed Patentability Analysis", style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlue800, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Widget 2: Schedule E(1) Botanical-Safety Checker
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
                        Text(text = "⚠️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Schedule E(1) Botanical Safety",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = RiskHigh.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "DRUGS & COSMETICS",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = RiskHigh,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Regulated poisonous botanicals requiring mandatory classical Shodhana (purification) and SLA batch-limits:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(poisonousHerbs.indices.toList()) { idx ->
                        val herb = poisonousHerbs[idx]
                        val isSelected = selectedPoisonIndex == idx
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Navy900 else Slate100,
                            modifier = Modifier.clickable { selectedPoisonIndex = idx }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = herb.commonName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else Navy900,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                val selHerb = poisonousHerbs[selectedPoisonIndex]
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate100,
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selHerb.commonName} (${selHerb.botanicalName})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = RiskHigh.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = selHerb.hazardRating,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = RiskHigh,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selHerb.shodhanaGuidance,
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700, fontSize = 12.sp, lineHeight = 16.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val prompt = ChatContextHelper.forBotanicalSafety(selHerb.commonName, selHerb.botanicalName, selHerb.shodhanaGuidance)
                                onOpenChatWithQuery(prompt, true)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, RoyalBlue800.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ask AyurBot for Shodhana Protocol & Dosage Limits", style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlue800, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // Widget 3: Classical Samhita Scoped Shortcuts
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
                        Text(text = "📜", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Classical Samhita Research Shortcuts",
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
                            text = "TKDL SOURCES",
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
                    text = "Launch context-scoped queries directly into authoritative Brihat Trayi treatises:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                SamhitaShortcutRow(
                    treatiseName = "Charaka Samhita",
                    focus = "Chikitsa Sthana Rasayana & Polyherbal Formulations",
                    onClick = {
                        val prompt = ChatContextHelper.forClassicalText("Charaka Samhita", "Rasayana formulations, Medhya compounds, and classical synergy")
                        onOpenChatWithQuery(prompt, true)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SamhitaShortcutRow(
                    treatiseName = "Sushruta Samhita",
                    focus = "Shalya Tantra & Botanical Wound Healing Formulations",
                    onClick = {
                        val prompt = ChatContextHelper.forClassicalText("Sushruta Samhita", "Vrana Ropana herbs, Ksharasutra preparation, and medicinal Ksharas")
                        onOpenChatWithQuery(prompt, true)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SamhitaShortcutRow(
                    treatiseName = "Ashtanga Hridaya",
                    focus = "Sutra Sthana Dosage Forms & Standard Kwathas",
                    onClick = {
                        val prompt = ChatContextHelper.forClassicalText("Ashtanga Hridaya", "Kashaya Kalpana decoction ratios, Sneha Kalpana oils, and stability markers")
                        onOpenChatWithQuery(prompt, true)
                    }
                )
            }
        }

        // Widget 4: Patient-Safe Citation Library
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate100),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📚 Patient-Safe Citation Library",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                    )
                    Text(
                        text = "API Standard",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Authoritative pharmacopoeial citations verified for clinical use with safe limits:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(8.dp))
                CitationLibraryItem(
                    title = "Ayurvedic Pharmacopoeia of India (API)",
                    subtitle = "Part I, Vol V: Standard monograph parameters for Withania somnifera & Tinospora cordifolia.",
                    onClick = {
                        val prompt = ChatContextHelper.forEvidence("Ayurvedic Pharmacopoeia of India", "API Part I, Vol V", "Physicochemical and heavy metal standards for Guduchi and Ashwagandha")
                        onOpenChatWithQuery(prompt, true)
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                CitationLibraryItem(
                    title = "Schedule T Good Manufacturing Practice",
                    subtitle = "Mandatory shelf-life, batch uniformity, and microbial limits for ASU tablets and churna.",
                    onClick = {
                        val prompt = ChatContextHelper.forEvidence("Drugs & Cosmetics Act Schedule T", "Rule 157", "Microbial contamination limits and stability testing for ASU formulations")
                        onOpenChatWithQuery(prompt, true)
                    }
                )
            }
        }
    }
}

private data class ScheduleE1Herb(
    val commonName: String,
    val botanicalName: String,
    val hazardRating: String,
    val shodhanaGuidance: String
)

@Composable
private fun SamhitaShortcutRow(
    treatiseName: String,
    focus: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Gold600.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📖", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = treatiseName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = focus,
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = RoyalBlue800,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CitationLibraryItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp),
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Ask AyurBot",
                tint = Gold700,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
