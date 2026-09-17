package com.ipsakti.sahayak.ui.screens.export

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*

enum class ExportMarket(
    val code: String,
    val countryName: String,
    val flagEmoji: String,
    val regulator: String,
    val legalFramework: String,
    val leadLimit: String,
    val arsenicLimit: String,
    val cadmiumLimit: String,
    val mercuryLimit: String,
    val mandatoryDisclaimer: String,
    val herbalStatusNote: String
) {
    USA(
        code = "us",
        countryName = "United States",
        flagEmoji = "🇺🇸",
        regulator = "US FDA (Food & Drug Administration)",
        legalFramework = "DSHEA 1994 (Dietary Supplement Health and Education Act) & 21 CFR Part 111 cGMP",
        leadLimit = "0.5 – 3.0 ppm (Prop 65: <0.5 mcg/day)",
        arsenicLimit = "2.0 ppm (Inorganic As < 1.0 ppm)",
        cadmiumLimit = "0.3 ppm (Prop 65: <4.1 mcg/day)",
        mercuryLimit = "1.0 ppm (Total methylmercury)",
        mandatoryDisclaimer = "These statements have not been evaluated by the Food and Drug Administration. This product is not intended to diagnose, treat, cure, or prevent any disease.",
        herbalStatusNote = "Dietary supplements cannot claim to treat disease. Classical formulations sold as supplements. Novel extracts require 75-day New Dietary Ingredient (NDI) notification."
    ),
    EUROPEAN_UNION(
        code = "eu",
        countryName = "European Union",
        flagEmoji = "🇪🇺",
        regulator = "EMA (HMPC) / EFSA (European Food Safety Authority)",
        legalFramework = "Directive 2002/46/EC (Food Supplements) & Directive 2004/24/EC (Traditional Herbal Medicinal Products - THMPD)",
        leadLimit = "3.0 ppm (Food supplements) / 1.0 ppm (Teas)",
        arsenicLimit = "1.0 ppm (Strict inorganic threshold)",
        cadmiumLimit = "1.0 ppm (Strict renal bioaccumulation cap)",
        mercuryLimit = "0.1 ppm (Extremely strict EU standard)",
        mandatoryDisclaimer = "Food supplements should not be used as a substitute for a varied and balanced diet. Do not exceed the recommended daily dose. Keep out of reach of children.",
        herbalStatusNote = "THMPD registration requires 30-year evidence of safe traditional use (including 15 years within the EU). Otherwise sold as Food Supplement without therapeutic claims."
    ),
    UAE(
        code = "uae",
        countryName = "United Arab Emirates",
        flagEmoji = "🇦🇪",
        regulator = "MoHAP (Ministry of Health & Prevention) / Dubai Municipality",
        legalFramework = "UAE Cabinet Resolution No. 4/2012 on Health Supplements & Traditional Medicines",
        leadLimit = "10.0 ppm (Complies with WHO ASU guidelines)",
        arsenicLimit = "3.0 ppm",
        cadmiumLimit = "0.3 ppm",
        mercuryLimit = "1.0 ppm",
        mandatoryDisclaimer = "Natural Health Product registered with Ministry of Health & Prevention. Consult a healthcare practitioner prior to use if pregnant or nursing.",
        herbalStatusNote = "Requires Halal certification of all excipients and manufacturing lines. Pre-registration via MoHAP electronic portal before customs clearance."
    )
}

data class ExportChecklistItem(
    val id: String,
    val title: String,
    val authority: String,
    val description: String,
    val isMandatory: Boolean = true
)

