package com.ipsakti.sahayak.ui.screens.wizard

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
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*

data class WizardQuestion(
    val step: Int,
    val title: String,
    val subtitle: String,
    val options: List<WizardOption>
)

data class WizardOption(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val tendency: String // "AYUSH", "NUTRACEUTICAL", "COSMETIC", "FOOD"
)

data class ClassificationResult(
    val categoryName: String,
    val regulatorName: String,
    val statutoryLaw: String,
    val applicationForms: String,
    val keyChecklist: List<String>,
    val riskWarning: String,
    val colorPrimary: Color
)

@Composable
fun ClassificationWizardScreen(
    onNavigateBack: () -> Unit,
    onStartInvestigation: (category: String, regulator: String) -> Unit = { _, _ -> }
) {
    val questions = remember {
        listOf(
            WizardQuestion(
                step = 1,
                title = "Primary Intended Use & Market Claims",
                subtitle = "What is the primary benefit or therapeutic intent communicated to the consumer?",
                options = listOf(
                    WizardOption(
                        id = "use_therapeutic",
                        emoji = "💊",
                        title = "Therapeutic / Medicinal Treatment",
                        description = "Cures, treats, mitigates, or diagnoses a disease/disorder (e.g. anti-diabetic, anti-arthritic).",
                        tendency = "AYUSH"
                    ),
                    WizardOption(
                        id = "use_supplement",
                        emoji = "🌿",
                        title = "Dietary Supplement & General Vitality",
                        description = "Supports physiological functions, boosts natural immunity, or supplements daily nutrition.",
                        tendency = "NUTRACEUTICAL"
                    ),
                    WizardOption(
                        id = "use_cosmetic",
                        emoji = "✨",
                        title = "Beautification, Cleansing & Grooming",
                        description = "Applied externally to alter appearance, moisturize, cleanse, or perfume without disease claims.",
                        tendency = "COSMETIC"
                    ),
                    WizardOption(
                        id = "use_food",
                        emoji = "🍵",
                        title = "Nourishment, Taste & Beverage",
                        description = "Conventional consumable nourishment, herbal tea, confection, or culinary spice blend.",
                        tendency = "FOOD"
                    )
                )
            ),
            WizardQuestion(
                step = 2,
                title = "Active Formulation Composition",
                subtitle = "How are the herbal and auxiliary ingredients sourced and composed?",
                options = listOf(
                    WizardOption(
                        id = "comp_classical",
                        emoji = "📜",
                        title = "100% Classical Textual Plants",
                        description = "Herbs and recipes exclusively referenced in First Schedule classical texts (AFI, Charaka, Sushruta).",
                        tendency = "AYUSH"
                    ),
                    WizardOption(
                        id = "comp_extracts",
                        emoji = "🧪",
                        title = "Standardized Botanical Extracts + Nutrients",
                        description = "Isolated active phytochemicals (e.g. 95% Curcuminoids) blended with minerals or vitamins.",
                        tendency = "NUTRACEUTICAL"
                    ),
                    WizardOption(
                        id = "comp_cosmetic_base",
                        emoji = "🧴",
                        title = "Dermatological Base with Herbal Actives",
                        description = "Topical lotions, creams, serums, and surfactant shampoos infused with plant extracts.",
                        tendency = "COSMETIC"
                    ),
                    WizardOption(
                        id = "comp_food_base",
                        emoji = "🌾",
                        title = "Conventional Food Ingredients + Spices",
                        description = "Whole herbs combined with honey, jaggery, cereals, or standard edible oils.",
                        tendency = "FOOD"
                    )
                )
            ),
            WizardQuestion(
                step = 3,
                title = "Dosage Form & Delivery Route",
                subtitle = "In what physical finished form is the product dispensed?",
                options = listOf(
                    WizardOption(
                        id = "form_pharma",
                        emoji = "💊",
                        title = "Pharmaceutical Solid/Liquid Dosage",
                        description = "Tablets (Vati), capsules, medicated ghee (Ghrita), fermented decoction (Asava/Arishta).",
                        tendency = "AYUSH"
                    ),
                    WizardOption(
                        id = "form_nutra",
                        emoji = "🥤",
                        title = "Effervescent / Powder / Sachet",
                        description = "Gummies, chewable tablets, protein blends, or dissolving effervescent sticks.",
                        tendency = "NUTRACEUTICAL"
                    ),
                    WizardOption(
                        id = "form_topical",
                        emoji = "💆",
                        title = "External Application (Topical)",
                        description = "Face oils (Taila), body pastes (Lepa), sunscreen balms, herbal scalp cleansers.",
                        tendency = "COSMETIC"
                    ),
                    WizardOption(
                        id = "form_culinary",
                        emoji = "🫖",
                        title = "Culinary Beverage or Ready-to-Eat",
                        description = "Brewing herbal infusion bags, RTD bottles, health bars, or syrup cordials.",
                        tendency = "FOOD"
                    )
                )
            ),
            WizardQuestion(
                step = 4,
                title = "Permissible Label Claims",
                subtitle = "What claim category does your packaging and marketing copy represent?",
                options = listOf(
                    WizardOption(
                        id = "claim_medical",
                        emoji = "🩺",
                        title = "Disease Specific Therapeutic Claims",
                        description = "'Clinically proven to reduce joint inflammation and support Ayurvedic management of Sandhivata.'",
                        tendency = "AYUSH"
                    ),
                    WizardOption(
                        id = "claim_structure_fn",
                        emoji = "🛡️",
                        title = "Structure / Function Health Claims",
                        description = "'Helps maintain healthy cellular energy and physiological resilience against environmental stress.'",
                        tendency = "NUTRACEUTICAL"
                    ),
                    WizardOption(
                        id = "claim_aesthetic",
                        emoji = "🌟",
                        title = "Aesthetic & Sensory Claims",
                        description = "'Deeply nourishes dermal moisture barrier, leaving skin radiant and hair visibly voluminous.'",
                        tendency = "COSMETIC"
                    ),
                    WizardOption(
                        id = "claim_taste",
                        emoji = "🍯",
                        title = "Traditional Refreshment & Nutritional Facts",
                        description = "'Rich comforting taste with natural antioxidants. No artificial preservatives.'",
                        tendency = "FOOD"
                    )
                )
            ),
            WizardQuestion(
                step = 5,
                title = "Quality Control & Manufacturing Standards",
                subtitle = "Which statutory Good Manufacturing Practice (GMP) audit guideline applies?",
                options = listOf(
                    WizardOption(
                        id = "gmp_schedule_t",
                        emoji = "🏛️",
                        title = "ASU Schedule T GMP Certification",
                        description = "State Licensing Authority audit for Ayurvedic, Siddha, Unani manufacturing premises.",
                        tendency = "AYUSH"
                    ),
                    WizardOption(
                        id = "gmp_fssai",
                        emoji = "🔬",
                        title = "FSSAI Schedule 4 / HACCP Standards",
                        description = "Central food safety hygiene, RDA limit compliance, and allergen declarations.",
                        tendency = "NUTRACEUTICAL"
                    ),
                    WizardOption(
                        id = "gmp_cosmetics",
                        emoji = "💄",
                        title = "Bureau of Indian Standards (BIS) / Part XIII",
                        description = "Cosmetic GMP standards, heavy metal test limits, and batch safety verification.",
                        tendency = "COSMETIC"
                    )
                )
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val selectedOptions = remember { mutableStateMapOf<Int, WizardOption>() }
    val isComplete = currentStepIndex >= questions.size

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Classification Wizard",
                subtitle = "Regulatory Decision Engine (AYUSH vs FSSAI vs CDSCO)",
                canNavigateBack = true,
                onNavigateBack = {
                    if (currentStepIndex > 0) {
                        currentStepIndex--
                    } else {
                        onNavigateBack()
                    }
                }
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
            // Step Progress Bar
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
                            text = if (isComplete) "CLASSIFICATION COMPLETE" else "STEP ${currentStepIndex + 1} OF ${questions.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isComplete) Color(0xFF16A34A) else RoyalBlue800,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = if (isComplete) "100%" else "${((currentStepIndex) * 100) / questions.size}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (isComplete) 1f else (currentStepIndex.toFloat() / questions.size.toFloat()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (isComplete) Color(0xFF16A34A) else RoyalBlue800,
                        trackColor = Slate200
                    )
                }
            }

            if (!isComplete) {
                val currentQ = questions[currentStepIndex]
                val currentSelected = selectedOptions[currentStepIndex]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy900),
                    border = BorderStroke(1.dp, Navy700)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "DECISION CRITERIA #${currentQ.step}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Gold600,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentQ.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentQ.subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate300)
                        )
                    }
                }

                // Options list
                currentQ.options.forEach { option ->
                    val isSelected = currentSelected?.id == option.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOptions[currentStepIndex] = option },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) RoyalBlue800.copy(alpha = 0.08f) else CardBackground
                        ),
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) RoyalBlue800 else CardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.emoji,
                                fontSize = 26.sp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) RoyalBlue800 else Navy900
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate600,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedOptions[currentStepIndex] = option },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = RoyalBlue800,
                                    unselectedColor = Slate400
                                )
                            )
                        }
                    }
                }

                // Navigation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Previous")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = { currentStepIndex++ },
                        enabled = currentSelected != null,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800)
                    ) {
                        Text(if (currentStepIndex == questions.size - 1) "Compute Classification" else "Next Step")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            } else {
                // Final Results Calculation
                val tendencies = selectedOptions.values.map { it.tendency }
                val ayushCount = tendencies.count { it == "AYUSH" }
                val nutraCount = tendencies.count { it == "NUTRACEUTICAL" }
                val cosmeticCount = tendencies.count { it == "COSMETIC" }

                val result = when {
                    ayushCount >= 2 -> ClassificationResult(
                        categoryName = "Ayurvedic Proprietary Medicine (ASU Drug)",
                        regulatorName = "Ministry of Ayush / State Licensing Authority (SLA)",
                        statutoryLaw = "Drugs & Cosmetics Act, 1940 & Rules 1945 (Rule 158B, Schedule T)",
                        applicationForms = "Form 24-D (Application for Manufacturing License) / Form 25-D",
                        keyChecklist = listOf(
                            "Mandatory Schedule T Good Manufacturing Practices (GMP) audit",
                            "Published scientific/classical citations proving synergistic safety",
                            "Heavy metal, pesticide residue, and microbial limit testing (API parameters)",
                            "State Licensing Authority technical expert committee review"
                        ),
                        riskWarning = "Strict prohibition against allopathic drug adulteration. Claims must match classical textual rationale or Rule 158B pilot clinical trials.",
                        colorPrimary = RoyalBlue800
                    )
                    cosmeticCount >= 2 -> ClassificationResult(
                        categoryName = "Ayurvedic Cosmeceutical / Herbal Cosmetic",
                        regulatorName = "CDSCO & State Drug Controller (Cosmetics Division)",
                        statutoryLaw = "Drugs & Cosmetics Rules 1945 (Part XIII) & BIS IS:4707 Standards",
                        applicationForms = "Form 31 (Manufacturing Application) / Form 32 (Grant of License)",
                        keyChecklist = listOf(
                            "No therapeutic disease curing claims allowed on package or carton",
                            "Dermal irritation & ocular safety testing protocol",
                            "Strict compliance with Bureau of Indian Standards (BIS) heavy metal limits (Lead < 20 ppm, Arsenic < 2 ppm)",
                            "List of complete cosmetic excipients and botanical INCI nomenclature"
                        ),
                        riskWarning = "Misbranding risk: Do NOT make medical claims like 'cures alopecia' or 'heals eczema' under cosmetic licensing.",
                        colorPrimary = Gold600
                    )
                    nutraCount >= 2 -> ClassificationResult(
                        categoryName = "Nutraceutical / Health Supplement (Ayur Aahar)",
                        regulatorName = "FSSAI (Food Safety & Standards Authority of India)",
                        statutoryLaw = "FSS (Health Supplements, Nutraceuticals, FSDU) Regs 2022 & Ayur Aahar Norms",
                        applicationForms = "FSSAI Central Food Safety License (Form B via FoSCoS)",
                        keyChecklist = listOf(
                            "Adherence to ICMR-NIN Recommended Dietary Allowances (RDA) 100% cap",
                            "Ingredients must exist on FSSAI Schedule I / Schedule IV botanical lists",
                            "Mandatory 'Ayur Aahar' logo and disclaimer 'NOT FOR MEDICINAL USE'",
                            "Purity certificate for botanical extract solvents"
                        ),
                        riskWarning = "FSSAI strictly penalizes disease treatment claims. Product cannot state 'treats diabetes'—only 'supports healthy blood sugar'.",
                        colorPrimary = Color(0xFF16A34A)
                    )
                    else -> ClassificationResult(
                        categoryName = "Herbal Food & Infusion Product",
                        regulatorName = "FSSAI (Food Safety and Standards Authority of India)",
                        statutoryLaw = "Food Safety and Standards Act, 2006 (General Food Regulations)",
                        applicationForms = "FSSAI State / Central FBO Registration & License",
                        keyChecklist = listOf(
                            "Good Hygiene Practices (GHP) and standard food safety packaging",
                            "Nutritional facts panel with energy, carbs, protein, and sugar content",
                            "Botanicals used must be conventional culinary herbs or permitted food ingredients",
                            "Shelf-life testing and best-before labeling compliance"
                        ),
                        riskWarning = "Must not mimic pharmaceutical tablet/capsule blister packaging without health supplement clearance.",
                        colorPrimary = Color(0xFF0D9488)
                    )
                }

                // Render Result Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy900),
                    border = BorderStroke(1.dp, Navy700)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Gold600.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "DETERMINED CLASSIFICATION",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Gold600,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = result.categoryName,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RoyalBlue800
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Primary Regulator: ${result.regulatorName}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // Statutory Governance & Application Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "STATUTORY LAW & FILING FORMS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.statutoryLaw,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Slate100
                        ) {
                            Text(
                                text = "Application Route: ${result.applicationForms}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RoyalBlue800,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // Mandatory Checklist Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MANDATORY REGULATORY CHECKLIST",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate500,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        result.keyChecklist.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Navy900,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Regulatory Risk Warning
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Regulatory Border Caution",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = result.riskWarning,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFB91C1C),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedOptions.clear()
                            currentStepIndex = 0
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restart Wizard")
                    }

                    Button(
                        onClick = {
                            onStartInvestigation(result.categoryName, result.regulatorName)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyze in App")
                    }
                }
            }
        }
    }
}
