/**
 * IP-SAKTI SAHAYAK — Chat & Research Workspace Controller
 * 3-Panel Coordination: History, Chat Stream, Verified Citations & Confidence Meter
 */

document.addEventListener("DOMContentLoaded", () => {
    initChat();
    loadHistory();
});

let currentQueryData = null;

function initChat() {
    const chatForm = document.getElementById("chatForm");
    const chatInput = document.getElementById("chatInput");

    if (chatForm && chatInput) {
        // Submit on form submit
        chatForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const text = chatInput.value.trim();
            if (text) {
                submitQuery(text);
                chatInput.value = "";
                chatInput.style.height = "auto";
            }
        });

        // Submit on Enter (unless Shift+Enter)
        chatInput.addEventListener("keydown", (e) => {
            if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                chatForm.dispatchEvent(new Event("submit"));
            }
        });

        // Auto-expand textarea
        chatInput.addEventListener("input", function() {
            this.style.height = "auto";
            this.style.height = (this.scrollHeight) + "px";
        });
    }

    // Bind suggestion chips
    document.querySelectorAll(".suggestion-chip").forEach(chip => {
        chip.addEventListener("click", () => {
            const query = chip.getAttribute("data-query");
            if (query) {
                submitQuery(query);
            }
        });
    });
}

async function submitQuery(queryText) {
    const messagesContainer = document.getElementById("chatMessages");
    const jurisdictionSelect = document.getElementById("jurisdictionSelect");
    const languageSelect = document.getElementById("languageSelect");

    const jurisdiction = jurisdictionSelect ? jurisdictionSelect.value : "India";
    const language = languageSelect ? languageSelect.value : "en";

    // 1. Append User Message
    appendMessage("user", queryText);

    // 2. Append Loading Indicator
    const loadingId = "loading-" + Date.now();
    appendLoadingIndicator(loadingId);

    try {
        const response = await fetch("/api/chat", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                query: queryText,
                jurisdiction: jurisdiction,
                language: language
            })
        });

        const data = await response.json();
        removeElement(loadingId);

        if (data.success) {
            currentQueryData = {
                query: queryText,
                answer: data.answer,
                confidence: data.confidence,
                confidence_level: data.confidence_level,
                domain: data.domain,
                jurisdiction: data.jurisdiction,
                sources: data.sources
            };

            // 3. Append Assistant Message with formatted Citations
            appendAssistantMessage(data);

            // 4. Update Right Panel: Confidence & Source Cards
            updateEvidencePanel(data);

            // 5. Refresh History
            loadHistory();
        } else {
            appendMessage("assistant", "⚠️ " + (data.error || "Unable to retrieve legal evidence."));
        }
    } catch (err) {
        removeElement(loadingId);
        appendMessage("assistant", "⚠️ Network connection error. Please verify the Flask server is running.");
    }
}

