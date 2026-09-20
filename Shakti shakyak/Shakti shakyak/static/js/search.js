/**
 * IP-SAKTI SAHAYAK — Prior-Art & Compliance Engine Client
 */

document.addEventListener("DOMContentLoaded", () => {
    initPriorArtForm();
    initComplianceForm();
});

function initPriorArtForm() {
    const form = document.getElementById("priorArtForm");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const desc = document.getElementById("formulationDescription").value.trim();
        const resultsContainer = document.getElementById("priorArtResults");

        if (!desc) return;

        resultsContainer.innerHTML = `
            <div class="form-card" style="text-align: center; padding: 2rem;">
                <p>Scanning botanical indexes, TKDL databases, and Section 3(p) precedents...</p>
                <div class="loading-indicator" style="justify-content: center; margin-top: 1rem;">
                    <div class="pulse-dot"></div>
                    <div class="pulse-dot"></div>
                    <div class="pulse-dot"></div>
                </div>
            </div>
        `;

        try {
            const res = await fetch("/api/prior-art", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ description: desc })
            });

            const data = await res.json();
            if (data.success) {
                renderPriorArtResults(data);
            } else {
                resultsContainer.innerHTML = `<div class="form-card" style="color: var(--danger);">Error: ${data.error}</div>`;
            }
        } catch (err) {
            resultsContainer.innerHTML = `<div class="form-card" style="color: var(--danger);">Failed to connect to prior-art service.</div>`;
        }
    });
}

function renderPriorArtResults(data) {
    const container = document.getElementById("priorArtResults");

    // Botanicals list
    let botanicalsHtml = "";
    if (data.detected_botanicals && data.detected_botanicals.length > 0) {
        botanicalsHtml = data.detected_botanicals.map(b => `
            <div style="border: 1px solid var(--border-subtle); padding: 1rem; border-radius: var(--radius-sm); margin-bottom: 0.75rem; background: var(--bg-base);">
                <div style="font-weight: 700; color: var(--primary);">${b.name} (<em>${b.scientific_name}</em>)</div>
                <div style="font-size: 0.85rem; color: var(--text-secondary); margin: 4px 0;"><strong>Traditional Ayurvedic Uses:</strong> ${b.traditional_uses}</div>
                <div style="font-size: 0.85rem; color: var(--text-secondary);"><strong>Classical Treatises:</strong> ${b.classical_texts}</div>
                <div style="font-size: 0.8rem; color: var(--warning); margin-top: 4px;"><strong>Section 3(p) Risk:</strong> ${b.sec_3p_risk}</div>
            </div>
        `).join("");
    } else {
        botanicalsHtml = `<p style="color: var(--text-muted);">No classical Ayurvedic botanicals detected in query.</p>`;
    }

    // Patentability Barriers
    const barriersHtml = data.patentability_barriers.map(bar => `
        <div style="border-left: 4px solid var(--warning); padding: 0.75rem 1rem; background: rgba(245, 158, 11, 0.08); border-radius: var(--radius-sm); margin-bottom: 0.75rem;">
            <div style="font-weight: 700; font-size: 0.9rem;">${bar.statute} • <span class="badge badge-warning">${bar.risk_level}</span></div>
            <div style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 4px;">${bar.explanation}</div>
        </div>
    `).join("");

    container.innerHTML = `
        <div class="form-card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                <h3 style="font-size: 1.25rem;">Prior-Art Analysis Report</h3>
                <span class="badge badge-primary">${data.conclusion_status}</span>
            </div>

            <h4 style="margin: 1rem 0 0.5rem 0;">Identified Classical Botanicals & Ingredients</h4>
            ${botanicalsHtml}

            <h4 style="margin: 1.5rem 0 0.5rem 0;">Statutory Patentability Hurdles (Indian Patents Act)</h4>
            ${barriersHtml}

            <div class="statutory-disclaimer-box" style="margin-top: 1.5rem;">
                <strong>DISCLAIMER:</strong> ${data.disclaimer}
            </div>
        </div>
    `;
}

function initComplianceForm() {
    const form = document.getElementById("complianceForm");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const applicant = document.getElementById("applicantType").value;
        const usesBio = document.getElementById("usesBio").checked;
        const commercial = document.getElementById("commercialIntent").checked;
        const ipFiling = document.getElementById("ipFiling").checked;
        const details = document.getElementById("resourceDetails").value;

        const resultsContainer = document.getElementById("complianceResults");

        try {
            const res = await fetch("/api/compliance", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    applicant_type: applicant,
                    uses_biological_resource: usesBio,
                    commercial_intent: commercial,
                    ip_filing_intended: ipFiling,
                    resource_details: details
                })
            });

            const data = await res.json();
            if (data.success) {
                renderComplianceResults(data);
            }
        } catch (err) {
            resultsContainer.innerHTML = `<div class="form-card" style="color: var(--danger);">Failed to calculate compliance.</div>`;
        }
    });
}

function renderComplianceResults(data) {
    const container = document.getElementById("complianceResults");

    const formsHtml = data.mandatory_forms.length > 0
        ? data.mandatory_forms.map(f => `<span class="badge badge-primary" style="font-size: 0.85rem; padding: 0.4rem 0.8rem;">${f}</span>`).join(" ")
        : `<span class="badge badge-accent">No special NBA clearances required</span>`;

    const triggersHtml = data.statutory_triggers.map(t => `
        <div style="border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); padding: 1rem; margin-bottom: 0.75rem; background: var(--bg-base);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                <div style="font-weight: 700; color: var(--text-primary);">${t.statute}</div>
                <div class="badge badge-warning">${t.authority}</div>
            </div>
            <p style="font-size: 0.85rem; color: var(--text-secondary);">${t.obligation}</p>
            <div style="font-size: 0.8rem; font-weight: 600; color: var(--primary); margin-top: 0.5rem;">Procedure / Form: ${t.form_required}</div>
        </div>
    `).join("");

    container.innerHTML = `
        <div class="form-card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
                <h3 style="font-size: 1.25rem;">Biological Diversity Act & ABS Guidance</h3>
                <span class="badge badge-accent">${data.status}</span>
            </div>

            <div style="margin-bottom: 1.5rem;">
                <div style="font-size: 0.85rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.04em; margin-bottom: 0.5rem;">Mandatory Approvals & Forms</div>
                <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                    ${formsHtml}
                </div>
            </div>

            <h4 style="margin-bottom: 0.75rem;">Applicable Statutory Requirements</h4>
            ${triggersHtml}

            <div class="statutory-disclaimer-box" style="margin-top: 1.5rem;">
                <strong>STATUTORY NOTICE:</strong> ${data.disclaimer}
            </div>
        </div>
    `;
}
