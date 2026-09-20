package com.ipsakti.sahayak.ui.screens.international

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.model.InternationalComparisonDimension
import com.ipsakti.sahayak.data.model.InternationalComparisonResponse
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun InternationalComparisonScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { IpSaktiRepository() }

    var productCategory by remember { mutableStateOf("Ayurvedic Polyherbal Formulation") }
    var selectedTargetMarkets by remember { mutableStateOf(listOf("India", "USA", "EU")) }

    var isLoading by remember { mutableStateOf(false) }
    var comparisonResult by remember { mutableStateOf<InternationalComparisonResponse?>(null) }
    var expandedDimensionIndex by remember { mutableStateOf(0) }

    LaunchedEffect(productCategory) {
        isLoading = true
        repository.compareInternationalRegulations(productCategory, selectedTargetMarkets).onSuccess {
            comparisonResult = it
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Global Regulatory Matrix",
                subtitle = "India (AYUSH) vs USA (FDA) vs European Union (EMA/THMPD)",
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
            // Header Card
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
                            Text(text = "🌐", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CROSS-BORDER HARMONIZATION",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Gold600,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "India vs USA vs European Union",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Comparative statutory analysis of pre-market licensing, cGMP manufacturing, heavy metal thresholds, structure-function claims, and Nagoya Protocol biodiversity obligations.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate300)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = RoyalBlue800.copy(alpha = 0.5f)) {
                                Text(text = "🇮🇳 AYUSH / D&C Act", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = RoyalBlue800.copy(alpha = 0.5f)) {
                                Text(text = "🇺🇸 US FDA DSHEA", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = RoyalBlue800.copy(alpha = 0.5f)) {
                                Text(text = "🇪🇺 EMA THMPD", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Formulation Category Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SELECT FORMULATION TYPE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Polyherbal Capsule",
                                "Herbal Churna",
                                "Medicated Taila"
                            ).forEach { cat ->
                                val isSelected = productCategory.contains(cat.split(" ")[0])
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) RoyalBlue800 else Slate100,
                                    border = BorderStroke(1.dp, if (isSelected) RoyalBlue800 else Slate300),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            productCategory = "$cat Formulation"
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) Color.White else Navy900,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Export Readiness Alerts Banner
            comparisonResult?.let { result ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationImportant, contentDescription = null, tint = Gold800, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CRITICAL EXPORT CLEARANCE ALERTS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Gold800,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            result.exportReadinessAlerts.forEach { alert ->
                                Text(
                                    text = "• $alert",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Navy900, fontSize = 11.sp, lineHeight = 16.sp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Comparative Dimensions List Header
                item {
                    Text(
                        text = "CROSS-BORDER REGULATORY MATRIX (${result.dimensions.size} DIMENSIONS)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                }

                // Dimension Rows
                items(result.dimensions.indices.toList()) { idx ->
                    val dim = result.dimensions[idx]
                    val isExpanded = expandedDimensionIndex == idx
                    DimensionComparisonCard(
                        dimension = dim,
                        isExpanded = isExpanded,
                        onToggle = {
                            expandedDimensionIndex = if (isExpanded) -1 else idx
                        }
                    )
                }

                // Country Cautions Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "COUNTRY-SPECIFIC COMPLIANCE CAUTIONS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            result.countrySpecificCautions.forEach { (country, note) ->
                                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = "$country: ",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = RoyalBlue800,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Text(
                                        text = note,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Navy900,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                HorizontalDivider(color = Slate100, thickness = 0.5.dp)
                            }
                        }
                    }
                }

                // Export Dossier Action
                item {
                    Button(
                        onClick = {
                            val matrixText = buildString {
                                appendLine("=== IP-SAKTI SAHAYAK: GLOBAL REGULATORY MATRIX ===")
                                appendLine("Product Category: ${result.productTitle}")
                                appendLine("Comparative Jurisdictions: India (AYUSH) vs USA (FDA) vs European Union (EMA)")
                                appendLine("\n--- CRITICAL EXPORT CLEARANCE ALERTS ---")
                                result.exportReadinessAlerts.forEach { alert ->
                                    appendLine("• $alert")
                                }
                                appendLine("\n--- REGULATORY COMPARISON MATRIX ---")
                                result.dimensions.forEach { d ->
                                    appendLine("\n[${d.dimension.uppercase()}]")
                                    appendLine("🇮🇳 India: ${d.indiaDetails}")
                                    appendLine("🇺🇸 USA: ${d.usaDetails}")
                                    appendLine("🇪🇺 EU: ${d.euDetails}")
                                    appendLine("⚡ Key Difference: ${d.keyDifferences}")
                                }
                                appendLine("\nGenerated by IP-SAKTI Sahayak (SIH PS26045).")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Global Regulatory Matrix - ${result.productTitle}")
                                putExtra(Intent.EXTRA_TEXT, matrixText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Global Regulatory Matrix"))
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
                        Text("Export Cross-Border Matrix Dossier", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DimensionComparisonCard(
    dimension: InternationalComparisonDimension,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
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
                    text = dimension.dimension,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Slate500
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = RoyalBlue800.copy(alpha = 0.08f)
            ) {
                Text(
                    text = "Key Difference: ${dimension.keyDifferences}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RoyalBlue800,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JurisdictionBlock(
                        flag = "🇮🇳",
                        country = "India (AYUSH / CDSCO)",
                        details = dimension.indiaDetails,
                        color = Color(0xFFD97706)
                    )
                    JurisdictionBlock(
                        flag = "🇺🇸",
                        country = "United States (US FDA / DSHEA)",
                        details = dimension.usaDetails,
                        color = RoyalBlue800
                    )
                    JurisdictionBlock(
                        flag = "🇪🇺",
                        country = "European Union (EMA / THMPD)",
                        details = dimension.euDetails,
                        color = Color(0xFF2563EB)
                    )
                }
            }
        }
    }
}

@Composable
private fun JurisdictionBlock(
    flag: String,
    country: String,
    details: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Slate100,
        border = BorderStroke(0.5.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = flag, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = country,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Navy900,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            )
        }
    }
}