function appendMessage(role, text) {
    const messagesContainer = document.getElementById("chatMessages");
    const msgDiv = document.createElement("div");
    msgDiv.className = `message message-${role}`;

    const avatarDiv = document.createElement("div");
    avatarDiv.className = `message-avatar ${role}-avatar`;
    avatarDiv.textContent = role === "user" ? "U" : "⚖️";

    const contentDiv = document.createElement("div");
    contentDiv.className = "message-content";
    contentDiv.textContent = text;

    msgDiv.appendChild(avatarDiv);
    msgDiv.appendChild(contentDiv);
    messagesContainer.appendChild(msgDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function appendAssistantMessage(data) {
    const messagesContainer = document.getElementById("chatMessages");
    const msgDiv = document.createElement("div");
    msgDiv.className = "message message-assistant";

    const avatarDiv = document.createElement("div");
    avatarDiv.className = "message-avatar assistant-avatar";
    avatarDiv.textContent = "⚖️";

    const contentDiv = document.createElement("div");
    contentDiv.className = "message-content";

    // Format headers and interactive citations
    let formatted = renderMarkdown(data.answer);

    // Convert [Source X] or 【Source X】 into interactive clickable tags
    formatted = formatted.replace(/[\[【\(]Source[\s\u202f]*(\d+)[\]】\)]/gi, (match, num) => {
        return `<span class="citation-tag" onclick="highlightSource(${num})">[Source ${num}]</span>`;
    });

    // Domain & Jurisdiction Pills Header
    const pillsHtml = `
        <div style="display: flex; gap: 0.5rem; margin-bottom: 0.85rem;">
            <span class="badge badge-primary">${data.domain || "General IP"}</span>
            <span class="badge badge-accent">${data.jurisdiction || "India"}</span>
            <span class="badge ${data.confidence_level === 'HIGH' ? 'badge-accent' : 'badge-warning'}">Confidence: ${data.confidence_percentage}%</span>
        </div>
    `;

    // Action Bar (Copy Answer, Export Report)
    const actionsHtml = `
        <div class="chat-action-bar">
            <button class="action-btn" onclick="copyAnswerText(this)">📋 Copy Answer</button>
            <button class="action-btn" onclick="exportReport()">📄 Download Dossier</button>
        </div>
    `;

    contentDiv.innerHTML = pillsHtml + formatted + actionsHtml;
    msgDiv.appendChild(avatarDiv);
    msgDiv.appendChild(contentDiv);
    messagesContainer.appendChild(msgDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function appendLoadingIndicator(id) {
    const messagesContainer = document.getElementById("chatMessages");
    const loadDiv = document.createElement("div");
    loadDiv.id = id;
    loadDiv.className = "message message-assistant";
    loadDiv.innerHTML = `
        <div class="message-avatar assistant-avatar">⚖️</div>
        <div class="message-content loading-indicator">
            <span>Retrieving authoritative legal evidence...</span>
            <div class="pulse-dot"></div>
            <div class="pulse-dot"></div>
            <div class="pulse-dot"></div>
        </div>
    `;
    messagesContainer.appendChild(loadDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function removeElement(id) {
    const el = document.getElementById(id);
    if (el) el.remove();
}

function updateEvidencePanel(data) {
    const confidenceValue = document.getElementById("confidenceValue");
    const confidenceBar = document.getElementById("confidenceProgressBar");
    const confidenceText = document.getElementById("confidenceExplanation");
    const sourcesContainer = document.getElementById("sourcesContainer");

    // Update Confidence Meter
    const pct = data.confidence_percentage || 50;
    if (confidenceValue) confidenceValue.textContent = `${pct}%`;
    if (confidenceBar) {
        confidenceBar.style.width = `${pct}%`;
        confidenceBar.className = "confidence-progress-bar " +
            (pct >= 80 ? "progress-high" : (pct >= 60 ? "progress-medium" : "progress-low"));
    }
    if (confidenceText) {
        confidenceText.textContent = data.confidence_explanation || "Evaluation based on statutory sources.";
    }

    // Populate Source Cards
    if (sourcesContainer) {
        sourcesContainer.innerHTML = "";
        const sources = data.sources || [];

        if (sources.length === 0) {
            sourcesContainer.innerHTML = `<p style="color: var(--text-muted); font-size: 0.85rem;">No primary statutory citations retrieved.</p>`;
            return;
        }

        sources.forEach(s => {
            const card = document.createElement("div");
            card.className = "source-card";
            card.id = `source-card-${s.citation_index}`;
            card.innerHTML = `
                <div class="source-card-badge">[SOURCE ${s.citation_index}] • ${s.authority_level || 'LEVEL_1'}</div>
                <div class="source-card-title">${s.title}</div>
                <div class="source-card-meta">
                    <strong>Authority:</strong> ${s.authority} | <strong>Section:</strong> ${s.section}
                </div>
                <div class="source-card-snippet">"${s.text_snippet}"</div>
                <a href="${s.source_url}" target="_blank" class="source-card-link">
                    Open Official Portal &rarr;
                </a>
            `;
            sourcesContainer.appendChild(card);
        });
    }
}

function highlightSource(index) {
    const card = document.getElementById(`source-card-${index}`);
    if (card) {
        card.scrollIntoView({ behavior: "smooth", block: "center" });
        card.style.borderColor = "var(--primary)";
        card.style.boxShadow = "0 0 0 3px var(--primary-glow)";
        setTimeout(() => {
            card.style.borderColor = "var(--border-subtle)";
            card.style.boxShadow = "var(--card-shadow)";
        }, 2000);
    }
}

function renderMarkdown(md) {
    if (!md) return "";
    let html = md
        .replace(/^### (.*$)/gim, "<h3>$1</h3>")
        .replace(/^## (.*$)/gim, "<h2>$1</h2>")
        .replace(/^# (.*$)/gim, "<h1>$1</h1>")
        .replace(/\*\*(.*?)\*\*/gim, "<strong>$1</strong>")
        .replace(/\*(.*?)\*/gim, "<em>$1</em>")
        .replace(/^\s*\-\s(.*$)/gim, "<li>$1</li>")
        .replace(/(<li>.*<\/li>)/gis, "<ul>$1</ul>")
        .replace(/\n\n/gim, "<br><br>");
    return html;
}

function copyAnswerText(btn) {
    if (!currentQueryData) return;
    navigator.clipboard.writeText(currentQueryData.answer).then(() => {
        btn.textContent = "✅ Copied!";
        setTimeout(() => { btn.textContent = "📋 Copy Answer"; }, 2000);
    });
}

async function exportReport() {
    if (!currentQueryData) {
        showToast("No active research query to export.", "warning");
        return;
    }

    try {
        const response = await fetch("/api/reports", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(currentQueryData)
        });

        const html = await response.text();
        const reportWindow = window.open("", "_blank");
        reportWindow.document.write(html);
        reportWindow.document.close();
    } catch (err) {
        showToast("Error generating report.", "danger");
    }
}

async function loadHistory() {
    const historyList = document.getElementById("historyList");
    if (!historyList) return;

    try {
        const response = await fetch("/api/history");
        const data = await response.json();

        if (data.success && data.history.length > 0) {
            historyList.innerHTML = "";
            data.history.forEach(item => {
                const li = document.createElement("li");
                li.className = "history-item";
                li.innerHTML = `
                    <div class="history-item-title">${item.query}</div>
                    <div class="history-item-meta">
                        <span>${item.domain}</span>
                        <span>${item.timestamp}</span>
                    </div>
                `;
                li.addEventListener("click", () => {
                    submitQuery(item.query);
                });
                historyList.appendChild(li);
            });
        }
    } catch (e) {}
}
