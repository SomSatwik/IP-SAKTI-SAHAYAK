package com.ipsakti.sahayak.ui.screens.upload

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.manager.LanguageManager
import com.ipsakti.sahayak.data.model.DocumentInfo
import com.ipsakti.sahayak.data.model.DocumentUploadResponse
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UploadScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { IpSaktiRepository() }

    val currentLang by LanguageManager.currentLanguage.collectAsState()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableLongStateOf(0L) }

    var isProcessing by remember { mutableStateOf(false) }
    var currentPipelineStep by remember { mutableIntStateOf(0) } // 0: idle, 1..7: processing/completed
    var uploadResponse by remember { mutableStateOf<DocumentUploadResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var indexedDocs by remember { mutableStateOf<List<DocumentInfo>>(emptyList()) }
    var isLoadingDocs by remember { mutableStateOf(false) }

    // Load indexed documents on entry
    fun loadDocuments() {
        coroutineScope.launch {
            isLoadingDocs = true
            val res = repository.getDocuments()
            indexedDocs = res.getOrDefault(repository.getFallbackDocuments())
            isLoadingDocs = false
        }
    }

    LaunchedEffect(Unit) {
        loadDocuments()
    }

    // Document Picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            errorMessage = null
            uploadResponse = null
            currentPipelineStep = 0

            // Resolve name and size
            var name = "Document.pdf"
            var size = 0L
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: name
                        if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
                    }
                }
            } catch (_: Exception) {}

            selectedFileName = name
            selectedFileSize = size
        }
    }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = LanguageManager.getString("upload_title"),
                subtitle = LanguageManager.getString("upload_subtitle"),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
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
            // Upload Drop / Selection Zone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRoundRect(
                            color = if (selectedUri != null) RoyalBlue800 else Slate300,
                            cornerRadius = CornerRadius(12f, 12f),
                            style = Stroke(
                                width = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                            )
                        )
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedUri != null) RoyalBlue800.copy(alpha = 0.04f) else Slate100)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedUri != null) Icons.Default.Description else Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = if (selectedUri != null) RoyalBlue800 else Slate500,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedFileName != null) {
                        Text(
                            text = selectedFileName!!,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        )
                        val sizeFormatted = if (selectedFileSize > 0) "${selectedFileSize / 1024} KB" else "PDF Document"
                        Text(
                            text = "Size: $sizeFormatted • Ready to process",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
                        )
                    } else {
                        Text(
                            text = LanguageManager.getString("select_document"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        )
                        Text(
                            text = "PDF files (Statutes, Acts, Rules, Prior Art, Guidelines)",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate500)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { filePickerLauncher.launch("application/pdf") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedUri != null) Slate200 else RoyalBlue800,
                                contentColor = if (selectedUri != null) Navy900 else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (selectedUri != null) "Change File" else "Browse Files",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (selectedUri != null) {
                            Button(
                                onClick = {
                                    if (isProcessing) return@Button
                                    isProcessing = true
                                    errorMessage = null

                                    coroutineScope.launch {
                                        // Staged animation through the pipeline
                                        currentPipelineStep = 1 // Upload
                                        delay(400)
                                        currentPipelineStep = 2 // Extract
                                        delay(450)
                                        currentPipelineStep = 3 // Clean
                                        delay(400)
                                        currentPipelineStep = 4 // Chunk
                                        delay(450)
                                        currentPipelineStep = 5 // Embed
                                        delay(400)
                                        currentPipelineStep = 6 // Index
                                        delay(400)

                                        val res = repository.uploadDocument(context, selectedUri!!)
                                        res.onSuccess { resp ->
                                            currentPipelineStep = 7 // Ready
                                            uploadResponse = resp
                                            isProcessing = false
                                            loadDocuments()
                                        }.onFailure { err ->
                                            errorMessage = err.localizedMessage ?: "Upload failed"
                                            isProcessing = false
                                        }
                                    }
                                },
                                enabled = !isProcessing,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Processing...")
                                } else {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Index into RAG", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Success feedback card
            uploadResponse?.let { resp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16A34A).copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, Color(0xFF16A34A).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Document Ingested & Indexed Successfully",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = resp.message,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Navy900)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Doc ID: ${resp.documentId} • Pages: ${resp.pages ?: 1} • Legal Chunks: ${resp.chunks ?: 3}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate600, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            // Error feedback card
            errorMessage?.let { err ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = RiskHighBg),
                    border = BorderStroke(1.dp, RiskHigh.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = RiskHigh)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = err, color = RiskHigh, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Processing Pipeline Visualization
            Text(
                text = "DOCUMENT PROCESSING PIPELINE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 1.sp
                )
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PipelineStepItem(
                        step = 1,
                        label = "Upload",
                        description = "Validate PDF formatting and upload to storage",
                        status = getStepStatus(1, currentPipelineStep)
                    )
                    PipelineStepItem(
                        step = 2,
                        label = "Extract",
                        description = "Extract raw text from PDF pages and headers",
                        status = getStepStatus(2, currentPipelineStep)
                    )
                    PipelineStepItem(
                        step = 3,
                        label = "Clean",
                        description = "Fix broken legal hyphenations and clean whitespace",
                        status = getStepStatus(3, currentPipelineStep)
                    )
                    PipelineStepItem(
                        step = 4,
                        label = "Legal-Aware Chunking",
                        description = "Split by Section, Rule, Article and Sub-clause boundaries",
                        status = getStepStatus(4, currentPipelineStep)
                    )
                    PipelineStepItem(
                        step = 5,
                        label = "Multilingual Embedding",
                        description = "Generate BAAI/bge-m3 embeddings (English, Hindi, Odia)",
                        status = getStepStatus(5, currentPipelineStep)
                    )
                    PipelineStepItem(
                        step = 6,
                        label = "FAISS Vector Indexing",
                        description = "Insert embeddings and legal metadata into index",
                        status = getStepStatus(6, currentPipelineStep)
                    )
                    PipelineStepItem(
                        step = 7,
                        label = "Ready for Retrieval",
                        description = "Available for grounded IP investigation queries",
                        status = getStepStatus(7, currentPipelineStep)
                    )
                }
            }

            // Live List of Indexed Documents
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageManager.getString("indexed_documents"),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                )
                IconButton(onClick = { loadDocuments() }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = RoyalBlue800)
                }
            }

            if (isLoadingDocs) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = RoyalBlue800)
                }
            } else {
                indexedDocs.forEach { doc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(RoyalBlue800.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = RoyalBlue800, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = doc.filename,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${doc.pages} pages • ${doc.chunks} chunks • ${doc.uploadDate}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF16A34A).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = doc.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF16A34A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getStepStatus(stepNumber: Int, currentStep: Int): String {
    return when {
        currentStep == 0 -> "idle"
        currentStep > stepNumber -> "completed"
        currentStep == stepNumber -> "processing"
        else -> "idle"
    }
}

@Composable
private fun PipelineStepItem(
    step: Int,
    label: String,
    status: String,
    description: String
) {
    val (color, icon) = when (status) {
        "completed" -> Color(0xFF16A34A) to Icons.Default.CheckCircle
        "processing" -> Gold600 to Icons.Default.Pending
        "error" -> RiskHigh to Icons.Default.Error
        else -> Slate400 to Icons.Default.RadioButtonUnchecked
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (status == "processing") {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = color
                )
            } else {
                Text(
                    text = "$step",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (status == "idle") Slate500 else Navy900
                    )
                )
                if (status == "completed") {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Slate400,
                    fontSize = 11.sp
                )
            )
        }
    }
}
