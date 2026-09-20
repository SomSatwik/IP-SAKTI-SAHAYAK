/**
 * IP-SAKTI SAHAYAK — Interactive Legal & Botanical Knowledge Graph
 * HTML5 Canvas force/circular network visualizer
 */

document.addEventListener("DOMContentLoaded", () => {
    initKnowledgeGraph();
});

async function initKnowledgeGraph() {
    const canvas = document.getElementById("graphCanvas");
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    const container = canvas.parentElement;

    // Resize canvas
    function resize() {
        canvas.width = container.clientWidth;
        canvas.height = 650;
    }
    resize();
    window.addEventListener("resize", resize);

    // Fetch graph data
    try {
        const response = await fetch("/api/knowledge-graph");
        const data = await response.json();
        if (data.success && data.graph) {
            renderGraph(canvas, ctx, data.graph);
        }
    } catch (e) {
        console.error("Failed to load knowledge graph data", e);
    }
}

function renderGraph(canvas, ctx, graph) {
    const nodes = graph.nodes;
    const edges = graph.edges;
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const radius = Math.min(centerX, centerY) - 100;

    // Arrange nodes in concentric/circular orbits
    nodes.forEach((node, i) => {
        const angle = (i / nodes.length) * 2 * Math.PI;
        // Inner and outer rings based on type
        const r = node.group === "domain" ? radius * 0.45 : radius * 0.85;
        node.x = centerX + r * Math.cos(angle);
        node.y = centerY + r * Math.sin(angle);
        node.radius = node.group === "domain" ? 24 : 18;
    });

    const nodeMap = {};
    nodes.forEach(n => nodeMap[n.id] = n);

    let hoveredNode = null;

    function draw() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // 1. Draw Edges
        edges.forEach(edge => {
            const source = nodeMap[edge.from];
            const target = nodeMap[edge.to];
            if (!source || !target) return;

            ctx.beginPath();
            ctx.moveTo(source.x, source.y);
            ctx.lineTo(target.x, target.y);
            ctx.strokeStyle = (hoveredNode && (hoveredNode.id === source.id || hoveredNode.id === target.id))
                ? "var(--primary)" : "rgba(148, 163, 184, 0.35)";
            ctx.lineWidth = (hoveredNode && (hoveredNode.id === source.id || hoveredNode.id === target.id)) ? 2.5 : 1.2;
            ctx.stroke();
        });

        // 2. Draw Nodes
        nodes.forEach(node => {
            const isHovered = hoveredNode && hoveredNode.id === node.id;

            ctx.beginPath();
            ctx.arc(node.x, node.y, isHovered ? node.radius + 4 : node.radius, 0, 2 * Math.PI);
            ctx.fillStyle = node.color || "#0284c7";
            ctx.fill();

            ctx.strokeStyle = isHovered ? "#ffffff" : "rgba(255, 255, 255, 0.4)";
            ctx.lineWidth = isHovered ? 3 : 1.5;
            ctx.stroke();

            // Label
            ctx.font = isHovered ? "bold 13px 'Plus Jakarta Sans', sans-serif" : "11px 'Plus Jakarta Sans', sans-serif";
            ctx.fillStyle = document.documentElement.getAttribute("data-theme") === "dark" ? "#f8fafc" : "#0f172a";
            ctx.textAlign = "center";
            ctx.fillText(node.label, node.x, node.y + node.radius + 14);
        });

        requestAnimationFrame(draw);
    }

    draw();

    // Mouse Interaction
    canvas.addEventListener("mousemove", (e) => {
        const rect = canvas.getBoundingClientRect();
        const mx = e.clientX - rect.left;
        const my = e.clientY - rect.top;

        hoveredNode = null;
        for (const n of nodes) {
            const dx = mx - n.x;
            const dy = my - n.y;
            if (Math.sqrt(dx * dx + dy * dy) <= n.radius + 4) {
                hoveredNode = n;
                canvas.style.cursor = "pointer";
                break;
            }
        }
        if (!hoveredNode) canvas.style.cursor = "default";
    });
}
