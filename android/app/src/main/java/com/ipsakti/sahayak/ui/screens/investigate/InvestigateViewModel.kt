package com.ipsakti.sahayak.ui.screens.investigate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipsakti.sahayak.data.model.InvestigationDetail
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InvestigateUiState(
    val queryText: String = "",
    val selectedDomains: Set<String> = setOf("Patent", "Traditional Knowledge", "Ayurveda", "Biodiversity / ABS"),
    val isDeepAnalysis: Boolean = true,
    val isLoading: Boolean = false,
    val loadingStep: String = "",
    val investigationResult: InvestigationDetail? = null,
    val errorMessage: String? = null
)

class InvestigateViewModel(
    private val repository: IpSaktiRepository = IpSaktiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestigateUiState())
    val uiState: StateFlow<InvestigateUiState> = _uiState.asStateFlow()

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(queryText = newQuery)
    }

    fun toggleDomain(domain: String) {
        val current = _uiState.value.selectedDomains.toMutableSet()
        if (current.contains(domain)) {
            if (current.size > 1) current.remove(domain) // Keep at least one
        } else {
            current.add(domain)
        }
        _uiState.value = _uiState.value.copy(selectedDomains = current)
    }

    fun setMode(deep: Boolean) {
        _uiState.value = _uiState.value.copy(isDeepAnalysis = deep)
    }

    fun applySampleQuery(sample: String) {
        _uiState.value = _uiState.value.copy(queryText = sample)
    }

    fun analyze(onComplete: (String) -> Unit) {
        val query = _uiState.value.queryText.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                loadingStep = "1. Classifying query & detecting relevant IP domains...",
                errorMessage = null
            )
            delay(400)

            _uiState.value = _uiState.value.copy(
                loadingStep = "2. Searching authoritative Indian legal & regulatory corpus..."
            )
            delay(400)

            _uiState.value = _uiState.value.copy(
                loadingStep = "3. Cross-referencing TKDL prior-art & Biodiversity provisions..."
            )

            val mode = if (_uiState.value.isDeepAnalysis) "deep" else "quick"
            val lang = com.ipsakti.sahayak.data.manager.LanguageManager.getLanguage()
            val result = repository.analyzeCase(query, mode = mode, language = lang)

            result.onSuccess { detail ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    investigationResult = detail
                )
                onComplete(detail.id)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Analysis failed"
                )
            }
        }
    }
}
