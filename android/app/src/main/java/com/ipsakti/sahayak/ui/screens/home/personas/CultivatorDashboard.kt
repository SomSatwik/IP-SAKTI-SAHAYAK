package com.ipsakti.sahayak.ui.screens.home.personas

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.ui.screens.chat.ChatContextHelper
import com.ipsakti.sahayak.ui.theme.*

@Composable
fun CultivatorDashboard(
    onStartInvestigation: () -> Unit,
    onOpenInvestigation: (String) -> Unit,
    onOpenChatWithQuery: (String, Boolean) -> Unit
) {
    // 1. ABS Fee Estimator state
    var commercialTurnoverLakhs by remember { mutableFloatStateOf(25f) }
    // Under NBA ABS Regulations 2014: 0.1% for <= 1 Cr, 0.2% for 1-3 Cr, 0.5% for > 3 Cr
    val estimatedFeePercent = when {
        commercialTurnoverLakhs <= 100f -> 0.1f
        commercialTurnoverLakhs <= 300f -> 0.2f
        else -> 0.5f
    }
    val estimatedFeeAmount = (commercialTurnoverLakhs * 100000f * (estimatedFeePercent / 100f)).toInt()

    // 2. High-Risk Flora items
    var selectedFloraIndex by remember { mutableIntStateOf(0) }
    val highRiskFlora = listOf(
        ProtectedFlora("Red Sanders", "Pterocarpus santalinus", "CITES App II & BDA Sec 38", "Commercial harvesting strictly restricted; requires State Forest Department transit pass and SBB export permit."),
        ProtectedFlora("Kutki", "Picrorhiza kurroa", "Schedule VI Wildlife / Threatened", "High-altitude medicinal herb. Wild collection banned in Uttarakhand & HP; cultivated stock requires Certificate of Cultivation."),
        ProtectedFlora("Jatamansi", "Nardostachys jatamansi", "Endangered / BDA Restricted", "Mandatory State Biodiversity Board intimation. Exemption only applies if procured from certified GACP nursery."),
        ProtectedFlora("Guggulu", "Commiphora wightii", "Critically Endangered (IUCN)", "Over-harvesting strictly prohibited. Special extraction permits required from State Forest Corporation.")
    )

    // 3. Authority decision tree state
    var selectedTreeScenario by remember { mutableIntStateOf(0) }
    val authorityScenarios = listOf(
        AuthorityStep("Indian farmer cultivating on private land", "No NBA approval needed", "Exempt under Section 7 proviso of Biological Diversity Act. Local BMC registration recommended for fair pricing."),
        AuthorityStep("Commercial Indian company sourcing wild herbs", "State Biodiversity Board (SBB)", "Prior intimation required under Section 7. SBB determines Access & Benefit Sharing (ABS) levy."),
        AuthorityStep("Foreign entity / NRI / multinational investment", "National Biodiversity Authority (NBA)", "Mandatory prior approval from NBA under Section 3(2) before obtaining any biological resource or associated knowledge.")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner: Herbal Cultivator & FPO Focus
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
                    Text(text = "🌱", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "HERBAL CULTIVATOR & FPO DASHBOARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Gold600,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Biodiversity, Fair ABS & Sustainable Sourcing",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Calculate statutory benefit-sharing fees, check endangered botanical transit regulations, navigate BMC vs SBB approvals, and review cultivation reminders.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate300)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onStartInvestigation,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A),
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
                            text = "Sourcing Check",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            val prompt = ChatContextHelper.forAbsBenefitSharing("Herbal grower cooperative", "SBB intimation and exemption from commercial ABS fees")
                            onOpenChatWithQuery(prompt, true)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Ask AyurBot on ABS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Widget 1: Biodiversity / NBA Benefit-Sharing Fee Estimator
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
                        Text(text = "💰", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NBA / ABS Benefit-Sharing Fee Calculator",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                        )
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF16A34A).copy(alpha = 0.12f)) {
                        Text(
                            text = "REGULATIONS 2014",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Calculate statutory benefit-sharing obligation based on annual ex-factory purchase / turnover of biological resources:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Commercial Value: ₹${commercialTurnoverLakhs.toInt()} Lakhs",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Navy900)
                    )
                    Text(
                        text = "Statutory Rate: $estimatedFeePercent%",
                        style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlue800, fontWeight = FontWeight.Bold)
                    )
                }
                Slider(
                    value = commercialTurnoverLakhs,
                    onValueChange = { commercialTurnoverLakhs = it },
                    valueRange = 5f..500f,
                    steps = 19,
                    colors = SliderDefaults.colors(thumbColor = RoyalBlue800, activeTrackColor = RoyalBlue800)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF16A34A).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "ESTIMATED ANNUAL ABS CONTRIBUTION", style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 9.sp))
                            Text(text = "₹${String.format("%,d", estimatedFeeAmount)} / year", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF15803D)))
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color.White) {
                            Text(
                                text = "Exempt for Indian Farmers",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            )
                        }
                    }
                }
            }
        }

        // Widget 2: Botanical Sourcing Compliance Checker (High-Risk Flora)
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
                        Text(text = "🌲", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "High-Risk Flora Sourcing Checker",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                        )
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = RiskHigh.copy(alpha = 0.12f)) {
                        Text(
                            text = "SEC 38 RESTRICTED",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = RiskHigh, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Verify whether harvested botanicals require Forest Department Transit Passes or SBB clearance:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(highRiskFlora.indices.toList()) { idx ->
                        val item = highRiskFlora[idx]
                        val isSel = selectedFloraIndex == idx
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Navy900 else Slate100,
                            modifier = Modifier.clickable { selectedFloraIndex = idx }
                        ) {
                            Text(
                                text = item.name,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSel) Color.White else Navy900,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                val selFlora = highRiskFlora[selectedFloraIndex]
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
                            Text(text = "${selFlora.name} (${selFlora.botanicalName})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900))
                            Text(text = selFlora.status, style = MaterialTheme.typography.labelSmall.copy(color = RiskHigh, fontWeight = FontWeight.Bold, fontSize = 10.sp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = selFlora.complianceRule, style = MaterialTheme.typography.bodySmall.copy(color = Slate700, fontSize = 11.sp))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val prompt = "Provide transit permit and State Biodiversity Board compliance requirements for harvesting and trading ${selFlora.name} (${selFlora.botanicalName}) under Biological Diversity Act Section 38."
                                onOpenChatWithQuery(prompt, true)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, RoyalBlue800.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ask AyurBot for Transit Permit Guidance", style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlue800, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // Widget 3: "Which Authority Do I Need Approval From?" Decision Tree
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate100),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🧭 Which Authority Do I Need Approval From?",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap a sourcing scenario to determine statutory jurisdiction (BMC vs SBB vs NBA):",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
                Spacer(modifier = Modifier.height(10.dp))

                authorityScenarios.forEachIndexed { idx, s ->
                    val isSel = selectedTreeScenario == idx
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) CardBackground else Slate50,
                        border = BorderStroke(if (isSel) 1.5.dp else 1.dp, if (isSel) RoyalBlue800 else Slate200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedTreeScenario = idx }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = s.scenario, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Navy900), modifier = Modifier.weight(1f))
                                Surface(shape = RoundedCornerShape(4.dp), color = if (s.authority.contains("No NBA")) Color(0xFF16A34A).copy(alpha = 0.15f) else RoyalBlue800.copy(alpha = 0.12f)) {
                                    Text(
                                        text = s.authority,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (s.authority.contains("No NBA")) Color(0xFF15803D) else RoyalBlue800,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                            if (isSel) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = s.detail, style = MaterialTheme.typography.bodySmall.copy(color = Slate700, fontSize = 11.sp))
                            }
                        }
                    }
                }
            }
        }

        // Widget 4: Plain-Language Biodiversity Glossary & GACP Reminders
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📖 Biodiversity Glossary & GACP Reminders",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Navy900)
                )
                Spacer(modifier = Modifier.height(6.dp))
                GlossaryTerm(term = "BMC (Biodiversity Management Committee)", desc = "Local village body maintaining People's Biodiversity Registers (PBR) and collecting local access fees.")
                GlossaryTerm(term = "NTC (Normally Traded Commodities)", desc = "List of 421 bio-resources exempted from SBB/NBA approval when traded strictly as agricultural produce.")
                GlossaryTerm(term = "GACP Compliance", desc = "WHO/NMPB Good Agricultural and Collection Practices for heavy metal-free soil and seasonal harvest timing.")
            }
        }
    }
}

private data class ProtectedFlora(val name: String, val botanicalName: String, val status: String, val complianceRule: String)
private data class AuthorityStep(val scenario: String, val authority: String, val detail: String)

@Composable
private fun GlossaryTerm(term: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = term, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = RoyalBlue800))
        Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp))
        HorizontalDivider(color = Slate100, modifier = Modifier.padding(top = 4.dp))
    }
}
