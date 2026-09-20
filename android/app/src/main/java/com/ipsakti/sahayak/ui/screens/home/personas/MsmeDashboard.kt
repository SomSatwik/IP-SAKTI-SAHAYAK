package com.ipsakti.sahayak.ui.screens.home.personas

import android.content.Intent
import android.net.Uri
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
fun MsmeDashboard(
    onStartInvestigation: () -> Unit,
    onOpenInvestigation: (String) -> Unit,
    onOpenChatWithQuery: (String, Boolean) -> Unit,
    onOpenExportReadiness: () -> Unit,
    onOpenUpload: () -> Unit
) {
    val context = LocalContext.current

    // Formulation decision helper state
    var selectedFormulationType by remember { mutableIntStateOf(0) }
    val formulationTypes = listOf(
        FormulationChoice("Classical ASU Medicine", "Form 25D", "Manufactured strictly in accordance with authoritative classical texts in First Schedule. No clinical trial safety data required; only proof of textual reference and Schedule T GMP audit."),
        FormulationChoice("ASU Proprietary Medicine", "Rule 158B (Form 25D/E)", "Contains classical ingredients processed in new ways or novel ratios. Requires pilot clinical safety studies, acute oral toxicity (OECD 423), and published proof of effectiveness."),
        FormulationChoice("Ayush Aahar / Food", "FSSAI + AYUSH Regs 2022", "Health supplement / nutraceutical under FSSAI-AYUSH dual regulations. No therapeutic or curative claims permitted on packaging.")
    )

    // Batch Compliance Limits Table items
    val complianceParameters = listOf(
        BatchParam("Lead (Pb)", "< 10.0 ppm", "Schedule T API Limit", true),
        BatchParam("Arsenic (As)", "< 3.0 ppm", "Schedule T API Limit", true),
        BatchParam("Cadmium (Cd)", "< 0.3 ppm", "Schedule T API Limit", true),
        BatchParam("Mercury (Hg)", "< 1.0 ppm", "Schedule T API Limit", true),
        BatchParam("Aflatoxin (B1+B2+G1+G2)", "< 10.0 ppb", "Export Standard", true)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner: MSME Manufacturer Focus
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
                    Text(text = "🏭", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "MSME AYUSH MANUFACTURER DASHBOARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Gold600,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Licensing, Form 28 Subsidies & GMP",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Claim 80% patent fee waivers via Form 28 Udyam, navigate State Licensing Authority (SLA) Rule 158B, audit batch limits, and consult empanelled IP facilitators.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate300)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onOpenExportReadiness,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBlue800,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "🌐 Export & GMP Audit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenUpload,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "📄 Ingest CoA / Batch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Widget 1: Classical vs Proprietary Formulation Decision Helper
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
                        Text(text = "🧭", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Classical vs Proprietary Helper",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                        )
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = RoyalBlue800.copy(alpha = 0.1f)) {
                        Text(
                            text = "RULE 158B ROUTING",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlue800, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Select your production archetype to check State Licensing Authority (SLA) clinical data burden:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    formulationTypes.forEachIndexed { idx, item ->
                        val isSel = selectedFormulationType == idx
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) RoyalBlue800 else Slate100,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFormulationType = idx }
                        ) {
                            Text(
                                text = item.title.split(" ").take(2).joinToString(" "),
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

                Spacer(modifier = Modifier.height(10.dp))
                val selType = formulationTypes[selectedFormulationType]
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate50,
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = selType.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900))
                            Surface(shape = RoundedCornerShape(4.dp), color = Gold600.copy(alpha = 0.2f)) {
                                Text(
                                    text = selType.licenseForm,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(color = Gold800, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = selType.guidance, style = MaterialTheme.typography.bodySmall.copy(color = Slate700, fontSize = 11.sp, lineHeight = 16.sp))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val prompt = ChatContextHelper.forRule158B(selType.title)
                                onOpenChatWithQuery(prompt, true)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, RoyalBlue800.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ask AyurBot for SLA Checklist & Safety Protocols", style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlue800, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // Widget 2: Batch Product Compliance Table Checker
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
                    Text(
                        text = "🧪 Batch Heavy Metal & Safety Limits",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                    )
                    Text(
                        text = "Schedule T",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Mandatory physicochemical parameters verified during SLA annual audit:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                complianceParameters.forEach { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = p.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Navy900))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = p.limit, style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlue800, fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                        }
                    }
                    HorizontalDivider(color = Slate200.copy(alpha = 0.5f))
                }
            }
        }

        // Widget 3: Form 158B / 25D Checklist with Upload Placeholders
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate100),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📁 SLA Dossier Ingestion & Verification",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Upload batch production records, CoA, and stability test reports for automatic regulatory compliance scanning:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onOpenUpload,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ingest Manufacturing Monograph / CoA", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Widget 4: Escalate to Empanelled Attorney / Facilitator
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
                        Text(text = "👨‍⚖️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Direct Facilitator & Attorney Link",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                        )
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF16A34A).copy(alpha = 0.12f)) {
                        Text(
                            text = "EMPANELLED",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Connect with DPIIT/AYUSH certified patent agents for Form 28 MSME fee discounts and state licensing filings.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@ipsakti.gov.in?subject=MSME%20AYUSH%20Attorney%20Facilitation%20Request")
                        }
                        context.startActivity(Intent.createChooser(emailIntent, "Contact Empanelled Facilitator"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, RoyalBlue800)
                ) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request Empanelled Attorney Facilitation", style = MaterialTheme.typography.labelMedium.copy(color = RoyalBlue800, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

private data class FormulationChoice(val title: String, val licenseForm: String, val guidance: String)
private data class BatchParam(val name: String, val limit: String, val standard: String, val isPassed: Boolean)
