package com.ipsakti.sahayak.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipsakti.sahayak.data.model.DashboardStats
import com.ipsakti.sahayak.data.model.InvestigationSummary
import com.ipsakti.sahayak.data.model.RegulationUpdate
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val isOnline: Boolean = true,
    val stats: DashboardStats = DashboardStats(),
    val recentInvestigations: List<InvestigationSummary> = emptyList(),
    val recentRegulations: List<RegulationUpdate> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: IpSaktiRepository = IpSaktiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // 1. Health check
            val healthResult = repository.checkHealth()
            val online = healthResult.isSuccess

            // 2. Stats
            val statsResult = repository.getDashboardStats()
            val stats = statsResult.getOrDefault(repository.getFallbackDashboardStats())

            // 3. Recent investigations
            val invResult = repository.getInvestigations()
            val investigations = invResult.getOrDefault(emptyList())

            // 4. Recent regulations
            val regResult = repository.getRecentRegulations()
            val regulations = regResult.getOrDefault(repository.getFallbackRegulationUpdates())

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isOnline = online,
                stats = stats,
                recentInvestigations = investigations,
                recentRegulations = regulations
            )
        }
    }
}
