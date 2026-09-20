package com.ipsakti.sahayak.ui.screens.craft

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.api.RetrofitClient
import com.ipsakti.sahayak.data.model.BotanicalHerb
import com.ipsakti.sahayak.data.model.CraftCombineRequest
import com.ipsakti.sahayak.data.model.CraftCombineResponse
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val DEFAULT_APOTHECARY = listOf(
    BotanicalHerb("turmeric", "Turmeric", "Haridra", "Curcuma longa", "Curcuminoids", "Lekhaniya", "🫚"),
    BotanicalHerb("black_pepper", "Black Pepper", "Maricha", "Piper nigrum", "Piperine", "Deepana", "🌶️"),
    BotanicalHerb("pipali", "Long Pepper", "Pipali", "Piper longum", "Piperlongumine", "Rasayana", "🌿"),
    BotanicalHerb("dry_ginger", "Dry Ginger", "Shunthi", "Zingiber officinale", "Gingerols", "Deepana", "🫚"),
    BotanicalHerb("amla", "Amla", "Amalaki", "Emblica officinalis", "Ascorbic acid & Tannoids", "Vayasthapana", "🍏"),
    BotanicalHerb("haritaki", "Haritaki", "Haritaki", "Terminalia chebula", "Chebulic acid", "Rasayana", "🌰"),
    BotanicalHerb("bibhitaki", "Bibhitaki", "Bibhitaki", "Terminalia bellirica", "Ellagic acid", "Chakshushya", "🥜"),
    BotanicalHerb("ashwagandha", "Ashwagandha", "Ashwagandha", "Withania somnifera", "Withanolides", "Balya", "🌾"),
    BotanicalHerb("brahmi", "Brahmi", "Brahmi", "Bacopa monnieri", "Bacosides A & B", "Medhya", "🍃"),
    BotanicalHerb("tulsi", "Holy Basil", "Tulsi", "Ocimum sanctum", "Eugenol & Ursolic acid", "Shvasahara", "🌱"),
    BotanicalHerb("neem", "Neem", "Nimba", "Azadirachta indica", "Azadirachtin", "Kushtaghna", "🌿"),
    BotanicalHerb("guduchi", "Giloy", "Guduchi", "Tinospora cordifolia", "Tinosporide & Berberine", "Jvarahara", "🎋"),
    BotanicalHerb("shatavari", "Shatavari", "Shatavari", "Asparagus racemosus", "Shatavarins", "Stanyajanana", "🌾"),
    BotanicalHerb("guggulu", "Bdellium", "Guggulu", "Commiphora mukul", "Guggulsterones", "Sandhivata", "🪵"),
    BotanicalHerb("mulethi", "Licorice", "Yashtimadhu", "Glycyrrhiza glabra", "Glycyrrhizin", "Kanthya", "🪵")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulationCraftScreen(
    onNavigateBack: () -> Unit,
    onTestInInvestigation: (String) -> Unit = {},
    onConsultAssistant: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var apothecary by remember { mutableStateOf(DEFAULT_APOTHECARY) }
    val crucibleHerbs = remember { mutableStateListOf<BotanicalHerb>() }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    
    var isCombining by remember { mutableStateOf(false) }
    var currentOutcome by remember { mutableStateOf<CraftCombineResponse?>(null) }
    var discoveredRecipes by remember { mutableStateOf(listOf<CraftCombineResponse>()) }
    var showLogbookDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "crucible_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getApothecary()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                apothecary = response.body()!!
            }
        } catch (_: Exception) {
        }
    }

    val categories = remember(apothecary) {
        listOf("All") + apothecary.map { it.classicalCategory }.distinct()
    }

    val filteredHerbs = remember(selectedCategoryFilter, apothecary) {
        if (selectedCategoryFilter == "All") apothecary
        else apothecary.filter { it.classicalCategory == selectedCategoryFilter }
    }

    fun performCombination() {
        if (crucibleHerbs.isEmpty()) return
        isCombining = true
        currentOutcome = null

        coroutineScope.launch {
            delay(700)
            val ingredientNames = crucibleHerbs.map { it.canonicalName }
            var outcome: CraftCombineResponse? = null

            try {
                val resp = RetrofitClient.apiService.combineCraft(CraftCombineRequest(ingredientNames))
                if (resp.isSuccessful && resp.body() != null) {
                    outcome = resp.body()
                }
            } catch (_: Exception) {
            }

            if (outcome == null) {
                outcome = evaluateOfflineCombination(crucibleHerbs.toList())
            }

            currentOutcome = outcome
            isCombining = false

            if (outcome.discovered && discoveredRecipes.none { it.title == outcome.title }) {
                discoveredRecipes = listOf(outcome) + discoveredRecipes
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Formulation Novelty Lab",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Gold600.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = "INFINITE CRAFT",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Gold600,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = "Combine botanicals to evaluate classical TKDL status & Section 3(e) synergy",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate300, fontSize = 11.sp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showLogbookDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = RoyalBlue800,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logbook (${discoveredRecipes.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900)
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Navy900,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.5.dp, if (crucibleHerbs.size >= 2) Gold600.copy(alpha = glowAlpha) else Navy700)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⚗️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Apothecary Crucible",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "${crucibleHerbs.size} of 4 herb slots loaded",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                                    )
                                }
                            }
                            if (crucibleHerbs.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        crucibleHerbs.clear()
                                        currentOutcome = null
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear", color = Slate400, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(105.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Navy800, Color(0xFF0F172A))
                                    )
                                )
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (crucibleHerbs.isEmpty()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "🌿", fontSize = 26.sp, modifier = Modifier.scale(0.85f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Crucible is empty. Tap herbs below to load vessel.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400, fontSize = 11.sp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    crucibleHerbs.forEach { herb ->
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = RoyalBlue800,
                                            border = BorderStroke(1.dp, Gold600.copy(alpha = 0.7f)),
                                            modifier = Modifier.clickable {
                                                crucibleHerbs.remove(herb)
                                                currentOutcome = null
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(herb.emoji, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = herb.canonicalName,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = Gold600,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { performCombination() },
                            enabled = crucibleHerbs.isNotEmpty() && !isCombining,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (crucibleHerbs.size >= 2) Gold600 else RoyalBlue800,
                                contentColor = if (crucibleHerbs.size >= 2) Navy900 else Color.White,
                                disabledContainerColor = Slate700,
                                disabledContentColor = Slate500
                            )
                        ) {
                            if (isCombining) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = if (crucibleHerbs.size >= 2) Navy900 else Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Scanning TKDL & Synergies...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (crucibleHerbs.size >= 2) "Transmute & Evaluate Novelty" else if (crucibleHerbs.size == 1) "Analyze Single Botanical" else "Select Herbs to Combine",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            currentOutcome?.let { outcome ->
                item {
                    val isClassicalBarred = outcome.patentabilityRiskPct > 70
                    val isNovelSynergy = outcome.synergyScorePct >= 70 && outcome.patentabilityRiskPct < 50
                    
                    val badgeColor = when {
                        isClassicalBarred -> RiskHigh
                        isNovelSynergy -> Color(0xFF10B981)
                        else -> RiskMedium
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.5.dp, badgeColor.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = badgeColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = outcome.tkdlStatus.uppercase(),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = badgeColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = outcome.title,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                    )
                                    outcome.sanskritName?.let { skt ->
                                        Text(
                                            text = skt,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Gold800,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isClassicalBarred) "🚫" else if (isNovelSynergy) "✨" else "⚖️",
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Slate50,
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Synergy Potency", style = MaterialTheme.typography.labelSmall.copy(color = Slate600))
                                            Text("${outcome.synergyScorePct}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RoyalBlue800))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { outcome.synergyScorePct / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = RoyalBlue800,
                                            trackColor = Slate200
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Slate50,
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Patentability Risk", style = MaterialTheme.typography.labelSmall.copy(color = Slate600))
                                            Text("${outcome.patentabilityRiskPct}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = badgeColor))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { outcome.patentabilityRiskPct / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = badgeColor,
                                            trackColor = Slate200
                                        )
                                    }
                                }
                            }

                            outcome.classicalSource?.let { src ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Gold800, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = src,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate700,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate50,
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = outcome.statutoryRationale,
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate700,
                                        lineHeight = 17.sp,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            if (outcome.statutoryRequirements.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "STATUTORY FILING PREREQUISITES",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate500,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    outcome.statutoryRequirements.forEach { req ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = RoyalBlue800,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = req,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Navy900,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onTestInInvestigation(outcome.title)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RoyalBlue800,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Test in Investigate",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        onConsultAssistant("Explain the patentability and Section 3(e) synergy requirements for ${outcome.title}")
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, RoyalBlue800),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalBlue800)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ask AyurBot", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "APOTHECARY SHELF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate500,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Tap to load into crucible",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategoryFilter
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) RoyalBlue800 else CardBackground,
                                border = BorderStroke(1.dp, if (isSelected) RoyalBlue800 else CardBorder),
                                modifier = Modifier.clickable { selectedCategoryFilter = cat }
                            ) {
                                Text(
                                    text = cat,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else Navy900,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            items(filteredHerbs.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { herb ->
                        val isSelected = crucibleHerbs.any { it.id == herb.id }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) RoyalBlue800.copy(alpha = 0.08f) else CardBackground,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) RoyalBlue800 else CardBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (isSelected) {
                                        crucibleHerbs.removeAll { it.id == herb.id }
                                    } else if (crucibleHerbs.size < 4) {
                                        crucibleHerbs.add(herb)
                                    }
                                    currentOutcome = null
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(herb.emoji, fontSize = 22.sp)
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = RoyalBlue800,
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = herb.canonicalName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                )
                                Text(
                                    text = "${herb.sanskritName} • ${herb.botanicalName}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate500,
                                        fontSize = 10.sp
                                    ),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Slate100
                                ) {
                                    Text(
                                        text = herb.activeActives,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate600,
                                            fontSize = 9.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    if (showLogbookDialog) {
        AlertDialog(
            onDismissRequest = { showLogbookDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Book, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Discovered Recipes Logbook",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy900)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    Text(
                        text = "Track your transmutations, classical bars, and novel candidates discovered in this session.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (discoveredRecipes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No formulations transmuted yet.\nCombine herbs in the crucible!",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(discoveredRecipes) { recipe ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Slate50,
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            currentOutcome = recipe
                                            showLogbookDialog = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = recipe.title,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Navy900
                                                )
                                            )
                                            Text(
                                                text = recipe.tkdlStatus,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = if (recipe.patentabilityRiskPct > 70) RiskHigh else Color(0xFF10B981),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLogbookDialog = false }) {
                    Text("Close", color = RoyalBlue800, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

fun evaluateOfflineCombination(herbs: List<BotanicalHerb>): CraftCombineResponse {
    val names = herbs.map { it.id.lowercase() }.toSet()
    val titles = herbs.map { it.canonicalName }

    if (names == setOf("amla", "haritaki", "bibhitaki")) {
        return CraftCombineResponse(
            discovered = true,
            title = "Triphala Churna",
            sanskritName = "Triphala (त्रिफला चूर्ण)",
            ingredients = titles,
            status = "Known Classical Formulation",
            classicalSource = "Charaka Samhita Chikitsa Sthana 1:3; Sushruta Samhita 38:56",
            tkdlStatus = "Section 3(p) Barred — Direct Classical Prescription",
            patentabilityRiskPct = 96,
            synergyScorePct = 12,
            therapeuticCategory = "Rasayana / Tridoshic Harmonizer / Digestive Tonic",
            statutoryRationale = "Directly disclosed in ancient classical Ayurvedic compendia. Section 3(p) of the Patents Act, 1970 strictly excludes traditional knowledge from patentability. Section 3(e) further excludes mere admixtures lacking non-obvious synergistic effect.",
            statutoryRequirements = listOf(
                "Patents Act Section 3(p) Traditional Knowledge Bar",
                "Drugs & Cosmetics Rules Rule 158B Classical ASU License",
                "Biological Diversity Act Section 6 NBA Clearance"
            ),
            suggestedQueries = listOf("Triphala patentability under Section 3(p)", "Rule 158B licensing for Triphala")
        )
    }

    if (names == setOf("dry_ginger", "black_pepper", "pipali")) {
        return CraftCombineResponse(
            discovered = true,
            title = "Trikatu Churna",
            sanskritName = "Trikatu (त्रिकटु चूर्ण)",
            ingredients = titles,
            status = "Known Classical Formulation",
            classicalSource = "Bhavaprakasha Nighantu; Sharangadhara Samhita Madhyama 6:12",
            tkdlStatus = "Section 3(p) Barred — Direct Classical Formulation",
            patentabilityRiskPct = 94,
            synergyScorePct = 15,
            therapeuticCategory = "Deepana / Pachana / Bioavailability Enhancer",
            statutoryRationale = "Tri-katu ('three acrids') is an ancient digestive formulation documented across classical AYUSH texts. Barred under Section 3(p). Requires standard ASU licensing rather than proprietary patenting.",
            statutoryRequirements = listOf(
                "Patents Act Section 3(p) Bar",
                "Classical Ayurvedic Pharmacopoeia of India (API) Compliance",
                "State Licensing Authority (SLA) ASU License"
            ),
            suggestedQueries = listOf("Trikatu Section 3(p) prior art", "Piperine-Gingerol synergy proof")
        )
    }

    if (names == setOf("turmeric", "black_pepper") || names == setOf("turmeric", "pipali")) {
        return CraftCombineResponse(
            discovered = true,
            title = "Curcumin-Piperine Synergistic Bioavailability Enhancer",
            sanskritName = "Haridra-Maricha Synergistic Yoga",
            ingredients = titles,
            status = "Novel Synergistic Formulation Candidate",
            classicalSource = "Modern Synergistic Validation of Traditional Bioavailability Concept",
            tkdlStatus = "Novel Candidate — Section 3(e) Synergistic Evidence Required",
            patentabilityRiskPct = 34,
            synergyScorePct = 92,
            therapeuticCategory = "Synergistic Bio-enhancer & Anti-Inflammatory",
            statutoryRationale = "Piperine inhibits hepatic and intestinal glucuronidation of curcumin, yielding an unexpected >2000% human bioavailability increase. Overcomes Section 3(e) if comparative pharmacokinetic AUC curve data is filed.",
            statutoryRequirements = listOf(
                "Patents Act Section 3(e) Synergistic Potency Documentation (Pharmacokinetic AUC)",
                "NBA Form III Approval under Section 6 of Biological Diversity Act, 2002",
                "Rule 158B Proprietary ASU Safety & Efficacy Dossier"
            ),
            suggestedQueries = listOf("Overcoming Section 3(e) for Curcumin Piperine synergy", "NBA Form III checklist")
        )
    }

    if (names == setOf("ashwagandha", "brahmi")) {
        return CraftCombineResponse(
            discovered = true,
            title = "Neuro-Adaptogenic Nootropic Complex",
            sanskritName = "Medhya-Balya Samyoga (मेध्य-बल्य संयोग)",
            ingredients = titles,
            status = "Novel Synergistic Formulation Candidate",
            classicalSource = "Medhya Rasayana Novel Combinatorial Protocol",
            tkdlStatus = "Novel Candidate — Requires Non-Obvious Synergy Substantiation",
            patentabilityRiskPct = 42,
            synergyScorePct = 85,
            therapeuticCategory = "Nootropic / Cognitive Enhancement & Neuroprotection",
            statutoryRationale = "Synergistic withanolide-bacoside modulation across GABAergic receptors, acetylcholinesterase inhibition, and BDNF signaling. Patentable under Section 3(e) if cellular or clinical trials show synergistic neuroprotection over monotherapy.",
            statutoryRequirements = listOf(
                "Section 3(e) In-vitro / In-vivo Neuroprotection Synergy Data",
                "Form III NBA Approval for Withania somnifera and Bacopa monnieri",
                "Rule 158B Safety Dossier for Proprietary ASU Drug"
            ),
            suggestedQueries = listOf("Patentability of Ashwagandha and Brahmi nootropics")
        )
    }

    if (names == setOf("neem", "turmeric")) {
        return CraftCombineResponse(
            discovered = true,
            title = "Antimicrobial Dermal Cleansing Complex",
            sanskritName = "Haridra-Nimba Lepa (हरिद्रा-निम्ब लेप)",
            ingredients = titles,
            status = "Classical Admixture with Novel Synergy Potential",
            classicalSource = "Kushtaghna Formulations (Chakradatta Kushtaroga)",
            tkdlStatus = "Classical Admixture — Section 3(p) & 3(e) Bar unless Novel Delivery",
            patentabilityRiskPct = 62,
            synergyScorePct = 68,
            therapeuticCategory = "Dermatological / Antimicrobial & Anti-Acne",
            statutoryRationale = "Widely known in classical Ayurvedic dermatological remedies. Direct simple mixing faces Section 3(p) and Section 3(e) bars. Requires novel delivery mechanism (nano-emulsion, lipid carriers) exhibiting non-obvious dermal permeation.",
            statutoryRequirements = listOf(
                "Overcome Section 3(p) via Inventive Delivery Carrier",
                "Section 3(e) MIC Synergy Proof",
                "AYUSH / CDSCO Topical Cosmetic or ASU Licensing"
            ),
            suggestedQueries = listOf("Patentability of Neem and Turmeric nano-formulations")
        )
    }

    if (herbs.size == 1) {
        val h = herbs.first()
        return CraftCombineResponse(
            discovered = true,
            title = "Single Botanical: ${h.canonicalName}",
            sanskritName = h.sanskritName,
            ingredients = titles,
            status = "Single Natural Herb",
            classicalSource = "Ayurvedic Pharmacopoeia of India (API) Monograph",
            tkdlStatus = "Non-Patentable under Section 3(c) & Section 3(p)",
            patentabilityRiskPct = 98,
            synergyScorePct = 0,
            therapeuticCategory = h.classicalCategory,
            statutoryRationale = "${h.canonicalName} (${h.botanicalName}) in isolated form is barred from patenting under Section 3(c) (discovery of natural substance) and Section 3(p) (traditional knowledge). Combine with complementary herbs to test formulation novelty and Section 3(e) synergy!",
            statutoryRequirements = listOf(
                "Patents Act Section 3(c) Natural Substance Bar",
                "Patents Act Section 3(p) Traditional Knowledge Bar",
                "Biological Diversity Act Section 6 Access Approval"
            ),
            suggestedQueries = listOf("Can single Ayurvedic herbs be patented?", "How to formulate synergistic patent claims")
        )
    }

    val combinedNames = titles.joinToString(" + ")
    return CraftCombineResponse(
        discovered = true,
        title = "Novel Polyherbal Prototype: $combinedNames",
        sanskritName = "Anukta Yoga (Novel Formulation)",
        ingredients = titles,
        status = "Experimental Formulation Candidate",
        classicalSource = "Novel Polyherbal Combination (Not directly indexed as classical single recipe in Charaka/Sushruta)",
        tkdlStatus = "Novel Candidate — Must Fulfill Section 3(e) Synergistic Burden",
        patentabilityRiskPct = 42,
        synergyScorePct = 74,
        therapeuticCategory = "Multi-Target Polyherbal Complex",
        statutoryRationale = "The combination of $combinedNames does not directly match single classical recipes in the TKDL database. To secure grant under Section 3(e), the applicant must establish quantitative, non-obvious synergistic therapeutic enhancement through comparative pharmacological assays.",
        statutoryRequirements = listOf(
            "Patents Act Section 3(e) Proof of Synergistic Efficacy over Individual Components",
            "National Biodiversity Authority (NBA) Form III Prior Approval under Section 6",
            "Drugs & Cosmetics Rules, 1945 Rule 158B Proprietary ASU Licensing Dossier",
            "Standardization & Heavy Metal / Aflatoxin Testing Protocol"
        ),
        suggestedQueries = listOf("Patentability of polyherbal mixture under Section 3(e)", "NBA Form III approval checklist")
    )
}
