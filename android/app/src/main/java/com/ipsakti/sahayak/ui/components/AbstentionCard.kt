package com.ipsakti.sahayak.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AbstentionCard(
    message: String,
    evidenceFoundCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDraftDialog by remember { mutableStateOf(false) }

    // Subtle entrance animation (fade + scale from 0.95 to 1.0, ~400ms)
    val animAlpha = remember { Animatable(0f) }
    val animScale = remember { Animatable(0.95f) }

    LaunchedEffect(Unit) {
        launch {
            animAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400)
            )
        }
        launch {
            animScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400)
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animAlpha.value
                scaleX = animScale.value
                scaleY = animScale.value
            },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Gold600.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, Gold600.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Gold600,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "VERIFICATION INSUFFICIENT — SAFE ABSTENTION",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Gold700,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = message.ifEmpty {
                    "I could not find sufficient authoritative evidence in the indexed Indian IP & regulatory knowledge base to provide a reliable, grounded answer. The system abstains rather than generating speculative legal statements."
                },
                style = MaterialTheme.typography.bodyMedium.copy(color = Navy900)
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = Gold600.copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sources considered: $evidenceFoundCount",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate600)
                )
                Text(
                    text = "Evidence strength: Low",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = RiskHigh,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Recommendation: Consult with an accredited Indian Patent Agent or Ministry of Ayush regulatory officer.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = Navy800,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Consult Expert & Draft Query Letter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:expert@ipsakti.gov.in")
                            putExtra(Intent.EXTRA_SUBJECT, "Legal Expert Consultation Request")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Query Details:\n$message\n\nPlease advise on applicable patent & regulatory compliance."
                            )
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:1800110180")
                            }
                            try {
                                context.startActivity(dialIntent)
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Gold700),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold800)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Consult Expert",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = { showDraftDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Draft Letter",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (showDraftDialog) {
        StatutoryQueryLetterModal(
            queryContext = message,
            onDismiss = { showDraftDialog = false }
        )
    }
}

@Composable
fun StatutoryQueryLetterModal(
    queryContext: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    val mLower = queryContext.lowercase()
    val isBiodiversity = mLower.contains("biodiversity") || mLower.contains("abs") || mLower.contains("flora") || mLower.contains("biological") || mLower.contains("harvest")
    val isAyush = mLower.contains("ayush") || mLower.contains("license") || mLower.contains("158b") || mLower.contains("asu") || mLower.contains("manufacturing")

    val authorityName = when {
        isBiodiversity -> "National Biodiversity Authority (NBA)"
        isAyush -> "Ministry of Ayush / State Licensing Authority (SLA)"
        else -> "Indian Patent Office (CGPDTM)"
    }

    val authorityEmail = when {
        isBiodiversity -> "chairperson@nba.nic.in"
        isAyush -> "regulatory@ayush.gov.in"
        else -> "delhi-patent@nic.in"
    }

    val initialLetterText = remember(queryContext) {
        buildString {
            appendLine("Date: 17 September 2026")
            appendLine("Ref: IPSAKTI/STATUTORY-CLARIF/2026/09")
            appendLine()
            appendLine("TO:")
            appendLine("The Competent Authority,")
            appendLine(authorityName)
            appendLine("Government of India")
            appendLine()
            appendLine("SUBJECT: Request for Statutory Clarification regarding Formulation IP & Regulatory Compliance")
            appendLine()
            appendLine("Respected Sir / Madam,")
            appendLine()
            appendLine("1. Background & Context:")
            appendLine("In the course of evaluating our Ayurvedic formulation for patentability and statutory filings via IP-SAKTI SAHAYAK, the following legal and factual ambiguity was encountered:")
            appendLine("\"${queryContext.ifEmpty { "Ambiguity regarding Section 3(p) novelty exclusion and Section 6 Biological Diversity Act clearance." }}\"")
            appendLine()
            appendLine("2. Specific Statutory Clarification Requested:")
            if (isBiodiversity) {
                appendLine("(a) Whether the commercial utilization of the specified medicinal plant requires Form III approval under Section 6 or falls under Section 40 (Normally Traded Commodities).")
                appendLine("(b) Mandatory intimation guidelines for State Biodiversity Boards (SBB).")
            } else if (isAyush) {
                appendLine("(a) Applicable manufacturing license route under Drugs & Cosmetics Rules, 1945 (Rule 158B).")
                appendLine("(b) Permissible synergistic efficacy documentation standards for classical vs proprietary ASU drugs.")
            } else {
                appendLine("(a) Applicability of Section 3(p) of the Patents Act, 1970 and examination guidelines regarding TKDL prior art citations.")
                appendLine("(b) Evidentiary threshold required to substantiate unexpected synergistic therapeutic effect.")
            }
            appendLine()
            appendLine("3. Prayer:")
            appendLine("We respectfully pray that the Competent Authority may kindly issue guidance or clarify the statutory position to enable full national compliance.")
            appendLine()
            appendLine("Yours faithfully,")
            appendLine("Authorized Representative / Applicant")
            appendLine("IP-SAKTI SAHAYAK Formal Escalation Dossier")
        }
    }

    var letterBody by remember { mutableStateOf(initialLetterText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Statutory Query Letter Draft",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy900)
                    )
                    Text(
                        text = "Addressed to: $authorityName",
                        style = MaterialTheme.typography.labelSmall.copy(color = Gold800, fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Editable formal representation citing exact statutory ambiguities. Review, edit, and export directly via email or document share.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = letterBody,
                    onValueChange = { letterBody = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue800,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = Slate50,
                        unfocusedContainerColor = Slate50
                    )
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(letterBody))
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Statutory Query Letter: Clarification Request")
                            putExtra(Intent.EXTRA_TEXT, letterBody)
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(authorityEmail))
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Send Query Letter to Authority"))
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share / Email", fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Slate600)
            }
        }
    )
}

