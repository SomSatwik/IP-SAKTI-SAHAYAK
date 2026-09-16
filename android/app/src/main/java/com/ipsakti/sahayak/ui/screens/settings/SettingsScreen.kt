package com.ipsakti.sahayak.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.api.RetrofitClient
import com.ipsakti.sahayak.data.manager.LanguageManager
import com.ipsakti.sahayak.data.model.HealthResponse
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    var backendUrl by remember { mutableStateOf(RetrofitClient.getBaseUrl()) }
    var healthStatus by remember { mutableStateOf<HealthResponse?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var showSavedMessage by remember { mutableStateOf(false) }

    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { IpSaktiRepository() }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = LanguageManager.getString("settings_title"),
                subtitle = LanguageManager.getString("settings_subtitle")
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
            // Language Configuration
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
                            text = LanguageManager.getString("language_section"),
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
                                text = "Active: ${LanguageManager.getDisplayName(currentLang)}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = RoyalBlue800,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LanguageOptionCard(
                            label = "English",
                            nativeLabel = "English",
                            code = "en",
                            isSelected = currentLang == "en",
                            onSelect = { LanguageManager.setLanguage("en") },
                            modifier = Modifier.weight(1f)
                        )
                        LanguageOptionCard(
                            label = "Hindi",
                            nativeLabel = "हिन्दी",
                            code = "hi",
                            isSelected = currentLang == "hi",
                            onSelect = { LanguageManager.setLanguage("hi") },
                            modifier = Modifier.weight(1f)
                        )
                        LanguageOptionCard(
                            label = "Odia",
                            nativeLabel = "ଓଡ଼ିଆ",
                            code = "or",
                            isSelected = currentLang == "or",
                            onSelect = { LanguageManager.setLanguage("or") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (currentLang) {
                                    "hi" -> "हिंदी भाषा सक्रिय है। सभी यूआई तत्व और कानूनी विश्लेषण अब हिंदी में संसाधित होंगे।"
                                    "or" -> "ଓଡ଼ିଆ ଭାଷା ସକ୍ରିୟ ଅଛି। ସମସ୍ତ UI ଏବଂ ଆଇନଗତ ବିଶ୍ଳେଷଣ ଏବେ ଓଡ଼ିଆରେ ପ୍ରକ୍ରିୟାକରଣ ହେବ।"
                                    else -> "English active. Queries will be retrieved with multilingual BGE-M3 embeddings and Groq LLM."
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(color = Slate600, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }

            // Backend Configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = LanguageManager.getString("backend_config"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = backendUrl,
                        onValueChange = { backendUrl = it },
                        label = { Text("Backend URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue800,
                            unfocusedBorderColor = CardBorder
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Emulator: http://10.0.2.2:8000/  •  Physical device: http://<your-ip>:8000/",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                RetrofitClient.updateBaseUrl(backendUrl)
                                showSavedMessage = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(LanguageManager.getString("save"), fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                isChecking = true
                                coroutineScope.launch {
                                    val result = repository.checkHealth()
                                    healthStatus = result.getOrNull()
                                    isChecking = false
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            if (isChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = RoyalBlue800
                                )
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(LanguageManager.getString("test_connection"))
                        }
                    }

                    if (showSavedMessage) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Configuration saved to preferences.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                        )
                    }

                    // Health status result
                    healthStatus?.let { health ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (health.status == "ok")
                                    Color(0xFF16A34A).copy(alpha = 0.08f) else RiskHighBg
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (health.status == "ok") Color(0xFF16A34A).copy(alpha = 0.3f) else RiskHigh.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "✓ Connected — API v${health.version}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (health.status == "ok") Color(0xFF16A34A) else RiskHigh,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Mode: ${health.mode} • Pipeline: ${if (health.pipelineReady) "Ready (Live RAG Active)" else "Demo Fallback (No Groq key / Index)"}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate600)
                                )
                            }
                        }
                    }
                }
            }

            // System Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SYSTEM INFORMATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoRow("Application", "IP-SAKTI SAHAYAK v1.1.0")
                    InfoRow("Event", "Smart India Hackathon 2026")
                    InfoRow("Backend", "FastAPI + Python RAG Pipeline")
                    InfoRow("LLM Provider", "Groq (llama-3.3-70b-versatile)")
                    InfoRow("Embedding Model", "BAAI/bge-m3 (Multilingual)")
                    InfoRow("Vector Store", "FAISS (CPU)")
                    InfoRow("Supported Languages", "English, हिन्दी, ଓଡ଼ିଆ")
                    InfoRow("Security", "Zero secrets stored on mobile device")
                }
            }
        }
    }
}

@Composable
private fun LanguageOptionCard(
    label: String,
    nativeLabel: String,
    code: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onSelect() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) RoyalBlue800 else CardBackground,
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) RoyalBlue800 else CardBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = nativeLabel,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = if (isSelected) Color.White else Navy900,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else Slate500,
                    fontSize = 11.sp
                )
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Gold600,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Slate500
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(color = Navy900)
        )
    }
}
