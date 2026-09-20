package com.ipsakti.sahayak.ui.screens.regulatory

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
import com.ipsakti.sahayak.data.model.*
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegulatoryGuidanceScreen(
    onNavigateBack: () -> Unit,
    onNavigateToClaims: () -> Unit = {},
    onNavigateToLabel: () -> Unit = {},
    onNavigateToInternational: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { IpSaktiRepository() }

    var productName by remember { mutableStateOf("Triphala Guggulu Plus") }
    var productType by remember { mutableStateOf("Ayurvedic Polyherbal Formulation") }
    var ingredientsInput by remember { mutableStateOf("Amalaki, Haritaki, Bibhitaki, Commiphora mukul, Piper nigrum") }
    var dosageForm by remember { mutableStateOf("Tablet / Vati") }
    var intendedUse by remember { mutableStateOf("Metabolic balance and anti-inflammatory joint support") }
    var claimsInput by remember { mutableStateOf("Supports joint mobility and healthy lipid balance. Traditionally used to cleanse ama.") }
    var selectedMarkets by remember { mutableStateOf(setOf("India", "USA")) }

    var isLoading by remember { mutableStateOf(false) }
    var guidanceResult by remember { mutableStateOf<RegulatoryGuidanceResponse?>(null) }
    var selectedRequirement by remember { mutableStateOf<RegulatoryRequirement?>(null) }

    // Execute initial analysis on screen enter so the user immediately sees data
    LaunchedEffect(Unit) {
        isLoading = true
        val input = RegulatoryGuidanceInput(
            productName = productName,
            productType = productType,
            ingredients = ingredientsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            dosageForm = dosageForm,
            intendedUse = intendedUse,
            claims = claimsInput,
            targetMarkets = selectedMarkets.toList()
        )
        repository.evaluateRegulatoryGuidance(input).onSuccess {
            guidanceResult = it
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Regulatory Guidance Engine",
                subtitle = "Statutory Classification & Multi-Jurisdiction Checklist",
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
                            Text(text = "⚖️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "STATUTORY GUIDANCE ENGINE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Gold600,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "Evidence-Backed Regulatory Routing",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Classifies your Ayurvedic formulation under the Drugs & Cosmetics Act 1940, Patents Act Sec 3(p), Biological Diversity Act 2002, and target export standards.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate300)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "QUICK LOAD PRESETS:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Gold600,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    productName = "Classical Triphala Vati"
                                    productType = "Classical Ayurvedic Medicine"
                                    ingredientsInput = "Haritaki (Terminalia chebula), Bibhitaki (Terminalia bellirica), Amalaki (Emblica officinalis)"
                                    dosageForm = "Vati / Tablet"
                                    intendedUse = "Digestive health and mild laxative Rasayana"
                                    claimsInput = "Supports healthy digestion and regular elimination"
                                    selectedMarkets = setOf("India")
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("🌿 Classical Vati", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            FilledTonalButton(
                                onClick = {
                                    productName = "Ashwagandha KSM-66 Synergy"
                                    productType = "Patent / Proprietary ASU Drug"
                                    ingredientsInput = "Withania somnifera standardized root extract, Piper nigrum (BioPerine)"
                                    dosageForm = "Gel Capsule"
                                    intendedUse = "Stress resilience, cortisol management and vitality"
                                    claimsInput = "Reduces stress and promotes restorative sleep. Non-drowsy formulation."
                                    selectedMarkets = setOf("India", "USA", "EU")
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("🚀 Proprietary Extract", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            text = "PRODUCT SPECIFICATIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            label = { Text("Product Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = ingredientsInput,
                            onValueChange = { ingredientsInput = it },
                            label = { Text("Botanical Ingredients (comma separated)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = dosageForm,
                                onValueChange = { dosageForm = it },
                                label = { Text("Dosage Form") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = productType,
                                onValueChange = { productType = it },
                                label = { Text("Category") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = claimsInput,
                            onValueChange = { claimsInput = it },
                            label = { Text("Proposed Marketing / Label Claims") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Target Jurisdictions:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Navy900)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("India", "USA", "EU").forEach { market ->
                                val selected = selectedMarkets.contains(market)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedMarkets = if (selected) selectedMarkets - market else selectedMarkets + market
                                    },
                                    label = { Text(market) },
                                    leadingIcon = if (selected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    val input = RegulatoryGuidanceInput(
                                        productName = productName,
                                        productType = productType,
                                        ingredients = ingredientsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                        dosageForm = dosageForm,
                                        intendedUse = intendedUse,
                                        claims = claimsInput,
                                        targetMarkets = selectedMarkets.toList()
                                    )
                                    repository.evaluateRegulatoryGuidance(input).onSuccess {
                                        guidanceResult = it
                                        isLoading = false
                                    }.onFailure {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing Regulatory Requirements...")
                            } else {
                                Icon(Icons.Default.Balance, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Regulatory Guidance", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Results View
            guidanceResult?.let { result ->
                // 1. Classification & Reasoning Card
                item {
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
                                    text = "POTENTIAL CLASSIFICATION",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate500,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = RoyalBlue800.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${(result.classification.confidenceScore * 100).toInt()}% Confidence",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = RoyalBlue800,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = result.classification.potentialCategory,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Governing Rule: ${result.classification.governingRules}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RoyalBlue800,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Regulatory Authority: ${result.classification.authority}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Slate100
                            ) {
                                Text(
                                    text = result.classification.legalReasoning,
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Navy900,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. Trust & Reliability Panel
                item {
                    TrustReliabilityCard(trust = result.trustReport)
                }

                // 3. Checklist Completion Banner
                item {
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
                                        text = "STATUTORY CHECKLIST COMPLETION",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate500,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Text(
                                        text = "Prerequisites Satisfied: ${result.checklistCompletionPct}%",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (result.checklistCompletionPct >= 70) Color(0xFF16A34A).copy(alpha = 0.15f) else Gold600.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (result.checklistCompletionPct >= 70) "SATISFACTORY" else "ACTION REQUIRED",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (result.checklistCompletionPct >= 70) Color(0xFF16A34A) else Gold800,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { result.checklistCompletionPct / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = if (result.checklistCompletionPct >= 70) Color(0xFF16A34A) else Gold600,
                                trackColor = Slate200
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Notice: This score reflects checklist item completion, not an arbitrary legal compliance certification.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate500,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // 4. Statutory Requirements List Header
                item {
                    Text(
                        text = "MANDATORY STATUTORY REQUIREMENTS (${result.checklistItems.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                }

                // 5. Statutory Requirements Items
                items(result.checklistItems) { req ->
                    RequirementItemCard(
                        requirement = req,
                        onClick = { selectedRequirement = req }
                    )
                }

                // 6. Dynamic Roadmap
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "DYNAMIC REGULATORY ROADMAP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            result.roadmapSteps.forEachIndexed { idx, step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when (step.status) {
                                            "completed" -> Color(0xFF16A34A)
                                            "in_progress" -> RoyalBlue800
                                            else -> Slate300
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${idx + 1}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = step.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Navy900
                                            )
                                        )
                                        Text(
                                            text = step.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate600,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = step.duration ?: "Standard",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate500,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                if (idx < result.roadmapSteps.size - 1) {
                                    HorizontalDivider(color = Slate200, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                // 7. Quick Jump to Specialized Modules
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "SPECIALIZED REGULATORY MODULES",
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
                                OutlinedButton(
                                    onClick = onNavigateToClaims,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("⚠️ Claims Scanner", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onNavigateToLabel,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("🏷️ Label Checker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = onNavigateToInternational,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("🌐 International Regulatory Comparison (India vs US vs EU)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 8. Export & Share Dossier
                item {
                    Button(
                        onClick = {
                            val dossier = buildString {
                                appendLine("=== IP-SAKTI SAHAYAK: REGULATORY GUIDANCE DOSSIER ===")
                                appendLine("Product: ${result.productName}")
                                appendLine("Potential Classification: ${result.classification.potentialCategory}")
                                appendLine("Governing Rules: ${result.classification.governingRules}")
                                appendLine("Authority: ${result.classification.authority}")
                                appendLine("Checklist Completion: ${result.checklistCompletionPct}%")
                                appendLine("\n--- STATUTORY REQUIREMENTS ---")
                                result.checklistItems.forEach { req ->
                                    appendLine("[${req.status.uppercase()}] ${req.title} (${req.sourceDocument} ${req.section ?: ""})")
                                    appendLine("  Action: ${req.whatUserShouldDoNext}")
                                }
                                appendLine("\n--- DYNAMIC ROADMAP ---")
                                result.roadmapSteps.forEach { step ->
                                    appendLine("• ${step.title} [${step.duration}]")
                                }
                                appendLine("\nDisclaimer: ${result.disclaimer}")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Regulatory Guidance Dossier - ${result.productName}")
                                putExtra(Intent.EXTRA_TEXT, dossier)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Regulatory Dossier"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Regulatory Dossier", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal dialog when clicking any statutory checklist item
    selectedRequirement?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedRequirement = null },
            title = {
                Text(
                    text = req.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (req.status) {
                                "complete" -> Color(0xFF16A34A).copy(alpha = 0.15f)
                                "missing" -> Color(0xFFDC2626).copy(alpha = 0.15f)
                                else -> Gold600.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = req.status.replace("_", " ").uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = when (req.status) {
                                        "complete" -> Color(0xFF16A34A)
                                        "missing" -> Color(0xFFDC2626)
                                        else -> Gold800
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = "Category: ${req.category}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }

                    Column {
                        Text(
                            text = "WHAT IS REQUIRED:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate600)
                        )
                        Text(
                            text = req.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = Navy900)
                        )
                    }

                    Column {
                        Text(
                            text = "SOURCE AUTHORITY & SECTION:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate600)
                        )
                        Text(
                            text = "${req.sourceDocument} (${req.section ?: "General Provision"})",
                            style = MaterialTheme.typography.bodySmall.copy(color = RoyalBlue800, fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Authority: ${req.authority}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate100
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "STATUTORY EVIDENCE PASSAGE:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate600)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "\"${req.evidencePassage}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Navy900,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "WHAT YOU SHOULD DO NEXT:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        )
                        Text(
                            text = req.whatUserShouldDoNext,
                            style = MaterialTheme.typography.bodySmall.copy(color = Navy900)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedRequirement = null }) {
                    Text("Close", color = RoyalBlue800, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun RequirementItemCard(
    requirement: RegulatoryRequirement,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        when (requirement.status) {
                            "complete" -> Color(0xFF16A34A)
                            "missing" -> Color(0xFFDC2626)
                            else -> Gold600
                        }
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = requirement.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = requirement.status.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = when (requirement.status) {
                                "complete" -> Color(0xFF16A34A)
                                "missing" -> Color(0xFFDC2626)
                                else -> Gold800
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${requirement.sourceDocument} • ${requirement.section ?: "Section N/A"}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RoyalBlue800,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = requirement.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate600,
                        fontSize = 11.sp
                    ),
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Details",
                tint = Slate400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun TrustReliabilityCard(trust: TrustReliabilityReport) {
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
                    text = "TRUST & RELIABILITY PANEL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF16A34A).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "AUTHORITATIVE RETRIEVAL",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricChip(label = "Evidence Found", value = if (trust.evidenceFound) "YES" else "NO", isPositive = trust.evidenceFound, modifier = Modifier.weight(1f))
                MetricChip(label = "Retrieval Conf.", value = "${(trust.retrievalConfidence * 100).toInt()}%", isPositive = true, modifier = Modifier.weight(1f))
                MetricChip(label = "Coverage", value = "${(trust.evidenceCoverage * 100).toInt()}%", isPositive = true, modifier = Modifier.weight(1f))
            }
            if (trust.humanEscalationRecommended) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Human Legal Verification Recommended for high-risk / novel extraction steps.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFDC2626), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String, isPositive: Boolean, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Slate100,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = Slate500, fontSize = 10.sp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) Navy900 else Color(0xFFDC2626)
                )
            )
        }
    }
}
