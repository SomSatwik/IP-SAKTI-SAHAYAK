package com.ipsakti.sahayak.ui.screens.label

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
import com.ipsakti.sahayak.data.model.LabelComplianceResponse
import com.ipsakti.sahayak.data.model.LabelItem
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LabelComplianceScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { IpSaktiRepository() }

    var productName by remember { mutableStateOf("Triphala Guggulu Vati") }

    // Map of mandatory statutory fields and their current draft values
    val fieldValues = remember {
        mutableStateMapOf(
            "Product Name & Category" to "Triphala Guggulu Vati (Classical Ayurvedic Medicine)",
            "List of Ingredients" to "Haritaki (Terminalia chebula) 100mg, Bibhitaki (Terminalia bellirica) 100mg, Amalaki (Emblica officinalis) 100mg, Guggulu (Commiphora mukul) 200mg",
            "Manufacturing License Number" to "Mfg. Lic. No. GA/1842-A",
            "Batch / Lot Number" to "Batch No. TG-2026-08",
            "Manufacturing & Expiry Dates" to "Mfg Date: 08/2026, Expiry Date: 07/2029",
            "Name & Address of Manufacturer" to "AyurVeda Formulations Pvt Ltd, Industrial Area, Ahmedabad, Gujarat 382445",
            "Schedule E(1) Caution Warning" to "", // Intentionally blank for draft demonstration
            "Storage Directions" to "Store in a cool, dry place away from direct sunlight.",
            "Net Quantity in Metric" to "60 Tablets (Net Wt: 30g)"
        )
    }

    var isLoading by remember { mutableStateOf(false) }
    var complianceResult by remember { mutableStateOf<LabelComplianceResponse?>(null) }

    // Evaluate compliance initially
    LaunchedEffect(Unit) {
        isLoading = true
        repository.analyzeLabelCompliance(productName, fieldValues.toMap()).onSuccess {
            complianceResult = it
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    fun applyPreset(presetType: String) {
        when (presetType) {
            "compliant" -> {
                productName = "Triphala Guggulu Vati (Classical)"
                fieldValues["Product Name & Category"] = "Triphala Guggulu Vati (Classical Ayurvedic Medicine - AFI Part I)"
                fieldValues["List of Ingredients"] = "Haritaki (Terminalia chebula) 100mg, Bibhitaki (Terminalia bellirica) 100mg, Amalaki (Emblica officinalis) 100mg, Guggulu (Commiphora mukul) 200mg"
                fieldValues["Manufacturing License Number"] = "Mfg. Lic. No. GA/1842-A"
                fieldValues["Batch / Lot Number"] = "Batch No. TG-2026-08"
                fieldValues["Manufacturing & Expiry Dates"] = "Mfg Date: 08/2026, Expiry Date: 07/2029"
                fieldValues["Name & Address of Manufacturer"] = "AyurVeda Formulations Pvt Ltd, Industrial Estate, Ahmedabad, Gujarat 382445"
                fieldValues["Schedule E(1) Caution Warning"] = "Caution: Contains Guggulu. To be taken under medical supervision."
                fieldValues["Storage Directions"] = "Store in a cool, dry place away from direct sunlight."
                fieldValues["Net Quantity in Metric"] = "60 Tablets (Net Wt: 30g)"
            }
            "incomplete" -> {
                productName = "Herbal Slimming Drops (Draft)"
                fieldValues["Product Name & Category"] = "Slimming Formula"
                fieldValues["List of Ingredients"] = "Garcinia, Green Tea, Ayurvedic herbs"
                fieldValues["Manufacturing License Number"] = "Applied for"
                fieldValues["Batch / Lot Number"] = "B. No. 01"
                fieldValues["Manufacturing & Expiry Dates"] = "Mfg 2026"
                fieldValues["Name & Address of Manufacturer"] = "Manufactured in India"
                fieldValues["Schedule E(1) Caution Warning"] = ""
                fieldValues["Storage Directions"] = ""
                fieldValues["Net Quantity in Metric"] = "30ml"
            }
        }
        coroutineScope.launch {
            isLoading = true
            repository.analyzeLabelCompliance(productName, fieldValues.toMap()).onSuccess {
                complianceResult = it
                isLoading = false
            }.onFailure {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Label Compliance Checker",
                subtitle = "Rule 161 Drugs & Cosmetics Rules & Legal Metrology Audit",
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
            // Header & Presets
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy900),
                    border = BorderStroke(1.dp, Navy700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🏷️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "PACKAGING & ARTWORK AUDITOR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Gold600,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "Statutory Rule 161 Audit",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Audits primary container and secondary carton artwork against mandatory statutory elements under Drugs & Cosmetics Rules (Rule 161(1)(a)-(f), Schedule E(1), and Legal Metrology).",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate300)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "PRESET TEMPLATES:",
                            style = MaterialTheme.typography.labelSmall.copy(color = Gold600, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { applyPreset("compliant") },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("✅ Fully Compliant Pack", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            FilledTonalButton(
                                onClick = { applyPreset("incomplete") },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("⚠️ Incomplete Startup Draft", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Readiness Score Banner
            complianceResult?.let { result ->
                item {
                    val readinessColor = if (result.complianceReadinessPct >= 80) Color(0xFF16A34A) else if (result.complianceReadinessPct >= 50) Gold700 else Color(0xFFDC2626)

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
                                Column {
                                    Text(
                                        text = "LABEL COMPLIANCE READINESS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate500,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Text(
                                        text = "${result.productLabelName}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = readinessColor
                                ) {
                                    Text(
                                        text = "${result.complianceReadinessPct}%",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { result.complianceReadinessPct / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = readinessColor,
                                trackColor = Slate200
                            )

                            if (result.criticalDeficiencies.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF2F2),
                                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "CRITICAL PACKAGING DEFICIENCIES (${result.criticalDeficiencies.size}):",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        result.criticalDeficiencies.forEach { def ->
                                            Text(
                                                text = "• $def",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF991B1B), fontSize = 11.sp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Editable Elements Header
            item {
                Text(
                    text = "MANDATORY STATUTORY LABEL FIELDS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Field Cards with Status and Inputs
            complianceResult?.fields?.let { fields ->
                items(fields) { fieldItem ->
                    LabelFieldAuditCard(
                        item = fieldItem,
                        currentValue = fieldValues[fieldItem.fieldName] ?: "",
                        onValueChange = { newVal ->
                            fieldValues[fieldItem.fieldName] = newVal
                        }
                    )
                }
            }

            // Re-evaluate Button
            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            repository.analyzeLabelCompliance(productName, fieldValues.toMap()).onSuccess {
                                complianceResult = it
                                isLoading = false
                            }.onFailure {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Re-auditing Label Artwork...")
                    } else {
                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Re-evaluate Label Compliance", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Export Action
            item {
                OutlinedButton(
                    onClick = {
                        val report = buildString {
                            appendLine("=== IP-SAKTI SAHAYAK: STATUTORY LABEL COMPLIANCE REPORT ===")
                            appendLine("Product: $productName")
                            appendLine("Readiness Score: ${complianceResult?.complianceReadinessPct ?: 0}%")
                            appendLine("\n--- MANDATORY STATUTORY FIELDS (RULE 161) ---")
                            complianceResult?.fields?.forEach { f ->
                                appendLine("\n[${f.status.uppercase()}] ${f.fieldName}")
                                appendLine("  Governing Rule: ${f.governingRule}")
                                appendLine("  Declared Value: ${f.detectedValue ?: "(NOT DECLARED)"}")
                                appendLine("  Requirement: ${f.statutoryRequirement}")
                                appendLine("  Corrective Action: ${f.correctiveAction}")
                            }
                            appendLine("\nGoverning Standard: Drugs and Cosmetics Rules, 1945 (Rule 161).")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Label Compliance Audit - $productName")
                            putExtra(Intent.EXTRA_TEXT, report)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Label Audit"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Label Compliance Report", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LabelFieldAuditCard(
    item: LabelItem,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    val (badgeBg, badgeText, badgeLabel) = when (item.status) {
        "detected" -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "COMPLIANT [✓]")
        "needs_verification" -> Triple(Color(0xFFFEF3C7), Gold800, "NEEDS DETAIL [?]")
        else -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "MISSING [✗]")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = item.fieldName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeLabel,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rule: ${item.governingRule}",
                style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlue800, fontWeight = FontWeight.SemiBold)
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentValue,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter declaration for ${item.fieldName}...") },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Statutory Requirement: ${item.statutoryRequirement}",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
            )

            if (item.status != "detected") {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFEF2F2)
                ) {
                    Text(
                        text = "Corrective Action: ${item.correctiveAction}",
                        modifier = Modifier.padding(6.dp),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFDC2626), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}
