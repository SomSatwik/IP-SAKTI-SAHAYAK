/**
 * IP-SAKTI SAHAYAK — Global Application Script
 * Theme Toggling, Language Preference, and Notifications
 */

document.addEventListener("DOMContentLoaded", () => {
    initTheme();
    initLanguageSelector();
});

function initTheme() {
    const savedTheme = localStorage.getItem("ip_sakti_theme") || "light";
    document.documentElement.setAttribute("data-theme", savedTheme);
    updateThemeIcon(savedTheme);

    const toggleBtn = document.getElementById("themeToggleBtn");
    if (toggleBtn) {
        toggleBtn.addEventListener("click", () => {
            const current = document.documentElement.getAttribute("data-theme") || "light";
            const next = current === "dark" ? "light" : "dark";
            document.documentElement.setAttribute("data-theme", next);
            localStorage.setItem("ip_sakti_theme", next);
            updateThemeIcon(next);
        });
    }
}

function updateThemeIcon(theme) {
    const iconSpan = document.getElementById("themeToggleIcon");
    if (iconSpan) {
        iconSpan.textContent = theme === "dark" ? "☀️" : "🌙";
    }
}

function initLanguageSelector() {
    const langSelect = document.getElementById("languageSelect");
    if (langSelect) {
        const savedLang = localStorage.getItem("ip_sakti_lang") || "en";
        langSelect.value = savedLang;

        langSelect.addEventListener("change", (e) => {
            localStorage.setItem("ip_sakti_lang", e.target.value);
            showToast(`Language preference set to ${e.target.options[e.target.selectedIndex].text}`);
        });
    }
}

function showToast(message, type = "info") {
    let container = document.querySelector(".toast-container");
    if (!container) {
        container = document.createElement("div");
        container.className = "toast-container";
        document.body.appendChild(container);
    }

    const toast = document.createElement("div");
    toast.className = "toast";
    toast.innerHTML = `<span>ℹ️</span><span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = "0";
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}
