package com.ipsakti.sahayak.ui.screens.graph

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.model.EvidenceGraphResponse
import com.ipsakti.sahayak.data.model.GraphNode
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import com.ipsakti.sahayak.ui.components.GraphCanvas
import com.ipsakti.sahayak.ui.components.IpTopAppBar
import com.ipsakti.sahayak.ui.components.getNodeColor
import com.ipsakti.sahayak.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EvidenceGraphScreen(
    investigationId: String,
    onNavigateBack: () -> Unit
) {
    val repository = remember { IpSaktiRepository() }
    val demoInv = remember { repository.getFallbackDemoInvestigation() }
    val graph = remember { demoInv.graph }

    var selectedNode by remember { mutableStateOf<GraphNode?>(null) }

    Scaffold(
        topBar = {
            IpTopAppBar(
                title = "Evidence Graph",
                subtitle = "Interactive Relationship Visualization",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Graph Canvas
            GraphCanvas(
                graph = graph,
                onNodeSelected = { selectedNode = it },
                modifier = Modifier.fillMaxSize()
            )

            // Legend
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "LEGEND",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LegendItem("Query", Color(0xFF6366F1))
                    LegendItem("Domain", Color(0xFF2563EB))
                    LegendItem("Source", Color(0xFF16A34A))
                    LegendItem("Risk", Color(0xFFDC2626))
                    LegendItem("Action", Color(0xFF7C3AED))
                }
            }

            // Selected Node Detail Panel
            selectedNode?.let { node ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = getNodeColor(node.type).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = node.type.uppercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = getNodeColor(node.type),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = node.label,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                )
                            }
                            IconButton(onClick = { selectedNode = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Show connected edges
                        val connectedEdges = graph.edges.filter { it.source == node.id || it.target == node.id }
                        if (connectedEdges.isNotEmpty()) {
                            Text(
                                text = "CONNECTIONS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            connectedEdges.forEach { edge ->
                                val connectedId = if (edge.source == node.id) edge.target else edge.source
                                val connectedNode = graph.nodes.find { it.id == connectedId }
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text(
                                        text = "→ ",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = RoyalBlue800)
                                    )
                                    Text(
                                        text = "${connectedNode?.label ?: connectedId} (${edge.relation})",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Navy900)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Navy800, fontSize = 11.sp)
        )
    }
}
