package com.ipsakti.sahayak.ui.screens.settings

import androidx.compose.foundation.BorderStroke
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

    val coroutineScope = rememberCoroutineScope()
    val repository = remember { IpSaktiRepository() }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Settings",
                subtitle = "System Configuration & API Status"
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
            // Backend Configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BACKEND API CONFIGURATION",
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
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", fontWeight = FontWeight.Bold)
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
                            Text("Test Connection")
                        }
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
                                    text = "Mode: ${health.mode} • Pipeline: ${if (health.pipelineReady) "Ready" else "Demo Only"}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate600)
                                )
                            }
                        }
                    }
                }
            }

            // Language Configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LANGUAGE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LanguageChip("English", isSelected = true)
                        LanguageChip("हिन्दी", isSelected = false, enabled = false)
                        LanguageChip("ଓଡ଼ିଆ", isSelected = false, enabled = false)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hindi and Odia support coming in future release. BGE-M3 embeddings are multilingual-ready.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate400, fontSize = 11.sp)
                    )
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
                    InfoRow("Application", "IP-SAKTI SAHAYAK v1.0.0")
                    InfoRow("Event", "Smart India Hackathon 2026")
                    InfoRow("Backend", "FastAPI + Python RAG Pipeline")
                    InfoRow("LLM Provider", "Groq (llama-3.3-70b-versatile)")
                    InfoRow("Embedding Model", "BAAI/bge-m3 (Multilingual)")
                    InfoRow("Vector Store", "FAISS (CPU)")
                    InfoRow("Security", "No API keys stored on device")
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(label: String, isSelected: Boolean, enabled: Boolean = true) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = when {
            isSelected -> RoyalBlue800
            !enabled -> Slate200
            else -> Slate100
        },
        border = BorderStroke(
            1.dp,
            when {
                isSelected -> RoyalBlue800
                !enabled -> Slate300
                else -> Slate300
            }
        )
    ) {
        Text(
            text = label + if (!enabled) " (soon)" else "",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = when {
                    isSelected -> Color.White
                    !enabled -> Slate400
                    else -> Navy800
                },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            )
        )
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