@Composable
fun ExportReadinessScreen(
    onNavigateBack: () -> Unit
) {
    var selectedMarket by remember { mutableStateOf(ExportMarket.USA) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val checklistItems = remember {
        listOf(
            ExportChecklistItem(
                id = "check_copp",
                title = "Certificate of Pharmaceutical Product (CoPP)",
                authority = "CDSCO / Central Drug Standards Control Organization",
                description = "WHO-GMP formatted certificate certifying manufacturing facility license and free sale eligibility."
            ),
            ExportChecklistItem(
                id = "check_ayush_mark",
                title = "AYUSH Premium Mark Certification",
                authority = "Quality Council of India (QCI)",
                description = "Voluntary high-level conformity assessment proving compliance with WHO and target country heavy metal/pesticide limits."
            ),
            ExportChecklistItem(
                id = "check_nba_form3",
                title = "Form III NBA Clearance / Intimation",
                authority = "National Biodiversity Authority (NBA)",
                description = "Statutory approval under Section 6 of Biological Diversity Act 2002 for international commercial utilization of Indian bio-resources."
            ),
            ExportChecklistItem(
                id = "check_facility_reg",
                title = "Foreign Facility Registration & US/EU Agent",
                authority = "Target Jurisdiction (US FDA / EU Single Market Portal)",
                description = "Biennial foreign food/drug establishment registration and designated domestic agent representation."
            ),
            ExportChecklistItem(
                id = "check_lab_coa",
                title = "NABL-Accredited 4-Metal & Residual Solvent CoA",
                authority = "GLP / NABL Certified Testing Laboratory",
                description = "Batch-specific testing for Lead, Cadmium, Mercury, Arsenic, Aflatoxins (B1, B2, G1, G2), and ethylene oxide."
            ),
            ExportChecklistItem(
                id = "check_label_disclaimer",
                title = "Compliant Supplement Facts & Mandatory Disclaimer",
                authority = "US FDA 21 CFR 101.36 / EU Regulation 1169/2011",
                description = "Bilingual packaging artwork displaying statutory font height, serving size, RDA %, and non-therapeutic disclaimer."
            )
        )
    }

    val checkedStates = remember { mutableStateMapOf<String, Boolean>() }
    val completedCount = checkedStates.values.count { it }
    val readinessPercentage = if (checklistItems.isNotEmpty()) (completedCount * 100) / checklistItems.size else 0

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Export Readiness Checklist",
                subtitle = "India vs US FDA vs EU Cross-Border Regulatory Dossier",
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
            // Target Market Selector Tabs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SELECT EXPORT DESTINATION MARKET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportMarket.entries.forEach { market ->
                            val isSelected = selectedMarket == market
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) RoyalBlue800 else Slate100,
                                border = BorderStroke(1.dp, if (isSelected) RoyalBlue800 else Slate300),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedMarket = market }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = market.flagEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = market.countryName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Navy900,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Readiness Score Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900),
                border = BorderStroke(1.dp, Navy700)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CROSS-BORDER READINESS SCORE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Gold600,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${selectedMarket.flagEmoji} ${selectedMarket.countryName} Market Track",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (readinessPercentage >= 80) Color(0xFF16A34A) else if (readinessPercentage >= 50) Gold600 else Color(0xFFDC2626)
                        ) {
                            Text(
                                text = "$readinessPercentage%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { readinessPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = Gold600,
                        trackColor = Navy700
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$completedCount of ${checklistItems.size} export clearance prerequisites satisfied",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate300, fontSize = 11.sp)
                    )
                }
            }

            // Heavy Metals Comparison Matrix Card
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
                        Text(
                            text = "HEAVY METAL TOLERANCES (PPM)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500,
                                letterSpacing = 1.sp
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Text(
                                text = "CRITICAL AUDIT ITEM",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Comparison Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate100, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(text = "Contaminant", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate700))
                        Text(text = "🇮🇳 India (API)", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate700))
                        Text(text = "${selectedMarket.flagEmoji} ${selectedMarket.countryName}", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RoyalBlue800))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    HeavyMetalRow(name = "Lead (Pb)", indiaLimit = "10.0 ppm", targetLimit = selectedMarket.leadLimit, isStricter = true)
                    HorizontalDivider(color = Slate200, thickness = 0.5.dp)
                    HeavyMetalRow(name = "Arsenic (As)", indiaLimit = "3.0 ppm", targetLimit = selectedMarket.arsenicLimit, isStricter = true)
                    HorizontalDivider(color = Slate200, thickness = 0.5.dp)
                    HeavyMetalRow(name = "Cadmium (Cd)", indiaLimit = "0.3 ppm", targetLimit = selectedMarket.cadmiumLimit, isStricter = false)
                    HorizontalDivider(color = Slate200, thickness = 0.5.dp)
                    HeavyMetalRow(name = "Mercury (Hg)", indiaLimit = "1.0 ppm", targetLimit = selectedMarket.mercuryLimit, isStricter = selectedMarket == ExportMarket.EUROPEAN_UNION)

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Gold800, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedMarket == ExportMarket.USA)
                                    "California Proposition 65 enforces a strict 0.5 mcg/day lead exposure ceiling without warnings."
                                else if (selectedMarket == ExportMarket.EUROPEAN_UNION)
                                    "EU limits total mercury to 0.1 ppm—10x stricter than the Ayurvedic Pharmacopoeia of India limit."
                                else
                                    "UAE requires certified accredited labs recognizing ISO/IEC 17025 standards.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Gold800, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }

            // Mandatory Packaging & Labeling Disclaimer Engine
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
                        Text(
                            text = "MANDATORY LABEL DISCLAIMER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500,
                                letterSpacing = 1.sp
                            )
                        )
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(selectedMarket.mandatoryDisclaimer))
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalBlue800)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 11.sp, color = RoyalBlue800, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate100,
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Text(
                            text = "\"${selectedMarket.mandatoryDisclaimer}\"",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Navy900,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Regulatory Note: ${selectedMarket.herbalStatusNote}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp, lineHeight = 15.sp)
                    )
                }
            }

            // Mandatory Export Certifications Checklist
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STATUTORY EXPORT CLEARANCE CHECKLIST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    checklistItems.forEach { item ->
                        val isChecked = checkedStates[item.id] ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { checkedStates[item.id] = !isChecked }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checkedStates[item.id] = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = RoyalBlue800,
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChecked) RoyalBlue800 else Navy900
                                    )
                                )
                                Text(
                                    text = "Authority: ${item.authority}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Gold800,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate600,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                )
                            }
                        }
                        HorizontalDivider(color = Slate100, thickness = 1.dp)
                    }
                }
            }

            // Share / Export Action
            Button(
                onClick = {
                    val shareContent = buildString {
                        appendLine("🇮🇳 IP-SAKTI SAHAYAK: EXPORT READINESS DOSSIER")
                        appendLine("Target Market: ${selectedMarket.countryName} (${selectedMarket.regulator})")
                        appendLine("Legal Framework: ${selectedMarket.legalFramework}")
                        appendLine("Readiness Score: $readinessPercentage% ($completedCount/${checklistItems.size} Completed)")
                        appendLine("\n--- HEAVY METAL LIMITS ---")
                        appendLine("Lead (Pb): API 10 ppm vs Target ${selectedMarket.leadLimit}")
                        appendLine("Arsenic (As): API 3 ppm vs Target ${selectedMarket.arsenicLimit}")
                        appendLine("Cadmium (Cd): API 0.3 ppm vs Target ${selectedMarket.cadmiumLimit}")
                        appendLine("Mercury (Hg): API 1 ppm vs Target ${selectedMarket.mercuryLimit}")
                        appendLine("\n--- MANDATORY DISCLAIMER ---")
                        appendLine(selectedMarket.mandatoryDisclaimer)
                        appendLine("\n--- SATISFIED CHECKLIST ITEMS ---")
                        checklistItems.forEach { item ->
                            val status = if (checkedStates[item.id] == true) "[✓] PASS" else "[ ] PENDING"
                            appendLine("$status ${item.title} (${item.authority})")
                        }
                    }

                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareContent)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Export Readiness Dossier"))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Export Readiness Dossier", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HeavyMetalRow(
    name: String,
    indiaLimit: String,
    targetLimit: String,
    isStricter: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Navy900))
        Text(text = indiaLimit, modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
        Row(
            modifier = Modifier.weight(1.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = targetLimit, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = if (isStricter) Color(0xFFDC2626) else Navy900))
            if (isStricter) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = Color(0xFFFEE2E2)
                ) {
                    Text(text = "Stricter", modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFDC2626), fontSize = 8.sp, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
