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
import com.ipsakti.sahayak.ui.components.IpTopAppBar
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
private fun GraphCanvas(
    graph: EvidenceGraphResponse,
    onNodeSelected: (GraphNode) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Pre-compute node positions in a radial layout
    val nodePositions = remember(graph.nodes) {
        val positions = mutableMapOf<String, Offset>()
        val centerX = 500f
        val centerY = 500f

        // Group by type for layered layout
        val byType = graph.nodes.groupBy { it.type }
        val typeOrder = listOf("query", "domain", "source", "risk", "action")

        var layer = 0
        typeOrder.forEach { type ->
            val nodesOfType = byType[type] ?: return@forEach
            val radius = 120f + layer * 160f
            nodesOfType.forEachIndexed { index, node ->
                val angle = (2 * Math.PI * index / nodesOfType.size) - Math.PI / 2
                val x = centerX + radius * cos(angle).toFloat()
                val y = centerY + radius * sin(angle).toFloat()
                positions[node.id] = Offset(x, y)
            }
            layer++
        }
        positions
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.4f, 3f)
                    offset += pan
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    // Check if tap hit a node
                    val adjustedTap = Offset(
                        (tapOffset.x - offset.x) / scale,
                        (tapOffset.y - offset.y) / scale
                    )
                    graph.nodes.forEach { node ->
                        val pos = nodePositions[node.id] ?: return@forEach
                        val dist = (adjustedTap - pos).getDistance()
                        if (dist < 40f) {
                            onNodeSelected(node)
                            return@detectTapGestures
                        }
                    }
                }
            }
    ) {
        drawIntoCanvas { canvas ->
            canvas.save()
            canvas.translate(offset.x, offset.y)
            canvas.scale(scale, scale)

            val nativeCanvas = canvas.nativeCanvas

            // Draw edges
            graph.edges.forEach { edge ->
                val from = nodePositions[edge.source] ?: return@forEach
                val to = nodePositions[edge.target] ?: return@forEach
                drawLine(
                    color = Slate300,
                    start = from,
                    end = to,
                    strokeWidth = 2f / scale
                )
                // Draw relation label
                val midX = (from.x + to.x) / 2
                val midY = (from.y + to.y) / 2
                nativeCanvas.drawText(
                    edge.relation,
                    midX,
                    midY - 8,
                    Paint().apply {
                        color = Slate500.toArgb()
                        textSize = 22f
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }

            // Draw nodes
            graph.nodes.forEach { node ->
                val pos = nodePositions[node.id] ?: return@forEach
                val nodeColor = getNodeColor(node.type)
                val nodeRadius = when (node.type) {
                    "query" -> 35f
                    "domain" -> 30f
                    else -> 25f
                }

                // Circle fill
                drawCircle(
                    color = nodeColor,
                    radius = nodeRadius,
                    center = pos
                )
                // Circle border
                drawCircle(
                    color = nodeColor.copy(alpha = 0.4f),
                    radius = nodeRadius + 4f,
                    center = pos,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )

                // Label
                nativeCanvas.drawText(
                    node.label,
                    pos.x,
                    pos.y + nodeRadius + 18f,
                    Paint().apply {
                        color = Navy900.toArgb()
                        textSize = 24f
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = true
                    }
                )
            }

            canvas.restore()
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

private fun getNodeColor(type: String): Color {
    return when (type) {
        "query" -> Color(0xFF6366F1)
        "domain" -> Color(0xFF2563EB)
        "source" -> Color(0xFF16A34A)
        "risk" -> Color(0xFFDC2626)
        "action" -> Color(0xFF7C3AED)
        else -> Color(0xFF64748B)
    }
}
